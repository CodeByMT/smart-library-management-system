package interfaces;

import models.User;

/**
 * Interface for items that can be borrowed by a user.
 */
public interface Borrowable {
    void issueItem(User user);
    void returnItem(User user);
}