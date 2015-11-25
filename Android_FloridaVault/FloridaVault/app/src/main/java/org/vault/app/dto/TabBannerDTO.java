package org.vault.app.dto;

/**
 * Created by aqeeb.pathan on 09-11-2015.
 */
public class TabBannerDTO {
    //Tab Properties
    private String tabName;
    private long tabId;
    private long tabPosition;
    private long lastServerTabUpdate;

    //Banner Properties
    private String bannerUrl;
    private long bannerId;
    private long lastBannerUpdate;
    private boolean isBannerActivated;
    private boolean isHyperLinkAvailable;
    private String actionUrl;

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public long getTabId() {
        return tabId;
    }

    public void setTabId(long tabId) {
        this.tabId = tabId;
    }

    public long getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(long tabPosition) {
        this.tabPosition = tabPosition;
    }

    public long getLastServerTabUpdate() {
        return lastServerTabUpdate;
    }

    public void setLastServerTabUpdate(long lastServerTabUpdate) {
        this.lastServerTabUpdate = lastServerTabUpdate;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public long getBannerId() {
        return bannerId;
    }

    public void setBannerId(long bannerId) {
        this.bannerId = bannerId;
    }

    public long getLastBannerUpdate() {
        return lastBannerUpdate;
    }

    public void setLastBannerUpdate(long lastBannerUpdate) {
        this.lastBannerUpdate = lastBannerUpdate;
    }

    public boolean isBannerActivated() {
        return isBannerActivated;
    }

    public void setIsBannerActivated(boolean isBannerActivated) {
        this.isBannerActivated = isBannerActivated;
    }

    public boolean isHyperLinkAvailable() {
        return isHyperLinkAvailable;
    }

    public void setIsHyperLinkAvailable(boolean isHyperLinkAvailable) {
        this.isHyperLinkAvailable = isHyperLinkAvailable;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}