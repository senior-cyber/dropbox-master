package com.senior.cyber.dropbox;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ArgDto {

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
    @SerializedName("mute")
    private boolean mute;

    @Expose
    @SerializedName("strict_conflict")
    private boolean strictConflict;

    @Expose
    @SerializedName("close")
    private boolean close;

    @Expose
    @SerializedName("cursor")
    private CursorDto cursor;

    @Expose
    @SerializedName("commit")
    private CommitDto commit;

    public CommitDto getCommit() {
        return commit;
    }

    public void setCommit(CommitDto commit) {
        this.commit = commit;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public CursorDto getCursor() {
        return cursor;
    }

    public void setCursor(CursorDto cursor) {
        this.cursor = cursor;
    }

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
