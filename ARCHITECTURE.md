# Architecture and design notes

This project is a simple Java CLI application with a small layered design. It is not a full enterprise architecture and does not use frameworks, a database, or a GUI.

## Current structure

The implementation is organized as follows:

- Main.java acts as the presentation layer and CLI entry point.
- LibraryService acts as the central service layer and holds the business logic for users, items, transactions, reports, and undo.
- FileManager handles CSV file creation, loading, and saving.
- Model classes represent domain objects and hold item/user state.
- Interfaces define behavior contracts such as Borrowable and FineCalculationStrategy.
- Exceptions represent validation and business errors.
- Utility classes handle input validation, searching, and sorting.
- LibrarySystemTestSuite provides the project’s full regression suite.

## Layered design

The project uses a basic three-part structure:

```text
CLI / presentation
    Main.java
        |
        v
Business logic
    LibraryService
        |
        v
Models and persistence
    LibraryItem, User, Transaction, FileManager
```

This is a lightweight service-oriented design suited to a course project, not a formal Clean Architecture setup.

## Main responsibilities

### Main.java

Main.java is the interactive menu-driven interface. It reads user input, calls LibraryService methods, and handles CLI flow.

### LibraryService

LibraryService is the main coordination point for:

- adding users and items
- searching and sorting
- issuing and returning items
- managing reservations
- creating and replaying transaction records
- undoing transactions
- generating basic reports
- writing data to CSV

### FileManager

FileManager is responsible for:

- ensuring the required CSV files exist
- loading data from data/books.csv, data/users.csv, and data/transactions.csv
- saving updated data back to disk
- parsing quoted CSV fields in the supported format
- skipping malformed rows safely instead of crashing

### Models

The model layer contains the domain objects:

- LibraryItem is the abstract base item type
- Book, EBook, and Journal extend LibraryItem
- User represents a library member
- Transaction stores issue/return audit information
- UndoRecord captures the last action for rollback
- TransactionAction is the enum for undo actions
- Person is the base class for User

### Interfaces

The interfaces in the project are intentionally small and specific:

- Borrowable: common borrowing behavior
- FineCalculationStrategy: fine calculation behavior for each item type

### Exceptions

The project uses custom exceptions for invalid or unavailable operations:

- InvalidUserException
- ItemNotAvailableException
- OverdueException

### Utilities

The utility layer contains:

- InputValidator for validation rules
- SearchUtil for ID/title search
- SortUtil for simple bubble sorting

### Tests

The project includes a custom console-based suite in LibrarySystemTestSuite. It verifies the functional behavior of the library without using JUnit or a framework.

## State semantics in the current implementation

Several state rules are important and reflected in the source:

- available indicates the current readiness of the item for borrowing
- borrowCount stores the total historical issue count for an item
- reservationQueue is an in-memory FIFO queue for the current session
- transactions store the persisted history of issue and return activity

Reservation state is session-only by design. It is not written to CSV and is cleared when a fresh LibraryService instance loads data.

## Design patterns actually supported by the code

The following patterns are genuinely present in the implementation:

### Strategy pattern

Fine calculation is delegated through the FineCalculationStrategy interface. Each item subclass implements the calculation differently.

### Enum-based action tracking

TransactionAction is used to represent issue, return, and reserve actions in a type-safe way.

### Simple service layer

LibraryService centralizes business logic and keeps most operations out of the CLI and model classes.

### Queue-based reservation handling

LibraryItem keeps a Queue<String> reservationQueue for runtime reservation ordering.

### Undo tracking

UndoRecord and a Deque/ArrayDeque-based undo stack are used to reverse recent transaction actions.

## What is not claimed here

This document does not describe the project as fully enterprise-level or as a formal Clean Architecture implementation. The project is a Java student OOP library system with command-line interaction and CSV persistence.

## Example flow

The basic flow is:

1. Main collects input.
2. LibraryService validates and processes the operation.
3. Model objects are updated in memory.
4. Transaction history and item state are kept consistent.
5. FileManager saves CSV data when needed.
6. Undo records can reverse the last action.

## Notes

- The project keeps logic simple and local to the course scope.
- The user and item state is intentionally straightforward and not built around a database or framework.
- CSV parsing supports quoted values but not multiline CSV records.
│  └─ Return before issue date
│
├─ reserveItem()
│  ├─ Add to reservation queue
│  ├─ Duplicate reservation handling
│  └─ Invalid user/item
│
└─ undoLastTransaction()
   ├─ Undo issue operation
   ├─ Undo return operation
   ├─ Undo reservation
   └─ No undo available
```

---

## Conclusion

This architecture balances:

✅ **Simplicity**: Easy for students to understand
✅ **Professionalism**: Uses industry-standard patterns
✅ **Maintainability**: Clear structure, well-documented
✅ **Extensibility**: Easy to add new features
✅ **Testability**: Components can be tested independently

The design demonstrates understanding of OOP principles, design patterns, and software engineering best practices suitable for both educational and professional contexts.
