package com.brentvatne.exoplayer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

class VideoEventEmitter {

    private final int viewId;
    private final ReactContext reactContext;

    VideoEventEmitter(int viewId, ReactContext reactContext) {
        this.viewId = viewId;
        this.reactContext = reactContext;
    }

    private enum Events {
        EVENT_LOAD_START("onVideoLoadStart"),
        EVENT_LOAD("onVideoLoad"),
        EVENT_ERROR("onVideoError"),
        EVENT_PROGRESS("onVideoProgress"),
        EVENT_SEEK("onVideoSeek"),
        EVENT_END("onVideoEnd"),
        EVENT_STALLED("onPlaybackStalled"),
        EVENT_RESUME("onPlaybackResume"),
        EVENT_READY_FOR_DISPLAY("onReadyForDisplay"),
        EVENT_BUFFER("onBuffer"),
        EVENT_IDLE("onIdle");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    private static final String EVENT_PROP_FAST_FORWARD = "canPlayFastForward";
    private static final String EVENT_PROP_SLOW_FORWARD = "canPlaySlowForward";
    private static final String EVENT_PROP_SLOW_REVERSE = "canPlaySlowReverse";
    private static final String EVENT_PROP_REVERSE = "canPlayReverse";
    private static final String EVENT_PROP_STEP_FORWARD = "canStepForward";
    private static final String EVENT_PROP_STEP_BACKWARD = "canStepBackward";

    private static final String EVENT_PROP_DURATION = "duration";
    private static final String EVENT_PROP_PLAYABLE_DURATION = "playableDuration";
    private static final String EVENT_PROP_CURRENT_TIME = "currentTime";
    private static final String EVENT_PROP_SEEK_TIME = "seekTime";
    private static final String EVENT_PROP_NATURAL_SIZE = "naturalSize";
    private static final String EVENT_PROP_WIDTH = "width";
    private static final String EVENT_PROP_HEIGHT = "height";
    private static final String EVENT_PROP_ORIENTATION = "orientation";

    private static final String EVENT_PROP_ERROR = "error";
    private static final String EVENT_PROP_ERROR_STRING = "errorString";
    private static final String EVENT_PROP_ERROR_EXCEPTION = "";
    private static final String EVENT_PROP_WHAT = "what";
    private static final String EVENT_PROP_EXTRA = "extra";

    void loadStart() {
        receiveEvent(Events.EVENT_LOAD_START, null); // TODO: do we need the src?
    }

    void ready(double duration, double currentPosition) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_DURATION, duration / 1000D);
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000D);
//        event.putMap(EVENT_PROP_NATURAL_SIZE, naturalSize);

//        WritableMap naturalSize = Arguments.createMap();
//        naturalSize.putInt(EVENT_PROP_WIDTH, mp.getVideoWidth());
//        naturalSize.putInt(EVENT_PROP_HEIGHT, mp.getVideoHeight());
//        if (mp.getVideoWidth() > mp.getVideoHeight()) {
//            naturalSize.putString(EVENT_PROP_ORIENTATION, "landscape");
//        } else {
//            naturalSize.putString(EVENT_PROP_ORIENTATION, "portrait");
//        }

        // TODO: Actually check if you can.
        event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
        event.putBoolean(EVENT_PROP_SLOW_FORWARD, true);
        event.putBoolean(EVENT_PROP_SLOW_REVERSE, true);
        event.putBoolean(EVENT_PROP_REVERSE, true);
        event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
        event.putBoolean(EVENT_PROP_STEP_BACKWARD, true);
        event.putBoolean(EVENT_PROP_STEP_FORWARD, true);

        receiveEvent(Events.EVENT_LOAD, event);
    }

    void onProgressChanged(double currentPosition, double bufferedDuration) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000.0);
        event.putDouble(EVENT_PROP_PLAYABLE_DURATION, bufferedDuration / 1000.0); //TODO:mBufferUpdateRunnable
        receiveEvent(Events.EVENT_PROGRESS, event);
    }

    void seek(long currentPosition, long seekTime) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000.0);
        event.putDouble(EVENT_PROP_SEEK_TIME, seekTime / 1000.0);
        receiveEvent(Events.EVENT_SEEK, event);
    }

    void buffering() {
        receiveEvent(Events.EVENT_BUFFER, null);
    }

    void idle() {
        receiveEvent(Events.EVENT_IDLE, null);
    }

    void end() {
        receiveEvent(Events.EVENT_END, null);
    }

    void error(String errorString, Exception exception) {
        WritableMap error = Arguments.createMap();
        error.putString(EVENT_PROP_ERROR_STRING, errorString);
        error.putString(EVENT_PROP_ERROR_EXCEPTION, exception.getMessage());
        WritableMap event = Arguments.createMap();
        event.putMap(EVENT_PROP_ERROR, error);
        receiveEvent(Events.EVENT_ERROR, event);
    }

    private void receiveEvent(Events type, WritableMap event) {
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(viewId, type.toString(), event);
    }
}
