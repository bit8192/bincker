# Repository Guidelines

## Project Structure & Module Organization
- `main.go` boots the Gin server and wires routing, middleware, and the SQLite connection.
- `internal/` holds app modules: `content` (post loading), `db` (SQLite init), `handlers`, `middleware`, `models`, `routes`, and `services`.
- `templates/` contains HTML templates; `static/` holds CSS/JS/assets served by Gin.
- `post/` stores markdown/content files loaded at startup; `blog.db` is the local SQLite file.

## Build, Test, and Development Commands
- `go run .` starts the app locally using `main.go`.
- `go build ./...` compiles all packages.
- `go test ./...` runs all tests (no test files are present yet).

## Coding Style & Naming Conventions
- Go 1.25.x, standard gofmt formatting, tabs for indentation.
- Packages are lowercase (e.g., `internal/routes`); exported identifiers use `PascalCase`.
- Prefer small, focused handlers and services; keep request/response structs near handlers.
- Code comments should be in Chinese; user-facing titles/descriptions should be in Chinese.

## Language
- code comments use Chinese
- title and description use Chinese

## Testing Guidelines
- Use Go testing (`*_test.go`), mirroring package paths under `internal/`.
- Name tests `TestXxx` and target service/handler behavior.
- Run `go test ./...` before submitting changes.

## Commit & Pull Request Guidelines
- Commit messages in history are short Chinese phrases (e.g., short 2-6 character subjects).
- PRs should include a concise summary, test command results, and screenshots for template/UI changes.
- Link related issues when applicable.

## Security & Configuration Tips
- SQLite file `blog.db` is created locally; avoid committing real data.
- Content is loaded from `post/`; keep file names stable to preserve routes.
