package services;

import models.Book;
import models.EBook;
import models.Journal;
import models.LibraryItem;
import models.Transaction;
import models.User;
import utils.InputValidator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class responsible for loading and saving CSV data files.
 *
 * <p>
 * This class ensures that the required data folder and files exist,
 * loads items, users, and transactions, and writes updates back to disk.</p>
 */
public class FileManager {
    public static void ensureDataFiles(String dataFolder) {
        try {
            Path folderPath = Paths.get(dataFolder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            createFileWithSampleData(folderPath.resolve("books.csv"), "ItemID,Title,Author,Type,Availability", new String[]{
                    "B001,Java Fundamentals,James Gosling,Book,true",
                    "B002,Data Structures in Java,Robert Lafore,Book,true",
                    "E001,Networks Today,Andrew Tanenbaum,EBook,true",
                    "J001,Science Journal,Editorial Board,Journal,true"
            });

            createFileWithSampleData(folderPath.resolve("users.csv"), "UserID,Name,Email,MaxBorrowLimit", new String[]{
                    "U001,Aman Kumar,aman@example.com,3",
                    "U002,Nisha Patel,nisha@example.com,3"
            });

            createFileWithSampleData(folderPath.resolve("transactions.csv"), "TransactionID,UserID,ItemID,IssueDay,DueDay,ReturnDay,Fine", new String[]{});
        } catch (IOException e) {
            System.out.println("Could not initialize data files: " + e.getMessage());
        }
    }

    private static void createFileWithSampleData(Path filePath, String header, String[] lines) throws IOException {
        if (Files.notExists(filePath)) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile()))) {
                writer.println(header);
                for (String line : lines) {
                    writer.println(line);
                }
            }
        }
    }

    private static String[] parseCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }

        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else if (ch == '\r') {
                continue;
            } else {
                current.append(ch);
            }
        }

        if (inQuotes) {
            return new String[0];
        }

        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    private static String escapeCsvField(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private static String normalizeCsvField(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    public static ArrayList<LibraryItem> loadItems(String filePath) {
        ArrayList<LibraryItem> items = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return items;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.toLowerCase().contains("itemid")) {
                    continue;
                }

                String[] data = parseCsvLine(line);
                if (data.length < 5) {
                    System.out.println("Warning: skipping malformed item row: " + line);
                    continue;
                }

                try {
                    String id = normalizeCsvField(data[0]);
                    String title = normalizeCsvField(data[1]);
                    String author = normalizeCsvField(data[2]);
                    String type = normalizeCsvField(data[3]);
                    boolean available = Boolean.parseBoolean(normalizeCsvField(data[4]));
                    switch (type.toLowerCase()) {
                        case "book":
                            items.add(new Book(id, title, author, available));
                            break;
                        case "ebook":
                            items.add(new EBook(id, title, author, available));
                            break;
                        case "journal":
                            items.add(new Journal(id, title, author, available));
                            break;
                        default:
                            System.out.println("Warning: unsupported item type in file: " + type);
                            break;
                    }
                } catch (Exception ex) {
                    System.out.println("Warning: skipping malformed item row: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading items file: " + e.getMessage());
        }
        return items;
    }

    public static ArrayList<User> loadUsers(String filePath) {
        ArrayList<User> users = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return users;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.toLowerCase().contains("userid")) {
                    continue;
                }

                String[] data = parseCsvLine(line);
                if (data.length >= 4) {
                    try {
                        String id = normalizeCsvField(data[0]);
                        String name = normalizeCsvField(data[1]);
                        String email = normalizeCsvField(data[2]);
                        int maxBorrow = Integer.parseInt(normalizeCsvField(data[3]));

                        if (!email.isEmpty()) {
                            InputValidator.validateOptionalEmail(email);
                        }

                        users.add(new User(id, name, email, maxBorrow));
                    } catch (NumberFormatException ex) {
                        System.out.println("Warning: skipping malformed user row: " + line);
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Warning: skipping invalid email user row: " + line);
                    }
                } else if (data.length == 2) {
                    users.add(new User(normalizeCsvField(data[0]), normalizeCsvField(data[1])));
                } else {
                    System.out.println("Warning: skipping malformed user row: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users file: " + e.getMessage());
        }
        return users;
    }

    public static ArrayList<Transaction> loadTransactions(String filePath) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return transactions;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.toLowerCase().contains("transactionid")) {
                    continue;
                }

                String[] data = parseCsvLine(line);
                if (data.length < 7) {
                    System.out.println("Warning: skipping malformed transaction row: " + line);
                    continue;
                }

                try {
                    String transactionID = normalizeCsvField(data[0]);
                    String userID = normalizeCsvField(data[1]);
                    String itemID = normalizeCsvField(data[2]);
                    int issueDay = Integer.parseInt(normalizeCsvField(data[3]));
                    int dueDay = Integer.parseInt(normalizeCsvField(data[4]));
                    int returnDay = Integer.parseInt(normalizeCsvField(data[5]));
                    double fine = Double.parseDouble(normalizeCsvField(data[6]));
                    transactions.add(new Transaction(transactionID, userID, itemID, issueDay, dueDay, returnDay, fine));
                } catch (NumberFormatException ex) {
                    System.out.println("Warning: skipping malformed transaction row: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading transactions file: " + e.getMessage());
        }
        return transactions;
    }

    public static void saveItems(String filePath, ArrayList<LibraryItem> items) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("ItemID,Title,Author,Type,Availability");
            for (LibraryItem item : items) {
                writer.println(item.toCsv());
            }
        } catch (IOException e) {
            System.out.println("Error saving items file: " + e.getMessage());
        }
    }

    public static void saveUsers(String filePath, ArrayList<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("UserID,Name,Email,MaxBorrowLimit");
            for (User user : users) {
                writer.println(user.toCsv());
            }
        } catch (IOException e) {
            System.out.println("Error saving users file: " + e.getMessage());
        }
    }

    public static void saveTransactions(String filePath, ArrayList<Transaction> transactions) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("TransactionID,UserID,ItemID,IssueDay,DueDay,ReturnDay,Fine");
            for (Transaction transaction : transactions) {
                writer.println(transaction.toCsv());
            }
        } catch (IOException e) {
            System.out.println("Error saving transactions file: " + e.getMessage());
        }
    }
}