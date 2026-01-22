# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/cn/bincker`: Spring Boot application code organized by feature modules (`auth`, `blog`, `clash`, `todo`, `tool`) plus shared `config` and `common`.
- `src/main/resources`: runtime config and assets, including `application.yml`, Thymeleaf templates under `templates/`, and static assets under `static/`.
- `src/test/java`: JUnit tests mirroring main packages (e.g., `.../UserServiceTest.java`).
- `my-env/` and shell scripts (`deploy.sh`, `sync-blog.sh`): local environment helpers and deployment utilities.

## Build, Test, and Development Commands
- `./gradlew bootRun` — run the Spring Boot app locally.
- `./gradlew test` — run unit/integration tests with JUnit Platform.
- `./gradlew build` — compile and package the app (also runs tests).
- `./gradlew clean` — remove build outputs for a fresh build.

## Coding Style & Naming Conventions
- Java 17 with Spring Boot 3.x; follow standard Java conventions.
- Indentation: 4 spaces; braces on the same line.
- Packages are lowercase (e.g., `cn.bincker.modules.blog`); classes use `PascalCase`; methods/fields use `camelCase`.
- Lombok is enabled; keep annotations minimal and readable.

## Language
- code comments use Chinese
- title and description use Chinese

## Testing Guidelines
- Frameworks: JUnit 5 + Spring Boot Test.
- Test classes live in `src/test/java` and end with `Test` (e.g., `DatabaseSchemaSynchronizerTest`).
- Prefer focused service tests; for web changes, add controller tests where practical.

## Commit & Pull Request Guidelines
- Commit messages are short and descriptive; existing history includes simple Chinese phrases (e.g., `模型环境变量错误`).
- If contributing via PR, include a brief summary, test results (`./gradlew test`), and screenshots for template or UI changes.
- Link related issues if applicable.

## Configuration & Security Notes
- Application config is in `src/main/resources/application.yml`; avoid committing secrets.
- SQLite is used by default (`org.xerial:sqlite-jdbc`); document any required local data files.
