package com.nlscan.android.appcopy;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import java.util.Objects;

public class ContentInfo {

    private String appName;
    private Drawable icon;
    private String path;
    private String packageName;

    public ContentInfo(String appName, Drawable icon, String path, String packageName) {
        this.appName = appName;
        this.icon = icon;
        this.path = path;
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        //自反性
        if (this == obj) return true;
        //任何对象不等于null，比较是否为同一类型
        if (!(obj instanceof ContentInfo)) return false;
        //强制类型转换
        ContentInfo contentInfo = (ContentInfo) obj;
        //比较属性值
        return Objects.equals(getAppName(),contentInfo.appName) &&
                Objects.equals(getPackageName(), contentInfo.packageName);

    }
}
