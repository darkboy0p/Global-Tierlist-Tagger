# GTLTagger

Fabric mod (Minecraft 1.20.1) implementing the GTLTagger spec: TierTag
generation, gamemode selection + cycling, keybinds, TAB tier display,
automatic kit detection, and an in-game HUD, all configurable from a
dedicated in-mod settings screen.

## Design decisions / assumptions

The spec left a few things unstated. Here's what this implementation
assumes, and where to change it if you meant something else:

- **Whose tier does the HUD/tag show?** The HUD and the
  "generate my TierTag" action are about the **local player's own**
  tier — the thing you'd paste in chat when someone asks "what's your
  tier?". The **TAB list** is the separate feature that shows *other*
  players' tested tiers next to their names, per spec section 5.
- **No "generate tag" keybind was specified.** Only "Switch Gamemode"
  and "Toggle Left/Right" have defaults in the spec. This mod adds an
  optional `key.gtltagger.copy_tag` keybinding (unbound by default —
  bind it in Controls & Keybinds if you want one) and a `/gtltagger
  tag` command, either of which copies your current TierTag to the
  system clipboard.
- **"[:icon:TIER]" is literal text**, not a rendered image — it
  matches how many servers resolve custom icons via a resource-pack
  font (`[:icon:LT3]` etc.). If you actually want a texture-based icon
  rendered by the mod itself (e.g. in the HUD), that's a separate,
  larger feature — ping me and I'll add it.
- **Kit detection** (`KitDetector`) is a best-effort heuristic: it
  scans the sidebar scoreboard title and the TAB header/footer for a
  known gamemode name. There is no universal way to know "what kit am
  I in" across arbitrary servers — adjust `KitDetector.detect()` to
  match how your server actually surfaces the current kit (action bar,
  boss bar, a specific scoreboard line, etc.).
- **The "Switch Gamemode" keybind cycles `gamemode1`** (the primary
  slot). `gamemode2` is only changed via its dropdown in Settings,
  matching the spec's "don't swap the two configured gamemodes" rule.

## Tier data

This mod doesn't assume any particular tier-list backend/API. Player
tier data lives in a local file you populate however fits your setup
(export script, a companion server plugin, or by hand):

```
<minecraft>/config/gtltagger/players.json
```

```json
{
  "SomePlayer": {
    "NethPot": { "tier": "LT3", "peak": "HT3" },
    "Crystal": { "tier": "HT2" }
  }
}
```

An example file is auto-created on first launch if none exists.
Reload it in-game without restarting via `/gtltagger reload`.

## Building — no local build needed

A GitHub Actions workflow (`.github/workflows/build.yml`) builds the
mod jar in the cloud on every push, so a low-end PC never has to run
Gradle/Loom locally:

1. Push this project to a GitHub repo.
2. Open the **Actions** tab — the "Build" workflow runs automatically.
3. Once it finishes, open the run and download the **GTLTagger**
   artifact (the built jar) from the bottom of the page.
4. Pushing a tag like `v1.0.0` also publishes the jar as a GitHub
   Release automatically.

Note: this project does **not** bundle the Gradle wrapper jar (a
binary file), so `./gradlew` won't work until you generate it once —
run `gradle wrapper` locally with any installed Gradle, or just rely
on CI above. If you do want to build locally, requires JDK 17+ and
internet access on first run:

```
gradle build       # or ./gradlew build, once the wrapper is generated
```

The built mod jar appears in `build/libs/`. Drop it (plus [Fabric API]
(https://modrinth.com/mod/fabric-api) for the matching Minecraft
version) into your `mods` folder along with Fabric Loader.

To test locally in a dev client:

```
gradle runClient
```

## In-game usage

- `H` — cycle Gamemode 1 through the configured list.
- `J` — cycle Left/Right tier display: Both → Left → Right → Off.
- `/gtltagger` (or `/gtltagger settings`) — open the settings menu.
- `/gtltagger tag` — copy your current TierTag to the clipboard.
- `/gtltagger reload` — reload `players.json` without restarting.

All keybinds are also rebindable from the mod's own Settings screen
(click the key button, then press a new key) or from vanilla's
Controls & Keybinds screen under the "GTLTagger" category.

## Not yet implemented

The current codebase targets **Minecraft 1.20.1, Fabric Loader
0.15.7+, Java 17** — the same verified-working baseline from before,
just renamed. It does **not** yet include the newer feature list
(1.21.1–1.21.11 support, Fabric Loader 0.16+, Java 21, Mod Menu
integration, an in-settings player search, per-tier customizable
colors, or resource-pack-driven icon textures). I held off on bumping
the Minecraft/Java versions in this pass because the existing code
calls 1.20.1-mapped API names (`PlayerListHud`, `Scoreboard`,
`CheckboxWidget`, etc.) that can rename between major versions —
changing the version numbers without re-verifying every call would
likely break the "no crashes, no errors" goal rather than help it.

Say the word and I'll do that port properly next: re-verify each API
call against 1.21.1 mappings, add the Mod Menu config-screen
entrypoint, and build out search + color customization + resource-pack
icon support.
