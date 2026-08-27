package utils;

import models.LibraryItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchUtil {

    /**
     * Finds an item by its ID.
     *
     * @param items the item list to search
     * @param id the item ID to locate
     * @return the matching item or null when not found
     */
    public static LibraryItem searchByID(List<LibraryItem> items, String id) {
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
    public static void searchByTitle(List<LibraryItem> items, String keyword) {
        List<LibraryItem> matches = searchByKeyword(items, keyword, false);
        if (matches.isEmpty()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                System.out.println("Please enter a non-empty keyword.");
            } else {
                System.out.println("No matching items found.");
            }
            return;
        }

        for (LibraryItem item : matches) {
            item.displayInfo();
        }
    }

    /**
     * Finds items whose ID, title, or author contains the keyword.
     */
    public static List<LibraryItem> searchByKeyword(List<LibraryItem> items, String keyword) {
        return searchByKeyword(items, keyword, true);
    }

    private static List<LibraryItem> searchByKeyword(List<LibraryItem> items, String keyword,
                                                     boolean includeIdAndAuthor) {
        List<LibraryItem> matches = new ArrayList<>();
        if (items == null || keyword == null || keyword.trim().isEmpty()) {
            return matches;
        }

        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        for (LibraryItem item : items) {
            boolean titleMatches = item.getTitle().toLowerCase(Locale.ROOT).contains(normalizedKeyword);
            boolean otherFieldMatches = includeIdAndAuthor
                    && (item.getItemID().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                    || item.getAuthor().toLowerCase(Locale.ROOT).contains(normalizedKeyword));
            if (titleMatches || otherFieldMatches) {
                matches.add(item);
            }
        }
        return matches;
    }

    public static List<LibraryItem> filter(List<LibraryItem> items, Set<String> types,
                                           Boolean available) {
        List<LibraryItem> matches = new ArrayList<>();
        if (items == null) {
            return matches;
        }

        Set<String> normalizedTypes = new HashSet<>();
        if (types != null) {
            for (String type : types) {
                if (type != null && !type.trim().isEmpty()) {
                    normalizedTypes.add(type.trim().toLowerCase(Locale.ROOT));
                }
            }
        }

        for (LibraryItem item : items) {
            boolean typeMatches = normalizedTypes.isEmpty()
                    || normalizedTypes.contains(item.getType().toLowerCase(Locale.ROOT));
            boolean availabilityMatches = available == null || item.isAvailable() == available;
            if (typeMatches && availabilityMatches) {
                matches.add(item);
            }
        }
        return matches;
    }
}