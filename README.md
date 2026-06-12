# FormulaBoatRacing

Minecraft Paper Plugin für Formel-1-artige Bootrennen. Die technische Basis sind markierte Boote, deren Fahrverhalten per Plugin angepasst wird. Optisch kann das Boot später über ein Resourcepack als Formel-1-Auto dargestellt werden.

## Installation

1. Mit Java 21 bauen: `mvn clean package`
2. Die erzeugte JAR aus `target/` in den `plugins/`-Ordner des Paper-Servers legen.
3. Server starten, damit `config.yml`, `messages.yml`, `tracks/` und `times.yml` angelegt werden.
4. Optional Resourcepack-URL und SHA1 in `config.yml` eintragen.

Zielplattform: Paper 1.21.x.

## Resourcepack

Das Plugin kann Spielern beim Join ein Resourcepack senden und es optional erzwingen. Das echte Pack wird hier nicht generiert. Hinweise liegen in `resourcepack/README_RESOURCEPACK.md`.

Wichtig: Das Resourcepack muss extern als ZIP gehostet und in `config.yml` verlinkt werden.

## Commands

User:
- `/f1`
- `/f1 tracks`
- `/f1 join <trackId>`
- `/f1 leave`
- `/f1 start`
- `/f1 best <trackId>`
- `/f1 leaderboard <trackId>`
- `/f1 spectate <trackId>` vorbereitet

Builder:
- `/f1builder create <trackId> <displayName>`
- `/f1builder setworld <trackId>`
- `/f1builder setlobby <trackId>`
- `/f1builder setspawn <trackId>`
- `/f1builder setstart <trackId>`
- `/f1builder setfinish <trackId>`
- `/f1builder addcheckpoint <trackId> <name> <radius>`
- `/f1builder removecheckpoint <trackId> <name>`
- `/f1builder setlaps <trackId> <laps>`
- `/f1builder enable <trackId>`
- `/f1builder disable <trackId>`
- `/f1builder save <trackId>`
- `/f1builder reload <trackId>`
- `/f1builder list`

Moderation:
- `/f1mod cancel <trackId|raceId>`
- `/f1mod kick <player>`
- `/f1mod tp <trackId>`
- `/f1mod info <trackId|raceId>`

Admin:
- `/f1admin reload`
- `/f1admin save`
- `/f1admin debug`
- `/f1admin resourcepack reload`
- `/f1admin physics reload`

## Permissions

Alle Permissions sind in `plugin.yml` registriert. Hauptgruppen:
- `formulaboat.user`
- `formulaboat.mod`
- `formulaboat.admin`
- `formulaboat.builder`

Feinrechte:
- `formulaboat.user.join`
- `formulaboat.user.leave`
- `formulaboat.user.start`
- `formulaboat.user.leaderboard`
- `formulaboat.mod.cancel`
- `formulaboat.mod.kick`
- `formulaboat.mod.tp`
- `formulaboat.mod.info`
- `formulaboat.admin.reload`
- `formulaboat.admin.debug`
- `formulaboat.admin.resourcepack`
- `formulaboat.admin.physics`
- `formulaboat.builder.create`
- `formulaboat.builder.edit`
- `formulaboat.builder.checkpoint`
- `formulaboat.builder.save`
- `formulaboat.builder.enable`

## Track-Setup

1. Welt laden, zum Beispiel über Multiverse.
2. `/f1builder create monaco Monaco`
3. An die Lobby-Position gehen: `/f1builder setlobby monaco`
4. An die Startposition gehen: `/f1builder setspawn monaco`
5. Start- und Ziellinie setzen: `/f1builder setstart monaco`, `/f1builder setfinish monaco`
6. Checkpoints in Reihenfolge setzen: `/f1builder addcheckpoint monaco cp1 5`
7. Runden setzen: `/f1builder setlaps monaco 3`
8. Aktivieren und speichern: `/f1builder enable monaco`, `/f1builder save monaco`

Tracks werden als YAML unter `plugins/FormulaBoatRacing/tracks/<track-id>.yml` gespeichert.

## Multiverse

Es gibt eine `softdepend` auf `Multiverse-Core`, aber keine harte Abhängigkeit. Das Plugin erstellt keine Welten automatisch. Wenn eine Track-Welt nicht geladen ist, wird die Strecke als nicht nutzbar behandelt und beim Laden gewarnt.

## TODO

- Finales 3D-Resourcepack bauen.
- Spectator-System fertigstellen.
- Boxenstopps, Qualifying, Teams und Fahrzeugklassen ergänzen.
- SQLite/MySQL implementieren.
- Web-Leaderboard anbinden.
- Replay/Ghost-System planen.
- GUI für Streckenauswahl bauen.
