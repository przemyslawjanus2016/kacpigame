from pathlib import Path
import subprocess


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if old in text:
        return text.replace(old, new, 1)
    if new in text:
        return text
    raise SystemExit(f"Patch point not found: {label}")


# 1) Remove duplicate Rewards/Parent screens and restore the proven Settings screen.
extras_path = "app/src/main/java/pl/kacperikapi/mathadventure/ui/screens/AdventureExtras.kt"
extras = Path(extras_path).read_text(encoding="utf-8")
main_extras = subprocess.check_output(
    ["git", "show", f"origin/main:{extras_path}"],
    text=True,
)

duplicate_marker = "@Composable\nfun RewardsScreen("
settings_marker = "@Composable\nfun SettingsScreen("
if duplicate_marker in extras:
    if settings_marker not in main_extras:
        raise SystemExit("SettingsScreen marker not found on main")
    extras = extras[: extras.index(duplicate_marker)] + main_extras[main_extras.index(settings_marker) :]
    Path(extras_path).write_text(extras, encoding="utf-8")


# 2) Fix WorldGrid signature regression.
world_path = "app/src/main/java/pl/kacperikapi/mathadventure/ui/screens/WorldSelectScreen.kt"
world = Path(world_path).read_text(encoding="utf-8")
old_world = """private fun WorldGrid(
    progress: GameProgress,
    premiumUnlocked: Boolean,
    activeProfileName: String,
    onProfile: () -> Unit,
    onWorld: (Int) -> Unit,
    onPremium: () -> Unit,
    modifier: Modifier = Modifier
)"""
new_world = """private fun WorldGrid(
    progress: GameProgress,
    premiumUnlocked: Boolean,
    onWorld: (Int) -> Unit,
    onPremium: () -> Unit,
    modifier: Modifier = Modifier
)"""
world = replace_once(world, old_world, new_world, "WorldGrid signature")
Path(world_path).write_text(world, encoding="utf-8")


# 3) First-run onboarding: language first, then player profile.
main_path = "app/src/main/java/pl/kacperikapi/mathadventure/MainActivity.kt"
main = Path(main_path).read_text(encoding="utf-8")

main = replace_once(
    main,
    "    data object Splash : Screen\n    data object Profiles : Screen",
    "    data object Splash : Screen\n    data object Language : Screen\n    data object Profiles : Screen",
    "Screen.Language",
)

main = replace_once(
    main,
    "BackHandler(enabled = screen != Screen.Worlds && screen != Screen.Splash && !(screen == Screen.Profiles && store.activeProfile() == null))",
    "BackHandler(enabled = screen != Screen.Worlds && screen != Screen.Splash && screen != Screen.Language && !(screen == Screen.Profiles && store.activeProfile() == null))",
    "BackHandler language guard",
)

main = replace_once(
    main,
    "            Screen.Splash -> Screen.Profiles\n            Screen.Profiles -> Screen.Worlds",
    "            Screen.Splash -> Screen.Language\n            Screen.Language -> Screen.Language\n            Screen.Profiles -> Screen.Worlds",
    "BackHandler language branch",
)

old_render = "        Screen.Splash -> SplashScreen { screen = Screen.Profiles }\n        Screen.Profiles -> ProfileSelectScreen("
new_render = (
    "        Screen.Splash -> SplashScreen {\n"
    "            screen = if (store.hasSavedLanguage()) Screen.Profiles else Screen.Language\n"
    "        }\n"
    "        Screen.Language -> LanguageSelectScreen(\n"
    "            suggestedLanguage = store.loadLanguage(),\n"
    "            onSelect = { tag ->\n"
    "                store.saveLanguage(tag)\n"
    "                onLanguageChanged()\n"
    "            }\n"
    "        )\n"
    "        Screen.Profiles -> ProfileSelectScreen("
)
main = replace_once(main, old_render, new_render, "LanguageSelectScreen render")
Path(main_path).write_text(main, encoding="utf-8")

print("v0.5.3 foundation and first-run onboarding patch applied")
