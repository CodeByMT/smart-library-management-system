# Contributing to the Smart Library Management System

This project is a small Java CLI and CSV-based library system for a student OOP assignment. Contributions should stay within that scope and should not add frameworks, databases, or unrelated technologies.

## Project scope

The repository is intentionally simple:

- Java standard library only
- command-line interface
- CSV persistence
- object-oriented domain model
- custom test suite instead of JUnit

Keep changes consistent with this structure.

## Local setup

1. Clone the repository.
2. Open the project in a Java-capable editor or terminal.
3. Use the existing source layout under src/.

## Build instructions

Compile the project with the current working command:

```powershell
javac -d bin -cp src @((Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }))
```

## Test instructions

Run the project’s existing regression suite:

```powershell
java -cp bin tests.LibrarySystemTestSuite
```

Run the CLI application:

```powershell
java -cp bin main.Main
```

## Contribution guidelines

- Keep changes focused and easy to review.
- Do not change the project architecture or add new technologies.
- Do not modify the CSV schema unless the current requirement explicitly requires it.
- Preserve the existing semantics:
  - available = current item availability
  - borrowCount = historical issue count
  - reservations = session-only in memory
- Preserve validation behavior for user IDs, optional email input, and positive day values.
- Avoid broad refactors unrelated to the current bug or requirement.
- Prefer simple Java OOP patterns already used in the project.

## Code style

- Use Java naming conventions.
- Keep methods readable and focused.
- Preserve the current package structure.
- Follow the style already used in the project rather than introducing new conventions.

## What to avoid

- No SQLite, databases, or JDBC.
- No framework additions such as Spring, Hibernate, or GUI libraries.
- No external libraries for persistence or UI.
- No broader redesign of the application.
- No changes to the existing CSV schema or required file layout.

## Pull requests and review

When preparing a change:

- confirm the project still compiles
- run the full LibrarySystemTestSuite
- keep the diff limited to the relevant files
- explain the reason for the change and the behavior it preserves

## Questions

If a change is unclear, review the current source and test suite before proposing a broader redesign. This project is intentionally small and course-oriented, so the safest contributions are narrow and behavior-preserving.
