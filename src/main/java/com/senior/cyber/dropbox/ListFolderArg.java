package com.senior.cyber.dropbox;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ListFolderArg {

    @Expose
    @SerializedName("path")
    private String path;

    @Expose
    @SerializedName("recursive")
    private boolean recursive;

    @Expose
    @SerializedName("include_media_info")
    private boolean includeMediaInfo;

    @Expose
    @SerializedName("include_deleted")
    private boolean includeDeleted;

    @Expose
    @SerializedName("include_has_explicit_shared_members")
    private boolean includeHasExplicitSharedMembers;

    @Expose
    @SerializedName("include_mounted_folders")
    private boolean includeMountedFolders;

    @Expose
    @SerializedName("include_non_downloadable_files")
    private boolean includeNonDownloadableFiles;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isIncludeMediaInfo() {
        return includeMediaInfo;
    }

    public void setIncludeMediaInfo(boolean includeMediaInfo) {
        this.includeMediaInfo = includeMediaInfo;
    }

    public boolean isIncludeDeleted() {
        return includeDeleted;
    }

    public void setIncludeDeleted(boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }

    public boolean isIncludeHasExplicitSharedMembers() {
        return includeHasExplicitSharedMembers;
    }

    public void setIncludeHasExplicitSharedMembers(boolean includeHasExplicitSharedMembers) {
        this.includeHasExplicitSharedMembers = includeHasExplicitSharedMembers;
    }

    public boolean isIncludeMountedFolders() {
        return includeMountedFolders;
    }

    public void setIncludeMountedFolders(boolean includeMountedFolders) {
        this.includeMountedFolders = includeMountedFolders;
    }

    public boolean isIncludeNonDownloadableFiles() {
        return includeNonDownloadableFiles;
    }

    public void setIncludeNonDownloadableFiles(boolean includeNonDownloadableFiles) {
        this.includeNonDownloadableFiles = includeNonDownloadableFiles;
    }

}
