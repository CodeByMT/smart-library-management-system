# UML diagrams

The diagrams describe the current Java source structure and public service/model APIs.

## Class diagram

```mermaid
classDiagram
    class Main
    class LibraryService {
        -ArrayList~LibraryItem~ items
        -ArrayList~User~ users
        -ArrayList~Transaction~ transactions
        -Map~String,LibraryItem~ itemsById
        -Map~String,User~ usersById
        -ArrayDeque~UndoRecord~ undoStack
        -String dataFolder
        +LibraryService(String)
        +getItems() List~LibraryItem~
        +getUsers() List~User~
        +getTransactions() List~Transaction~
        +getBorrowingHistoryForUser(String) List~Transaction~
        +getBorrowingHistoryForItem(String) List~Transaction~
        +addItem(String,String,String)
        +addItemWithDetails(String,String,String,String)
        +addUser(String,String)
        +addUser(String,String,String)
        +viewItems()
        +viewUsers()
        +searchItemById(String)
        +searchUserById(String)
        +searchItemsByTitle(String)
        +searchItems(String) List~LibraryItem~
        +filterItems(String,Boolean) List~LibraryItem~
        +filterItems(Set~String~,Boolean) List~LibraryItem~
        +sortItems()
        +sortItemsBy(SortOption)
        +issueItem(String,String,int)
        +issueItem(String,String,LocalDate)
        +returnItem(String,String,int)
        +returnItem(String,String,LocalDate)
        +reserveItem(String,String)
        +cancelReservation(String,String) boolean
        +getReservationQueue(String) List~String~
        +recordFinePayment(String,double,LocalDate)
        +saveData()
        +undoLastTransaction() boolean
        +validateDataIntegrity() List~String~
        +showReports()
        +showReports(LocalDate)
    }
    class FileManager
    class InputValidator
    class SearchUtil
    class SortUtil
    class SortOption {
        <<enumeration>>
        TITLE
        AUTHOR
        TYPE
        AVAILABILITY
        BORROW_COUNT
    }
    class Person {
        #String id
        #String name
    }
    class User {
        -ArrayList~LibraryItem~ borrowedItems
        -String email
        -int maxBorrowLimit
    }
    class LibraryItem {
        <<abstract>>
        #String itemID
        #String title
        #String author
        #boolean available
        #LinkedList~String~ reservationQueue
        #int borrowCount
    }
    class Book
    class EBook
    class Journal
    class Transaction {
        -String transactionID
        -String userID
        -String itemID
        -LocalDate issueDate
        -LocalDate dueDate
        -LocalDate returnDate
        -double fine
        -double paidFine
        -LocalDate paymentDate
        -boolean legacyNumericDates
    }
    class UndoRecord
    class TransactionAction {
        <<enumeration>>
        ISSUE
        RETURN
        RESERVE
    }
    class Borrowable {
        <<interface>>
    }
    class FineCalculationStrategy {
        <<interface>>
    }

    Main --> LibraryService
    LibraryService --> FileManager
    LibraryService --> InputValidator
    LibraryService --> SearchUtil
    LibraryService --> SortUtil
    LibraryService --> LibraryItem
    LibraryService --> User
    LibraryService --> Transaction
    LibraryService --> UndoRecord
    LibraryService --> TransactionAction
    LibraryItem <|-- Book
    LibraryItem <|-- EBook
    LibraryItem <|-- Journal
    User --|> Person
    LibraryItem ..|> Borrowable
    Book ..> FineCalculationStrategy
    EBook ..> FineCalculationStrategy
    Journal ..> FineCalculationStrategy
    User o-- LibraryItem
    LibraryItem o-- "0..*" String : reservations
    UndoRecord --> Transaction
    UndoRecord --> TransactionAction
    SortUtil --> SortOption
```

## Issue and return flow

```mermaid
sequenceDiagram
    actor Operator
    participant Main
    participant Service as LibraryService
    participant User
    participant Item as LibraryItem
    participant CSV as FileManager

    Operator->>Main: Select transaction workflow
    Main->>Service: issueItem(...) or returnItem(...)
    Service->>User: Validate and update borrowed items
    Service->>Item: Update availability and borrow count
    Service->>Service: Create or update Transaction
    Service-->>Main: Success or domain exception
    Operator->>Main: Save data
    Main->>Service: saveData()
    Service->>CSV: Save items, users, and transactions
```

## State notes

Transactions are persisted. Reservations are session-only. Legacy numeric transaction rows are converted to dates internally, while ISO date rows are supported directly. `HashMap` indexes items and users by normalized ID, and `ArrayDeque` stores undo records.