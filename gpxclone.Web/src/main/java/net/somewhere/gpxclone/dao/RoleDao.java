package net.somewhere.gpxclone.dao;

import java.util.Collection;

import net.somewhere.gpxclone.entities.Role;

public interface RoleDao {
    boolean roleExists(String name);
    Role getRole(String name);
    Role createRole(String name, String description);
    void updateRole(Role role);
    boolean deleteRole(Role role);
    Collection<Role> getRoles();
}
