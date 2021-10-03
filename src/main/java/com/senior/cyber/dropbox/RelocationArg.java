package com.senior.cyber.dropbox;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RelocationArg {

    @Expose
    @SerializedName("from_path")
    private String fromPath;

    @Expose
    @SerializedName("to_path")
    private String toPath;

    @Expose
    @SerializedName("allow_shared_folder")
    private boolean allowSharedFolder;

    @Expose
    @SerializedName("autorename")
    private boolean autoRename;

    @Expose
    @SerializedName("allow_ownership_transfer")
    private boolean allowOwnershipTransfer;

    public String getFromPath() {
        return fromPath;
    }

    public void setFromPath(String fromPath) {
        this.fromPath = fromPath;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public boolean isAllowSharedFolder() {
        return allowSharedFolder;
    }

    public void setAllowSharedFolder(boolean allowSharedFolder) {
        this.allowSharedFolder = allowSharedFolder;
    }

    public boolean isAutoRename() {
        return autoRename;
    }

    public void setAutoRename(boolean autoRename) {
        this.autoRename = autoRename;
    }

    public boolean isAllowOwnershipTransfer() {
        return allowOwnershipTransfer;
    }

    public void setAllowOwnershipTransfer(boolean allowOwnershipTransfer) {
        this.allowOwnershipTransfer = allowOwnershipTransfer;
    }

}
