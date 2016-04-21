package org.vault.app.service;

import org.vault.app.dto.MailChimpData;
import org.vault.app.dto.TabBannerDTO;
import org.vault.app.dto.User;
import org.vault.app.dto.VideoDTO;

import java.util.ArrayList;

public interface VaultApiInterface {

    public ArrayList<VideoDTO> getVideosListFromServer(String url) throws BusinessException;
    public VideoDTO getVideosDataFromServer(String url) throws BusinessException;
    public String postFavoriteStatus(long userId, long videoId, long playListId, boolean status) throws BusinessException;
    public String postSharingInfo(String videoId) throws BusinessException;
    public String validateEmail(String emailId) throws BusinessException;
    public String validateUsername(String userName) throws BusinessException;
    public String postUserData(User user) throws BusinessException;
    public String validateUserCredentials(String emailId, String password) throws BusinessException;
    public String getUserData(long userId, String emailId) throws BusinessException;
    public String updateUserData(User updatedUser) throws BusinessException;

    public String validateSocialLogin(String emailId, String flagStatus) throws BusinessException;
    public String changeUserPassword(long emailId, String oldPassword, String newPassword) throws BusinessException;

    public String sendPushNotificationRegistration(String url, String regId, String deviceId, boolean isAllowed) throws BusinessException;
    public String createTaskOnAsana(String nameAndEmail, String taskNotes, String type) throws BusinessException;
    public String createTagForAsanaTask(String tagId, String taskId) throws BusinessException;

    public ArrayList<TabBannerDTO> getAllTabBannerData() throws BusinessException;
    public TabBannerDTO getTabBannerDataById(long bannerId, String tabName, long tabId) throws BusinessException;

    public String postMailChimpData(MailChimpData mailChimpData) throws BusinessException;

}
