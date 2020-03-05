/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rnfloatingvideowidget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class IjkVideoView extends FrameLayout implements MediaController.MediaPlayerControl {
    private String TAG = "IjkVideoView";
    // settable by the client
    private Uri mUri;
    private Map<String, String> mHeaders;
    private String mUserAgent;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private IRenderView.ISurfaceHolder mSurfaceHolder = null;
    private IMediaPlayer mMediaPlayer = null;
    // private int         mAudioSession;
    private IjkMediaPlayer mIjkMediaPlayer = null;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mVideoRotationDegree;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private int mCurrentBufferPercentage;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mCanPause = true;
    private boolean mCanSeekBack = true;
    private boolean mCanSeekForward = true;
    private boolean mSubtitlesEnabled = false;
    private boolean mVideoDisabled = false;
    private boolean mAudioDisabled = false;
    private boolean mSoundtouch = true;

    private int mAudioSessionId = -1;
    private boolean mBackgroundPlayEnabled = false;

    private TextureRenderView renderView = new TextureRenderView(getContext());


    /**
     * Equalizer Props
     */

    private Equalizer mEqualizer;
    private Equalizer.Settings mEqualizerSettings;
    private boolean mEqualizerEnabled = false;

    private int mNumOfBands = -1;
    private int mNumOfPresets = -1;



    private PresetReverb mPresetReverb;
    private PresetReverb.Settings mPresetReverbSettings;

    private EnvironmentalReverb mEnvoirmentalReverb;
    private EnvironmentalReverb.Settings mEnvoirmentalReverbSettings;



    /** Subtitle rendering widget overlaid on top of the video. */
    //private RenderingWidget mSubtitleWidget;

    /**
     * Listener for changes to subtitle data, used to redraw when needed.
     */
    // private RenderingWidget.OnChangedListener mSubtitlesChangedListener;

    private Context mAppContext;
    private IRenderView mRenderView;
    private int mVideoSarNum;
    private int mVideoSarDen;

    private long mPrepareStartTime = 0;
    private long mPrepareEndTime = 0;

    private long mSeekStartTime = 0;
    private long mSeekEndTime = 0;

    private TextView subtitleDisplay;

    public IjkVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    public IjkVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public IjkVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IjkVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVideoView(context);
    }

    // REMOVED: onMeasure
    // REMOVED: onInitializeAccessibilityEvent
    // REMOVED: onInitializeAccessibilityNodeInfo
    // REMOVED: resolveAdjustedSize

    private void initVideoView(Context context) {
        mAppContext = context.getApplicationContext();

        if (mEqualizer != null) {
            mAudioSessionId = -1;
            unbindCustomEqualizer();
        }

        if (mSurfaceHolder == null) {
            mSurfaceHolder = renderView.getSurfaceHolder();
        }

        setRenderView(renderView);

        // REMOVED: getHolder().addCallback(mSHCallback);
        // REMOVED: getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        // REMOVED: mPendingSubtitleTracks = new Vector<Pair<InputStream, MediaFormat>>();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;

        setSubtitleDisplay(context, 16, null, "#ffffff", null);

        FrameLayout.LayoutParams layoutParams_txt = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        addView(subtitleDisplay, layoutParams_txt);
    }


    public void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            if (mMediaPlayer != null)
                mMediaPlayer.setDisplay(null);

            View renderUIView = mRenderView.getView();
            mRenderView.removeRenderCallback(mSHCallback);
            mRenderView = null;
            removeView(renderUIView);
        }

        if (renderView == null)
            return;

        mRenderView = renderView;

        View renderUIView = mRenderView.getView();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        renderUIView.setLayoutParams(lp);
        addView(renderUIView);

        mRenderView.addRenderCallback(mSHCallback);
        mRenderView.setVideoRotation(mVideoRotationDegree);
    }


    /**
     * Changes the style of subtitle display
     *
     * @param textSize        size of subtitle text
     * @param position        position of subtitle text, can be top, bottom, left, right or center.
     * @param color           color of subtitle text
     * @param backgroundColor backgroundColor of subtitle text.
     */

    public void setSubtitleDisplay(Context context, int textSize, @Nullable String position, String color, @Nullable String backgroundColor) {

        subtitleDisplay = new TextView(context);
        subtitleDisplay.setTextSize(textSize);

        if (position == null) {
            position = "bottom";
        }
        if (backgroundColor == null) {
            backgroundColor = "#00000000";
        }

        switch (position) {
            case "top": {
                subtitleDisplay.setGravity(Gravity.TOP);
                break;
            }
            case "bottom": {

                subtitleDisplay.setGravity(Gravity.BOTTOM);
                break;
            }
            case "left": {
                subtitleDisplay.setGravity(Gravity.LEFT);
                break;
            }
            case "right": {
                subtitleDisplay.setGravity(Gravity.RIGHT);
                break;
            }
            default: {
                subtitleDisplay.setGravity(Gravity.CENTER);
                break;
            }
        }

        subtitleDisplay.setBackgroundColor(Color.parseColor(backgroundColor));
        subtitleDisplay.setTextColor(Color.parseColor(color));
        subtitleDisplay.setText("");

    }

    public void setSubtitles(final boolean subtitlesEnabled) {

        mSubtitlesEnabled = subtitlesEnabled;
    }


    /**
     * Change the aspect ratio of the video.
     *
     * @param resizeMode can be one of the following.
     *                   "contain","cover","original",
     *                   fill_horizontal,"fill_vertical"
     */

    public void setVideoAspect(final String resizeMode) {

        switch (resizeMode) {
            case "cover": {
                mCurrentAspectRatio = IRenderView.AR_COVER;
                break;
            }
            case "stretch": {
                mCurrentAspectRatio = IRenderView.AR_STRETCH;
                break;
            }
            case "original": {
                mCurrentAspectRatio = IRenderView.AR_ORIGINAL;
                break;
            }
            case "fill_horizontal": {
                mCurrentAspectRatio = IRenderView.AR_FILL_HORIZONTAL;
                break;
            }
            case "fill_vertical": {
                mCurrentAspectRatio = IRenderView.AR_FILL_VERTICAL;
                break;
            }
            default: {
                mCurrentAspectRatio = IRenderView.AR_CONTAIN;
                break;
            }
        }
        if (mMediaPlayer != null)
            mRenderView.setAspectRatio(mCurrentAspectRatio);
    }

    /**
     * Change the speed of video playback.
     *
     * @param rate rate of video playback.
     *             default is 1.0f
     */

    public void setPlaybackRate(float rate) {
        if (mMediaPlayer != null)
            mIjkMediaPlayer.setSpeed(rate);

    }

    /**
     * Enable or disable repeat mode
     *
     * @param repeat sets whether the video should repeat itself.
     */

    public void repeat(boolean repeat) {
        if (mMediaPlayer != null)
            mIjkMediaPlayer.setLooping(repeat);
    }

    /**
     * Gets audio focus for the current audio session instance.
     */

    public void getAudioFocus() {


        AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(onAudioFocusChange, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

    }

    /**
     * Abandons audio focus from current audio session instance.
     */


    public void abandonAudioFocus() {

        AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(onAudioFocusChange);

    }


    /**
     * Track selector
     *
     * @param trackID select the track based on trackID. trackID is basically the
     *                index of stream in IjkStreamMeta which can contain audio, video
     *                and timedtext tracks altogether.
     */


    public void selectTrack(int trackID) {
        if (mMediaPlayer != null)
            mIjkMediaPlayer.selectTrack(trackID);

    }

    public void setVideo(final boolean videoDisabled) {
        if (mMediaPlayer != null)
            mVideoDisabled = videoDisabled;
    }

    public void setAudio(final boolean audioDisabled) {
        if (mMediaPlayer != null)
            mAudioDisabled = audioDisabled;
    }


    AudioManager.OnAudioFocusChangeListener onAudioFocusChange = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

            Log.i("AUDIO_SESSION_IDDDDDDDD", String.valueOf(mIjkMediaPlayer.getAudioSessionId()));
        }
    };

    /**
     * Equalizer
     */


    public Equalizer getEqualizer() {
        if (mEqualizer == null) {
            Log.i("EQExoPlayer", "Initializing equalizer...");
            updateEqualizerPrefs(true, true);
        }
        return mEqualizer;
    }

    public void setEqualizerSettings(boolean enabled, Equalizer.Settings settings) {
        boolean invalidate = mEqualizerEnabled != enabled || mEqualizerEnabled;
        boolean wasSystem = isUsingSystemEqualizer();

        mEqualizerEnabled = enabled;
        mEqualizerSettings = settings;

        if (invalidate) {
            updateEqualizerPrefs(enabled, wasSystem);
        }
    }

    private void updateEqualizerPrefs(boolean useCustom, boolean wasSystem) {
        Log.i("EQExoPlayer", "Updating equalizer prefs...");
        int audioSessionId = mMediaPlayer.getAudioSessionId();
        mAudioSessionId = audioSessionId;
        Log.i("EQExoPlayer", "AudioSessionId=" + String.valueOf(audioSessionId));

        if (audioSessionId == 0) {
            // No equalizer is currently bound. Nothing to do.
            return;
        }

        if (useCustom) {
            if (wasSystem || mEqualizer == null) {
                // System -> custom
                unbindSystemEqualizer(audioSessionId);
                bindCustomEqualizer(audioSessionId);
            } else {
                // Custom -> custom
                mEqualizer.setProperties(mEqualizerSettings);
            }
        } else {
            if (!wasSystem) {
                // Custom -> system
                unbindCustomEqualizer();
                bindSystemEqualizer(audioSessionId);
            }
            // Nothing to do for system -> system
        }
    }

    private boolean isUsingSystemEqualizer() {
        return false; // mEqualizerSettings == null || !mEqualizerEnabled;
    }

    private void onBindEqualizer(int newAudioSessionId) {

        if (isUsingSystemEqualizer()) {
            bindSystemEqualizer(newAudioSessionId);
        } else {
            bindCustomEqualizer(newAudioSessionId);
        }
    }

    private void bindSystemEqualizer(int audioSessionId) {
        Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getContext().getPackageName());
        getContext().sendBroadcast(intent);
    }

    private void bindCustomEqualizer(int audioSessionId) {
        mEqualizer = new Equalizer(0, audioSessionId);

        if (mEqualizerSettings != null)
            mEqualizer.setProperties(mEqualizerSettings);

        mEqualizer.setEnabled(true);
    }

    private void onUnbindEqualizer(int oldAudioSessionId) {
        if (isUsingSystemEqualizer()) {
            unbindSystemEqualizer(oldAudioSessionId);

        } else {
            unbindCustomEqualizer();
        }
    }

    private void unbindSystemEqualizer(int audioSessionId) {
        Intent intent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getContext().getPackageName());
        getContext().sendBroadcast(intent);
    }

    public void destroy() {
        if (mEqualizer != null) {
            Log.i("EQExoPlayer", "Destroying equalizer...");
            mEqualizer.setEnabled(false);
            mEqualizer.setEnableStatusListener(null);
            mEqualizer.release();
            mEqualizer = null;
        }
    }

    public void unbindCustomEqualizer() {
        destroy();
    }

    /**
     * Equalizer End.
     */


    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));

    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path, Map<String, String> headers) {
        setVideoURI(Uri.parse(path), headers);
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        initVideoView(getContext());
        openVideo();
        requestLayout();
        invalidate();
    }

    // REMOVED: addSubtitleSource
    // REMOVED: mPendingSubtitleTracks

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            unbindCustomEqualizer();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            abandonAudioFocus();

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }


        // we shouldn't clear the target state, because somebody might have
        // called start() previously

        release(false);

        getAudioFocus();


        mCurrentAspectRatio = IRenderView.AR_CONTAIN;


        try {

            mMediaPlayer = createPlayer();

            enableBackgroundPlayback(false);
            mBackgroundPlayEnabled = false;

            // TODO: create SubtitleController in MediaPlayer, but we need
            // a context for the subtitle renderers
            final Context context = getContext();
            // REMOVED: SubtitleController


            // REMOVED: mAudioSession
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);

            mCurrentBufferPercentage = 0;
            String scheme = mUri.getScheme();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mMediaPlayer.setDataSource(mAppContext, mUri, mHeaders);
            } else {
                mMediaPlayer.setDataSource(mUri.toString());
            }

            bindSurfaceHolder(mMediaPlayer, mSurfaceHolder);

            getAudioFocus();
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mPrepareStartTime = System.currentTimeMillis();
            mMediaPlayer.prepareAsync();

            // REMOVED: mPendingSubtitleTracks

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } finally {
            // REMOVED: mPendingSubtitleTracks.clear();

        }
    }

    Equalizer.OnEnableStatusChangeListener mOnEqualizerEnableStatusChange = new Equalizer.OnEnableStatusChangeListener() {
        @Override
        public void onEnableStatusChange(AudioEffect effect, boolean enabled) {

            Log.i("EQ", String.valueOf(enabled));
            Log.i("EQ", String.valueOf(effect));
        }

    };

    private View floatingWindow;

    public void setFloatingWindow(View floating) {
        floatingWindow = floating;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    mVideoSarNum = mp.getVideoSarNum();
                    mVideoSarDen = mp.getVideoSarDen();




                    if (mVideoWidth != 0 && mVideoHeight != 0) {

                        final float scale = getContext().getResources().getDisplayMetrics().density;


                           RelativeLayout relativeLayout = (RelativeLayout) floatingWindow.findViewById(R.id.view_wrapper);
                            double aspectRatio = (double) mVideoWidth / (double) mVideoHeight;

                             if (mVideoHeight > mVideoWidth) {
                                int h = (int) (200 * scale + 0.5f);
                               double w = h * aspectRatio;

                                relativeLayout.getLayoutParams().width = (int) w;
                                  relativeLayout.getLayoutParams().height = h;

                             } else {
                                  int w = (int) (250 * scale + 0.5f);
                                  double h = w / aspectRatio;
                                  relativeLayout.getLayoutParams().width = w;
                                 relativeLayout.getLayoutParams().height = (int) h;

                                }



                        if (mRenderView != null) {
                            mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                            mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                            mRenderView.setAspectRatio(mCurrentAspectRatio);
                        }
                        requestLayout();
                    }
                }
            };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            mPrepareEndTime = System.currentTimeMillis();
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
            // REMOVED: Metadata

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mAudioSessionId = mIjkMediaPlayer.getAudioSessionId();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);

                if (mRenderView != null) {
                    mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                    mRenderView.setVideoSize(mVideoWidth, mVideoHeight);


                    if (!mRenderView.shouldWaitForResize() || mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                        // We didn't actually change the size (it was already at the size
                        // we need), so we won't get a "surface changed" callback, so
                        // start the video here instead of in the callback.
                        if (mTargetState == STATE_PLAYING) {
                            start();
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.

                if (mTargetState == STATE_PLAYING) {
                    start();


                }
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                        unbindCustomEqualizer();
                        initVideoView(getContext());
                    }
                }
            };


    private IMediaPlayer.OnInfoListener mInfoListener =
            new IMediaPlayer.OnInfoListener() {
                public boolean onInfo(IMediaPlayer mp, int message, int val) {
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, message, val);
                    }

                    switch (message) {
                        case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                            Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + val);
                            break;
                        case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                            Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                            Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                            Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                            Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                            Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                            mVideoRotationDegree = val;
                            Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + val);
                            if (mRenderView != null)
                                mRenderView.setVideoRotation(val);
                            break;

                        case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                            Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                            Log.d("AUDIO_SESSION_ID", String.valueOf(mMediaPlayer.getAudioSessionId()));

                            break;
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    if (mOnBufferingUpdateListener != null)
                        mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
                    mCurrentBufferPercentage = percent;
                    Log.i("SPEED_BUFFER", String.valueOf(mIjkMediaPlayer.getTcpSpeed() / 1000));
                }
            };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            Log.i("TEXT", "HELLOOOOO");
            mSeekEndTime = System.currentTimeMillis();
            if (!mMediaPlayer.isPlaying())
                start();
        }
    };


    private IMediaPlayer.OnTimedTextListener mOnTimedTextListener = new IMediaPlayer.OnTimedTextListener() {


        @Override
        public void onTimedText(IMediaPlayer mp, IjkTimedText text) {

            Log.i("SUBTITLE_LOAD_ED", String.valueOf(text.getText()));
            if (text != null) {

                subtitleDisplay.setText(text.getText());

            }
        }

    };


    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;

    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
    }


    // REMOVED: mSHCallback
    private void bindSurfaceHolder(IMediaPlayer mp, IRenderView.ISurfaceHolder holder) {
        if (mp == null)
            return;

        if (holder == null) {
            mp.setDisplay(null);
            return;
        }


        holder.bindToMediaPlayer(mp);
    }

    IRenderView.IRenderCallback mSHCallback = new IRenderView.IRenderCallback() {
        @Override
        public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int w, int h) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceChanged: unmatched render callback\n");
                return;
            }

            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = !mRenderView.shouldWaitForResize() || (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceCreated: unmatched render callback\n");
                return;
            }

            mSurfaceHolder = holder;
            if (mMediaPlayer != null)
                bindSurfaceHolder(mMediaPlayer, holder);
            else
                openVideo();
        }

        @Override
        public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceDestroyed: unmatched render callback\n");
                return;
            }

            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            // REMOVED: if (mMediaController != null) mMediaController.hide();
            // REMOVED: release(true);
            releaseWithoutStop();
        }
    };


    public void releaseWithoutStop() {
        if (mMediaPlayer != null)
            mMediaPlayer.setDisplay(null);
    }

    /*
     * release the media player in any state
     */
    public void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            unbindCustomEqualizer();
            mAudioSessionId = -1;
            mMediaPlayer = null;
            // REMOVED: mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            abandonAudioFocus();


        }
    }


    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mSeekStartTime = System.currentTimeMillis();
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public Bitmap getBitmap() {
        if (isInPlaybackState())

            return mRenderView.getBitmap();
        return null;
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);

    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {

        return mIjkMediaPlayer.getAudioSessionId();


    }

    private int mCurrentAspectRatio = IRenderView.AR_CONTAIN;
    private int mMaxFpsLimit = 60;

    public IjkMediaPlayer createPlayer() {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-all-videos", 1);
        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);


        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", mMaxFpsLimit);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "find_stream_info", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "async-init-decoder", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 1);

        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "loop", 0);
        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "render-wait-start", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "android_version", android.os.Build.VERSION.SDK_INT);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-sync", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "accurate-seek", 1);


        if (mSoundtouch)
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 0);

        if (mAudioDisabled)
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "an", 1);

        if (mVideoDisabled) {
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "nodisp", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "vn", 1);
        }


        if (mSubtitlesEnabled)
            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "subtitle", 1);

        if (mUserAgent != null)
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", mUserAgent);

        mIjkMediaPlayer = ijkMediaPlayer;

        mIjkMediaPlayer.setCacheShare(IjkMediaPlayer.FFP_PROP_INT64_SHARE_CACHE_DATA);
        mIjkMediaPlayer.setLogEnabled(true);

        return ijkMediaPlayer;
    }


    public void setmMaxFpsLimit(int fps) {
        mMaxFpsLimit = fps;
    }


    public void setVolume(float left, float right) {
        if (mMediaPlayer != null)
            mMediaPlayer.setVolume(left, right);
    }

    public void setUserAgent(final String userAgent) {
        mUserAgent = userAgent;
    }

    public int getSelectedTrack(int trackType) {

        return mIjkMediaPlayer.getSelectedTrack(trackType);

    }


    public void enableBackgroundPlayback(final boolean backgroundPlayback) {
        mBackgroundPlayEnabled = backgroundPlayback;
        if (mMediaPlayer != null)
            mIjkMediaPlayer.setKeepInBackground(mBackgroundPlayEnabled);

    }


}
