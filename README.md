# Smart Library Management System

## Overview

The Smart Library Management System is a CLI-based Java application developed using Object-Oriented Programming principles.  
It simulates core library operations such as managing books, users, issuing/returning items, reservations, and tracking fines.

The system is designed to demonstrate proper software design using OOP concepts, modular structure, and basic data structures.

---

## Features

- Add, update, and manage books
- Register and manage users
- Issue and return books
- Reservation queue system for unavailable books
- Fine calculation for late returns
- Search and sorting functionality
- File-based data persistence (CSV handling)
- Transaction tracking (basic undo support if implemented)

---

## Object-Oriented Programming Concepts Used

- Encapsulation (data hiding in classes)
- Inheritance (hierarchical structure of entities)
- Polymorphism (method overriding and dynamic behavior)
- Abstraction (separation of interface and implementation)
- Interfaces for defining system behavior contracts

---

## Data Structures Used

- ArrayList (for dynamic storage of books and users)
- Queue (for reservation handling)
- Stack (for tracking recent transactions, if implemented)

---

## Technologies Used

- Java (JDK 25)
- Object-Oriented Programming (OOP)
- File Handling (CSV-based persistence)
- Command Line Interface (CLI)
- VS Code / IntelliJ IDEA (development environment)

---

## Project Structure
├── src/
│   ├── models/
│   │   ├── Person.java
│   │   ├── User.java
│   │   ├── LibraryItem.java
│   │   ├── Book.java
│   │   ├── EBook.java
│   │   ├── Journal.java
│   │   └── Transaction.java
│   │
│   ├── interfaces/
│   │   └── Loanable.java
│   │
│   ├── exceptions/
│   │   ├── ItemNotAvailableException.java
│   │   ├── InvalidUserException.java
│   │   └── OverdueException.java
│   │
│   ├── services/
│   │   ├── LibraryService.java
│   │   └── FileManager.java
│   │
│   ├── utils/
│   │   ├── SearchUtil.java
│   │   └── SortUtil.java
│   │
│   └── main/
│       
│
├── data/
│   ├── books.csv
│   ├── users.csv
│   └── transactions.csv
│
└── README.txt