# Development Notes

## Architektur

Die Main-Klasse `FormulaBoatRacingPlugin` initialisiert nur Manager, Commands und Listener. Die Logik liegt in getrennten Paketen:

- `race`: Rennen, Status, Spieler, Boot-Markierung, Countdown, Checkpoint- und Zielauswertung.
- `physics`: Anpassung der Rennboot-Velocity.
- `track`: Track-Modelle und YAML-Laden/Speichern.
- `resourcepack`: Resourcepack-Versand und Ablehnungsbehandlung.
- `storage`: Bestzeiten-Abstraktion und YAML-Implementation.
- `commands`: User-, Builder-, Mod- und Admin-Commands.

## Race-Logik

`RaceManager` verwaltet aktive Rennen, Spielerzuordnung, Countdown, Boot-Spawn, Runden, Checkpoints und Zielzeiten. `Race`, `RacePlayer`, `RaceState`, `RaceType`, `RaceResult` und `LapTimer` sind kleine Daten-/Statusklassen.

## Physik

`BoatPhysicsManager` hört auf `VehicleMoveEvent`, verarbeitet nur per `PersistentDataContainer` markierte Rennboote und reduziert seitliche Drift. Werte kommen aus `config.yml`.

## Resourcepack

`ResourcePackManager` lädt die `resourcepack`-Config, sendet das Pack beim Join und kickt Spieler bei Ablehnung oder Downloadfehler, wenn `force` aktiv ist.

## Track-Daten

Tracks liegen unter `plugins/FormulaBoatRacing/tracks/<track-id>.yml`. `TrackManager` lädt alle YAML-Dateien und prüft, ob referenzierte Welten geladen sind. Es gibt keine harte Multiverse-Abhängigkeit.

## Storage-Erweiterung

`StorageManager` ist das Interface für Bestzeiten. Aktuell ist `YamlStorageManager` aktiv. Für SQLite/MySQL kann eine neue Implementation ergänzt und in der Main-Klasse abhängig von `storage.type` ausgewählt werden.
