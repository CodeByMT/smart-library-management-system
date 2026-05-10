package services;

import models.Book;
import models.EBook;
import models.Journal;
import models.LibraryItem;
import models.Transaction;
import models.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileManager {
    public static void ensureDataFiles(String dataFolder) {
        try {
            Path folderPath = Paths.get(dataFolder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            createFileWithSampleData(folderPath.resolve("books.csv"), "ItemID,Title,Type,Availability", new String[]{
                    "B001,Java Fundamentals,Book,true",
                    "B002,Data Structures in Java,Book,true",
                    "E001,Networks Today,EBook,true",
                    "J001,Science Journal,Journal,true"
            });

            createFileWithSampleData(folderPath.resolve("users.csv"), "UserID,Name", new String[]{
                    "U001,Aman Kumar",
                    "U002,Nisha Patel"
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

    public static ArrayList<LibraryItem> loadItems(String filePath) {
        ArrayList<LibraryItem> items = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return items;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty() && !trimmedLine.toLowerCase().contains("itemid")) {
                    String[] data = line.split(",");
                    if (data.length >= 4) {
                        String id = data[0].trim();
                        String title = data[1].trim();
                        String type = data[2].trim();
                        boolean available = Boolean.parseBoolean(data[3].trim());
                        switch (type.toLowerCase()) {
                            case "book":
                                items.add(new Book(id, title, available));
                                break;
                            case "ebook":
                                items.add(new EBook(id, title, available));
                                break;
                            case "journal":
                                items.add(new Journal(id, title, available));
                                break;
                            default:
                                break;
                        }
                    }
                }
                line = reader.readLine();
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

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty() && !trimmedLine.toLowerCase().contains("userid")) {
                    String[] data = line.split(",");
                    if (data.length >= 2) {
                        users.add(new User(data[0].trim(), data[1].trim()));
                    }
                }
                line = reader.readLine();
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

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty() && !trimmedLine.toLowerCase().contains("transactionid")) {
                    String[] data = line.split(",");
                    if (data.length >= 7) {
                        String transactionID = data[0].trim();
                        String userID = data[1].trim();
                        String itemID = data[2].trim();
                        int issueDay = Integer.parseInt(data[3].trim());
                        int dueDay = Integer.parseInt(data[4].trim());
                        int returnDay = Integer.parseInt(data[5].trim());
                        double fine = Double.parseDouble(data[6].trim());
                        transactions.add(new Transaction(transactionID, userID, itemID, issueDay, dueDay, returnDay, fine));
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error loading transactions file: " + e.getMessage());
        }
        return transactions;
    }

    public static void saveItems(String filePath, ArrayList<LibraryItem> items) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("ItemID,Title,Type,Availability");
            for (LibraryItem item : items) {
                writer.println(item.toCsv());
            }
        } catch (IOException e) {
            System.out.println("Error saving items file: " + e.getMessage());
        }
    }

    public static void saveUsers(String filePath, ArrayList<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("UserID,Name");
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
