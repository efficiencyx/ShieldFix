# ShieldFix — Short Documentation for Owners

Version: 1.6

---

## Overview

ShieldFix is a small Paper plugin that manages the shield blocking delay (shield blocking cooldown). It provides: global settings, per-player overrides, commands to manage delays (set/get/inc/dec), configurable minimum/maximum limits, and optional PlaceholderAPI integration.

This quick guide is intended for server owners and administrators who will install and configure the plugin.

---

## Requirements

* Paper server (native for 1.19.x, should work 1.12-1.20)
* Java compatible with your server
* (Optional) **PlaceholderAPI** if you want to use placeholders

---

## Where per-player delays are stored

Custom per-player delays are stored in `config.yml` under the `player-delays` section using UUID keys:

`#TODO: Migrate to a DB such as SQLite/MySQL/MariaDB`
```yaml
player-delays:
  "2a09550e-b4cb-45be-b37e-708048303812": 2
```

Using UUIDs ensures the value stays associated with the account even if the player changes their name.

---

## Main `config.yml` options

* `shielddelay` (int): global default shield delay (e.g. 0).
* `custom-cooldown` (int): compatibility alternate value used for listed players or users with a permission node.
* `custom-cooldown-perms` (string): permission node that grants `custom-cooldown`.
* `custom-cooldown-players` (list): list of player names that receive `custom-cooldown`.
* `features.commands.enable` (bool): enable/disable commands (`/shieldset`, `/shielddelay`).
* `features.papi.enable` (bool): if true, attempts to register PlaceholderAPI expansion (only works if PlaceholderAPI is present).
* `features.player_delays.enable` (bool): enable per-player delay support (`player-delays`).
* `features.shield_event.enable` (bool): enable/disable the plugin's event logic.
* `limits.min-delay` (int): minimum allowed delay value.
* `limits.max-delay` (int): maximum allowed delay value.

> Note: values set via commands are automatically clamped between `min-delay` and `max-delay`.

---

## Commands (quick reference)

* `/shieldset <player> <value>`

    * Usage: set another player's delay (player must be online).
    * Permission: `shieldfix.set.others` (default: op).

* `/shielddelay get [player]` — get your delay or another player's delay (requires `shieldfix.get.others` for others).

* `/shielddelay set <value> [player]` — set the delay (without player sets your own; requires `shieldfix.set.self` for self, `shieldfix.set.others` for others).

* `/shielddelay inc [amount]` — increase your delay (default +1). Permission: `shieldfix.modify.self`.

* `/shielddelay dec [amount]` — decrease your delay (default -1). Permission: `shieldfix.modify.self`.

* `/shielddelay reload` — reload config. Permission: `shieldfix.reload`.

Tip: add these permission nodes to an admin group in your permission plugin (e.g., LuckPerms).

---

## PlaceholderAPI placeholders

If `features.papi.enable` is `true` and PlaceholderAPI is installed, the plugin registers the following placeholders:

* `%shieldfix_delay%` — returns the effective delay of the player viewing the placeholder.
* `%shieldfix_delay_<player>%` — returns the effective delay of an online player (e.g. `%shieldfix_delay_SomeNick%`).

If placeholders show empty values: ensure PlaceholderAPI is installed and `features.papi.enable: true`, then restart the server.

---

## Behavior & priority rules

1. If `features.player_delays.enable` is active and a `player-delays` entry exists for the player's UUID, that value is applied.
2. Otherwise, if the player is listed in `custom-cooldown-players` or has the `custom-cooldown-perms` permission, `custom-cooldown` is used (backwards compatibility).
3. Otherwise the global `shielddelay` value is used.

The plugin also checks whether the interaction event is `cancelled()`; if so, it applies the original fallback behavior from the base plugin.

---

## Quick examples

Set min/max in `config.yml`:

```yaml
limits:
  min-delay: 0
  max-delay: 10
```

Set a per-player delay (player must be online):

```
/shieldset PlayerName 3
```

This writes the delay into `player-delays` using the player's UUID.

---

## Quick troubleshooting

* *Commands not working*: check `features.commands.enable` in config.
* *Placeholders empty*: ensure PlaceholderAPI is installed and `features.papi.enable: true`.
* *Value different from expected*: values are clamped to `limits.min-delay` / `limits.max-delay`.
* *Remove per-player override*: delete the UUID entry under `player-delays` in the config and reload.

---

## Recommendations for owners

* UUID-based storage is recommended (already implemented).
* Restrict sensitive permission nodes (`shieldfix.set.others`, `shieldfix.reload`) to trusted staff only.
* If you don't use PlaceholderAPI, set `features.papi.enable` to `false` in the config to avoid registration attempts.