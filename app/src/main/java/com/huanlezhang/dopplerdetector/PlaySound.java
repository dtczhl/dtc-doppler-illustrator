package com.huanlezhang.dopplerdetector;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 *  Auhtor: Huanle Zhang
 *  Personal Website: www.huanlezhang.com
 *  June 13, 2016
 */
public class PlaySound {
    public int mOutputFreq = 0;

    private static final int SOURCE_FREQ = 48000;
    private static Thread mThread = null;
    private static AudioTrack mAudioTrack = null;
    private static boolean mIsPlay = false;

    public void start(){
        if (mOutputFreq == 0){
            mIsPlay = false;
            return;
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int buffSize = AudioTrack.getMinBufferSize(SOURCE_FREQ, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        SOURCE_FREQ, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        buffSize, AudioTrack.MODE_STREAM);
                short samples[] = new short[buffSize];
                int amp = 32767;
                final double twopi =2*Math.PI;
                double ph = 0.0;
                mAudioTrack.play();
                mIsPlay = true;
                while (mIsPlay){
                    for (int i=0; i<buffSize; i++){
                        samples[i] = (short)(amp*Math.sin(ph));
                        ph += twopi*mOutputFreq/SOURCE_FREQ;
                    }
                    mAudioTrack.write(samples, 0, buffSize);
                }
                if(mAudioTrack != null) mAudioTrack.stop();
                if(mAudioTrack != null) mAudioTrack.release();
            }
        });
        mThread.start();
    }

    public void stop(){
        mIsPlay = false;
    }
}
