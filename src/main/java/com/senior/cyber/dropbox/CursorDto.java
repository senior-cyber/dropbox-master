package com.senior.cyber.dropbox;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CursorDto {

    @Expose
    @SerializedName("session_id")
    private String sessionId;

    @Expose
    @SerializedName("offset")
    private long offset;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

}
