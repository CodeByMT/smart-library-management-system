# Architecture & Design Documentation

## Overview

This document describes the architectural decisions, design patterns, and software engineering principles applied to the Smart Library Management System.

---

## System Architecture

### Layered Architecture Pattern

The system follows a classic three-layer architecture:

```
┌─────────────────────────────────────────────────────────┐
│                 Presentation Layer (CLI)                │
│                    (Main.java)                          │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│              Service/Business Logic Layer                │
│        (LibraryService, FileManager)                    │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│              Domain Model Layer                          │
│  (LibraryItem, User, Transaction, etc.)                │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│         Persistence & Infrastructure Layer               │
│    (CSV files, FileManager utilities)                  │
└─────────────────────────────────────────────────────────┘
```

### Benefits of This Architecture

✅ **Separation of Concerns**: Each layer has distinct responsibility
✅ **Testability**: Layers can be tested independently
✅ **Maintainability**: Changes to one layer don't affect others
✅ **Reusability**: Domain models can be reused in different UIs
✅ **Scalability**: Easy to add new presentation layers (Web, Mobile)

---

## Design Patterns Implemented

### 1. Strategy Pattern - Fine Calculation

**Problem**: Different item types have different fine calculation rules

**Solution**: `FineCalculationStrategy` interface with polymorphic implementations

```
FineCalculationStrategy (interface)
  │
  ├─► Book Strategy: Rs 50/day after 2-day grace period
  ├─► EBook Strategy: Rs 0 (no fines)
  └─► Journal Strategy: Rs 80/day after 2-day grace period
```

**Benefits**:
- Easy to add new fine rules without modifying existing code (Open/Closed Principle)
- Each item type encapsulates its own fine logic
- Follows Single Responsibility Principle

**Usage**:
```java
LibraryItem item = new Book(...);
double fine = item.calculateFine(5);  // Calls Book's strategy
```

### 2. Service/Facade Pattern - LibraryService

**Problem**: Complex business logic spread across multiple operations

**Solution**: `LibraryService` acts as single entry point for all operations

```
Main.java
   │
   ├─ addItem() ──┐
   ├─ issueItem()─┼──► LibraryService (Facade)
   ├─ returnItem()┤
   └─ reserveItem┴──► Coordinates:
                    ├─ Models (LibraryItem, User, Transaction)
                    ├─ Persistence (FileManager)
                    ├─ Utils (InputValidator, SearchUtil, SortUtil)
```

**Benefits**:
- Simplifies client code (Main.java)
- Centralizes complex business logic
- Easy to understand system behavior
- Single point for transaction management

### 3. Type-Safe Enum Pattern

**Problem**: Magic strings like "ISSUE", "RETURN" are error-prone

**Solution**: Type-safe enums replacing strings

```java
// Before (error-prone)
undoStack.push(new UndoRecord("ISSUE", ...));
String action = "ISUE";  // ❌ Typo not caught at compile time

// After (type-safe)
undoStack.push(new UndoRecord(TransactionAction.ISSUE, ...));
TransactionAction action = TransactionAction.ISSUE;  // ✅ Compile-time check
```

**Enums Used**:
- `TransactionAction`: ISSUE, RETURN, RESERVE

**Benefits**:
- Compile-time type safety
- Auto-completion in IDEs
- Self-documenting code
- Easier refactoring

### 4. Data Transfer Object (DTO) Pattern

**Problem**: Undo operations need to capture complex state

**Solution**: `UndoRecord` encapsulates undo information

```java
public class UndoRecord {
    private final TransactionAction action;      // Type-safe
    private final Transaction transaction;       // Full audit trail
    private final String itemId;                 // Affected entities
    private final String userId;
    private final boolean reservationRemoved;    // State flags
}
```

**Benefits**:
- Strongly-typed instead of loose parameters
- Self-documenting intent
- Easier to extend (add new undo types)

### 5. Validation Strategy Pattern

**Problem**: Input validation scattered across multiple methods

**Solution**: Centralized `InputValidator` utility class

```java
// Usage
InputValidator.validateNonEmpty(userID, "User ID");
InputValidator.validatePositive(issueDay, "Issue day");
InputValidator.validateDaySequence(returnDay, issueDay, "Return", "Issue");
```

**Benefits**:
- DRY (Don't Repeat Yourself)
- Single source of truth for validation rules
- Consistent error messages
- Easy to enhance validation logic

---

## SOLID Principles Application

### S - Single Responsibility Principle

Each class has one reason to change:

| Class | Responsibility |
|-------|-----------------|
| `LibraryItem` | Represent a borrowable item |
| `LibraryService` | Orchestrate business operations |
| `FileManager` | Persist data to disk |
| `InputValidator` | Validate user input |
| `SearchUtil` | Search functionality |

### O - Open/Closed Principle

System is open for extension, closed for modification:

```java
// Want to add new item type? Just extend LibraryItem
public class Newspaper extends LibraryItem {
    @Override
    public double calculateFine(int daysLate) {
        return 0;  // Newspapers have no fines
    }
}
// No modification needed to existing code!
```

### L - Liskov Substitution Principle

Subtypes are substitutable for base types:

```java
LibraryItem item1 = new Book(...);
LibraryItem item2 = new EBook(...);
LibraryItem item3 = new Journal(...);

// All work identically through LibraryItem interface
issueItem(item1);  // Works for all subtypes
item1.calculateFine(5);  // Polymorphic behavior
```

### I - Interface Segregation Principle

Focused, client-specific interfaces:

```java
// Specific to borrowable behavior
public interface Borrowable {
    void issueItem(User user);
    void returnItem(User user);
}

// Specific to fine calculation strategy
public interface FineCalculationStrategy {
    double calculateFine(int daysLate);
    String getDescription();
}
```

### D - Dependency Inversion Principle

Depend on abstractions, not concrete classes:

```java
// ❌ Wrong - depends on concrete Book class
LibraryService(ArrayList<Book> books) { ... }

// ✅ Correct - depends on abstract LibraryItem
LibraryService(ArrayList<LibraryItem> items) { ... }
```

---

## Class Hierarchy Diagram

```
                    Borrowable (Interface)
                         ▲
                         │
                    LibraryItem (Abstract)
                    ├─ itemID: String
                    ├─ title: String
                    ├─ author: String
                    ├─ available: boolean
                    ├─ reservationQueue: Queue<String>
                    ├─ borrowCount: int
                    │
                    ├─ calculateFine(int): double ◄──── FineCalculationStrategy
                    ├─ issueItem(User)
                    ├─ returnItem(User)
                    └─ reserveItem(String)
                         ▲
                ┌────────┼────────┐
                │        │        │
            Book      EBook     Journal
            ├─ Fine: Rs 50/day  ├─ Fine: Rs 0  ├─ Fine: Rs 80/day
            │   after 2 days    │ (no fines)   │   after 2 days
            └─ Represents       └─ Digital     └─ Academic
               physical books      content       publications


                        Person (Abstract)
                        ├─ id: String
                        └─ name: String
                             ▲
                             │
                           User
                           ├─ borrowedItems: List<LibraryItem>
                           ├─ email: String
                           ├─ maxBorrowLimit: int
                           └─ borrowItem(LibraryItem)


                    LibraryService (Facade)
                    ├─ items: List<LibraryItem>
                    ├─ users: List<User>
                    ├─ transactions: List<Transaction>
                    ├─ undoStack: Deque<UndoRecord>
                    │
                    ├─ issueItem(String, String, int)
                    ├─ returnItem(String, String, int)
                    ├─ reserveItem(String, String)
                    └─ undoLastTransaction()
```

---

## Data Flow Diagram

### Issue Item Operation Flow

```
User Input (Main.java)
    │
    ▼
LibraryService.issueItem(userID, itemID, issueDay)
    │
    ├─► Validate inputs (InputValidator)
    ├─► Find user (linear search)
    ├─► Find item (linear search)
    ├─► Check item availability
    ├─► Check user borrow limit
    ├─► Handle reservations (remove from queue if applicable)
    │
    ├─► Execute operation
    │   ├─► item.issueItem(user)  ◄─── Updates item state
    │   ├─► user.borrowItem(item) ◄─── Updates user's borrowed list
    │
    ├─► Create Transaction record (audit trail)
    ├─► Push UndoRecord to stack (for undo functionality)
    │
    └─► Persist to disk (FileManager)
        └─► Save transactions.csv
```

### State Machine - Item Availability

```
              ┌─────────────┐
              │  AVAILABLE  │
              └──────┬──────┘
                     │
        Issue / Reserve
                     │
                     ▼
              ┌─────────────┐
         ┌────┤  BORROWED   │◄────┐
         │    └──────┬──────┘     │
         │           │           │
      Undo        Return         │
         │           │           │
         │           ▼           │
         │    ┌─────────────┐    │
         └───►│ AVAILABLE   │────┘
              └─────────────┘
```

---

## Transaction Processing & Audit Trail

### Transaction Record

```java
Transaction
├─ transactionID: String     // Unique identifier (T001, T002, ...)
├─ userID: String            // Who borrowed it
├─ itemID: String            // What was borrowed
├─ issueDay: int             // When issued
├─ dueDay: int               // When due (issueDay + 7)
├─ returnDay: int            // When returned (0 if not returned)
└─ fine: double              // Fine if overdue
```

### Undo Record

```java
UndoRecord
├─ action: TransactionAction // Type of action (ISSUE, RETURN, RESERVE)
├─ transaction: Transaction  // Full transaction details
├─ itemId: String            // Affected item
├─ userId: String            // Affected user
└─ reservationRemoved: bool  // Whether reservation was removed
```

**Benefits of Audit Trail**:
- Complete history of all operations
- Ability to undo/rollback
- Debugging and verification
- Regulatory compliance (if needed)

---

## Error Handling Strategy

### Exception Hierarchy

```
Exception
│
├─► InvalidUserException
│   └─ Thrown when: User ID not found or invalid
│   └─ Caught by: Main.java, LibraryService callers
│   └─ Example: "User ID not found: U999"
│
├─► ItemNotAvailableException
│   └─ Thrown when: Item not found or not available
│   └─ Caught by: Main.java, LibraryService callers
│   └─ Example: "Item is currently issued to another user"
│
└─► OverdueException
    └─ Thrown when: Item returned late (but operation succeeds)
    └─ Caught by: Main.java specifically
    └─ Example: "Overdue by 2 days. Fine to pay: Rs 100"
```

### Exception Handling Pattern

```java
try {
    libraryService.issueItem(userID, itemID, issueDay);
} catch (InvalidUserException | ItemNotAvailableException ex) {
    // User/item validation failed
    System.out.println("Error: " + ex.getMessage());
} catch (OverdueException ex) {
    // Special case: operation succeeded but with penalty
    System.out.println("Return complete with overdue fine. " + ex.getMessage());
}
```

---

## Performance Considerations

### Current Implementation

| Operation | Complexity | Justification |
|-----------|-----------|---------------|
| Find item by ID | O(n) | Linear search through items |
| Find user by ID | O(n) | Linear search through users |
| Search items by title | O(n) | Must check all items |
| Reserve item | O(n) | Queue operations are O(1), but checking membership is O(n) |
| Issue item | O(n) | All above operations |
| Sort items | O(n²) | Bubble sort (simple, clear implementation) |

### Future Optimization Opportunities

- Use HashMap for O(1) ID lookups
- Implement binary search for sorted data
- Use more efficient sorting (QuickSort, MergeSort)
- Implement indexing on frequently searched fields
- Consider database for large datasets

---

## Security Considerations

### Current Safeguards

✅ Input validation at service layer
✅ Type-safe enums prevent value errors
✅ Exception handling for error cases
✅ Transaction audit trail for accountability

### Potential Future Enhancements

- [ ] Role-based access control (Admin/User)
- [ ] Password protection for user accounts
- [ ] Encrypted data storage
- [ ] Input sanitization for CSV special characters
- [ ] Rate limiting on operations
- [ ] Logging of suspicious activities

---

## Extensibility Points

### Easy to Extend

✅ **New Item Types**: Extend `LibraryItem` with new subclass
✅ **New Fine Rules**: Implement new `FineCalculationStrategy`
✅ **New Operations**: Add methods to `LibraryService`
✅ **New Search Criteria**: Add methods to `SearchUtil`
✅ **New Validation Rules**: Add methods to `InputValidator`

### Example: Adding a Magazine Item Type

```java
public class Magazine extends LibraryItem {
    private int issueNumber;
    
    public Magazine(String itemID, String title, String author, int issueNumber) {
        super(itemID, title, author, true);
        this.issueNumber = issueNumber;
    }
    
    @Override
    public double calculateFine(int daysLate) {
        if (daysLate <= 7) return 0;  // 1-week grace period
        return (daysLate - 7) * 25;   // Rs 25/day after grace period
    }
    
    @Override
    public String getType() {
        return "Magazine";
    }
}

// Usage: Just add to switch statement in addItemWithDetails()
case "magazine":
    item = new Magazine(itemID, title, author, issueNumber);
    break;
```

---

## Testing Strategy

### Unit Testing Scope

```
LibraryService
├─ issueItem()
│  ├─ Valid issue with available item
│  ├─ Invalid user ID
│  ├─ Invalid item ID
│  ├─ Item already issued
│  ├─ User at borrow limit
│  └─ Reservation handling
│
├─ returnItem()
│  ├─ Valid return
│  ├─ Calculate fines correctly
│  ├─ Invalid user/item
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
