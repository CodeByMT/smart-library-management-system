# Smart Library Management System

## Overview

A Java command-line library system with CSV persistence. It manages books, e-books, journals, users, reservations, borrowing transactions, overdue fines, payments, undo operations, reports, and record-integrity checks.

## Features

- Add books, e-books, and journals with IDs, titles, and authors.
- Register users with optional validated email addresses and borrow limits.
- Search by item ID, title, or author; filter by type and availability.
- Sort by title, author, type, availability, or historical borrow count.
- Issue and return items using `LocalDate`; due dates are seven days after issue.
- Calculate type-specific overdue fines and record partial or complete payments.
- Reserve items with FIFO queues, inspect queues, and cancel reservations.
- View borrowing history by user or item.
- Undo the latest issue, return, or reservation action.
- Display inventory, loan, overdue, reservation, and fine statistics.
- Validate duplicate IDs, missing references, duplicate active loans, and availability mismatches.
- Save and load data through quote-aware CSV handling.

## Technologies and Java/OOP concepts

- Java 8 or later and the standard library only.
- CLI presentation, a service layer, domain models, and CSV persistence.
- Abstract classes and inheritance: `LibraryItem` is extended by `Book`, `EBook`, and `Journal`; `User` extends `Person`.
- Polymorphism: item types provide their own fine calculation and display behavior.
- Interfaces and Strategy pattern: `Borrowable` and `FineCalculationStrategy`.
- Encapsulation: service collections and borrowed-item lists are exposed as read-only views.
- Enums: `TransactionAction` and `SortUtil.SortOption`.
- Composition: users contain borrowed items, items contain reservation queues, and undo records contain transaction details.
- Collections: `ArrayList`, `HashMap`, `HashSet`, `LinkedList`, and `ArrayDeque` are used for ordered records, indexes, filters, reservations, and undo history.

## Architecture

`Main` handles menus, input, and display. `LibraryService` owns business rules and coordinates models, utilities, and `FileManager`. See [ARCHITECTURE.md](ARCHITECTURE.md) and [UML_Diagrams.md](UML_Diagrams.md).

## Project structure

```text
SmartLibraryManagementSystem/
├── data/                    # Application CSV data
├── src/
│   ├── exceptions/          # Domain exceptions
│   ├── interfaces/          # Borrowing and fine contracts
│   ├── main/                # CLI entry point
│   ├── models/              # Domain objects
│   ├── services/            # Business logic and CSV persistence
│   ├── tests/               # Custom regression suite
│   └── utils/               # Validation, search, and sorting
└── test_data/               # Test fixtures and generated round-trip files
```

## Setup and run

Prerequisite: JDK 8 or later. From the repository root in PowerShell:

```powershell
javac -d bin -cp src @((Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }))
java -cp bin main.Main
```

The application uses `data` by default. Set `LIBRARY_DATA_DIR` to use another directory:

```powershell
$env:LIBRARY_DATA_DIR = "data"
java -cp bin main.Main
```

Missing CSV files are created automatically with sample data.

## Example usage

1. Open **Catalog and item tools** and search by author or filter for available books.
2. Open **Users and borrowing history** to register or inspect a user.
3. In **Transaction workflows**, issue an item with a date such as `2026-08-28`.
4. Return it later; the service calculates the due date and any fine.
5. Record a payment for the returned transaction, then review **Reports and maintenance**.
6. Choose **Save and exit** to persist users, items, and transactions.

## CSV persistence

Items use `ItemID,Title,Author,Type,Availability`; users use `UserID,Name,Email,MaxBorrowLimit`. Transactions load the legacy seven-column layout and write optional payment fields as `TransactionID,UserID,ItemID,IssueDay,DueDay,ReturnDay,Fine,PaidFine,PaymentDate`. ISO date rows are supported. Quoted commas and quotes are handled; malformed rows are skipped with warnings. Reservations are session-only and are not persisted.

## Testing

The repository uses a standard-library custom test runner rather than JUnit:

```powershell
javac -d bin -cp src @((Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }))
java -cp bin tests.LibrarySystemTestSuite
```

The suite covers management, search/filter/sort, `LocalDate` transactions, fines and payments, reservations, undo, reports, integrity checks, validation, and CSV round trips.

## Limitations

- The CLI is single-user and has no concurrency or locking model.
- Reservation queues are in memory and reset when a new service instance starts.
- CSV is lightweight and does not support multiline fields.
- There is no authentication, user-role system, database, or GUI.
- Legacy numeric transaction files are supported through an adapter; new CLI transactions use calendar dates.

## Future improvements

- Persist reservations with a versioned schema when restart-safe queues are required.
- Add structured logging and richer report export.
- Add authentication and user roles.
- Introduce automated build tooling and a unit-test framework.
- Add configurable loan periods, fine policies, and richer item metadata.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.