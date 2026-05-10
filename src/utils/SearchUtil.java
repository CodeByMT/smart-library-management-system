package utils;

import models.LibraryItem;

import java.util.ArrayList;

public class SearchUtil {

    public static LibraryItem searchByID(ArrayList<LibraryItem> items, String id) {
        for (LibraryItem item : items) {
            if (item.getItemID().equalsIgnoreCase(id)) {
                return item;
            }
        }
        return null;
    }

    public static void searchByTitle(ArrayList<LibraryItem> items, String keyword) {
        boolean found = false;
        for (LibraryItem item : items) {
            if (item.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                item.displayInfo();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No matching items found.");
        }
    }
}