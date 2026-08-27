# Architecture and design notes

## Overview

The project is a small, single-user Java CLI application with practical presentation, service, domain, and persistence responsibilities. It uses only the Java standard library and CSV files.

## Layers

```text
CLI and presentation
    Main
      |
      v
Business coordination
    LibraryService
      |\
      | \---- SearchUtil, SortUtil, InputValidator
      v
Domain models and persistence
    LibraryItem, User, Transaction, UndoRecord
    FileManager
```

### Main

`Main` owns menu navigation, prompts, input parsing, and formatted output. It delegates business operations to `LibraryService`; it does not calculate fines, alter service collections, or implement borrowing rules.

### LibraryService

`LibraryService` owns application state and coordinates registration, indexed lookup, search, filtering, sorting, issue/return workflows, due dates, fines, session reservations, history queries, payments, undo, reports, integrity diagnostics, and CSV persistence.

It maintains `HashMap` indexes for case-insensitive item and user IDs and exposes read-only views of items, users, and transactions. Active borrowed-item lists are rebuilt from unreturned transactions at startup.

### FileManager

`FileManager` creates missing CSV files, loads model objects, and saves lists. Its parser handles quoted commas and quotes and skips malformed rows with warnings. It reads both legacy numeric transaction dates and ISO dates. New transaction records include optional paid-fine and payment-date fields while older seven-column files remain loadable.

### Models

- `Person` is the base class for people; `User` adds email, borrowing limits, and borrowed-item composition.
- `LibraryItem` is the abstract base for `Book`, `EBook`, and `Journal`. It owns availability, historical borrow count, and the runtime reservation queue.
- `Transaction` stores immutable identity and dates plus return, fine, and payment state.
- `UndoRecord` captures context for reversing issue, return, and reservation actions.
- `TransactionAction` identifies the supported undo action types.

## OOP and collection choices

`Book`, `EBook`, and `Journal` inherit common item state and override fine and display behavior. `LibraryItem` implements `Borrowable`; concrete items use `FineCalculationStrategy` implementations for their fine policies.

- `ArrayList` preserves ordered item, user, and transaction records.
- `HashMap` provides efficient normalized ID lookup.
- `HashSet` supports multi-type filtering without duplicate criteria.
- `LinkedList` provides FIFO reservations and front restoration during undo.
- `ArrayDeque` provides last-in-first-out undo history.
- `Comparator` supports alternate item sort orders; bubble sort remains available for the default title sort.

## State semantics

- `available` means current borrowing availability.
- `borrowCount` means historical successful issue count, not active loans.
- Transactions store issue, due, return, assessed fine, paid fine, and payment date.
- Reservations are session-only and are not written to CSV.

## Integrity checks

`validateDataIntegrity()` reports duplicate item or user IDs, transactions referencing missing users or items, multiple active transactions for one item, and mismatches between active transactions and item availability. It does not mutate data.

## Error handling and scope

Validation and business failures use `IllegalArgumentException`, `InvalidUserException`, `ItemNotAvailableException`, and `OverdueException`. An overdue return completes and communicates the fine through `OverdueException`.

The project intentionally has no database, framework, GUI, authentication, concurrency model, or restart-persistent reservation store.