package net.somewhere.gpxclone.dao;

import java.util.Collection;
import net.somewhere.gpxclone.entities.*;

public interface UserDao {
    String activateUser(String activationKey);
    boolean userExists(String userName);
    User getUser(long userId);
    User getUser(String userName);
    User createUser(User newUser);
    void updateUser(User user);
    boolean deleteUser(User user);
    Collection<User> getUsers();
    void updateUserPreferences(UserPreferences prefs);
}
