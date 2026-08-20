# UML overview

This document summarizes the current Java source structure and the key relationships in the project.

## Class diagram

```mermaid
classDiagram
    package main {
        class Main {
            +main(String[])
            -printMenu()
            -readInteger(Scanner, String)
            -readPositiveInteger(Scanner, String)
            -readNonEmptyString(Scanner, String)
            -readOptionalString(Scanner, String)
            -getDataDirectory()
            -addItem(Scanner, LibraryService)
            -addUser(Scanner, LibraryService)
            -searchItem(Scanner, LibraryService)
            -searchUser(Scanner, LibraryService)
            -issueItem(Scanner, LibraryService)
            -returnItem(Scanner, LibraryService)
            -reserveItem(Scanner, LibraryService)
        }
    }

    package services {
        class LibraryService {
            -ArrayList~LibraryItem~ items
            -ArrayList~User~ users
            -ArrayList~Transaction~ transactions
            -ArrayDeque~UndoRecord~ undoStack
            -String dataFolder
            +LibraryService(String)
            +getItems()
            +getUsers()
            +addItem(String, String, String)
            +addItemWithDetails(String, String, String, String)
            +addUser(String, String)
            +addUser(String, String, String)
            +viewItems()
            +viewUsers()
            +searchItemById(String)
            +searchUserById(String)
            +searchItemsByTitle(String)
            +sortItems()
            +issueItem(String, String, int)
            +returnItem(String, String, int)
            +reserveItem(String, String)
            +saveData()
            +showReports()
            +undoLastTransaction()
        }

        class FileManager {
            +ensureDataFiles(String)
            +loadItems(String)
            +loadUsers(String)
            +loadTransactions(String)
            +saveItems(String, ArrayList~LibraryItem~)
            +saveUsers(String, ArrayList~User~)
            +saveTransactions(String, ArrayList~Transaction~)
        }
    }

    package utils {
        class InputValidator {
            +validateNonEmpty(String, String)
            +validateOptionalEmail(String)
            +validatePositive(int, String)
            +validateDaySequence(int, int, String, String)
        }

        class SearchUtil {
            +searchByID(ArrayList~LibraryItem~, String)
            +searchByTitle(ArrayList~LibraryItem~, String)
        }

        class SortUtil {
            +bubbleSortByTitle(ArrayList~LibraryItem~)
        }
    }

    package interfaces {
        interface Borrowable {
            +issueItem(User)
            +returnItem(User)
        }

        interface FineCalculationStrategy {
            +calculateFine(int)
            +getDescription()
        }
    }

    package models {
        class Person {
            #String id
            #String name
            +Person(String, String)
            +getId()
            +getName()
        }

        class User {
            -ArrayList~LibraryItem~ borrowedItems
            -String email
            -int maxBorrowLimit
            +User(String, String)
            +User(String, String, String)
            +User(String, String, String, int)
            +borrowItem(LibraryItem)
            +returnBorrowedItem(LibraryItem)
            +getBorrowedItems()
            +canBorrow()
            +getEmail()
            +getMaxBorrowLimit()
            +toCsv()
        }

        abstract class LibraryItem {
            #String itemID
            #String title
            #String author
            #boolean available
            #Queue~String~ reservationQueue
            #int borrowCount
            +LibraryItem(String, String, String, boolean)
            +issueItem(User)
            +returnItem(User)
            +reserveItem(String)
            +restoreReservationAtFront(String)
            +peekReservation()
            +pollReservation()
            +removeReservation(String)
            +hasReservation()
            +getReservationList()
            +getItemID()
            +getTitle()
            +isAvailable()
            +getBorrowCount()
            +incrementBorrowCount()
            +decrementBorrowCount()
            +resetBorrowCount()
            +setAvailable(boolean)
            +toCsv()
        }

        class Book {
            +Book(String, String, String, boolean)
            +calculateFine(int)
            +getType()
            +displayInfo()
        }

        class EBook {
            +EBook(String, String, String, boolean)
            +calculateFine(int)
            +getType()
            +displayInfo()
        }

        class Journal {
            +Journal(String, String, String, boolean)
            +calculateFine(int)
            +getType()
            +displayInfo()
        }

        class Transaction {
            -String transactionID
            -String userID
            -String itemID
            -int issueDay
            -int dueDay
            -int returnDay
            -double fine
            +Transaction(String, String, String, int, int, int, double)
            +getTransactionID()
            +getUserID()
            +getItemID()
            +getIssueDay()
            +getDueDay()
            +getReturnDay()
            +setReturnDay(int)
            +getFine()
            +setFine(double)
            +isReturned()
            +toCsv()
        }

        class UndoRecord {
            -TransactionAction action
            -Transaction transaction
            -String itemId
            -String userId
            -boolean reservationRemoved
            +UndoRecord(TransactionAction, Transaction, String, String, boolean)
            +getAction()
            +getTransaction()
            +getItemId()
            +getUserId()
            +isReservationRemoved()
        }

        enum TransactionAction {
            ISSUE
            RETURN
            RESERVE
        }
    }

    package exceptions {
        class InvalidUserException
        class ItemNotAvailableException
        class OverdueException
    }

    package tests {
        class LibrarySystemTestSuite {
            +main(String[])
        }
    }

    Main --> LibraryService
    LibraryService --> FileManager
    LibraryService --> InputValidator
    LibraryService --> SearchUtil
    LibraryService --> SortUtil
    LibraryService --> Transaction
    LibraryService --> User
    LibraryService --> LibraryItem
    LibraryService --> UndoRecord
    LibraryService --> TransactionAction
    FileManager --> Book
    FileManager --> EBook
    FileManager --> Journal
    FileManager --> User
    FileManager --> Transaction
    LibraryItem <|-- Book
    LibraryItem <|-- EBook
    LibraryItem <|-- Journal
    User --|> Person
    LibraryItem ..|> Borrowable
    Book ..> FineCalculationStrategy
    EBook ..> FineCalculationStrategy
    Journal ..> FineCalculationStrategy
    UndoRecord --> Transaction
    UndoRecord --> TransactionAction
```

## Relationship notes

- Main depends on LibraryService for the UI workflow.
- LibraryService coordinates data access and business operations.
- FileManager loads and saves the CSV files without changing the schema.
- LibraryItem stores the runtime reservation queue and historical borrow count.
- Transaction and UndoRecord maintain the audit trail and reversal information.
- ReservationQueue is created as a Queue and is intentionally not persisted to CSV.
- The undo stack is implemented with ArrayDeque, not Stack.

## What this UML does not show

This diagram intentionally avoids obsolete or removed ideas such as:

- ItemStatus
- Loanable
- Stack-based undo model
- database or framework layers
- unsupported multi-user or GUI architecture

## Current implementation scope

This project is best understood as a single-user Java CLI library system with CSV persistence and a simple object-oriented model. The UML reflects the actual source structure rather than a broader enterprise design.
