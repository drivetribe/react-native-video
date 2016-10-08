package com.brentvatne.react;

public enum VideoViewEvents {
    EVENT_LOAD_START("onVideoLoadStart"),
    EVENT_LOAD("onVideoLoad"),
    EVENT_ERROR("onVideoError"),
    EVENT_PROGRESS("onVideoProgress"),
    EVENT_SEEK("onVideoSeek"),
    EVENT_END("onVideoEnd"),
    EVENT_STALLED("onPlaybackStalled"),
    EVENT_RESUME("onPlaybackResume"),
    EVENT_READY_FOR_DISPLAY("onReadyForDisplay");

    private final String mName;

    VideoViewEvents(final String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }
}
