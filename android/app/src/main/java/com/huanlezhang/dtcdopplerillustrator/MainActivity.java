package com.huanlezhang.dtcdopplerillustrator;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Arrays;

/**
 * Illustrating Doppler effect
 *
 * @author  Huanle Zhang, University of California, Davis
 *          www.huanlezhang.com
 * @version 0.2
 * @since   2019-07-11
 */

public class MainActivity extends Activity {

    // private static final String TAG = "DTC MainActivity";

    private static final String[] PermissionStrings = {
            Manifest.permission.RECORD_AUDIO
    };
    private static final int Permission_ID = 1;

    private RadioGroup mRadioGroup;

    private boolean mIsSender;

    private ToggleButton mMainToggleBtn;

    private ImageView mImageView;
    private int mImageViewWidth;
    private int mImageViewHeight;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private Paint mBaseLinePaint;

    // for sender
    PlaySound mPlaySound = new PlaySound();
    private final int FREQ_SOUND = 19000;   // emit 19 KHz sounds

    // for receiver
    private Handler mHandler = new Handler();
    private Runnable mDrawFFTRun = new DrawFFT();

    private AnalyzeFrequency mFftAnalysis;
    private final int N_FFT_DOT = 4096;
    private float[] mCurArray = new float[N_FFT_DOT/2-1];
    private static final int FREQ_OFFSET_MAX = 20;  // maximum frequency range

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, PermissionStrings, Permission_ID);

        mRadioGroup = findViewById(R.id.radioGroup);

        mMainToggleBtn = findViewById(R.id.startToggleBtn);
        mMainToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton toggleButton = (ToggleButton) v;
                if (toggleButton.isChecked()) {
                    startMain();
                } else {
                    stopMain();
                }
            }
        });

        // set up the imageview
        mImageView = findViewById(R.id.mainImageView);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mImageViewWidth = displayMetrics.widthPixels - (int)(getResources().getDisplayMetrics().density*4.0+0.5);
        mImageViewHeight = mImageViewWidth; // a square view
        mBitmap = Bitmap.createBitmap(mImageViewWidth, mImageViewHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.LTGRAY);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(10);
        mBaseLinePaint = new Paint();
        mBaseLinePaint.setColor(Color.BLUE);
        mBaseLinePaint.setStrokeWidth(5);
        mImageView.setImageBitmap(mBitmap);
        mImageView.invalidate();

        TextView maxFreqText = findViewById(R.id.maxFreq);
        maxFreqText.setText(FREQ_OFFSET_MAX + " Hz");
        TextView minFreqText = findViewById(R.id.minFreq);
        minFreqText.setText(-FREQ_OFFSET_MAX + " Hz");
    }

    void startMain() {
        enableAllUI(false);
        mMainToggleBtn.setEnabled(true);
        int radioBtnId = mRadioGroup.getCheckedRadioButtonId();
        if (radioBtnId == R.id.senderRadio) {
            // sender
            mIsSender = true;
            mPlaySound = new PlaySound();
            mPlaySound.mOutputFreq = FREQ_SOUND;
            mPlaySound.start();
        } else {
            // receiver
            mIsSender = false;
            mFftAnalysis = new AnalyzeFrequency(mHandler, mDrawFFTRun);
            mFftAnalysis.start();
        }
    }

    void stopMain() {
        enableAllUI(true);
        if (mIsSender) {
            if (mPlaySound != null) {
                mPlaySound.stop();
                mPlaySound = null;
            }
        } else {
            if (mFftAnalysis != null) {
                mFftAnalysis.stop();
                mFftAnalysis = null;
            }
            Arrays.fill(mCurArray, (float) 0.0);
        }
    }

    void enableAllUI(boolean enable) {
        // for radio group
        for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
            mRadioGroup.getChildAt(i).setEnabled(enable);
        }

        mMainToggleBtn.setEnabled(enable);
    }

    // draw doppler on screen
    public class DrawFFT implements Runnable{
        @Override
        public void run() {
            if (mFftAnalysis == null) {
                return;
            }
            int scaleFFT = (mImageViewHeight/2) / FREQ_OFFSET_MAX;
            int N_CH_DOT = mFftAnalysis.N_CH_DOT;
            int fftInterval = (int) (1.0 * mImageViewWidth / N_CH_DOT);
            int fftDisplayRange = fftInterval * N_CH_DOT;
            int index = (mFftAnalysis.mChIndex - 1) % N_CH_DOT;

            mCanvas.drawColor(Color.LTGRAY);

            // horizontal base line
            mCanvas.drawLine(0, mImageViewHeight/2, mImageViewWidth, mImageViewHeight/2, mBaseLinePaint);

            for (int i = fftDisplayRange; i > fftInterval; i -= fftInterval) {
                mCanvas.drawLine(i, mImageViewHeight / 2 - scaleFFT * mFftAnalysis.mCh[(index + 2 * N_CH_DOT) % N_CH_DOT],
                        i - fftInterval, mImageViewHeight / 2 - scaleFFT * mFftAnalysis.mCh[(index + 2 * N_CH_DOT - 1) % N_CH_DOT],
                        mPaint);
                --index;
            }
            mImageView.invalidate();
        }
    }
}
