package net.somewhere.gpxclone.services;

import net.somewhere.gpxclone.utils.UserAlreadyExistsException;
import java.util.Collection;
import net.somewhere.gpxclone.entities.*;


public interface UserService {
    String activateUser(String activationKey);
    void resetPassword(String userId, String email) throws Exception;
    boolean hasUser(String userId);
    User getUser(String userId);
    User createUser(User userDto) throws UserAlreadyExistsException;
    void updateUser(User user);
    void updateUserPreferences(UserPreferences prefs);
    boolean deleteUser(User user);
    Collection<User> getUsers();
    boolean isUserInRole(String userName, String roleName);
    Role createRole(String roleName, String description);
    void updateRole(Role role);
    boolean deleteRole(Role role);
    Role getRole(String roleName);
    Collection<Role> getRoles();
}