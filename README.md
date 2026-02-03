# Moltbook Java Client

Ein inoffizieller, typisierter Java-Client für die öffentliche **Moltbook REST API**
sowie ein kleiner CLI-Demo-Client (**ShyClient**) zum Erkunden des realen Dienstes.

Der Client orientiert sich am offiziellen Moltbook-Frontend
(`moltbook-frontend/src/lib/api.ts`) und bildet dessen REST-Mapping möglichst
1:1 nach.

---

## Status

**Funktionsfähig (Stand jetzt):**

- Registrierung eines Agents
- Persistieren und Laden des API-Keys
- Lesen des eigenen Profils (`/agents/me`)
- Auflisten von Submolts (paginiert)
- Lesen einzelner Submolts
- Lesen von Posts (`/posts`)
- Lesen von Submolt-Feeds (`/submolts/{name}/feed`)
- Lesen des globalen Feeds (`/feed`)
- CLI-Demo (`ShyClient`) bis inkl. Feed-Aufrufe

**Bekanntes Verhalten:**

- `401 Unauthorized` bei Feed-Endpoints ist aktuell **erwartet**
  (API-Key / Auth-Flow auf Server-Seite noch nicht stabil)

---

## Projektstruktur

```
moltbook-java-client/
├── src/main/java/
│   ├── de/ralfrosenkranz/moltbook/client/
│   │   ├── api/
│   │   ├── http/
│   │   ├── model/
│   │   └── response/
│   └── de/ralfrosenkranz/moltbook/shy/
│       └── ShyClient.java
└── README.md
```

---

## ShyClient (CLI-Demo)

`ShyClient` ist ein bewusst einfacher CLI-Client, der:

1. einen Agent registriert (falls kein API-Key vorhanden)
2. den API-Key lokal speichert
3. Submolts auflistet
4. Beispiel-Feeds abruft (global + Submolts)

### Beispiel

```bash
java -jar shyclient.jar overview --submolts=20 --posts=25 --sample=3 --sort=new
```

---

## Authentifizierung

- Auth erfolgt via `Authorization: Bearer <API_KEY>`
- Basis-URL:
  ```
  https://www.moltbook.com/api/v1
  ```

---

## Lizenz / Haftung

- Inoffizieller Client
- Nutzung auf eigene Verantwortung
