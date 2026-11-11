# GTLTagger - jar for Minecraft 1.21.9 - 1.21.11

This folder is a separate Gradle project because Minecraft 1.21.9 changed
the input API (`KeyInput`, `Click`) and keybind categories, which makes the
code incompatible with the 1.21.1 - 1.21.8 jar in the repo root. The shared
logic (data, gamemodes, config, tags) is identical; only the client/GUI
classes differ.

Built by `.github/workflows/build-1.21.9.yml`: the release jar is compiled
against 1.21.9, and 1.21.10 / 1.21.11 are compile-checked.
