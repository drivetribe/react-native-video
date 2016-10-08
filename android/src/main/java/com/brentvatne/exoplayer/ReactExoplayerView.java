package com.brentvatne.exoplayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.brentvatne.react.R;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelections;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.yqritc.scalablevideoview.ScalableType;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class ReactExoplayerView extends FrameLayout implements
        LifecycleEventListener,
        ExoPlayer.EventListener,
        TrackSelector.EventListener<MappingTrackSelector.MappedTrackInfo>,
        PlaybackControlView.VisibilityListener {

    private static final String TAG = "ReactExoplayerView";

    public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
    public static final String DRM_LICENSE_URL = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";

    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";
    public static final String ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW";

    public static final String EXTENSION_EXTRA = "extension";
    public static final String ACTION_VIEW_LIST =
            "com.google.android.exoplayer.demo.action.VIEW_LIST";
    public static final String URI_LIST_EXTRA = "uri_list";

    public static final String EXTENSION_LIST_EXTRA = "extension_list";
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    private static final int SHOW_PROGRESS = 1;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private final VideoEventEmitter eventEmmiter;

    private Handler mainHandler;
    private EventLogger eventLogger;
    private SimpleExoPlayerView simpleExoPlayerView;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private MappingTrackSelector trackSelector;
    private boolean playerNeedsSource;

    private boolean shouldAutoPlay;
    private boolean shouldRestorePosition;
    private int playerWindow;
    private long playerPosition;

    private final Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
                    if (player != null
                            && player.getPlaybackState() == ExoPlayer.STATE_READY
//                            && player.isLoading()
                            ) {
                        long pos = player.getCurrentPosition();
                        eventEmmiter.onProgressChanged(pos, player.getBufferedPercentage());
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    public ReactExoplayerView(ThemedReactContext context) {
        super(context);
        createViews();
        eventEmmiter = new VideoEventEmitter(getId(), context);
    }

    private void createViews() {
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);

        shouldAutoPlay = true;
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        simpleExoPlayerView = new SimpleExoPlayerView(getContext());
        simpleExoPlayerView.setControllerVisibilityListener(this);
        simpleExoPlayerView.requestFocus();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    // LifecycleEventListener implementation

    @Override
    public void onHostResume() {
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onHostPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onHostDestroy() {
        releasePlayer();
    }

    public void cleanupMediaPlayerResources() {
        releasePlayer();
    }


    // PlaybackControlView.VisibilityListener implementation

    @Override
    public void onVisibilityChange(int visibility) {
        // TODO: should we use this?
    }

    // Internal methods

    private void initializePlayer() {
        Intent intent = new Intent();
        if (player == null) {
            boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
            UUID drmSchemeUuid = intent.hasExtra(DRM_SCHEME_UUID_EXTRA)
                    ? UUID.fromString(intent.getStringExtra(DRM_SCHEME_UUID_EXTRA)) : null;
            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            if (drmSchemeUuid != null) {
                String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL);
                String[] keyRequestPropertiesArray = intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES);
                Map<String, String> keyRequestProperties;
                if (keyRequestPropertiesArray == null || keyRequestPropertiesArray.length < 2) {
                    keyRequestProperties = null;
                } else {
                    keyRequestProperties = new HashMap<>();
                    for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                        keyRequestProperties.put(keyRequestPropertiesArray[i],
                                keyRequestPropertiesArray[i + 1]);
                    }
                }
                try {
                    drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl,
                            keyRequestProperties);
                } catch (UnsupportedDrmException e) {
                    int errorStringId = Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                            : (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                            ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
                    showToast(errorStringId);
                    return;
                }
            }

            eventLogger = new EventLogger();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
            trackSelector.addListener(this);
            trackSelector.addListener(eventLogger);
            player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, new DefaultLoadControl(),
                    drmSessionManager, preferExtensionDecoders);
            player.addListener(this);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setId3Output(eventLogger);
            simpleExoPlayerView.setPlayer(player);
            if (shouldRestorePosition) {
                if (playerPosition == C.TIME_UNSET) {
                    player.seekToDefaultPosition(playerWindow);
                } else {
                    player.seekTo(playerWindow, playerPosition);
                }
            }
            player.setPlayWhenReady(shouldAutoPlay);
            playerNeedsSource = true;
        }
        if (playerNeedsSource) {
            String action = intent.getAction();
            Uri[] uris;
            String[] extensions;
            if (ACTION_VIEW.equals(action)) {
                uris = new Uri[]{intent.getData()};
                extensions = new String[]{intent.getStringExtra(EXTENSION_EXTRA)};
            } else if (ACTION_VIEW_LIST.equals(action)) {
                String[] uriStrings = intent.getStringArrayExtra(URI_LIST_EXTRA);
                uris = new Uri[uriStrings.length];
                for (int i = 0; i < uriStrings.length; i++) {
                    uris[i] = Uri.parse(uriStrings[i]);
                }
                extensions = intent.getStringArrayExtra(EXTENSION_LIST_EXTRA);
                if (extensions == null) {
                    extensions = new String[uriStrings.length];
                }
            } else {
                showToast(getResources().getString(R.string.unexpected_intent_action, action));
                return;
            }
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            player.prepare(mediaSource, !shouldRestorePosition);
            playerNeedsSource = false;
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                : uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
                                                                           String licenseUrl, Map<String, String> keyRequestProperties) throws UnsupportedDrmException {
        if (Util.SDK_INT < 18) {
            return null;
        }
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(false), keyRequestProperties);
        return new StreamingDrmSessionManager<>(uuid,
                FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
    }

    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            shouldRestorePosition = false;
            Timeline timeline = player.getCurrentTimeline();
            if (timeline != null) {
                playerWindow = player.getCurrentWindowIndex();
                Timeline.Window window = timeline.getWindow(playerWindow, new Timeline.Window());
                if (!window.isDynamic) {
                    shouldRestorePosition = true;
                    playerPosition = window.isSeekable ? player.getCurrentPosition() : C.TIME_UNSET;
                }
            }
            player.release();
            player = null;
            trackSelector = null;
            eventLogger = null;
        }
        progressHandler.removeMessages(SHOW_PROGRESS);
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return DataSourceUtil.buildDataSourceFactory(getContext(), useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return DataSourceUtil.buildHttpDataSourceFactory(getContext(), useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // ExoPlayer.EventListener implementation

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String text = "onStateChanged: playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                eventEmmiter.buffering();
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                eventEmmiter.end();
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                eventEmmiter.idle();
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                eventEmmiter.ready(player.getDuration(), player.getCurrentPosition());
                playerReady();
                break;
            default:
                text += "unknown";
                break;
        }
        Log.d(TAG, text);
    }

    private void playerReady() {
        progressHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    @Override
    public void onPositionDiscontinuity() {
        // Do nothing.
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = getResources().getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getResources().getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getResources().getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getResources().getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString);
        }
        playerNeedsSource = true;
    }

    // MappingTrackSelector.EventListener implementation

    @Override
    public void onTrackSelectionsChanged(TrackSelections<? extends MappingTrackSelector.MappedTrackInfo> trackSelections) {
        MappingTrackSelector.MappedTrackInfo trackInfo = trackSelections.info;
        if (trackInfo.hasOnlyUnplayableTracks(C.TRACK_TYPE_VIDEO)) {
            showToast(R.string.error_unsupported_video);
        }
        if (trackInfo.hasOnlyUnplayableTracks(C.TRACK_TYPE_AUDIO)) {
            showToast(R.string.error_unsupported_audio);
        }
    }

    private void showToast(int messageId) {
        showToast(getResources().getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    // View Manager methods

    public void setSrc(final String uriString, final String type, final boolean isNetwork, final boolean isAsset) {

    }

    public void setSrc(final String uriString, final String type, final boolean isNetwork, final boolean isAsset, final int expansionMainVersion, final int expansionPatchVersion) {

    }

    public void setResizeModeModifier(ScalableType scalableType) {

    }

    public void setRepeatModifier(boolean repeat) {

    }


    public void setPausedModifier(boolean paused) {

    }


    public void setMutedModifier(boolean muted) {

    }


    public void setVolumeModifier(float volume) {

    }

    public void seekTo(int round) {

    }


    public void setRateModifier(float rate) {

    }


    public void setPlayInBackground(boolean playInBackground) {

    }

    public void setControls(boolean controls) {

    }
}
