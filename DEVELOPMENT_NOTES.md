# Development Notes

## Architektur

Die Main-Klasse `AkzuwoFormulaOnePlugin` initialisiert nur Manager, Commands und Listener. Die Logik liegt in getrennten Paketen:

- `race`: Rennen, Status, Spieler, Boot-Markierung, Countdown, Checkpoint- und Zielauswertung.
- `physics`: Anpassung der Rennboot-Velocity.
- `track`: Track-Modelle und YAML-Laden/Speichern.
- `features`: Zentrale Feature-Schalter für Testmodus, Resourcepack, Custom Models, Sounds und Vanilla-Fallback.
- `resourcepack`: Resourcepack-Versand und Ablehnungsbehandlung.
- `visuals`: Vorbereitete Fahrzeug-Visualisierung; aktuell Vanilla-Boot-Fallback, später ItemDisplay/CustomModel.
- `sounds`: Sound-Abstraktion mit Vanilla-Fallback.
- `storage`: Bestzeiten-Abstraktion und YAML-Implementation.
- `commands`: User-, Builder-, Mod- und Admin-Commands.

## Race-Logik

`RaceManager` verwaltet aktive Rennen, Spielerzuordnung, Countdown, Boot-Spawn, Runden, Checkpoints und Zielzeiten. Er ruft `VehicleVisualManager` und `SoundManager` nur über deren abstrakte Methoden auf, damit Resourcepack-/Modell-/Sound-Details ausserhalb der Race-Logik bleiben.

## Physik

`BoatPhysicsManager` hört auf `VehicleMoveEvent`, verarbeitet nur per `PersistentDataContainer` markierte Rennboote und reduziert seitliche Drift. Werte kommen aus `config.yml`.

## Resourcepack

`ResourcePackManager` lädt die `resourcepack`-Config, sendet das Pack beim Join und kickt Spieler bei Ablehnung oder Downloadfehler nur dann, wenn Resourcepack-Features aktiv sind und der Testmodus nicht aktiv ist.

## Feature- und Fallback-Modus

`FeatureManager` liest die `features`-Sektion aus `config.yml`, validiert die Kombinationen und schreibt beim Start einen Feature-Status in die Konsole. Der Default ist ein Vanilla-Testmodus ohne Resourcepack, Custom Model und Custom Sounds.

## Visuals und Sounds

`VehicleVisualManager` lässt aktuell normale Boote sichtbar und enthält die Erweiterungspunkte für spätere `ItemDisplay`-Modelle. `SoundManager` kapselt Countdown-, Start-, Checkpoint-, Finish- und Fehler-Sounds und kann Vanilla-Fallback-Sounds nutzen.

## Track-Daten

Tracks liegen unter `plugins/AkzuwoFormulaOne/tracks/<track-id>.yml`. `TrackManager` lädt alle YAML-Dateien und prüft, ob referenzierte Welten geladen sind. Es gibt keine harte Multiverse-Abhängigkeit.

## Storage-Erweiterung

`StorageManager` ist das Interface für Bestzeiten. Aktuell ist `YamlStorageManager` aktiv. Für SQLite/MySQL kann eine neue Implementation ergänzt und in der Main-Klasse abhängig von `storage.type` ausgewählt werden.

## Analytics Endpoint Policy

Der offizielle Analytics-Endpoint ist absichtlich nicht konfigurierbar. Dadurch wird verhindert, dass Serverbetreiber den offiziellen Endpoint versehentlich falsch setzen oder Analytics-Daten an eine unerwartete Adresse senden.

Die Config enthält nur `analytics.official-endpoint-info` als sichtbaren Hinweis. Der eigentliche Versand nutzt immer `AnalyticsConstants.OFFICIAL_ANALYTICS_ENDPOINT`.

Serveradmins können:
- offizielle Analytics mit `analytics.send-analytics-data=false` deaktivieren
- eine zusätzliche Kopie an einen eigenen Endpoint senden
- eine lokale JSON-Kopie speichern

Der Analytics-Code liegt im Package `ch.akzuwo.analytics`. Versandfehler werden pro Versandweg separat abgefangen, damit fehlendes Internet, ein Custom-Copy-Ausfall oder ein lokaler Schreibfehler den Plugin-Shutdown nicht blockieren.

## Crash Reporting

`CrashReporter` ist für gezielte Plugin-Exception-Berichte zuständig. Es wird bewusst kein kompletter Console Output abgefangen, da dieser sensible Daten oder Logs anderer Plugins enthalten kann.

`PluginLogBuffer` speichert nur interne AkzuwoFormulaOne-Logs im RAM. Aktuell werden gezielt Plugin-Ereignisse und gemeldete Exceptions in diesen Buffer geschrieben, nicht die globale Bukkit-Konsole.

`SafeExceptionUtil` maskiert sensible Werte wie Passwörter, Tokens, Authorization Header, JDBC-Credentials, UUIDs und IP-Adressen und begrenzt Stacktrace-Längen. Crashreports nutzen denselben festen offiziellen Endpoint wie Analytics, können aber zusätzlich über Custom Copy und Local Copy laufen. Rate-Limiting verhindert Report-Spam bei wiederholten Tick-Fehlern.

## RankPointsAPI Service Integration

Die Punkte-Integration liegt in:

`ch.ksrminecraft.points`

AkzuwoFormulaOne greift nicht direkt auf Datenbank oder Credentials zu. Der Zugriff erfolgt über:

- `RankPointsHook`
- `RankPointsConfig`
- `RacePointsCalculator`
- `PointsRewardService`
- `PointsRewardResult`

`RankPointsHook` bezieht den `RankPointsService` über Bukkit `ServicesManager`. Die direkte Service-Klasse ist in `RankPointsServiceBridge` isoliert, damit AkzuwoFormulaOne auch ohne installiertes RankPointsAPI-Plugin starten kann.

Aktuell wird vorübergehend `https://github.com/Akzuwo/RankPointsAPI.git` auf dem Branch `feature/bukkit-service-api` verwendet, bis die Service-Schnittstelle im offiziellen Repository verfügbar ist.
