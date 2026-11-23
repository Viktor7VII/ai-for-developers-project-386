# AGENTS.md

## Project overview

This repository contains a meeting scheduling application inspired by Cal.com.

The project is currently at the scaffolding stage. Do not assume that business features, authentication, database integration, or booking workflows are implemented.

## Technology stack

- Backend: Java 21, Spring Boot 4, Maven.
- Frontend: TypeScript, React, Vite.
- UI: Mantine.
- API contract: TypeSpec.
- API specification: OpenAPI.
- Frontend API client: generated TypeScript Fetch SDK.
- Backend tests: JUnit 5.
- Frontend tests: Vitest, Testing Library.
- Frontend linting: Oxlint.
- Tool version management: mise.
- Git hooks: hk.
- CI: GitHub Actions.

## Repository structure

- `backend/` — Spring Boot application, Maven build and backend tests.
- `frontend/` — React application, Vite configuration, frontend tests and generated API client.
- `contracts/` — TypeSpec API contract, generated OpenAPI specification and SDK generation configuration.
- `frontend/src/shared/api/generated/` — generated TypeScript API client.
- `scripts/check.mjs` — unified project check script.
- `.github/workflows/project-check.yml` — project CI workflow.
- `.github/workflows/hexlet-check.yml` — Hexlet-managed workflow; do not modify.
- `mise.toml` — tool versions and project tasks.
- `mise.lock` — locked tool versions.
- `hk.pkl` and `.hk/pkl/` — hk configuration and local Pkl dependencies.
- `.claude/skills/` and `skills-lock.json` — agent skills installed from `mattpocock/skills`; restore them with `npx skills experimental_install`.
- `docs/agents/` — skills configuration: issue tracker (GitHub Issues), triage labels, domain docs layout.
- `.scratch/` — local copy of the specification and decision tickets, implementation slice tickets.

## Development setup

Install the pinned project tools:

```bash
mise install
```

Install project dependencies:

```bash
cd backend
./mvnw dependency:resolve
cd ../contracts
npm install
cd ../frontend
npm install
```

On Windows PowerShell, use `.\mvnw.cmd` instead of `./mvnw` and `npm.cmd` instead of `npm`.

## Running the application

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm run dev
```

The backend exposes the Spring Boot Actuator health endpoint:

`GET /actuator/health`

## Tests and quality checks

Run all project checks from the repository root:

```bash
mise run check
```

This command performs:

1. TypeSpec compilation.
2. TypeScript SDK generation.
3. Backend tests.
4. Frontend tests.
5. Frontend Oxlint.
6. Frontend production build.

Run backend tests separately:

```bash
cd backend
./mvnw test
```

Run frontend tests:

```bash
cd frontend
npm test
```

Run the browser scenario (Playwright, `e2e/`). It starts the backend on port 18080 with an in-memory database and the Vite dev server on port 15173 itself; install Chromium once with `npx playwright install chromium` in `e2e/`:

```bash
mise run e2e
```

Run frontend lint:

```bash
cd frontend
npm run lint
```

## API contract and generated code

- Treat `contracts/main.tsp` as the source of truth for the API contract.
- OpenAPI output is generated from the TypeSpec contract.
- The TypeScript SDK is generated from the OpenAPI specification.
- Never edit files in `frontend/src/shared/api/generated/` manually.
- The backend API interfaces and DTOs are generated from the same OpenAPI by the Maven plugin into `backend/target/generated-sources/openapi` (packages `com.hexlet.backend.api` and `com.hexlet.backend.api.model`, not committed). Controllers implement the generated interfaces; business logic stays in hand-written code (ADR-0003).
- When changing the API contract, regenerate OpenAPI, the SDK and the server interfaces, then commit the regenerated `contracts/openapi/` and SDK together with the contract change:

```bash
mise run generate
```

- In CI, `mise run check` fails if the committed generated files differ from a fresh generation (modified, deleted or new files).

- Keep the backend API behavior consistent with the contract.

## Git conventions

Use Conventional Commits.

Every implementation commit references its ticket with a `Refs` line in the commit body: the GitHub issue (`Refs: #<number>`) or, for implementation slices kept only locally, the ticket file:

```text
feat: add owner notes to bookings

Refs: #18
Refs: .scratch/core-spec/slices/07-owner-notes.md
```

Allowed commit types:

- `build`
- `chore`
- `ci`
- `docs`
- `feat`
- `fix`
- `perf`
- `refactor`
- `revert`
- `style`
- `test`

Examples:

```text
feat: add meeting types endpoint
fix: handle unavailable time slots
test: add health endpoint test
docs: update project setup instructions
```

The hk `commit-msg` hook validates commit messages.

Releases are managed by release-please (`.github/workflows/release-please.yml`, `release-please-config.json`): commit types drive the version and `CHANGELOG.md`; the release happens when the release PR is merged. Do not edit `version.txt`, `CHANGELOG.md` or `.release-please-manifest.json` manually.

After cloning the repository, install the hook with:

```bash
mise exec -- hk install
```

## Agent skills

### Issue tracker

GitHub Issues is the primary tracker: decision map #3, specification #19, decision tickets #4–#18 (sub-issues of #3 with native blocked-by dependencies, answers as `## Answer` comments). `.scratch/core-spec/` is the local copy and working material; implementation slice tickets live only in `.scratch/core-spec/slices/`. Never close issues automatically. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`), applied as GitHub labels (local slice files record them as a `Status:` line). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `GLOSSARY.md` and ADRs in `docs/adr/` at the repo root. See `docs/agents/domain.md`.

## MCP

- `.mcp.json` connects the Render MCP server (`https://mcp.render.com/mcp`) used to create and inspect the Render deployment (service `call-calendar`, see README).
- Authorization is a Render API key read from the `RENDER_API_KEY` environment variable (`Authorization: Bearer ${RENDER_API_KEY}`); the variable is set in the user's system, and Claude Code must be restarted after setting it.
- Secrets are never stored in the repository: no API keys, tokens or passwords in `.mcp.json`, code, docs or commit messages. Reference them only through environment variables.

## Working rules

- Read existing code and configuration before changing them.
- Prefer small, focused changes.
- Do not introduce new dependencies without a clear reason.
- Do not modify generated SDK files manually.
- Do not modify `.github/workflows/hexlet-check.yml`.
- Keep tool versions managed by mise.
- Run `mise run check` after making changes.
- Do not claim that a feature is implemented unless it exists in the code.
- Do not commit or push changes unless explicitly requested.
