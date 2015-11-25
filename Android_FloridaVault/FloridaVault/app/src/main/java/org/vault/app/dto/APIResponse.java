package org.vault.app.dto;

/**
 * Created by aqeeb.pathan on 15-04-2015.
 */
public class APIResponse {
    private String returnStatus;
    private long UserID;

    public long getUserID() {
        return UserID;
    }

    public void setUserID(long userID) {
        UserID = userID;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }
}
