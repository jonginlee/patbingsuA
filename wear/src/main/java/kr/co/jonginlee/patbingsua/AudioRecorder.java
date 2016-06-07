package kr.co.jonginlee.patbingsua;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

/**
 * Created by jonginlee on 15. 5. 31..
 */
public class AudioRecorder {
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;

//    public void onRecord(boolean start) {
//        if (start) {
//            startRecording();
//        } else {
//            stopRecording();
//        }
//    }

    public void startRecording(String filename) {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ filename;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
            Thread.sleep(1000);
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        mRecorder.start();
    }


    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


    public AudioRecorder() {


    }




}
