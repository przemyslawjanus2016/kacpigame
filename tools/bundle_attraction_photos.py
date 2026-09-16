#!/usr/bin/env python3
"""Bundle one open-licensed attraction photo per game stage.

Curated exact Commons filenames already present in AttractionContent.kt are downloaded
straight from Wikimedia's file redirect (no search API). Cards that currently contain a
search query are resolved through Openverse, preferring commercial-use Creative Commons
or public-domain images. Every image is centre-cropped to 16:9, resized to 1200x675 and
saved as WebP for predictable APK size.
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
USER_AGENT = "KacperKapiTheGame/0.5.3 (educational Android app; open-licensed offline media)"
ALLOWED_LICENSES = {"cc0", "pdm", "by", "by-sa"}
EXPECTED_CARDS = 70


def parse_cards() -> list[dict]:
    text = SOURCE.read_text(encoding="utf-8")
    block_re = re.compile(r"^ {8}(item|searchItem)\(\s*\n(.*?)^ {8}\),?\s*$", re.M | re.S)
    str_re = re.compile(r'"((?:\\.|[^"\\])*)"')
    cards: list[dict] = []
    for kind, body in block_re.findall(text):
        args = [bytes(s, "utf-8").decode("unicode_escape") if "\\" in s else s for s in str_re.findall(body)]
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


def request_bytes(url: str, *, accept: str | None = None, attempts: int = 7) -> tuple[bytes, str]:
    headers = {"User-Agent": USER_AGENT}
    if accept:
        headers["Accept"] = accept
    last_error: Exception | None = None
    for attempt in range(attempts):
        try:
            req = Request(url, headers=headers)
            with urlopen(req, timeout=60) as response:
                return response.read(), response.headers.get("Content-Type", "")
        except HTTPError as exc:
            last_error = exc
            if exc.code not in {429, 500, 502, 503, 504}:
                raise
            retry_after = exc.headers.get("Retry-After") if exc.headers else None
            wait = float(retry_after) if retry_after and retry_after.isdigit() else min(45.0, 2.5 * (2 ** attempt))
            print(f"  HTTP {exc.code}; retrying in {wait:.1f}s", flush=True)
            time.sleep(wait)
        except (URLError, TimeoutError) as exc:
            last_error = exc
            wait = min(30.0, 2.0 * (attempt + 1))
            print(f"  network error; retrying in {wait:.1f}s: {exc}", flush=True)
            time.sleep(wait)
    raise RuntimeError(f"Download failed after {attempts} attempts: {url} ({last_error})")


def get_json(url: str) -> dict:
    raw, _ = request_bytes(url, accept="application/json")
    return json.loads(raw.decode("utf-8"))


def commons_exact(card: dict) -> tuple[str, dict]:
    filename = card["value"]
    redirect = "https://commons.wikimedia.org/wiki/Special:Redirect/file/" + quote(filename, safe="") + "?width=1600"
    source = "https://commons.wikimedia.org/wiki/File:" + quote(filename.replace(" ", "_"), safe="()_,.-")
    credit = {
        "author": card["author"],
        "license": card["license"],
        "source": source,
        "provider": "Wikimedia Commons",
    }
    return redirect, credit


def openverse_search(query: str) -> tuple[list[dict], str]:
    params = {
        "q": query,
        "page_size": "20",
        "mature": "false",
    }
    api = "https://api.openverse.org/v1/images/?" + urlencode(params)
    data = get_json(api)
    return data.get("results", []), api


def score_candidate(item: dict, query: str) -> float:
    license_code = (item.get("license") or "").lower()
    if license_code not in ALLOWED_LICENSES:
        return -10_000
    if item.get("watermarked") is True:
        return -10_000

    title = (item.get("title") or "").lower()
    query_tokens = [t.lower() for t in re.findall(r"[A-Za-zÀ-ž0-9]+", query) if len(t) > 2]
    title_hits = sum(1 for token in query_tokens if token in title)
    width = int(item.get("width") or 0)
    height = int(item.get("height") or 0)
    size_bonus = min(width, 2200) / 1000 if width else 0
    landscape_bonus = 1.5 if width and height and width >= height else 0
    source_bonus = 1.0 if (item.get("source") or "").lower() == "wikimedia" else 0
    return title_hits * 4.0 + size_bonus + landscape_bonus + source_bonus


def resolve_openverse(query: str) -> tuple[list[str], dict]:
    results, api = openverse_search(query)
    ranked = sorted(results, key=lambda item: score_candidate(item, query), reverse=True)
    ranked = [item for item in ranked if score_candidate(item, query) > -1000]
    if not ranked:
        raise RuntimeError(f"No commercial-use open-license result from Openverse for: {query} ({api})")

    # Try several ranked results because some upstream hosts reject automated image downloads.
    best = ranked[0]
    urls: list[str] = []
    for item in ranked[:8]:
        for key in ("url", "thumbnail"):
            value = item.get(key)
            if value and value not in urls:
                urls.append(value)

    license_code = (best.get("license") or "").upper()
    license_version = best.get("license_version") or ""
    license_label = "Public Domain" if license_code == "PDM" else ("CC0" if license_code == "CC0" else f"CC {license_code}{(' ' + license_version) if license_version else ''}")
    credit = {
        "author": html.unescape(best.get("creator") or "Open-licensed photo contributor"),
        "license": license_label,
        "source": best.get("foreign_landing_url") or best.get("detail_url") or best.get("url") or "https://openverse.org/",
        "provider": best.get("provider") or best.get("source") or "Openverse",
        "openverse_id": best.get("id") or "",
        "title": best.get("title") or query,
    }
    return urls, credit


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
        raise RuntimeError(f"Generated image is suspiciously small: {target}")


def download_first_working(urls: list[str]) -> bytes:
    errors: list[str] = []
    for url in urls:
        try:
            raw, content_type = request_bytes(url, accept="image/*", attempts=4)
            if len(raw) < 10_000:
                errors.append(f"too small: {url}")
                continue
            if "text/html" in content_type.lower():
                errors.append(f"HTML instead of image: {url}")
                continue
            return raw
        except Exception as exc:
            errors.append(f"{url}: {exc}")
    raise RuntimeError("No candidate image could be downloaded: " + " | ".join(errors[-3:]))


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    ASSETS.mkdir(parents=True, exist_ok=True)
    cards = parse_cards()
    credits: dict[str, dict] = {}
    failures: list[tuple[str, str]] = []

    for index, card in enumerate(cards, start=1):
        stage_id = card["stage_id"]
        print(f"[{index:02d}/{len(cards)}] {stage_id}: {card['value']}", flush=True)
        try:
            if card["kind"] == "item":
                url, credit = commons_exact(card)
                raw = download_first_working([url])
                # Slow down exact Commons requests to avoid HTTP 429.
                time.sleep(1.4)
            else:
                urls, credit = resolve_openverse(card["value"])
                raw = download_first_working(urls)
                # Anonymous Openverse clients are intentionally paced.
                time.sleep(1.1)

            save_webp(stage_id, raw)
            credits[stage_id] = credit
        except Exception as exc:
            failures.append((stage_id, str(exc)))
            print(f"  FAILED: {exc}", flush=True)

    CREDITS.write_text(json.dumps(credits, ensure_ascii=False, indent=2, sort_keys=True), encoding="utf-8")

    if failures:
        print("\nFailures:")
        for stage_id, reason in failures:
            print(f"- {stage_id}: {reason}")
        print(f"Bundled {len(credits)}/{len(cards)} photos.")
        return 2

    total_bytes = sum((OUT / f"attraction_{card['stage_id']}.webp").stat().st_size for card in cards)
    print(f"\nBundled {len(credits)} attraction photos; total WebP size: {total_bytes / 1024 / 1024:.1f} MiB")
    return 0


if __name__ == "__main__":
    sys.exit(main())
