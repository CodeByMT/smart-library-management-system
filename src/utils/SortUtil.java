package utils;

import models.LibraryItem;

import java.util.ArrayList;

/**
 * Utility helper for sorting library items.
 */
public class SortUtil {

    /**
     * Sorts a list of library items by title using bubble sort.
     *
     * @param items the list of items to sort
     */
    public static void bubbleSortByTitle(ArrayList<LibraryItem> items) {
        if (items == null || items.isEmpty()) {
            System.out.println("No items to sort.");
            return;
        }

        for (int i = 0; i < items.size() - 1; i++) {
            for (int j = 0; j < items.size() - i - 1; j++) {
                if (items.get(j).getTitle().compareToIgnoreCase(items.get(j + 1).getTitle()) > 0) {
                    LibraryItem temp = items.get(j);
                    items.set(j, items.get(j + 1));
                    items.set(j + 1, temp);
                }
            }
        }
        System.out.println("Items sorted successfully");
    }
}