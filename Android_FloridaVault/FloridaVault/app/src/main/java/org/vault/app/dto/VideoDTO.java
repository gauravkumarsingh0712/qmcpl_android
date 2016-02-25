package org.vault.app.dto;

import java.io.Serializable;

/**
 * Created by aqeeb.pathan on 17-06-2015.
 */
public class VideoDTO implements Serializable {

    private long videoId;
    private String videoName;
    private String videoShortDescription;
    private String videoLongDescription;
    private String videoShortUrl;
    private String videoLongUrl;
    private String videoThumbnailUrl;
    private String videoStillUrl;
    private String videoWideStillUrl;
    private String videoCoverUrl;
    private String videoBadgeUrl;
    private long videoDuration;
    private String videoTags;
    private boolean videoIsFavorite;
    private int videoIndex;
    private long playlistId;
    private String playlistName;
    private String playlistThumbnailUrl;
    private String playlistShortDescription;
    private String playlistLongDescription;
    private String playlistTags;
    private String playlistReferenceId;
    private String videoSocialUrl;

    public long getVideoId() {
        return videoId;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoShortDescription() {
        return videoShortDescription;
    }

    public void setVideoShortDescription(String videoShortDescription) {
        this.videoShortDescription = videoShortDescription;
    }

    public String getVideoLongDescription() {
        return videoLongDescription;
    }

    public void setVideoLongDescription(String videoLongDescription) {
        this.videoLongDescription = videoLongDescription;
    }

    public String getVideoShortUrl() {
        return videoShortUrl;
    }

    public void setVideoShortUrl(String videoShortUrl) {
        this.videoShortUrl = videoShortUrl;
    }

    public String getVideoLongUrl() {
        return videoLongUrl;
    }

    public void setVideoLongUrl(String videoLongUrl) {
        this.videoLongUrl = videoLongUrl;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public void setVideoThumbnailUrl(String videoThumbnailUrl) {
        this.videoThumbnailUrl = videoThumbnailUrl;
    }

    public String getVideoStillUrl() {
        return videoStillUrl;
    }

    public void setVideoStillUrl(String videoStillUrl) {
        this.videoStillUrl = videoStillUrl;
    }

    public String getVideoWideStillUrl() {
        return videoWideStillUrl;
    }

    public void setVideoWideStillUrl(String videoWideStillUrl) {
        this.videoWideStillUrl = videoWideStillUrl;
    }

    public String getVideoCoverUrl() {
        return videoCoverUrl;
    }

    public void setVideoCoverUrl(String videoCoverUrl) {
        this.videoCoverUrl = videoCoverUrl;
    }

    public String getVideoBadgeUrl() {
        return videoBadgeUrl;
    }

    public void setVideoBadgeUrl(String videoBadgeUrl) {
        this.videoBadgeUrl = videoBadgeUrl;
    }

    public long getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getVideoTags() {
        return videoTags;
    }

    public void setVideoTags(String videoTags) {
        this.videoTags = videoTags;
    }

    public boolean isVideoIsFavorite() {
        return videoIsFavorite;
    }

    public void setVideoIsFavorite(boolean videoIsFavorite) {
        this.videoIsFavorite = videoIsFavorite;
    }

    public int getVideoIndex() {
        return videoIndex;
    }

    public void setVideoIndex(int videoIndex) {
        this.videoIndex = videoIndex;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistThumbnailUrl() {
        return playlistThumbnailUrl;
    }

    public void setPlaylistThumbnailUrl(String playlistThumbnailUrl) {
        this.playlistThumbnailUrl = playlistThumbnailUrl;
    }

    public String getPlaylistTags() {
        return playlistTags;
    }

    public void setPlaylistTags(String playlistTags) {
        this.playlistTags = playlistTags;
    }

    public String getPlaylistReferenceId() {
        return playlistReferenceId;
    }

    public void setPlaylistReferenceId(String playlistReferenceId) {
        this.playlistReferenceId = playlistReferenceId;
    }

    public String getPlaylistShortDescription() {
        return playlistShortDescription;
    }

    public void setPlaylistShortDescription(String playlistShortDescription) {
        this.playlistShortDescription = playlistShortDescription;
    }

    public String getPlaylistLongDescription() {
        return playlistLongDescription;
    }

    public void setPlaylistLongDescription(String playlistLongDescription) {
        this.playlistLongDescription = playlistLongDescription;
    }

    public String getVideoSocialUrl() {
        return videoSocialUrl;
    }

    public void setVideoSocialUrl(String videoSocialUrl) {
        this.videoSocialUrl = videoSocialUrl;
    }

}
