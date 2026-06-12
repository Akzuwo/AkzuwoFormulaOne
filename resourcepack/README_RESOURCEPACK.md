# AkzuwoFormulaOne Resourcepack

Dieses Projekt baut noch kein echtes Resourcepack. Der Ordner ist als Vorbereitung gedacht.

Geplant:
- Das verwendete Minecraft-Boot bleibt codeseitig ein Boot.
- Optisch soll es später über ein 3D-Modell als Formel-1-Auto dargestellt werden.
- Dafür kann ein Model Override oder Custom-Model-Data-Workflow vorbereitet werden, je nachdem wie das finale Item-/Entity-Setup umgesetzt wird.
- Das Resourcepack muss extern als ZIP gehostet werden.
- Die URL und der SHA1-Hash werden in `plugins/AkzuwoFormulaOne/config.yml` unter `resourcepack` eingetragen.

Aktueller Stand:
- Standardmässig ist der Vanilla-Testmodus aktiv.
- `features.resourcepack-enabled` ist standardmässig `false`.
- `resourcepack.url` und `resourcepack.sha1` sind leer.
- Resourcepack-Versand und Ablehnungs-Kick sind im Plugin vorbereitet, aber optional.
- Im Testmodus darf kein Spieler wegen fehlendem oder abgelehntem Resourcepack gekickt werden.
- Kein finales Modell, keine finalen Texturen.

Aktivierung später:
1. Resourcepack extern als ZIP hosten.
2. `features.test-mode` auf `false` setzen.
3. `features.resourcepack-enabled` auf `true` setzen.
4. `resourcepack.url` und optional `resourcepack.sha1` eintragen.
5. Optional `features.force-resourcepack` oder `resourcepack.kick-on-decline` aktivieren.
