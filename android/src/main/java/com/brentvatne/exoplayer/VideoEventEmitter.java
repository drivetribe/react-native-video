package com.brentvatne.exoplayer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class VideoEventEmitter {

    private int viewId;
    private ReactContext reactContext;

    public VideoEventEmitter(int viewId, ReactContext reactContext) {
        this.viewId = viewId;
        this.reactContext = reactContext;
    }

    public enum Events {
        EVENT_PREPARE("onVideoPrepare"),
        EVENT_ERROR("onVideoError"),
        EVENT_PROGRESS("onVideoProgress"),
        EVENT_SEEK("onVideoSeek"),
        EVENT_END("onVideoEnd"),
        EVENT_BUFFERING("onBuffering"),
        EVENT_PREPARING("onPreparing"),
        EVENT_IDLE("onIdle"),
        EVENT_VIDEO_SIZE_CHANGED("onVideoSizeChanged"),
        EVENT_READY("onVideoReady");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }

    }

    public static final String EVENT_PROP_DURATION = "duration";
    public static final String EVENT_PROP_PLAYABLE_DURATION = "playableDuration";
    public static final String EVENT_PROP_CURRENT_TIME = "currentTime";
    public static final String EVENT_PROP_SEEK_TIME = "seekTime";
    public static final String EVENT_PROP_NATURALSIZE = "naturalSize";
    public static final String EVENT_PROP_WIDTH = "width";
    public static final String EVENT_PROP_HEIGHT = "height";
    public static final String EVENT_PROP_ORIENTATION = "orientation";

    public static final String EVENT_PROP_ERROR = "error";
    public static final String EVENT_PROP_ERROR_STRING = "errorString";

    public void loadStart() {
        receiveEvent(Events.EVENT_PREPARE, null);
    }

    public void ready(double duration, double currentPosition) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_DURATION, duration / 1000.0);
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000.0);

        receiveEvent(Events.EVENT_READY, event);
    }

    public void onVideoSizeChanged(int width, int height) {
        WritableMap naturalSize = Arguments.createMap();
        naturalSize.putInt(EVENT_PROP_WIDTH, width);
        naturalSize.putInt(EVENT_PROP_HEIGHT, height);
        if (width > height) {
            naturalSize.putString(EVENT_PROP_ORIENTATION, "landscape");
        } else {
            naturalSize.putString(EVENT_PROP_ORIENTATION, "portrait");
        }
        WritableMap event = Arguments.createMap();
        event.putMap(EVENT_PROP_NATURALSIZE, naturalSize);
        receiveEvent(Events.EVENT_VIDEO_SIZE_CHANGED, event);
    }

    public void onProgressChanged(double currentPostition, double bufferedDuration) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPostition / 1000.0);
        event.putDouble(EVENT_PROP_PLAYABLE_DURATION, bufferedDuration / 1000.0); //TODO:mBufferUpdateRunnable
        receiveEvent(Events.EVENT_PROGRESS, event);
    }

    public void seek(double currentPostition, double seekTime) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPostition / 1000.0);
        event.putDouble(EVENT_PROP_SEEK_TIME, seekTime / 1000.0);
        receiveEvent(Events.EVENT_SEEK, event);
    }

    public void buffering() {
        receiveEvent(Events.EVENT_BUFFERING, null);
    }

    public void preparing() {
        receiveEvent(Events.EVENT_PREPARING, null);
    }

    public void idle() {
        receiveEvent(Events.EVENT_IDLE, null);
    }

    public void end() {
        receiveEvent(Events.EVENT_END, null);
    }

    public void error(String errorString, Exception extra) {
        WritableMap error = Arguments.createMap();
        error.putString(EVENT_PROP_ERROR_STRING, errorString);
        WritableMap event = Arguments.createMap();
        event.putMap(EVENT_PROP_ERROR, error);
        receiveEvent(Events.EVENT_ERROR, event);
    }

    private void receiveEvent(Events type, WritableMap event) {
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(viewId, type.toString(), event);
    }
}
