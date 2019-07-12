package com.huanlezhang.dtcdopplerillustrator;

import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Handler;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.*;

/**
 * calculate Doppler shift
 *
 * @author  Huanle Zhang, University of California, Davis
 *          www.huanlezhang.com
 * @version 0.2
 * @since   2019-07-11
 */

import java.util.Arrays;

public class AnalyzeFrequency {

    private static final String TAG = "DTC AnalyzeFrequency";

    // interface to draw on screen
    private Handler mHandler;
    private Runnable mRunable;

    private AudioRecord mAudioRecord;
    private final int mSampleRate = 48000;
    private boolean mIsRecording = false;

    // frequency analysis
    private FastFourierTransformer mFFT;
    private final int N_FFT_DOT = 4096;
    private final int UNDER_SAMPLE_X = 8;   // undersampling
    private double mOverlapRatio = 0.8575;  // overlapping
    private short[] mAudioBuffer = new short[N_FFT_DOT*UNDER_SAMPLE_X];
    private double[] mFftBuffer = new double[N_FFT_DOT];
    private Complex[] mResultFFT = new Complex[N_FFT_DOT];

    public final int N_CH_DOT = 100;    // for drawing
    public int[] mCh = new int[N_CH_DOT];
    public int mChIndex = 0;
    public double[] mMagnitude = new double[N_FFT_DOT/2 - 1];
    public double FREQ_RESOLUTION = (21000.0 - 18000.0) / N_FFT_DOT / 2;   // frequency shift for each FFT dot

    AnalyzeFrequency(Handler mainHandler, Runnable drawRun) {
        mHandler = mainHandler;
        mRunable = drawRun;

        int bufferSize = AudioRecord.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                mSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        Arrays.fill(mFftBuffer, 0.0);

        mFFT = new FastFourierTransformer(DftNormalization.UNITARY);
    }

    public void start() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                mIsRecording = true;

                int remainFftBufSize = 0;
                int audioReadSize = 0;
                int indexFftBuf = 0;
                while (mIsRecording) {
                    System.arraycopy(mFftBuffer, (int) ((1 - mOverlapRatio) * N_FFT_DOT),
                            mFftBuffer, 0, (int) (mOverlapRatio * N_FFT_DOT));
                    remainFftBufSize = (int) ((1 - mOverlapRatio) * N_FFT_DOT); // overlapping
                    indexFftBuf = (int) (mOverlapRatio * N_FFT_DOT);
                    while (remainFftBufSize > 0 && mIsRecording) {
                        audioReadSize = mAudioRecord.read(mAudioBuffer, 0, remainFftBufSize * UNDER_SAMPLE_X);
                        for (int i = 0; i < audioReadSize; i++) {
                            // undersampling
                            if ((i % UNDER_SAMPLE_X) == 0) {
                                mFftBuffer[indexFftBuf++] = filter((double) mAudioBuffer[i]) / 32768.0;
                                remainFftBufSize--;
                            } else {
                                filter((double) mAudioBuffer[i]);
                            }
                        }
                    }
                    if (!mIsRecording){
                        break;
                    }

                    // ---- window ----
                    for(int i=0; i<mFftBuffer.length; i++){  // Blackman-Harris window
                        mFftBuffer[i] = mFftBuffer[i] * (0.35875-0.48829*Math.cos(2*Math.PI*i/(N_FFT_DOT -1))
                                + 0.14128*Math.cos(4*Math.PI*i/(N_FFT_DOT -1))-0.01168*Math.cos(6*Math.PI*i/(N_FFT_DOT -1)));
                    }

                    // fft
                    mResultFFT = mFFT.transform(mFftBuffer, TransformType.FORWARD);
                    for (int i = 0; i < mMagnitude.length; i++){
                        mMagnitude[i] = mResultFFT[i].abs();
                    }

                    // offset with max magnitude
                    calculateFrequencyOffset();

                    // draw on screen
                    mHandler.post(mRunable);
                }
                mAudioRecord.stop();
                mAudioRecord.release();
            }
        });
        thread.start();
    }

    private void calculateFrequencyOffset() {
        // 2048 bins for [18KHz, 21KHz]
        // 19 KHZ is at 1/3 * 2048 = 683

        // get max offset
        int maxIndex = 0;
        // 18.5K -> 19.5K
        for (int i = 341; i < 1024; i++) {
            if (mMagnitude[i] > mMagnitude[maxIndex]) {
                maxIndex = i;
            }
        }

        mCh[mChIndex] = (int) (FREQ_RESOLUTION * (maxIndex - 683));
        mChIndex = (++mChIndex) % N_CH_DOT;
    }


    public void stop() {
        mIsRecording = false;
    }

    // codes generated by FIR Filter Designer
    private final int N = 109;
    private int n = 0;
    private double[] x = new double[N];
    private final double[] h ={
            -0.000452475548192976,
            0.00129499058874955,
            -0.00104379148395869,
            0.000661517480952563,
            0.000667118334469181,
            -0.00208925735857007,
            0.00298975106292743,
            -0.00259578037105986,
            0.000838436991203126,
            0.00162068443420918,
            -0.00357997917478736,
            0.00399689290327437,
            -0.00264031748964509,
            0.000315744581448504,
            0.00159273271703699,
            -0.00202394803080458,
            0.00101782132280574,
            0.000220901050404929,
            -0.000129978472368510,
            -0.00197322029943899,
            0.00507928729565731,
            -0.00679476437332444,
            0.00479058331560558,
            0.00141457242380541,
            -0.00954412871093481,
            0.0153498715622134,
            -0.0149069238113032,
            0.00715009043400282,
            0.00498088051651027,
            -0.0157508084452062,
            0.0197304107166670,
            -0.0150193938937010,
            0.00453687146015867,
            0.00564713726045466,
            -0.0100973649710677,
            0.00758201816181044,
            -0.00199115748150843,
            -0.000298799795090139,
            -0.00482464626580575,
            0.0154244649926620,
            -0.0235569717723724,
            0.0197894877779472,
            0.000121428864566431,
            -0.0305452372733660,
            0.0568402602756439,
            -0.0622836911699475,
            0.0378005159623464,
            0.0110227301031027,
            -0.0642926107740058,
            0.0963804482102806,
            -0.0888391233479958,
            0.0408867868548142,
            0.0285443882503055,
            -0.0889362101674115,
            0.112737964436084,
            -0.0889362101674115,
            0.0285443882503055,
            0.0408867868548142,
            -0.0888391233479958,
            0.0963804482102806,
            -0.0642926107740058,
            0.0110227301031027,
            0.0378005159623464,
            -0.0622836911699475,
            0.0568402602756439,
            -0.0305452372733660,
            0.000121428864566431,
            0.0197894877779472,
            -0.0235569717723724,
            0.0154244649926620,
            -0.00482464626580575,
            -0.000298799795090139,
            -0.00199115748150843,
            0.00758201816181044,
            -0.0100973649710677,
            0.00564713726045466,
            0.00453687146015867,
            -0.0150193938937010,
            0.0197304107166670,
            -0.0157508084452062,
            0.00498088051651027,
            0.00715009043400282,
            -0.0149069238113032,
            0.0153498715622134,
            -0.00954412871093481,
            0.00141457242380541,
            0.00479058331560558,
            -0.00679476437332444,
            0.00507928729565731,
            -0.00197322029943899,
            -0.000129978472368510,
            0.000220901050404929,
            0.00101782132280574,
            -0.00202394803080458,
            0.00159273271703699,
            0.000315744581448504,
            -0.00264031748964509,
            0.00399689290327437,
            -0.00357997917478736,
            0.00162068443420918,
            0.000838436991203126,
            -0.00259578037105986,
            0.00298975106292743,
            -0.00208925735857007,
            0.000667118334469181,
            0.000661517480952563,
            -0.00104379148395869,
            0.00129499058874955,
            -0.000452475548192976,
    };

    public double filter(double x_in){
        double y = 0.0;
        // Store the current input, overwriting the oldest input
        x[n] = x_in;
        // Multiply the filter coefficients by the previous inputs and sum
        for (int i=0; i<N; i++)
        {
            y += h[i] * x[((N - i) + n) % N];
        }
        // Increment the input buffer index to the next location
        n = (n + 1) % N;
        return y;
    }
}
