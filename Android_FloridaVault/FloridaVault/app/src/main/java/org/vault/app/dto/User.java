package org.vault.app.dto;

import java.io.Serializable;

/**
 * Created by aqeeb.pathan on 13-04-2015.
 */
public class User implements Serializable {
    private String emailID;
    private String username;
    private String passwd;
    private String fname ;
    private String lname ;
    private int age ;
    private String gender ;
    private String imageurl ;
//    private String appname ;
    private int appID;
    private String deviceType;
    private String appVersion;
    private String biotext;
    private String flagStatus;
    private long userID;
    private String IsRegisteredUser;
    public String getIsRegisteredUser() {
        return IsRegisteredUser;
    }

    public void setIsRegisteredUser(String isRegisteredUser) {
        IsRegisteredUser = isRegisteredUser;
    }



    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public String getBiotext() {
        return biotext;
    }

    public void setBiotext(String biotext) {
        this.biotext = biotext;
    }

    public String getFlagStatus() {
        return flagStatus;
    }

    public void setFlagStatus(String flagStatus) {
        this.flagStatus = flagStatus;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    /*public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }*/

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
