#!/usr/bin/env python3
"""Bundle open-licensed attraction photos for offline play.

The script is intentionally incremental: successful images stay in the repository and later
runs only fill missing cards. Curated Commons files are tried directly first; if Wikimedia
throttles the request, Openverse is used as a fallback. Search-only cards use Openverse.
All images are converted to 1200x675 WebP and full attribution metadata is saved in assets.
"""

from __future__ import annotations

from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import quote, urlencode
from urllib.request import Request, urlopen
import html
import io
import json
import re
import sys
import time

from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/java/pl/kacperikapi/mathadventure/data/AttractionContent.kt"
OUT = ROOT / "app/src/main/res/drawable-nodpi"
ASSETS = ROOT / "app/src/main/assets"
CREDITS = ASSETS / "attraction_photo_credits.json"
REPORT = ASSETS / "attraction_photo_bundle_report.json"
USER_AGENT = "KacperKapiTheGame/0.5.3 (educational Android app; open-licensed offline media)"
ALLOWED_LICENSES = {"cc0", "pdm", "by", "by-sa"}
EXPECTED_CARDS = 70


def parse_cards() -> list[dict]:
    text = SOURCE.read_text(encoding="utf-8")
    block_re = re.compile(r"^ {8}(item|searchItem)\(\s*\n(.*?)^ {8}\),?\s*$", re.M | re.S)
    str_re = re.compile(r'"((?:\\.|[^"\\])*)"')
    cards: list[dict] = []
    for kind, body in block_re.findall(text):
        args = str_re.findall(body)
        if kind == "item" and len(args) >= 8:
            cards.append({
                "kind": kind,
                "stage_id": args[0],
                "value": args[5],
                "author": args[6],
                "license": args[7],
            })
        elif kind == "searchItem" and len(args) >= 6:
            cards.append({"kind": kind, "stage_id": args[0], "value": args[5]})
    if len(cards) != EXPECTED_CARDS:
        raise RuntimeError(f"Expected {EXPECTED_CARDS} attraction cards, parsed {len(cards)}")
    return cards


def request_bytes(url: str, *, accept: str | None = None, attempts: int = 3, timeout: int = 30) -> tuple[bytes, str]:
    headers = {"User-Agent": USER_AGENT}
    if accept:
        headers["Accept"] = accept
    last_error: Exception | None = None
    for attempt in range(attempts):
        try:
            req = Request(url, headers=headers)
            with urlopen(req, timeout=timeout) as response:
                return response.read(), response.headers.get("Content-Type", "")
        except HTTPError as exc:
            last_error = exc
            if exc.code not in {429, 500, 502, 503, 504}:
                raise
            wait = min(12.0, 2.0 + attempt * 3.0)
            print(f"  HTTP {exc.code}; retry in {wait:.0f}s", flush=True)
            time.sleep(wait)
        except (URLError, TimeoutError) as exc:
            last_error = exc
            wait = min(8.0, 2.0 + attempt * 2.0)
            print(f"  network retry in {wait:.0f}s: {exc}", flush=True)
            time.sleep(wait)
    raise RuntimeError(f"request failed: {url} ({last_error})")


def get_json(url: str) -> dict:
    raw, _ = request_bytes(url, accept="application/json", attempts=4, timeout=35)
    return json.loads(raw.decode("utf-8"))


def load_existing_credits() -> dict[str, dict]:
    if not CREDITS.exists():
        return {}
    try:
        return json.loads(CREDITS.read_text(encoding="utf-8"))
    except Exception:
        return {}


def exact_commons(card: dict) -> tuple[list[str], dict]:
    filename = card["value"]
    redirect = "https://commons.wikimedia.org/wiki/Special:Redirect/file/" + quote(filename, safe="") + "?width=1400"
    source = "https://commons.wikimedia.org/wiki/File:" + quote(filename.replace(" ", "_"), safe="()_,.-")
    credit = {
        "author": card["author"],
        "license": card["license"],
        "source": source,
        "provider": "Wikimedia Commons",
        "title": filename,
    }
    return [redirect], credit


def openverse_results(query: str) -> list[dict]:
    api = "https://api.openverse.org/v1/images/?" + urlencode({
        "q": query,
        "page_size": "20",
        "mature": "false",
    })
    return get_json(api).get("results", [])


def score_candidate(item: dict, query: str) -> float:
    license_code = (item.get("license") or "").lower()
    if license_code not in ALLOWED_LICENSES or item.get("watermarked") is True:
        return -10_000
    title = (item.get("title") or "").lower()
    tokens = [t.lower() for t in re.findall(r"[A-Za-zÀ-ž0-9]+", query) if len(t) > 2]
    hits = sum(1 for token in tokens if token in title)
    width = int(item.get("width") or 0)
    height = int(item.get("height") or 0)
    landscape = 1.2 if width and height and width >= height else 0.0
    size_bonus = min(width, 2000) / 1200 if width else 0.0
    source_bonus = 0.8 if (item.get("source") or "").lower() == "wikimedia" else 0.0
    return hits * 4.0 + landscape + size_bonus + source_bonus


def openverse_candidates(query: str) -> list[tuple[list[str], dict]]:
    ranked = sorted(openverse_results(query), key=lambda item: score_candidate(item, query), reverse=True)
    ranked = [item for item in ranked if score_candidate(item, query) > -1000][:6]
    candidates: list[tuple[list[str], dict]] = []
    for item in ranked:
        license_code = (item.get("license") or "").upper()
        version = item.get("license_version") or ""
        label = "Public Domain" if license_code == "PDM" else ("CC0" if license_code == "CC0" else f"CC {license_code}{(' ' + version) if version else ''}")
        urls = []
        # Openverse thumbnails are normally smaller and more reliable for automated bundling.
        for key in ("thumbnail", "url"):
            value = item.get(key)
            if value and value not in urls:
                urls.append(value)
        if not urls:
            continue
        credit = {
            "author": html.unescape(item.get("creator") or "Open-licensed photo contributor"),
            "license": label,
            "source": item.get("foreign_landing_url") or item.get("detail_url") or item.get("url") or "https://openverse.org/",
            "provider": item.get("provider") or item.get("source") or "Openverse",
            "openverse_id": item.get("id") or "",
            "title": item.get("title") or query,
        }
        candidates.append((urls, credit))
    return candidates


def save_webp(stage_id: str, raw: bytes) -> None:
    image = Image.open(io.BytesIO(raw))
    image.load()
    if image.mode not in ("RGB", "RGBA"):
        image = image.convert("RGB")
    if image.mode == "RGBA":
        background = Image.new("RGB", image.size, "white")
        background.paste(image, mask=image.getchannel("A"))
        image = background
    fitted = ImageOps.fit(image, (1200, 675), method=Image.Resampling.LANCZOS, centering=(0.5, 0.5))
    target = OUT / f"attraction_{stage_id}.webp"
    fitted.save(target, "WEBP", quality=78, method=6)
    if target.stat().st_size < 8_000:
        target.unlink(missing_ok=True)
        raise RuntimeError("generated WebP is suspiciously small")


def try_urls(urls: list[str]) -> bytes:
    errors: list[str] = []
    for url in urls:
        try:
            raw, content_type = request_bytes(url, accept="image/*", attempts=2, timeout=35)
            if len(raw) < 10_000 or "text/html" in content_type.lower():
                raise RuntimeError("response is not a usable image")
            return raw
        except Exception as exc:
            errors.append(str(exc))
    raise RuntimeError("; ".join(errors[-2:]))


def resolve_and_download(card: dict) -> tuple[bytes, dict]:
    if card["kind"] == "item":
        urls, credit = exact_commons(card)
        try:
            return try_urls(urls), credit
        except Exception as exact_error:
            print(f"  direct Commons unavailable, using Openverse: {exact_error}", flush=True)
            query = re.sub(r"\.(jpe?g|png|webp)$", "", card["value"], flags=re.I).replace("_", " ")
    else:
        query = card["value"]

    candidates = openverse_candidates(query)
    if not candidates:
        raise RuntimeError(f"no suitable Openverse result for '{query}'")
    last_error: Exception | None = None
    for urls, credit in candidates:
        try:
            return try_urls(urls), credit
        except Exception as exc:
            last_error = exc
    raise RuntimeError(f"all Openverse candidates failed for '{query}': {last_error}")


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    ASSETS.mkdir(parents=True, exist_ok=True)
    cards = parse_cards()
    credits = load_existing_credits()
    failures: dict[str, str] = {}
    newly_bundled: list[str] = []

    for index, card in enumerate(cards, start=1):
        stage_id = card["stage_id"]
        target = OUT / f"attraction_{stage_id}.webp"
        if target.exists() and target.stat().st_size >= 8_000 and stage_id in credits:
            print(f"[{index:02d}/{len(cards)}] {stage_id}: already bundled", flush=True)
            continue

        print(f"[{index:02d}/{len(cards)}] {stage_id}: {card['value']}", flush=True)
        try:
            raw, credit = resolve_and_download(card)
            save_webp(stage_id, raw)
            credits[stage_id] = credit
            newly_bundled.append(stage_id)
            # Polite pacing. Commons exact files get a little more breathing room.
            time.sleep(2.2 if card["kind"] == "item" else 0.8)
        except Exception as exc:
            failures[stage_id] = str(exc)
            print(f"  FAILED: {exc}", flush=True)

    CREDITS.write_text(json.dumps(credits, ensure_ascii=False, indent=2, sort_keys=True), encoding="utf-8")
    present = [card["stage_id"] for card in cards if (OUT / f"attraction_{card['stage_id']}.webp").exists()]
    report = {
        "expected": len(cards),
        "bundled": len(present),
        "newly_bundled": newly_bundled,
        "missing": [card["stage_id"] for card in cards if card["stage_id"] not in present],
        "failures": failures,
    }
    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2, sort_keys=True), encoding="utf-8")

    total_bytes = sum((OUT / f"attraction_{stage}.webp").stat().st_size for stage in present)
    print(f"\nBundled {len(present)}/{len(cards)} photos ({total_bytes / 1024 / 1024:.1f} MiB).")
    if failures:
        print("Missing cards can be filled by a later incremental run; successful images are kept.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
