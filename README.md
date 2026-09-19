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
- 🔍 **Player search** — look up any player's GTL tiers across every kit
  without leaving the game.
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

- Minecraft **1.21.1 – 1.21.7**
- [Fabric Loader](https://fabricmc.net/use/) 0.16.14+
- [Fabric API](https://modrinth.com/mod/fabric-api) (matching your MC
  version)
- Java 21

GTL Tiertagger is **client-side only** — no server installation needed.

## 🛠️ Building from source

A GitHub Actions workflow builds the mod jar in the cloud on every push, one
job per supported Minecraft patch:

1. Push this project to a GitHub repo.
2. Open the **Actions** tab and let the **Build** workflow run.
3. Download the matching **GTLTagger-mc\*** artifact for your Minecraft
   version from the finished run.
4. Pushing a tag like `v1.0.0` also publishes every version's jar together
   as a single GitHub Release.

To build locally instead (requires JDK 21):

```
gradle build       # or ./gradlew build once the wrapper is generated
gradle runClient   # to test in a dev client
```

The built jar appears in `build/libs/`.

## 🙌 Credits

Created by **[@darkboyop](https://github.com/darkboyop)** for the
[Global Tierlist](https://gtltierlist.vercel.app).

## 💬 Support / Community

Questions, bug reports, or feature requests — join the Discord:
**[discord.gg/6Yvgvr8Vdy](https://discord.gg/6Yvgvr8Vdy)**

Or check out the Global Tierlist itself: **[gtltierlist.vercel.app](https://gtltierlist.vercel.app)**

## 📄 License

All Rights Reserved — see [LICENSE](LICENSE). Compiled builds (the jar on
Modrinth/GitHub) are free to download and use; the source code is not
licensed for reuse or redistribution without permission.
