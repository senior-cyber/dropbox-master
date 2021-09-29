package com.senior.cyber.dropbox;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CommitDto {

    @Expose
    @SerializedName("path")
    private String path;

    @Expose
    @SerializedName("mode")
    private String mode;

    @Expose
    @SerializedName("autorename")
    private boolean autoRename;

    @Expose
    @SerializedName("client_modified")
    private String clientModified;

    @Expose
    @SerializedName("mute")
    private boolean mute;

    @Expose
    @SerializedName("strict_conflict")
    private boolean strictConflict;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isAutoRename() {
        return autoRename;
    }

    public void setAutoRename(boolean autoRename) {
        this.autoRename = autoRename;
    }

    public String getClientModified() {
        return clientModified;
    }

    public void setClientModified(String clientModified) {
        this.clientModified = clientModified;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isStrictConflict() {
        return strictConflict;
    }

    public void setStrictConflict(boolean strictConflict) {
        this.strictConflict = strictConflict;
    }

}
