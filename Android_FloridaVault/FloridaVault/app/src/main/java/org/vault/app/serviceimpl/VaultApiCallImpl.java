package org.vault.app.serviceimpl;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.vault.app.dto.APIResponse;
import org.vault.app.dto.AssigneeDto;
import org.vault.app.dto.FavoritePostData;
import org.vault.app.dto.MailChimpData;
import org.vault.app.dto.NotificationData;
import org.vault.app.dto.TabBannerDTO;
import org.vault.app.dto.TaskDto;
import org.vault.app.dto.User;
import org.vault.app.dto.VideoDTO;
import org.vault.app.globalconstants.GlobalConstants;
import org.vault.app.service.BusinessException;
import org.vault.app.service.ServiceManager;
import org.vault.app.service.VaultApiInterface;
import org.vault.app.service.VaultService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by aqeeb.pathan on 25-03-2015.
 */
public class VaultApiCallImpl extends VaultService implements VaultApiInterface {

    private Gson gson;

    /*private String PLAYLISTID = "playListId";
    private String PLAYLISTNAMEOFVIDEO = "playlistNameOfVideo";
    private String PLAYLISTTYPE = "playlistType";
    private String PLAYLISTREFID = "playListRefID";
    private String VIDEOID = "videoID";
    private String ITEMID = "item_id";
    private String VIDEONAME = "videoName";
    private String DESCRIPTION = "videoDescription";
    private String VIDEOTHUMBNAIL = "videoThumbnail";
    private String VIDEOSTILLURL = "videoStillURL";
    private String LENGTH = "length";
    private String FAVSTATUS = "favStatus";
    private String VIDEOSHORTURL = "VideoShortURL";
    private String VIDEOLONGURL = "VideoURL";
    private String BIOTEXT = "biotext";*/

    //Parameters that are returned from server in form of JSON
    private static final String KEY_PLAYLIST_ID = "playlistId";
    private static final String KEY_PLAYLIST_NAME = "playlistName";
    //private static final String KEY_PLAYLIST_TYPE = "playlistType";
    private static final String KEY_PLAYLIST_REFERENCEID = "playlistReferenceId";
    private static final String KEY_PLAYLIST_THUMBNAIL_URL = "playlistThumbnailUrl";
    private static final String KEY_PLAYLIST_SHORT_DESCRIPTION = "playlistShortDescription";
    private static final String KEY_PLAYLIST_LONG_DESCRIPTION = "playlistLongDescription";
    private static final String KEY_PLAYLIST_TAGS = "playlistTags";
    private static final String KEY_VIDEO_ID = "videoId";
    private static final String KEY_VIDEO_NAME = "videoName";
    private static final String KEY_VIDEO_SHORT_DESCRIPTION = "videoShortDescription";
    private static final String KEY_VIDEO_LONG_DESCRIPTION = "videoLongDescription";
    private static final String KEY_VIDEO_THUMBNAIL_URL = "videoThumbnailUrl";
    private static final String KEY_VIDEO_STILL_URL = "videoStillUrl";
    private static final String KEY_VIDEO_DURATION = "videoDuration";
    private static final String KEY_VIDEO_IS_FAVORITE = "videoIsFavorite";
    private static final String KEY_VIDEO_LONG_URL = "videoLongUrl";
    private static final String KEY_VIDEO_SHORT_URL = "videoShortUrl";
    private static final String KEY_VIDEO_TAGS = "videoTags";
    private static final String KEY_VIDEO_COVER_URL = "videoCoverUrl";
    private static final String KEY_VIDEO_WIDE_STILL_URL = "videoWideStillUrl";
    private static final String KEY_VIDEO_BADGE_URL = "videoBadgeUrl";
    private static final String KEY_VIDEO_INDEX = "videoIndex";
    private static final String KEY_VIDEO_SOCIAL_URL = "videoSocialUrl";


    public VaultApiCallImpl(ServiceManager serviceManager) {
        super(serviceManager);
        init();
    }

    @Override
    public void init() {
        super.init();
        gson = new Gson();
    }

    @Override
    public ArrayList<VideoDTO> getVideosListFromServer(String url) throws BusinessException {
        url = url.replaceAll(" ", "%20");
        String result = doGet(url);
//        System.out.println("Response of url : "+result);
        if (result != null) {
//            if (result.contains("timeout"))
//                return null;
        }
        ArrayList<VideoDTO> videoList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(result);
            VideoDTO vidObj;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                vidObj = new VideoDTO();

                vidObj.setVideoDuration(!obj.isNull(KEY_VIDEO_DURATION) ? obj.getLong(KEY_VIDEO_DURATION) : 0);
                vidObj.setPlaylistId(!obj.isNull(KEY_PLAYLIST_ID) ? obj.getLong(KEY_PLAYLIST_ID) : 0);
                vidObj.setPlaylistName(!obj.isNull(KEY_PLAYLIST_NAME) ? obj.getString(KEY_PLAYLIST_NAME) : "");
                vidObj.setVideoShortDescription(!obj.isNull(KEY_VIDEO_SHORT_DESCRIPTION) ? obj.getString(KEY_VIDEO_SHORT_DESCRIPTION) : "");
                vidObj.setVideoId(!obj.isNull(KEY_VIDEO_ID) ? obj.getLong(KEY_VIDEO_ID) : 0);
                vidObj.setVideoName(!obj.isNull(KEY_VIDEO_NAME) ? obj.getString(KEY_VIDEO_NAME) : "");
                vidObj.setVideoStillUrl(!obj.isNull(KEY_VIDEO_STILL_URL) ? obj.getString(KEY_VIDEO_STILL_URL) : "");
                vidObj.setVideoCoverUrl(!obj.isNull(KEY_VIDEO_COVER_URL) ? obj.getString(KEY_VIDEO_COVER_URL) : "");
                vidObj.setVideoWideStillUrl(!obj.isNull(KEY_VIDEO_WIDE_STILL_URL) ? obj.getString(KEY_VIDEO_WIDE_STILL_URL) : "");
                vidObj.setVideoBadgeUrl(!obj.isNull(KEY_VIDEO_BADGE_URL) ? obj.getString(KEY_VIDEO_BADGE_URL) : "");
                vidObj.setVideoThumbnailUrl(!obj.isNull(KEY_VIDEO_THUMBNAIL_URL) ? obj.getString(KEY_VIDEO_THUMBNAIL_URL) : "");
                vidObj.setVideoLongUrl(!obj.isNull(KEY_VIDEO_LONG_URL) ? obj.getString(KEY_VIDEO_LONG_URL) : "");
                vidObj.setVideoShortUrl(!obj.isNull(KEY_VIDEO_SHORT_URL) ? obj.getString(KEY_VIDEO_SHORT_URL) : "");
                vidObj.setVideoIndex(!obj.isNull(KEY_VIDEO_INDEX) ? obj.getInt(KEY_VIDEO_INDEX) : 0);
                vidObj.setPlaylistReferenceId(!obj.isNull(KEY_PLAYLIST_REFERENCEID) ? obj.getString(KEY_PLAYLIST_REFERENCEID) : "");

                vidObj.setVideoTags(!obj.isNull(KEY_VIDEO_TAGS) ? obj.getString(KEY_VIDEO_TAGS) : "");
                vidObj.setVideoLongDescription(!obj.isNull(KEY_VIDEO_LONG_DESCRIPTION) ? obj.getString(KEY_VIDEO_LONG_DESCRIPTION) : "");
                vidObj.setPlaylistTags(!obj.isNull(KEY_PLAYLIST_TAGS) ? obj.getString(KEY_PLAYLIST_TAGS) : "");
                vidObj.setPlaylistThumbnailUrl(!obj.isNull(KEY_PLAYLIST_THUMBNAIL_URL) ? obj.getString(KEY_PLAYLIST_THUMBNAIL_URL) : "");
                vidObj.setPlaylistLongDescription(!obj.isNull(KEY_PLAYLIST_LONG_DESCRIPTION) ? obj.getString(KEY_PLAYLIST_LONG_DESCRIPTION) : "");
                vidObj.setPlaylistShortDescription(!obj.isNull(KEY_PLAYLIST_SHORT_DESCRIPTION) ? obj.getString(KEY_PLAYLIST_SHORT_DESCRIPTION) : "");
                vidObj.setVideoSocialUrl(!obj.isNull(KEY_VIDEO_SOCIAL_URL) ? obj.getString(KEY_VIDEO_SOCIAL_URL) : "");

                vidObj.setVideoIsFavorite(!obj.isNull(KEY_VIDEO_IS_FAVORITE) && obj.getBoolean(KEY_VIDEO_IS_FAVORITE));
                // if (VaultDatabaseHelper.getInstance(AppController.getInstance().getApplicationContext()).isVideoAvailableInDB(vidObj.getVideoId(), vidObj.getPlaylistReferenceId())) {
                // if (VaultDatabaseHelper.getInstance(AppController.getInstance().getApplicationContext()).isFavorite(vidObj.getVideoId()))
                if (vidObj.isVideoIsFavorite())
                    vidObj.setVideoIsFavorite(true);
                else
                    vidObj.setVideoIsFavorite(false);
                //  }
                videoList.add(vidObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*try {
            Type classType = new TypeToken<ArrayList<NewVideoDTO>>() {
            }.getType();
            ArrayList<NewVideoDTO> response = gson.fromJson(result.trim(), classType);
            videoList = response;
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        System.out.println("Size of video list : " + videoList.size());
        VideoDTO vidObj;
        for (Iterator<VideoDTO> it = videoList.iterator(); it.hasNext(); ) {
            vidObj = it.next();
            if (vidObj.getVideoName().equals("") && vidObj.getVideoShortDescription() == "")
                it.remove();
        }
        System.out.println("Updated Size of video list : " + videoList.size());
        return videoList;
    }

    @Override
    public VideoDTO getVideosDataFromServer(String url) throws BusinessException {
        try {
            String result = doGet(url);
            if (result != null) {
//                if (result.contains("timeout"))
//                    return null;
            }
            System.out.println("Response of video Data : " + result);
            // JSONArray jsonArray = new JSONArray(result);
            VideoDTO vidObj = null;
            //  for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = new JSONObject(result);

            vidObj = new VideoDTO();

            vidObj.setVideoDuration(!obj.isNull(KEY_VIDEO_DURATION) ? obj.getLong(KEY_VIDEO_DURATION) : 0);
            vidObj.setPlaylistId(!obj.isNull(KEY_PLAYLIST_ID) ? obj.getLong(KEY_PLAYLIST_ID) : 0);
            vidObj.setPlaylistName(!obj.isNull(KEY_PLAYLIST_NAME) ? obj.getString(KEY_PLAYLIST_NAME) : "");
            vidObj.setVideoShortDescription(!obj.isNull(KEY_VIDEO_SHORT_DESCRIPTION) ? obj.getString(KEY_VIDEO_SHORT_DESCRIPTION) : "");
            vidObj.setVideoId(!obj.isNull(KEY_VIDEO_ID) ? obj.getLong(KEY_VIDEO_ID) : 0);
            vidObj.setVideoName(!obj.isNull(KEY_VIDEO_NAME) ? obj.getString(KEY_VIDEO_NAME) : "");
            vidObj.setVideoStillUrl(!obj.isNull(KEY_VIDEO_STILL_URL) ? obj.getString(KEY_VIDEO_STILL_URL) : "");
            vidObj.setVideoCoverUrl(!obj.isNull(KEY_VIDEO_COVER_URL) ? obj.getString(KEY_VIDEO_COVER_URL) : "");
            vidObj.setVideoWideStillUrl(!obj.isNull(KEY_VIDEO_WIDE_STILL_URL) ? obj.getString(KEY_VIDEO_WIDE_STILL_URL) : "");
            vidObj.setVideoBadgeUrl(!obj.isNull(KEY_VIDEO_BADGE_URL) ? obj.getString(KEY_VIDEO_BADGE_URL) : "");
            vidObj.setVideoThumbnailUrl(!obj.isNull(KEY_VIDEO_THUMBNAIL_URL) ? obj.getString(KEY_VIDEO_THUMBNAIL_URL) : "");
            vidObj.setVideoLongUrl(!obj.isNull(KEY_VIDEO_LONG_URL) ? obj.getString(KEY_VIDEO_LONG_URL) : "");
            vidObj.setVideoShortUrl(!obj.isNull(KEY_VIDEO_SHORT_URL) ? obj.getString(KEY_VIDEO_SHORT_URL) : "");
            vidObj.setVideoIndex(!obj.isNull(KEY_VIDEO_INDEX) ? obj.getInt(KEY_VIDEO_INDEX) : 0);
            vidObj.setPlaylistReferenceId(!obj.isNull(KEY_PLAYLIST_REFERENCEID) ? obj.getString(KEY_PLAYLIST_REFERENCEID) : "");

            vidObj.setVideoTags(!obj.isNull(KEY_VIDEO_TAGS) ? obj.getString(KEY_VIDEO_TAGS) : "");
            vidObj.setVideoLongDescription(!obj.isNull(KEY_VIDEO_LONG_DESCRIPTION) ? obj.getString(KEY_VIDEO_LONG_DESCRIPTION) : "");
            vidObj.setPlaylistTags(!obj.isNull(KEY_PLAYLIST_TAGS) ? obj.getString(KEY_PLAYLIST_TAGS) : "");
            vidObj.setPlaylistThumbnailUrl(!obj.isNull(KEY_PLAYLIST_THUMBNAIL_URL) ? obj.getString(KEY_PLAYLIST_THUMBNAIL_URL) : "");
            vidObj.setPlaylistLongDescription(!obj.isNull(KEY_PLAYLIST_LONG_DESCRIPTION) ? obj.getString(KEY_PLAYLIST_LONG_DESCRIPTION) : "");
            vidObj.setPlaylistShortDescription(!obj.isNull(KEY_PLAYLIST_SHORT_DESCRIPTION) ? obj.getString(KEY_PLAYLIST_SHORT_DESCRIPTION) : "");
            vidObj.setVideoSocialUrl(!obj.isNull(KEY_VIDEO_SOCIAL_URL) ? obj.getString(KEY_VIDEO_SOCIAL_URL) : "");

            vidObj.setVideoIsFavorite(!obj.isNull(KEY_VIDEO_IS_FAVORITE) && obj.getBoolean(KEY_VIDEO_IS_FAVORITE));
            //if (VaultDatabaseHelper.getInstance(AppController.getInstance().getApplicationContext()).isVideoAvailableInDB(vidObj.getVideoId(), vidObj.getPlaylistReferenceId())) {
            if (vidObj.isVideoIsFavorite())
                vidObj.setVideoIsFavorite(true);
            else
                vidObj.setVideoIsFavorite(false);
            // }
            //    }

            return vidObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * API call with POST request for storing Favorite status on server
     *
     * @param userId
     * @param videoId
     * @param playListId
     * @param status
     * @return
     * @throws org.vault.app.service.BusinessException
     */
    @Override
    public String postFavoriteStatus(long userId, long videoId, long playListId, boolean status) throws BusinessException {

        FavoritePostData postData = new FavoritePostData();
        postData.setUserid(userId);
        postData.setPlayListId(playListId);
        postData.setVideoId(videoId);
        postData.setFavStatus(status);

        String postStr;
        try {
            BasicHttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
            HttpConnectionParams.setSoTimeout(httpParameters, 60000);

            // create HttpClient
            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            InputStream inputStream = null;

            HttpPost httpPost = new HttpPost(GlobalConstants.FAVORITE_POST_STATUS_URL);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("appID", String.valueOf(GlobalConstants.APP_ID));
            httpPost.setHeader("appVersion", GlobalConstants.APP_VERSION);
            httpPost.setHeader("deviceType", GlobalConstants.DEVICE_TYPE);
            if (postData != null) {
                postStr = new Gson().toJson(postData);
                StringEntity stringEntity = new StringEntity(postStr);
                System.out.println("Json String For Favorite Status Change : " + postStr);
                httpPost.setEntity(new StringEntity(postStr, HTTP.UTF_8));
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();


            if (inputStream != null) {
                try {
                    String result = getStringFromInputStream(inputStream);
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert inputstream to string


        return null;
    }

    @Override
    public String postSharingInfo(String videoId) throws BusinessException {

        // String postData = String.valueOf(videoId);
        String postStr;
        try {
            BasicHttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
            HttpConnectionParams.setSoTimeout(httpParameters, 60000);

            // create HttpClient
            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            InputStream inputStream = null;
            HttpPost httpPost = new HttpPost(GlobalConstants.SOCIAL_SHARING_INFO + "?videoId=" + videoId);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("appID", String.valueOf(GlobalConstants.APP_ID));
            httpPost.setHeader("appVersion", GlobalConstants.APP_VERSION);
            httpPost.setHeader("deviceType", GlobalConstants.DEVICE_TYPE);
//            if (postData != null) {
//                postStr = new Gson().toJson(postData);
//                StringEntity stringEntity = new StringEntity(postStr);
//                System.out.println("Json String For Favorite Status Change : " + postStr);
//                httpPost.setEntity(new StringEntity(postStr, HTTP.UTF_8));
//            }
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();


            if (inputStream != null) {
                try {
                    String result = getStringFromInputStream(inputStream);
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert inputstream to string


        return null;
    }

    /**
     * API call with GET request having parameters in QueryString for validating EmailID
     *
     * @param emailId
     * @return
     * @throws org.vault.app.service.BusinessException
     */
    @Override
    public String validateEmail(String emailId) throws BusinessException {
        try {
            APIResponse response = null;
            String url = GlobalConstants.VALIDATE_EMAIL_URL + URLEncoder.encode(emailId, "UTF-8");
            url = url.replaceAll(" ", "%20");
            String result = doGet(url);
            System.out.println("Response of url : " + result);
            Type classType = new TypeToken<APIResponse>() {
            }.getType();
            response = gson.fromJson(result.trim(), classType);
            if (response != null)
                return response.getReturnStatus();
            else
                return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String validateUsername(String userName) throws BusinessException {
        try {
            APIResponse response = null;
            String url = GlobalConstants.VALIDATE_USERNAME_URL + URLEncoder.encode(userName, "UTF-8");
            url = url.replaceAll(" ", "%20");
            String result = doGet(url);
            System.out.println("Response of url : " + result);
            Type classType = new TypeToken<APIResponse>() {
            }.getType();
            response = gson.fromJson(result.trim(), classType);
            if (response != null)
                return response.getReturnStatus();
            else
                return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * API call with GET request, having parameters in QueryString for fetching user data
     *
     * @param userId
     * @param emailId
     * @return
     * @throws org.vault.app.service.BusinessException
     */
    @Override
    public String getUserData(long userId, String emailId) throws BusinessException {
        try {
            String url = GlobalConstants.GET_USER_DATA_URL + "?emailID=" + URLEncoder.encode(emailId, "UTF-8") + "&userID=" + URLEncoder.encode(String.valueOf(userId), "UTF-8");
            url = url.replaceAll(" ", "%20");
            String result = doGet(url);
            System.out.println("Response of getUserData : " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String changeUserPassword(long userID, String oldPassword, String newPassword) throws BusinessException {
        try {
            String url = GlobalConstants.CHANGE_PASSWORD_URL + "?userID=" + URLEncoder.encode(String.valueOf(userID), "UTF-8") + "&oldpass=" + URLEncoder.encode(oldPassword, "UTF-8") + "&newpass=" + URLEncoder.encode(newPassword, "UTF-8");
            url = url.replaceAll(" ", "%20");
            String result = doGet(url);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * API call for updating user data by POST request for updating user data on server
     *
     * @param updatedUser
     * @return
     * @throws org.vault.app.service.BusinessException
     */
    @Override
    public String updateUserData(User updatedUser) throws BusinessException {
        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
        HttpConnectionParams.setSoTimeout(httpParameters, 60000);

        // create HttpClient
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        InputStream inputStream = null;

        HttpPost httpPost = new HttpPost(GlobalConstants.POST_UPDATED_USER_DATA_URL);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("appID", String.valueOf(GlobalConstants.APP_ID));
        httpPost.setHeader("appVersion", GlobalConstants.APP_VERSION);
        httpPost.setHeader("deviceType", GlobalConstants.DEVICE_TYPE);
        String postStr;
        try {
            if (updatedUser != null) {
                postStr = new Gson().toJson(updatedUser);
                StringEntity stringEntity = new StringEntity(postStr);
                System.out.println("Json String For POST User Data : " + postStr);
                httpPost.setEntity(new StringEntity(postStr, HTTP.UTF_8));
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert inputstream to string
        if (inputStream != null) {
            try {
                String result = getStringFromInputStream(inputStream);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * API call with GET request having parameters in QueryString for validating user
     *
     * @param emailId
     * @param password
     * @return
     * @throws org.vault.app.service.BusinessException
     */
    @Override
    public String validateUserCredentials(String emailId, String password) throws BusinessException {
        String url = GlobalConstants.VALIDATE_USER_CREDENTIALS_URL + "?emailID=" + emailId + "&pass=" + password;
        url = url.replaceAll(" ", "%20");
        String result = doGet(url);
        System.out.println("Response of url : " + result);
        return result;
    }

    /**
     * Request for storing User data on server with POST request
     *
     * @param user
     * @return
     * @throws org.vault.app.service.BusinessException
     */
    @Override
    public String postUserData(User user) throws BusinessException {
        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
        HttpConnectionParams.setSoTimeout(httpParameters, 60000);

        // create HttpClient
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        InputStream inputStream = null;

        HttpPost httpPost = new HttpPost(GlobalConstants.POST_USER_DATA_URL);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("appID", String.valueOf(GlobalConstants.APP_ID));
        httpPost.setHeader("appVersion", GlobalConstants.APP_VERSION);
        httpPost.setHeader("deviceType", GlobalConstants.DEVICE_TYPE);
        String postStr;
        try {
            if (user != null) {
                postStr = new Gson().toJson(user);
                StringEntity stringEntity = new StringEntity(postStr);
                System.out.println("Json String For POST User Data : " + postStr);
                httpPost.setEntity(new StringEntity(postStr, HTTP.UTF_8));
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert inputstream to string
        if (inputStream != null) {
            try {
                String result = getStringFromInputStream(inputStream);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Request for storing User data on server with POST request
     *
     * @param mailChimpData
     * @return
     * @throws org.vault.app.service.BusinessException
     */
    @Override
    public String postMailChimpData(MailChimpData mailChimpData) throws BusinessException {
        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
        HttpConnectionParams.setSoTimeout(httpParameters, 60000);

        // create HttpClient
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        InputStream inputStream = null;

        HttpPost httpPost = new HttpPost(GlobalConstants.POST_MAIL_CHIMP_DATA);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("appID", String.valueOf(GlobalConstants.APP_ID));
        httpPost.setHeader("appVersion", GlobalConstants.APP_VERSION);
        httpPost.setHeader("deviceType", GlobalConstants.DEVICE_TYPE);
        String postStr;
        try {
            if (mailChimpData != null) {
                postStr = new Gson().toJson(mailChimpData);
                StringEntity stringEntity = new StringEntity(postStr);
                System.out.println("Json String For POST User Data : " + postStr);
                httpPost.setEntity(new StringEntity(postStr, HTTP.UTF_8));
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert inputstream to string
        if (inputStream != null) {
            try {
                String result = getStringFromInputStream(inputStream);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    /**
     * API call with GET request having parameters in QueryString
     *
     * @param emailId
     * @param flagStatus
     * @return
     * @throws org.vault.app.service.BusinessException
     */
    @Override
    public String validateSocialLogin(String emailId, String flagStatus) throws BusinessException {
        String url = GlobalConstants.VALIDATE_SOCIAL_LOGIN_URL + "?emailID=" + emailId + "&flagStatus=" + flagStatus;
        url = url.replaceAll(" ", "%20");
        String result = doGet(url);
        System.out.println("Response of url : " + result);
        return result;
    }

    @Override
    public String sendPushNotificationRegistration(String url, String regId, String deviceId, boolean isAllowed) throws BusinessException {
        System.out.println("Url for Push Notification : " + url);
        NotificationData postObj = new NotificationData();
        postObj.setRegId(regId);
        postObj.setDeviceId(deviceId);
        postObj.setDeviceType(GlobalConstants.DEVICE_TYPE);
        postObj.setAppName(GlobalConstants.APP_NAME);
        postObj.setAppVersion(GlobalConstants.APP_VERSION);
        postObj.setAppID(GlobalConstants.APP_ID);
        postObj.setStatus1(String.valueOf(isAllowed));

        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
        HttpConnectionParams.setSoTimeout(httpParameters, 60000);

        // create HttpClient
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        InputStream inputStream = null;

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        String postStr;
        try {
            if (postObj != null) {
                postStr = new Gson().toJson(postObj);
                StringEntity stringEntity = new StringEntity(postStr);
                System.out.println("Json String For Notification Registration : " + postStr);
                httpPost.setEntity(new StringEntity(postStr, HTTP.UTF_8));
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert inputstream to string
        if (inputStream != null) {
            try {
                String result = getStringFromInputStream(inputStream);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String createTaskOnAsana(String nameAndEmail, String taskNotes, String tagId) throws BusinessException {
        System.out.println("Url for Task Creation : " + GlobalConstants.ASANA_TASK_API_URL);

        AssigneeDto assigneeDto = new AssigneeDto();
        /*if(type.equalsIgnoreCase("feedback")) {
            assigneeDto.setId(Long.parseLong(GlobalConstants.FEEDBACK_ASSIGNEE_ID));
            assigneeDto.setName(GlobalConstants.FEEDBACK_ASSIGNEE_NAME);
        }else if(type.equalsIgnoreCase("clip_request")) {
            assigneeDto.setId(Long.parseLong(GlobalConstants.CLIP_REQUEST_ASSIGNEE_ID));
            assigneeDto.setName(GlobalConstants.CLIP_REQUEST_ASSIGNEE_NAME);
        }else if(type.equalsIgnoreCase("support") || type.equalsIgnoreCase("no_login")) {
            assigneeDto.setId(Long.parseLong(GlobalConstants.SUPPORT_ASSIGNEE_ID));
            assigneeDto.setName(GlobalConstants.SUPPORT_ASSIGNEE_NAME);
        }*/

        if (tagId.equalsIgnoreCase(GlobalConstants.FEEDBACK_TAG_ID)) {
            assigneeDto.setId(Long.parseLong(GlobalConstants.FEEDBACK_ASSIGNEE_ID));
            assigneeDto.setName(GlobalConstants.FEEDBACK_ASSIGNEE_NAME);
        } else if (tagId.equalsIgnoreCase(GlobalConstants.CLIP_REQUEST_TAG_ID)) {
            assigneeDto.setId(Long.parseLong(GlobalConstants.CLIP_REQUEST_ASSIGNEE_ID));
            assigneeDto.setName(GlobalConstants.CLIP_REQUEST_ASSIGNEE_NAME);
        } else if (tagId.equalsIgnoreCase(GlobalConstants.SUPPORT_TAG_ID) || tagId.equalsIgnoreCase(GlobalConstants.NO_LOGIN_TAG_ID)) {
            assigneeDto.setId(Long.parseLong(GlobalConstants.SUPPORT_ASSIGNEE_ID));
            assigneeDto.setName(GlobalConstants.SUPPORT_ASSIGNEE_NAME);
        }

        TaskDto postObj = new TaskDto();
        postObj.setName(nameAndEmail);
        postObj.setNotes(taskNotes);
        postObj.setAssignee_status(GlobalConstants.ASSIGNEE_STATUS);
        postObj.setAssignee(assigneeDto);
        postObj.setWorkspace(Long.parseLong(GlobalConstants.WORKSPACE_ID));
        postObj.setProjects(GlobalConstants.PROJECT_ID);

        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
        HttpConnectionParams.setSoTimeout(httpParameters, 60000);

        // create HttpClient
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        InputStream inputStream = null;

        HttpPost httpPost = new HttpPost(GlobalConstants.ASANA_TASK_API_URL);

        //API key for workspace along with : which represents password which is empty
        String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                (GlobalConstants.ASANA_WORKSPACE_API_KEY + ":").getBytes(),
                Base64.NO_WRAP);

        System.out.println("Authorization : " + base64EncodedCredentials);

        httpPost.setHeader("Authorization", base64EncodedCredentials);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        String postStr;
        try {
            if (postObj != null) {
                postStr = new Gson().toJson(postObj);
                String data = "{\"data\":" + postStr + "}";

                StringEntity stringEntity = new StringEntity(data);
                System.out.println("Json String For Task Creation : " + data);
                httpPost.setEntity(stringEntity);
            }

            HttpResponse httpResponse = httpClient.execute(httpPost);

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert inputstream to string
        if (inputStream != null) {
            try {
                String result = getStringFromInputStream(inputStream);
                System.out.println("$$$$$$$$$$ Response from Asana for Task creation : " + result);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String createTagForAsanaTask(String tagId, String taskId) throws BusinessException {
        System.out.println("Url for Task Tag Creation : " + GlobalConstants.ASANA_TAG_API_URL);

        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
        HttpConnectionParams.setSoTimeout(httpParameters, 60000);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

        nameValuePairs.add(new BasicNameValuePair("tag", tagId));

        // create HttpClient
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        InputStream inputStream = null;

        HttpPost httpPost = new HttpPost(GlobalConstants.ASANA_TAG_API_URL + taskId + "/addTag");

        //API key for workspace along with : which represents password which is empty
        String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                (GlobalConstants.ASANA_WORKSPACE_API_KEY + ":").getBytes(),
                Base64.NO_WRAP);

        System.out.println("Authorization : " + base64EncodedCredentials);

        httpPost.setHeader("Authorization", base64EncodedCredentials);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert inputstream to string
        if (inputStream != null) {
            try {
                String result = getStringFromInputStream(inputStream);
                System.out.println("$$$$$$$$$$ Response from Asana for Tag creation : " + result);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    //Tab Columns
    public static final String KEY_TAB_ID = "tabId";
    public static final String KEY_TAB_NAME = "tabName";
    public static final String KEY_TAB_POSITION = "tabIndexPosition";
    public static final String KEY_TAB_CREATED = "tabCreated";
    public static final String KEY_TAB_MODIFIED = "tabModified";
    public static final String KEY_TAB_DATA_CREATED = "tabDataCreated";
    public static final String KEY_TAB_DATA_MODIFIED = "tabDataModified";
    public static final String KEY_TAB_KEYWORD = "tabKeyword";
    public static final String KEY_TAB_DISPLAY_TYPE = "tabDisplayType";

    //Banner Columns
    public static final String KEY_BANNER_ID = "tabBannerId";
    public static final String KEY_BANNER_NAME = "tabBannerName";
    public static final String KEY_BANNER_URL = "bannerURL";
    public static final String KEY_IS_BANNER_ACTIVATED = "isBannerActive";
    public static final String KEY_IS_HYPER_LINK_AVAILABLE = "isHyperlinkActive";
    public static final String KEY_ACTION_URL = "bannerActionURL";
    public static final String KEY_BANNER_CREATED = "bannerCreated";
    public static final String KEY_BANNER_MODIFIED = "bannerModified";

    @Override
    public ArrayList<TabBannerDTO> getAllTabBannerData() throws BusinessException {
        ArrayList<TabBannerDTO> listTabBannerData = new ArrayList<>();
        try {
            String result = doGet(GlobalConstants.GET_ALL_TAB_BANNER_DATA_URL);
            if (result != null) {
//                if (result.contains("timeout"))
//                    return null;
            }

            System.out.println("Response for Banner Tab Data : " + result);

            JSONArray jsonArray = new JSONArray(result);
            TabBannerDTO tabBannerDTO;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                tabBannerDTO = new TabBannerDTO();

                tabBannerDTO.setTabId(!obj.isNull(KEY_TAB_ID) ? obj.getLong(KEY_TAB_ID) : 0);
                tabBannerDTO.setTabName(!obj.isNull(KEY_TAB_NAME) ? obj.getString(KEY_TAB_NAME) : "");
                tabBannerDTO.setTabIndexPosition(!obj.isNull(KEY_TAB_POSITION) ? obj.getLong(KEY_TAB_POSITION) : 0);
                tabBannerDTO.setTabCreated(!obj.isNull(KEY_TAB_CREATED) ? Long.parseLong(obj.getString(KEY_TAB_CREATED)) : 0);
                tabBannerDTO.setTabModified(!obj.isNull(KEY_TAB_MODIFIED) ? Long.parseLong(obj.getString(KEY_TAB_MODIFIED)) : 0);
                tabBannerDTO.setTabDataCreated(!obj.isNull(KEY_TAB_DATA_CREATED) ? Long.parseLong(obj.getString(KEY_TAB_DATA_CREATED)) : 0);
                tabBannerDTO.setTabDataModified(!obj.isNull(KEY_TAB_DATA_MODIFIED) ? Long.parseLong(obj.getString(KEY_TAB_DATA_MODIFIED)) : 0);
                tabBannerDTO.setTabKeyword(!obj.isNull(KEY_TAB_KEYWORD) ? obj.getString(KEY_TAB_KEYWORD) : "");
                tabBannerDTO.setTabDisplayType(!obj.isNull(KEY_TAB_DISPLAY_TYPE) ? obj.getString(KEY_TAB_DISPLAY_TYPE) : "");

                tabBannerDTO.setTabBannerId(!obj.isNull(KEY_BANNER_ID) ? obj.getLong(KEY_BANNER_ID) : 0);
                tabBannerDTO.setTabBannerName(!obj.isNull(KEY_BANNER_NAME) ? obj.getString(KEY_BANNER_NAME) : "");

                tabBannerDTO.setBannerURL(!obj.isNull(KEY_BANNER_URL) ? obj.getString(KEY_BANNER_URL) : "");
                tabBannerDTO.setBannerURL(tabBannerDTO.getBannerURL().replace(" ", "%20"));

                tabBannerDTO.setIsBannerActive(!obj.isNull(KEY_IS_BANNER_ACTIVATED) ? obj.getBoolean(KEY_IS_BANNER_ACTIVATED) : false);
                tabBannerDTO.setIsHyperlinkActive(!obj.isNull(KEY_IS_HYPER_LINK_AVAILABLE) ? obj.getBoolean(KEY_IS_HYPER_LINK_AVAILABLE) : false);
                tabBannerDTO.setBannerActionURL(!obj.isNull(KEY_ACTION_URL) ? obj.getString(KEY_ACTION_URL) : "");
                tabBannerDTO.setBannerCreated(!obj.isNull(KEY_BANNER_CREATED) ? Long.parseLong(obj.getString(KEY_BANNER_CREATED)) : 0);
                tabBannerDTO.setBannerModified(!obj.isNull(KEY_BANNER_MODIFIED) ? Long.parseLong(obj.getString(KEY_BANNER_MODIFIED)) : 0);
                listTabBannerData.add(tabBannerDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listTabBannerData;
    }

    @Override
    public TabBannerDTO getTabBannerDataById(long bannerId, String tabKeyword, long tabId) throws BusinessException {
        try {
            String result = doGet(GlobalConstants.GET_TAB_BANNER_DATA_URL + "?tab_id=" + tabId + "&tab_keyword=" + tabKeyword);
            if (result != null) {
//                if (result.contains("timeout"))
//                    return null;
            }
            System.out.println("Response of Tab Banner Data : " + result);
            JSONArray jsonArray = new JSONArray(result);
            TabBannerDTO tabBannerDTO = null;
            if (jsonArray != null) {
                if (jsonArray.length() == 1)
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        tabBannerDTO = new TabBannerDTO();

                        tabBannerDTO.setTabId(!obj.isNull(KEY_TAB_ID) ? obj.getLong(KEY_TAB_ID) : 0);
                        tabBannerDTO.setTabName(!obj.isNull(KEY_TAB_NAME) ? obj.getString(KEY_TAB_NAME) : "");
                        tabBannerDTO.setTabIndexPosition(!obj.isNull(KEY_TAB_POSITION) ? obj.getLong(KEY_TAB_POSITION) : 0);
                        tabBannerDTO.setTabCreated(!obj.isNull(KEY_TAB_CREATED) ? Long.parseLong(obj.getString(KEY_TAB_CREATED)) : 0);
                        tabBannerDTO.setTabModified(!obj.isNull(KEY_TAB_MODIFIED) ? Long.parseLong(obj.getString(KEY_TAB_MODIFIED)) : 0);
                        tabBannerDTO.setTabDataCreated(!obj.isNull(KEY_TAB_DATA_CREATED) ? Long.parseLong(obj.getString(KEY_TAB_DATA_CREATED)) : 0);
                        tabBannerDTO.setTabDataModified(!obj.isNull(KEY_TAB_DATA_MODIFIED) ? Long.parseLong(obj.getString(KEY_TAB_DATA_MODIFIED)) : 0);
                        tabBannerDTO.setTabKeyword(!obj.isNull(KEY_TAB_KEYWORD) ? obj.getString(KEY_TAB_KEYWORD) : "");
                        tabBannerDTO.setTabDisplayType(!obj.isNull(KEY_TAB_DISPLAY_TYPE) ? obj.getString(KEY_TAB_DISPLAY_TYPE) : "");

                        tabBannerDTO.setTabBannerId(!obj.isNull(KEY_BANNER_ID) ? obj.getLong(KEY_BANNER_ID) : 0);
                        tabBannerDTO.setTabBannerName(!obj.isNull(KEY_BANNER_NAME) ? obj.getString(KEY_BANNER_NAME) : "");

                        tabBannerDTO.setBannerURL(!obj.isNull(KEY_BANNER_URL) ? obj.getString(KEY_BANNER_URL) : "");
                        tabBannerDTO.setBannerURL(tabBannerDTO.getBannerURL().replace(" ", "%20"));

                        tabBannerDTO.setIsBannerActive(!obj.isNull(KEY_IS_BANNER_ACTIVATED) ? obj.getBoolean(KEY_IS_BANNER_ACTIVATED) : false);
                        tabBannerDTO.setIsHyperlinkActive(!obj.isNull(KEY_IS_HYPER_LINK_AVAILABLE) ? obj.getBoolean(KEY_IS_HYPER_LINK_AVAILABLE) : false);
                        tabBannerDTO.setBannerActionURL(!obj.isNull(KEY_ACTION_URL) ? obj.getString(KEY_ACTION_URL) : "");
                        tabBannerDTO.setBannerCreated(!obj.isNull(KEY_BANNER_CREATED) ? Long.parseLong(obj.getString(KEY_BANNER_CREATED)) : 0);
                        tabBannerDTO.setBannerModified(!obj.isNull(KEY_BANNER_MODIFIED) ? Long.parseLong(obj.getString(KEY_BANNER_MODIFIED)) : 0);
                    }
            }
            return tabBannerDTO;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getStringFromInputStream(InputStream is)
            throws IOException {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    public String doGet(String url) {

        try {

            Log.d("URL", url);
            BasicHttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 600000);
            HttpConnectionParams.setSoTimeout(httpParameters, 600000);

            // create HttpClient
            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            InputStream inputStream = null;
            // make GET request to the given URL
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Accept", "application/json"); // or
            // application/jsonrequest
            httpget.setHeader("Content-Type", "application/json");
            httpget.setHeader("appID", String.valueOf(GlobalConstants.APP_ID));
            httpget.setHeader("appVersion", GlobalConstants.APP_VERSION);
            httpget.setHeader("deviceType", GlobalConstants.DEVICE_TYPE);
            try {
                HttpResponse httpResponse = httpClient.execute(httpget);
                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            } catch (ConnectTimeoutException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // convert inputstream to string
            if (inputStream != null) {
                try {
                    String result = getStringFromInputStream(inputStream);
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

