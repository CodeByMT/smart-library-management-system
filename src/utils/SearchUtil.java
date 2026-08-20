package utils;

import models.LibraryItem;

import java.util.ArrayList;

public class SearchUtil {

    /**
     * Finds an item by its ID.
     *
     * @param items the item list to search
     * @param id the item ID to locate
     * @return the matching item or null when not found
     */
    public static LibraryItem searchByID(ArrayList<LibraryItem> items, String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        for (LibraryItem item : items) {
            if (item.getItemID().equalsIgnoreCase(id.trim())) {
                return item;
            }
        }

        return null;
    }

    /**
     * Displays all items whose titles contain the provided keyword.
     *
     * @param items the list of library items
     * @param keyword the case-insensitive title substring
     */
    public static void searchByTitle(ArrayList<LibraryItem> items, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Please enter a non-empty keyword.");
            return;
        }

        boolean found = false;
        String normalizedKeyword = keyword.toLowerCase().trim();

        for (LibraryItem item : items) {
            if (item.getTitle().toLowerCase().contains(normalizedKeyword)) {
                item.displayInfo();
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching items found.");
        }
    }
}