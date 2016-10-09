package com.brentvatne.exoplayer;

import com.brentvatne.react.VideoViewEvents;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.yqritc.scalablevideoview.ScalableType;

import java.util.Map;

import javax.annotation.Nullable;

public class ReactExoplayerViewManager extends ViewGroupManager<ReactExoplayerView> {

    private static final String REACT_CLASS = "RCTVideo";

    private static final String PROP_SRC = "src";
    private static final String PROP_SRC_URI = "uri";
    private static final String PROP_SRC_TYPE = "type";
    private static final String PROP_SRC_IS_NETWORK = "isNetwork";
    private static final String PROP_SRC_MAINVER = "mainVer";
    private static final String PROP_SRC_PATCHVER = "patchVer";
    private static final String PROP_SRC_IS_ASSET = "isAsset";
    private static final String PROP_RESIZE_MODE = "resizeMode";
    private static final String PROP_REPEAT = "repeat";
    private static final String PROP_PAUSED = "paused";
    private static final String PROP_MUTED = "muted";
    private static final String PROP_VOLUME = "volume";
    private static final String PROP_SEEK = "seek";
    private static final String PROP_RATE = "rate";
    private static final String PROP_PLAY_IN_BACKGROUND = "playInBackground";
    private static final String PROP_CONTROLS = "controls";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactExoplayerView createViewInstance(ThemedReactContext themedReactContext) {
        return new ReactExoplayerView(themedReactContext);
    }

    @Override
    public void onDropViewInstance(ReactExoplayerView view) {
        view.cleanupMediaPlayerResources();
    }

    @Override
    public @Nullable Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        for (VideoViewEvents event : VideoViewEvents.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
        }
        return builder.build();
    }

    @Override
    public @Nullable Map<String, Object> getExportedViewConstants() {
        return MapBuilder.<String, Object>of(
                "ScaleNone", Integer.toString(ScalableType.LEFT_TOP.ordinal()),
                "ScaleToFill", Integer.toString(ScalableType.FIT_XY.ordinal()),
                "ScaleAspectFit", Integer.toString(ScalableType.FIT_CENTER.ordinal()),
                "ScaleAspectFill", Integer.toString(ScalableType.CENTER_CROP.ordinal())
        );
    }

    @ReactProp(name = PROP_SRC)
    public void setSrc(final ReactExoplayerView videoView, @Nullable ReadableMap src) {
        videoView.setSrc(
                src.getString(PROP_SRC_URI),
                src.getString(PROP_SRC_TYPE),
                src.getBoolean(PROP_SRC_IS_NETWORK),
                src.getBoolean(PROP_SRC_IS_ASSET)
        );
    }

    @ReactProp(name = PROP_RESIZE_MODE)
    public void setResizeMode(final ReactExoplayerView videoView, final String resizeModeOrdinalString) {
//        videoView.setResizeModeModifier();
    }

    @ReactProp(name = PROP_REPEAT, defaultBoolean = false)
    public void setRepeat(final ReactExoplayerView videoView, final boolean repeat) {
        videoView.setRepeatModifier(repeat);
    }

    @ReactProp(name = PROP_PAUSED, defaultBoolean = false)
    public void setPaused(final ReactExoplayerView videoView, final boolean paused) {
        videoView.setPausedModifier(paused);
    }

    @ReactProp(name = PROP_MUTED, defaultBoolean = false)
    public void setMuted(final ReactExoplayerView videoView, final boolean muted) {
        videoView.setMutedModifier(muted);
    }

    @ReactProp(name = PROP_VOLUME, defaultFloat = 1.0f)
    public void setVolume(final ReactExoplayerView videoView, final float volume) {
        videoView.setVolumeModifier(volume);
    }

    @ReactProp(name = PROP_SEEK)
    public void setSeek(final ReactExoplayerView videoView, final float seek) {
        videoView.seekTo(Math.round(seek * 1000.0f));
    }

    @ReactProp(name = PROP_RATE)
    public void setRate(final ReactExoplayerView videoView, final float rate) {
        videoView.setRateModifier(rate);
    }

    @ReactProp(name = PROP_PLAY_IN_BACKGROUND, defaultBoolean = false)
    public void setPlayInBackground(final ReactExoplayerView videoView, final boolean playInBackground) {
        videoView.setPlayInBackground(playInBackground);
    }

}
