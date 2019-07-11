package com.huanlezhang.dtcdopplerillustrator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private static final String TAG = "DTC MainActivity";

    private RadioGroup mRadioGroup;

    private boolean mIsSender;

    private ToggleButton mMainToggleBtn;

    private ImageView mImageView;
    private int mImageViewWidth;
    private int mImageViewHeight;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;

    // for sender
    PlaySound mPlaySound = new PlaySound();
    private final int FREQ_SOUND = 19000;   // emit 19 KHz sounds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
        mImageView.setImageBitmap(mBitmap);
        mImageView.invalidate();

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
            Log.d(TAG, "receiver");
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

        }
    }

    void enableAllUI(boolean enable) {
        // for radio group
        for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
            mRadioGroup.getChildAt(i).setEnabled(enable);
        }

        mMainToggleBtn.setEnabled(enable);
    }
}
