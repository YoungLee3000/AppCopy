package com.nlscan.android.appcopy;

import java.util.List;

public class TitleInfo {

    private String titleName;
    private List<ContentInfo> contentList;

    public TitleInfo(String titleName, List<ContentInfo> contentList) {
        this.titleName = titleName;
        this.contentList = contentList;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public List<ContentInfo> getContentList() {
        return contentList;
    }

    public void setContentList(List<ContentInfo> contentList) {
        this.contentList = contentList;
    }
}
