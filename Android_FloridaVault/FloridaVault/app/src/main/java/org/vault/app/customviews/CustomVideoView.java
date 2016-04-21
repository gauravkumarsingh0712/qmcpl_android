package org.vault.app.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * Created by aqeeb.pathan on 10-09-2015.
 */
public class CustomVideoView extends VideoView {

    private PlayPauseListener mListener;
    private int mForceHeight = 0;
    private int mForceWidth = 0;
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth = w;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i("@@@@", "onMeasure");

        setMeasuredDimension(mForceWidth, mForceHeight);
    }

    public void setPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }

    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlay();
        }
    }


    public static interface PlayPauseListener {
        void onPlay();
        void onPause();
    }
}