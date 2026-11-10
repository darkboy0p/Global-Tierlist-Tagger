# GTL Tiertagger

**See Global Tierlist (GTL) tiers. Everywhere.**

GTL Tiertagger is a client-side Fabric mod for the
[**Global Tierlist**](https://gtltierlist.vercel.app), pulling live player
tier data straight into your game and showing it right where you're already
looking — the TAB list, nametags above players' heads, and your own
on-screen HUD. No more alt-tabbing to the tier list website mid-fight.

🌐 **Global Tierlist website:** [gtltierlist.vercel.app](https://gtltierlist.vercel.app)

## ✨ Features

- 🏷️ **TAB list tiers** — every GTL-tested player in the TAB list gets
  their tier shown next to their name, with a real icon for the kit it was
  tested in (NethPot, Pot, Crystal, Sword, UHC, Axe, SMP, Mace).
- 🎮 **In-world nametags** — the same tier + icon can also show on a
  player's nametag above their head, so you know who you're fighting before
  you're even close enough to check TAB.
- 📊 **On-screen HUD** — a movable, resizable HUD showing your Global
  Tierlist tier info at a glance, positioned however you like via
  `/gtltagger settings` or the HUD position screen.
- 🧩 **Every gamemode** — all kits the Global Tierlist API returns are
  supported, not just a fixed list. Gamemodes added to the tier list later
  show up automatically (TAB, nametags, HUD, search, settings cyclers and
  kit detection); the eight classic kits keep their custom icons, new ones
  show tier text without an icon.
- 🔍 **Player search** — look up any player's tiers (and peaks) across every
  gamemode without leaving the game: live name suggestions with
  autocomplete (↑/↓ to pick, Tab to complete, Enter to search), results that
  update as you type, and a scrollable list for players with many modes.
- 🤖 **Automatic kit detection** — reads your server's scoreboard/TAB
  header-footer to figure out which kit you're currently playing, so the
  right tier shows automatically.
- 🎯 **Independent Left/Right slots** — track two different kits at once
  (e.g. your current kit on the left, always-visible Crystal on the right),
  each with its own TAB / Nametag / Both / Off toggle.
- 📋 **TierTag generator** — instantly copy a `[:icon:TIER]IGN[:icon:TIER]`
  tag for your own current tier to your clipboard, for pasting in chat.
- ⚙️ **Fully configurable in-game** — every setting lives in its own
  settings screen (`/gtltagger` or a bindable keybind), no config file
  editing required.
- 🔄 **Always up to date** — tier data refreshes automatically every 5
  minutes in the background, plus an instant `/gtltagger reload`.

## ⌨️ Default keybinds

| Key | Action |
|---|---|
| `G` | Cycle the Left slot's gamemode |
| `K` | Cycle the Right slot's gamemode |
| `H` | Open player search |

All keybinds are rebindable in-game, either from GTL Tiertagger's own
Settings screen or from vanilla's Controls & Keybinds menu under "GTLTagger".
Two extra actions ("Copy My TierTag" and "Open Settings") exist as keybinds
too but ship unbound — set them yourself if you want them.

## 💬 Commands

- `/gtltagger` or `/gtltagger settings` — open the settings menu
- `/gtltagger tag` — copy your current TierTag to the clipboard
- `/gtltagger reload` — force-refresh tier data from Global Tierlist

## 📦 Requirements

- Minecraft **1.21.1 – 1.21.8**
- [Fabric Loader](https://fabricmc.net/use/) 0.16.14+
- [Fabric API](https://modrinth.com/mod/fabric-api) (matching your MC
  version)
- Java 21

GTL Tiertagger is **client-side only** — no server installation needed.

## 🛠️ Building from source

One jar covers every supported Minecraft 1.21.x version (1.21.1 – 1.21.8).
GitHub Actions compiles it against 1.21.1 and then compile-checks the same
source against 1.21.2 – 1.21.8:

1. Push to GitHub and open the **Actions** tab.
2. Download the **GTLTagger** artifact from the finished **Build** run.
3. Pushing a tag like `v1.1.0` also publishes the jar as a GitHub Release.

Local build (JDK 21): `gradle build` (jar in `build/libs/`; local builds
compile against whatever `gradle.properties` says - the CI jar uses 1.21.1).

## 🙌 Credits

Created by **[@darkboyop](https://github.com/darkboyop)** for the
[Global Tierlist](https://gtltierlist.vercel.app).

The search screen's behaviour (name suggestions, live results) is inspired by
[uku3lig/TierTagger](https://github.com/uku3lig/TierTagger) (MPL-2.0); the
implementation in this repo is separate, and no TierTagger source code is
included.

### AI assistance disclosure

Parts of this project's code were written with the help of an AI assistant
(Anthropic's Claude) and reviewed/maintained by the author. This project is
not published on Modrinth.

## 💬 Support / Community

Questions, bug reports, or feature requests — join the Discord:
**[discord.gg/6Yvgvr8Vdy](https://discord.gg/6Yvgvr8Vdy)**

Or check out the Global Tierlist itself: **[gtltierlist.vercel.app](https://gtltierlist.vercel.app)**

## 📄 License

All Rights Reserved — see [LICENSE](LICENSE). Compiled builds (the jar on
Modrinth/GitHub) are free to download and use; the source code is not
licensed for reuse or redistribution without permission.
