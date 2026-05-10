package interfaces;

import models.User;

public interface Loanable {
    void issueItem(User user);
    void returnItem(User user);
}