package utils;

import models.LibraryItem;

import java.util.Comparator;
import java.util.List;

/**
 * Utility helper for sorting library items.
 */
public class SortUtil {

    public enum SortOption {
        TITLE,
        AUTHOR,
        TYPE,
        AVAILABILITY,
        BORROW_COUNT
    }

    /**
     * Sorts a list of library items by title using bubble sort.
     *
     * @param items the list of items to sort
     */
    public static void bubbleSortByTitle(List<LibraryItem> items) {
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

    public static void sort(List<LibraryItem> items, SortOption option) {
        if (items == null || items.isEmpty()) {
            System.out.println("No items to sort.");
            return;
        }
        if (option == null) {
            throw new IllegalArgumentException("Sort option cannot be null.");
        }

        Comparator<LibraryItem> comparator;
        switch (option) {
            case AUTHOR:
                comparator = Comparator.comparing(LibraryItem::getAuthor,
                        String.CASE_INSENSITIVE_ORDER).thenComparing(LibraryItem::getTitle,
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case TYPE:
                comparator = Comparator.comparing(LibraryItem::getType,
                        String.CASE_INSENSITIVE_ORDER).thenComparing(LibraryItem::getTitle,
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case AVAILABILITY:
                comparator = Comparator.comparing(LibraryItem::isAvailable).reversed()
                        .thenComparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case BORROW_COUNT:
                comparator = Comparator.comparingInt(LibraryItem::getBorrowCount).reversed()
                        .thenComparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case TITLE:
            default:
                comparator = Comparator.comparing(LibraryItem::getTitle,
                        String.CASE_INSENSITIVE_ORDER);
                break;
        }
        items.sort(comparator);
    }
}