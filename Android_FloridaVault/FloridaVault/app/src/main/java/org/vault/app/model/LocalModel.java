package org.vault.app.model;

/**
 * Created by gauravkumar.singh on 1/13/2016.
 */
public class LocalModel {

    public static LocalModel mInstance;

    private int mDisplayHeight = 0;
    private int mDisplayWidth = 0;
    private int mListViewHeight = 0;
    private String firstName;
    private String lastName;
    private String emailId;
    private String videoId;

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public static LocalModel getInstance() {
        if (mInstance == null) {
            mInstance = new LocalModel();
        }

        return mInstance;
    }

    public int getmListViewHeight() {
        return mListViewHeight;
    }

    public void setmListViewHeight(int mListViewHeight) {
        this.mListViewHeight = mListViewHeight;
    }

    public int getmDisplayWidth() {
        return mDisplayWidth;
    }

    public void setmDisplayWidth(int mDisplayWidth) {
        this.mDisplayWidth = mDisplayWidth;
    }

    public int getmDisplayHeight() {
        return mDisplayHeight;
    }

    public void setmDisplayHeight(int mDisplayHeight) {
        this.mDisplayHeight = mDisplayHeight;
    }


}
