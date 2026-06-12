# AkzuwoFormulaOne

Minecraft Paper Plugin für Formel-1-artige Bootrennen. Die technische Basis sind markierte Boote, deren Fahrverhalten per Plugin angepasst wird. Optisch kann das Boot später über ein Resourcepack als Formel-1-Auto dargestellt werden.

## Installation

1. Mit Java 21 bauen: `mvn clean package`
2. Die erzeugte JAR aus `target/` in den `plugins/`-Ordner des Paper-Servers legen.
3. Server starten, damit `config.yml`, `messages.yml`, `tracks/` und `times.yml` angelegt werden.
4. Optional Resourcepack-URL und SHA1 in `config.yml` eintragen.

Zielplattform: Paper 1.21.x.

## Testmodus ohne Resourcepack

Standardmässig läuft AkzuwoFormulaOne im Vanilla-Testmodus. Dabei gilt:
- kein Resourcepack erforderlich
- kein 3D-Modell erforderlich
- keine Custom Sounds erforderlich
- Spieler fahren mit normalen sichtbaren Minecraft-Booten
- Rennen, Strecken, Checkpoints, Physik, Bestzeiten und Commands funktionieren trotzdem

Die wichtigsten Default-Werte in `config.yml`:
- `features.test-mode: true`
- `features.resourcepack-enabled: false`
- `features.custom-model-enabled: false`
- `features.sounds-enabled: false`
- `features.vanilla-fallback: true`

Dadurch kann das Plugin direkt auf einen Paper-Server gelegt und mit `/f1builder ...` sowie `/f1 join ...` getestet werden, auch wenn noch kein Resourcepack, kein Blender-Modell und keine Sounds existieren.

## Resourcepack

Das Plugin kann Spielern beim Join ein Resourcepack senden und es optional erzwingen. Im Default-Testmodus ist diese Funktion deaktiviert. Das echte Pack wird hier nicht generiert. Hinweise liegen in `resourcepack/README_RESOURCEPACK.md`.

Wichtig: Das Resourcepack muss extern als ZIP gehostet und in `config.yml` verlinkt werden.

## Späterer Resourcepack-/Modell-Modus

Die spätere Architektur ist vorbereitet:
- `FeatureManager` steuert Testmodus, Resourcepack, Custom Models, Sounds und Vanilla-Fallback.
- `ResourcePackManager` sendet optional ein Pack und behandelt Ablehnung nur ausserhalb des Testmodus.
- `VehicleVisualManager` entscheidet, ob normale Boote sichtbar bleiben oder später ein Custom-Model-System wie `ItemDisplay` angebunden wird.
- `SoundManager` spielt aktuell Vanilla-Fallback-Sounds oder keine Sounds; später können Custom Sound Keys aus einem Resourcepack genutzt werden.

Wenn ein Extra aktiviert ist, aber fehlt oder noch nicht implementiert ist, soll das Plugin nicht crashen. Mit `features.vanilla-fallback: true` wird auf normale Minecraft-Boote und sichere Defaults zurückgefallen.

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
- `/f1admin features`
- `/f1admin resourcepack`
- `/f1admin visuals`
- `/f1admin sounds`
- `/f1admin analytics`
- `/f1admin analytics test`
- `/f1admin points`
- `/f1admin points reload`
- `/f1admin points test <player> <amount>`
- `/f1admin resourcepack reload`
- `/f1admin physics reload`

## Permissions

Alle Permissions sind in `plugin.yml` registriert. Hauptgruppen:
- `akzuwoformulaone.user`
- `akzuwoformulaone.mod`
- `akzuwoformulaone.admin`
- `akzuwoformulaone.builder`

Feinrechte:
- `akzuwoformulaone.user.join`
- `akzuwoformulaone.user.leave`
- `akzuwoformulaone.user.start`
- `akzuwoformulaone.user.leaderboard`
- `akzuwoformulaone.mod.cancel`
- `akzuwoformulaone.mod.kick`
- `akzuwoformulaone.mod.tp`
- `akzuwoformulaone.mod.info`
- `akzuwoformulaone.admin.reload`
- `akzuwoformulaone.admin.debug`
- `akzuwoformulaone.admin.resourcepack`
- `akzuwoformulaone.admin.physics`
- `formulaboat.admin.analytics`
- `formulaboat.admin.points`
- `formulaboat.admin.points.test`
- `akzuwoformulaone.builder.create`
- `akzuwoformulaone.builder.edit`
- `akzuwoformulaone.builder.checkpoint`
- `akzuwoformulaone.builder.save`
- `akzuwoformulaone.builder.enable`

## Track-Setup

1. Welt laden, zum Beispiel über Multiverse.
2. `/f1builder create monaco Monaco`
3. An die Lobby-Position gehen: `/f1builder setlobby monaco`
4. An die Startposition gehen: `/f1builder setspawn monaco`
5. Start- und Ziellinie setzen: `/f1builder setstart monaco`, `/f1builder setfinish monaco`
6. Checkpoints in Reihenfolge setzen: `/f1builder addcheckpoint monaco cp1 5`
7. Runden setzen: `/f1builder setlaps monaco 3`
8. Aktivieren und speichern: `/f1builder enable monaco`, `/f1builder save monaco`

Tracks werden als YAML unter `plugins/AkzuwoFormulaOne/tracks/<track-id>.yml` gespeichert.

## Multiverse

Es gibt eine `softdepend` auf `Multiverse-Core`, aber keine harte Abhängigkeit. Das Plugin erstellt keine Welten automatisch. Wenn eine Track-Welt nicht geladen ist, wird die Strecke als nicht nutzbar behandelt und beim Laden gewarnt.

## Analytics

Standardmässig sendet das Plugin beim Herunterfahren einen anonymisierten technischen Statusbericht an:

`https://analytics.akzuwo.ch/api/reports`

Dieser offizielle Endpoint ist fest im Plugin hinterlegt und kann nicht über die Config geändert werden. Der Config-Wert `analytics.official-endpoint-info` ist nur ein sichtbarer Hinweis.

Analytics können deaktiviert werden:

```yaml
analytics:
  send-analytics-data: false
```

Zusätzlich kann optional eine Kopie der Daten an einen eigenen Endpoint gesendet oder lokal als JSON-Datei gespeichert werden:

```yaml
analytics:
  send-analytics-data: true
  official-endpoint-info: "https://analytics.akzuwo.ch/api/reports"
  custom-copy-endpoint:
    enabled: true
    url: "https://example.com/my-analytics-copy"
  local-copy:
    enabled: true
    folder: "analytics"
    pretty-print: true
```

Gesendet werden nur technische und statistische Daten, zum Beispiel:
- Plugin-Version
- Minecraft-Version
- Java-Version
- aktivierte Features
- Anzahl geladener Strecken
- Anzahl gestarteter, beendeter und abgebrochener Rennen seit Serverstart
- grobe Physik-Konfiguration

Nicht gesendet werden:
- Spielernamen
- UUIDs
- IP-Adressen
- Chatnachrichten
- Koordinaten
- Passwörter oder Tokens

## Crash Reporting

AkzuwoFormulaOne kann bei plugininternen Exceptions einen anonymisierten Fehlerbericht senden. Es wird nicht die komplette Serverkonsole abgefangen oder übertragen.

Gesendet werden:
- Plugin-Version
- Minecraft-/Java-Version
- Exception-Klasse
- Exception-Message
- begrenzter Stacktrace
- technische Kontextdaten
- letzte interne AkzuwoFormulaOne-Logs

Nicht gesendet werden:
- komplette Serverkonsole
- Chatnachrichten
- Spielernamen
- UUIDs
- IP-Adressen
- Passwörter
- Tokens
- komplette Configs

Crashreporting kann deaktiviert werden:

```yaml
analytics:
  crash-reporting:
    enabled: false
```

Lokale Crashreport-Kopien werden, falls `analytics.local-copy.enabled` aktiv ist, unter `plugins/AkzuwoFormulaOne/analytics/crashes/` gespeichert.

## RankPointsAPI Integration

AkzuwoFormulaOne kann Punkte über die KSRMinecraft RankPointsAPI vergeben.

Wichtig: AkzuwoFormulaOne speichert keine Datenbank-Credentials für RankPointsAPI. Stattdessen wird RankPointsAPI als eigenes Serverplugin erwartet. RankPointsAPI bleibt verantwortlich für Datenbankverbindung, Credentials, Tabellen, Staff-Ausschluss und Speichern der Punkte.

Die Integration läuft über Bukkit `ServicesManager`:

```java
RegisteredServiceProvider<RankPointsService> provider =
    Bukkit.getServicesManager().getRegistration(RankPointsService.class);

RankPointsService rankPoints = provider.getProvider();
rankPoints.addPoints(player.getUniqueId(), points);
```

Solange der PR mit der Service-Schnittstelle noch nicht im offiziellen Repository gemerged ist, wird vorerst dieses Repository verwendet:

`https://github.com/Akzuwo/RankPointsAPI.git`

Verwendeter Branch für JitPack:

`feature/bukkit-service-api`

Konfigurierbar sind:
- Punkte für ein abgeschlossenes Rennen
- Bonus für persönliche Bestzeit
- Bonus für Streckenrekord
- Multiplikator pro Strecke
- Cooldown gegen Farming

Wenn RankPointsAPI nicht installiert oder der Service nicht verfügbar ist, läuft AkzuwoFormulaOne weiter. Es werden dann einfach keine Punkte vergeben.

## TODO

- Finales 3D-Resourcepack bauen.
- Spectator-System fertigstellen.
- Boxenstopps, Qualifying, Teams und Fahrzeugklassen ergänzen.
- SQLite/MySQL implementieren.
- Web-Leaderboard anbinden.
- Replay/Ghost-System planen.
- GUI für Streckenauswahl bauen.
