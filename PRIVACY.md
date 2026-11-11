# Privacy & anonymous usage stats

GTLTagger can send **optional, anonymous usage statistics**. It is **off by
default**. Nothing is sent unless you tick **Anonymous Usage Stats** in the
GTLTagger settings screen (and the mod build you use has a collector
configured).

## What is sent (at most once per day)

| Field | Example |
|---|---|
| Random install id (created when you opt in) | `3f2b8c1e-...` |
| GTLTagger version | `1.1.0` |
| Minecraft / Fabric Loader / Java versions | `26.3` / `0.19.5` / `25` |
| OS family | `windows`, `macos`, `linux` or `other` |
| Your GTLTagger settings | selected gamemodes, where tiers are shown, tier text format, HUD on/off, auto-detection on/off |

## What is never sent

Your Minecraft username or UUID, server addresses, chat, world data, file
paths, or anything about other players.

The collector necessarily receives your IP address as part of the network
request; the example collector in `telemetry-server-example/` neither reads
nor stores it.

## Control

* Untick the setting at any time - your install id is deleted immediately.
* Every report is written to the game log (`Sending anonymous usage stats...`)
  with its complete body, so you can see exactly what was sent.
* The code is in `com/gtltagger/telemetry/Telemetry.java`; the fields are
  built in `buildPayload`.
