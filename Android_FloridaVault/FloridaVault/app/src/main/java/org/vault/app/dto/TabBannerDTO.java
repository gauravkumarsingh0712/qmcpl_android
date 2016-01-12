package org.vault.app.dto;

import java.io.Serializable;

/**
 * Created by aqeeb.pathan on 09-11-2015.
 */
public class TabBannerDTO implements Serializable {

    //Tab Properties
    public long tabId;
    public String tabName;
    public long tabIndexPosition;
    public String tabDisplayType;
    public String tabKeyword;
    public long tabCreated;
    public long tabModified;
    public long tabDataCreated;
    public long tabDataModified;

    //Banner Properties
    public long tabBannerId;
    public String tabBannerName;
    public boolean isBannerActive;
    public boolean isHyperlinkActive;
    public String bannerActionURL;
    public long bannerCreated;
    public long bannerModified;
    public String bannerURL;

    public long getTabId() {
        return tabId;
    }

    public void setTabId(long tabId) {
        this.tabId = tabId;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public long getTabIndexPosition() {
        return tabIndexPosition;
    }

    public void setTabIndexPosition(long tabIndexPosition) {
        this.tabIndexPosition = tabIndexPosition;
    }

    public String getTabDisplayType() {
        return tabDisplayType;
    }

    public void setTabDisplayType(String tabDisplayType) {
        this.tabDisplayType = tabDisplayType;
    }

    public String getTabKeyword() {
        return tabKeyword;
    }

    public void setTabKeyword(String tabKeyword) {
        this.tabKeyword = tabKeyword;
    }

    public long getTabCreated() {
        return tabCreated;
    }

    public void setTabCreated(long tabCreated) {
        this.tabCreated = tabCreated;
    }

    public long getTabModified() {
        return tabModified;
    }

    public void setTabModified(long tabModified) {
        this.tabModified = tabModified;
    }

    public long getTabDataCreated() {
        return tabDataCreated;
    }

    public void setTabDataCreated(long tabDataCreated) {
        this.tabDataCreated = tabDataCreated;
    }

    public long getTabDataModified() {
        return tabDataModified;
    }

    public void setTabDataModified(long tabDataModified) {
        this.tabDataModified = tabDataModified;
    }

    public long getTabBannerId() {
        return tabBannerId;
    }

    public void setTabBannerId(long tabBannerId) {
        this.tabBannerId = tabBannerId;
    }

    public String getTabBannerName() {
        return tabBannerName;
    }

    public void setTabBannerName(String tabBannerName) {
        this.tabBannerName = tabBannerName;
    }

    public boolean isBannerActive() {
        return isBannerActive;
    }

    public void setIsBannerActive(boolean isBannerActive) {
        this.isBannerActive = isBannerActive;
    }

    public boolean isHyperlinkActive() {
        return isHyperlinkActive;
    }

    public void setIsHyperlinkActive(boolean isHyperlinkActive) {
        this.isHyperlinkActive = isHyperlinkActive;
    }

    public String getBannerActionURL() {
        return bannerActionURL;
    }

    public void setBannerActionURL(String bannerActionURL) {
        this.bannerActionURL = bannerActionURL;
    }

    public long getBannerCreated() {
        return bannerCreated;
    }

    public void setBannerCreated(long bannerCreated) {
        this.bannerCreated = bannerCreated;
    }

    public long getBannerModified() {
        return bannerModified;
    }

    public void setBannerModified(long bannerModified) {
        this.bannerModified = bannerModified;
    }

    public String getBannerURL() {
        return bannerURL;
    }

    public void setBannerURL(String bannerURL) {
        this.bannerURL = bannerURL;
    }
}