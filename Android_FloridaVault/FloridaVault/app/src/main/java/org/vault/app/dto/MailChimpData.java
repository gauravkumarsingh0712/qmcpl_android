package org.vault.app.dto;

/**
 * Created by gauravkumar.singh on 2/2/2016.
 */
public class MailChimpData {

    private long userID;
    private String IsRegisteredUser;

    public String getIsRegisteredUser() {
        return IsRegisteredUser;
    }

    public void setIsRegisteredUser(String isRegisteredUser) {
        IsRegisteredUser = isRegisteredUser;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }


}
