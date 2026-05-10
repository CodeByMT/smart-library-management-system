package utils;

import models.LibraryItem;

import java.util.ArrayList;

public class SortUtil {

    public static void bubbleSortByTitle(ArrayList<LibraryItem> items) {
        for (int i = 0; i < items.size() - 1; i++) {
            for (int j = 0; j < items.size() - i - 1; j++) {
                if (items.get(j).getTitle().compareToIgnoreCase(items.get(j + 1).getTitle()) > 0) {
                    LibraryItem temp = items.get(j);
                    items.set(j, items.get(j + 1));
                    items.set(j + 1, temp);
                }
            }
        }
        System.out.println("Items sorted successfully.");
    }
}