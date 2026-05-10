Smart Library Management System
===============================

This is a Java CLI application built for a university-level library management project.
The application uses object-oriented design, CSV file handling, custom exceptions,
and simple data structures to provide a basic library workflow.

Project structure:
- src/models: Person, User, LibraryItem, Book, EBook, Journal, Transaction
- src/interfaces: Loanable interface
- src/exceptions: custom exceptions for invalid user, unavailable item, overdue
- src/services: library business logic and CSV file manager
- src/utils: search and sort helpers
- src/main: Main application entry point
- data: CSV files used for library data storage

How to run:
1. Open this folder in VS Code.
2. Compile from the project root:
   javac src/main/Main.java src/services/*.java src/models/*.java src/utils/*.java src/exceptions/*.java src/interfaces/*.java
3. Run from the project root:
   java -cp src main.Main

Data files:
- data/books.csv
- data/users.csv
- data/transactions.csv

The application automatically loads data at startup and saves on exit.
