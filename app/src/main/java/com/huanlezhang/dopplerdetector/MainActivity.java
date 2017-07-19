package com.huanlezhang.dopplerdetector;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Author: Huanle Zhang
 * Personal Website: www.huanlezhang.com
 * Date: June 13, 2016
**/
public class MainActivity extends Activity{

    private static final String[] PermissionStrings = {
            Manifest.permission.RECORD_AUDIO
    };
    private static final int PERMISSION_REQUEST_ID = 1;

    private static Spinner mTransSpinner;
    private static int mTransChannel = 0;
    static private PlaySound mPlaySound = null;
    static private final int[] CH2FREQ = {0, 18200, 18600, 19000, 19400, 19800};

    static private Spinner mDispSpinner;
    private static int mDispChannel = 0;

    static private FFTAnalysis mFftAnalysis = null;

    static private ImageView mImageView;

    static private Bitmap mBitmap = null;
    static private Canvas mCanvas = null;
    static private Paint mPaint;
    static private int mImgWidth = 0;
    static private int mImgHeight = 0;

    static private Button mStartBtn;
    static private boolean mIsRun = false;

    static private Button mExitBtn;

    @Override
    protected void onResume() {
        super.onResume();

        mStartBtn.setText("Start");
        mExitBtn.setEnabled(true);

        if (mPlaySound != null){
            mPlaySound.stop();
            mPlaySound = null;
        }
        if (mFftAnalysis != null){
            mFftAnalysis.stop();
            mFftAnalysis = null;
        }

    }

    @Override
    protected void onPause() {

        if (mPlaySound != null) {
            mPlaySound.stop();
            mPlaySound = null;
        }
        if (mPlaySound != null){
            mFftAnalysis.stop();
            mFftAnalysis = null;
        }

        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23){
            ActivityCompat.requestPermissions(this, PermissionStrings, PERMISSION_REQUEST_ID);
        }

        mTransSpinner = (Spinner) findViewById(R.id.transSpinner);
        ArrayAdapter<CharSequence> transAdapter = ArrayAdapter.createFromResource(this,
                R.array.trans_array, android.R.layout.simple_spinner_item);
        transAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTransSpinner.setAdapter(transAdapter);
        mTransSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTransChannel = position;

                if (!mIsRun) return;

                if (mTransChannel != 0){
                    if (mPlaySound == null){
                        mPlaySound = new PlaySound();
                        mPlaySound.mOutputFreq = CH2FREQ[mTransChannel];
                        mPlaySound.start();
                    } else {
                        mPlaySound.mOutputFreq = CH2FREQ[mTransChannel];
                    }
                } else {
                    if (mPlaySound != null){
                        mPlaySound.stop();
                        mPlaySound = null;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDispSpinner = (Spinner) findViewById(R.id.dispSpinner);
        ArrayAdapter<CharSequence> recvAdapter = ArrayAdapter.createFromResource(this,
                R.array.recv_array, android.R.layout.simple_spinner_item);
        recvAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDispSpinner.setAdapter(recvAdapter);
        mDispSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDispChannel = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mImageView = (ImageView) findViewById(R.id.imageView);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mImgWidth = displayMetrics.widthPixels - (int)(getResources().getDisplayMetrics().density*2.0*1+0.5f);
        mImgHeight = (int)(mImgWidth / 1.3);
        mBitmap = Bitmap.createBitmap(mImgWidth, mImgHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.LTGRAY);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(10);
        mImageView.setImageBitmap(mBitmap);
        mImageView.invalidate();

        mStartBtn = (Button) findViewById(R.id.startBtn);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartBtn.getText().toString().trim().toLowerCase().compareTo("start") == 0){
                    mExitBtn.setEnabled(false);
                    mStartBtn.setText("Stop");

                    mIsRun = true;
                    if (mTransChannel != 0){
                        mPlaySound = new PlaySound();
                        mPlaySound.mOutputFreq = CH2FREQ[mTransChannel];
                        mPlaySound.start();
                    }
                    mFftAnalysis = new FFTAnalysis(getApplicationContext(), new Handler(), new DisplayFftRun());
                    mFftAnalysis.start();
                } else {
                    mExitBtn.setEnabled(true);
                    mStartBtn.setText("Start");

                    mIsRun = false;

                    if (mPlaySound != null){
                        mPlaySound.stop();
                        mPlaySound = null;
                    }
                    if (mFftAnalysis != null){
                        mFftAnalysis.stop();
                        mFftAnalysis = null;
                    }
                }
            }
        });

        mExitBtn = (Button) findViewById(R.id.exitBtn);
        mExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    public void aboutMe(View view){
        AboutMe.showDialog(this);
    }

    public class DisplayFftRun implements Runnable {

        @Override
        public void run() {

            if (mFftAnalysis == null) {
                return;
            }
            int scaleFFT = mImgHeight/50;
            int N_CH_DOT = mFftAnalysis.N_CH_DOT;
            int fftInterval = (int) (1.0 * mImgWidth / N_CH_DOT);
            int fftDisplayRange = fftInterval * N_CH_DOT;
            int index = (mFftAnalysis.mChIndex - 1) % N_CH_DOT;
            int minCh = 0;


            mCanvas.drawColor(Color.LTGRAY);
            switch (mDispChannel) {
                case 0:
                    // no need update screen
                    mImageView.invalidate();
                    break;
                case 1:
                    for (int i = fftDisplayRange; i > fftInterval; i -= fftInterval) {
                        mCanvas.drawLine(i, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh1[(index + 2 * N_CH_DOT) % N_CH_DOT],
                                i - fftInterval, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh1[(index + 2 * N_CH_DOT - 1) % N_CH_DOT],
                                mPaint);
                        --index;
                    }
                    mImageView.invalidate();
                    break;
                case 2:
                    for (int i = fftDisplayRange; i > fftInterval; i -= fftInterval) {
                        mCanvas.drawLine(i, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh2[(index + 2 * N_CH_DOT) % N_CH_DOT],
                                i - fftInterval, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh2[(index + 2 * N_CH_DOT - 1) % N_CH_DOT],
                                mPaint);
                        --index;
                    }
                    mImageView.invalidate();
                    break;
                case 3:
                    for (int i = fftDisplayRange; i > fftInterval; i -= fftInterval) {
                        mCanvas.drawLine(i, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh3[(index + 2 * N_CH_DOT) % N_CH_DOT],
                                i - fftInterval, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh3[(index + 2 * N_CH_DOT - 1) % N_CH_DOT],
                                mPaint);
                        --index;
                    }
                    mImageView.invalidate();
                    break;
                case 4:
                    for (int i = fftDisplayRange; i > fftInterval; i -= fftInterval) {
                        mCanvas.drawLine(i, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh4[(index + 2 * N_CH_DOT) % N_CH_DOT],
                                i - fftInterval, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh4[(index + 2 * N_CH_DOT - 1) % N_CH_DOT],
                                mPaint);
                        --index;
                    }
                    mImageView.invalidate();
                    break;
                case 5:
                    for (int i = fftDisplayRange; i > fftInterval; i -= fftInterval) {
                        mCanvas.drawLine(i, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh5[(index + 2 * N_CH_DOT) % N_CH_DOT],
                                i - fftInterval, mImgHeight / 2 - scaleFFT * mFftAnalysis.mCh5[(index + 2 * N_CH_DOT - 1) % N_CH_DOT],
                                mPaint);
                        --index;
                    }
                    mImageView.invalidate();
                    break;
                default:
                    Toast.makeText(getApplication(), "drawFFTRun Error", Toast.LENGTH_LONG).show();
            }
        }
    }
}
