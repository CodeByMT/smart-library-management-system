# Contributing to the Smart Library Management System

## Scope

Keep contributions within the existing Java standard-library CLI, object-oriented domain model, and CSV persistence design. Avoid frameworks, databases, GUI layers, and unrelated refactors.

## Local setup

Use JDK 8 or later and work from the repository root. Source files are under `src/`; compiled classes are written to `bin/`.

## Build and test

Compile all sources:

```powershell
javac -d bin -cp src @((Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }))
```

Run the complete custom regression suite:

```powershell
java -cp bin tests.LibrarySystemTestSuite
```

Run the CLI:

```powershell
java -cp bin main.Main
```

The test runner uses only the standard library. It covers management, search/filter/sort, `LocalDate` transactions, fines and payments, reservations, undo, reports, integrity checks, validation, and CSV round trips.

## Design guidelines

- Keep business rules in `LibraryService` or the relevant model, not in `Main`.
- Preserve polymorphism among `Book`, `EBook`, and `Journal`.
- Use `LocalDate` for new transaction operations. Keep legacy numeric adapters only for compatibility.
- Preserve the meanings of `available`, historical `borrowCount`, persisted transactions, and session-only reservations.
- Keep collection boundaries encapsulated and return read-only views for inspection-only APIs.
- Reuse `HashMap`, `HashSet`, queues, deques, comparators, and existing utilities when they provide a real benefit.
- Preserve CSV compatibility where practical and do not silently discard existing fields.
- Keep public behavior and exception semantics stable unless a change explicitly requires otherwise.

## Code style

- Follow Java naming conventions and the existing package structure.
- Keep methods focused and avoid duplicated validation or formatting logic.
- Prefer clear standard-library solutions over unnecessary abstractions.
- Add concise comments only where behavior is not obvious from the code.

## Tests and changes

Add focused regression coverage for every behavior change. Before submitting a change:

1. Compile all Java sources.
2. Run `tests.LibrarySystemTestSuite` to completion.
3. Check that generated test files and application data are not unintentionally changed.
4. Update architecture or UML documentation when public responsibilities or relationships change.

Do not commit or push from an automated coding session unless explicitly requested.

## Known boundaries

The application is single-user, has no authentication or concurrency model, uses CSV rather than a database, and keeps reservations in memory for the current session only. CSV parsing supports quoted fields but not multiline records.