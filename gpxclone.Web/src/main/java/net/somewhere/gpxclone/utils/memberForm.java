package net.somewhere.gpxclone.utils;

import javax.validation.Valid;
import net.somewhere.gpxclone.entities.User;

@FieldConfirm(field="user.password",confirmation="passwordConfirm",message="The password fields must match")
public class memberForm {
    
    private User user;
    private String passwordConfirm;

    @Valid
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
    
    
    
}
