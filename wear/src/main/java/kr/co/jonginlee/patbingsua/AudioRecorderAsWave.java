package kr.co.jonginlee.patbingsua;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by jonginlee on 15. 5. 14..
 */
public class AudioRecorderAsWave {

    private static final int SPEECH_REQUEST_CODE = 1;
    private static  int RECORDER_SAMPLERATE = 0;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final String TAG = "WearWatch";

    private AudioRecord recorder;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private volatile boolean isRecording;
    private FileOutputStream mFos;
    private int mNumOfBlock = 0;
    private String RECORDED_FILE_NAME = "recorded.wav";
    private long mSizeOfRecData = 0;
    private DataOutputStream outFile;

    public AudioRecorderAsWave(String filename) {
        RECORDED_FILE_NAME = filename;
    }

    public AudioRecorderAsWave() {
        RECORDED_FILE_NAME = "recorded.wav";
    }

    public int getValidSampleRates() {
        int samplingrate = -1;
        for (int rate : new int[] { 8000, 22050,16000, 11025, 44100,8000}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                samplingrate = rate;
                break;
            }
        }
        Log.d(TAG, "sampling rate - "+samplingrate);
        return samplingrate;
    }

//    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
//    public AudioRecord findAudioRecord() {
//        for (int rate : mSampleRates) {
//            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
//                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
//                    try {
//                        Log.d(C.TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
//                                + channelConfig);
//                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
//
//                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
//                            // check if we can instantiate and have a success
//                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);
//
//                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
//                                return recorder;
//                        }
//                    } catch (Exception e) {
//                        Log.e(C.TAG, rate + "Exception, keep trying.",e);
//                    }
//                }
//            }
//        }
//        return null;
//    }

    public void startAudioCapture(String filename) {
        if(filename!=null)
           RECORDED_FILE_NAME = filename;

        RECORDER_SAMPLERATE = getValidSampleRates();
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        Log.d(TAG, "Starting audio capture" + " buffer size (" + bufferSize + ")");
        bufferSize = bufferSize*10;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
        byte[] header = new byte[44];

        try {
            mFos = new FileOutputStream(new File(getExternalStorageDirectory(), RECORDED_FILE_NAME));
            BufferedOutputStream bos = new BufferedOutputStream(mFos);
            outFile = new DataOutputStream(bos);
            outFile.write(header, 0, 44);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        recData = new ByteArrayOutputStream();
//        dos = new DataOutputStream(recData);

        if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording();
//            if(recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED){
//                Log.v(TAG, "startrecording again!!");
//                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
//                if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
//                    recorder.startRecording();
//            }

            isRecording = true;
            Log.v(TAG, "Successfully started recording " + bufferSize + " byte");
            mSizeOfRecData = 0;
            recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    savingRawAudioData();
                }
            }, "AudioRecorder_Thread");

            recordingThread.start();
        } else {
            Log.v(TAG, "Failed to started recording");
        }
    }

    private void savingRawAudioData() {
        short data[] = new short[bufferSize];
        int read = 0;
        Log.v(TAG, "enter savingRawAudioData thread");
        while(isRecording) {
            read = recorder.read(data, 0, bufferSize);
            if(read<0){
                Log.v(TAG, "recording state : "+ recorder.getRecordingState());
                Log.v(TAG, "recorder state : "+recorder.getState());
            }
            if(AudioRecord.ERROR_INVALID_OPERATION != read) {
                Log.v(TAG, "Successfully read " + data.length + " bytes of audio , read "+read);
                for(int i = 0; i < read;i++) {
                    try {
                        writeShortLE(outFile, data[i]);
                        mSizeOfRecData+=(2*1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                Log.v(TAG, "AudioRecord.ERROR_INVALID_OPERATION, read "+read);
            }
        }
    }

    private static void writeShortLE(DataOutputStream out, short value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >> 8) & 0xFF);
    }

    public void stopAudioCapture() {
        Log.v(TAG, "Stop audio capture");

        if(recorder.getState() != AudioRecord.STATE_UNINITIALIZED ){
            recorder.stop();

            isRecording = false;
            recorder.release();

            try {
                outFile.flush();
                outFile.close();
                mFos.flush();
                mFos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            attachingHeadInfoWAV();
        }

    }

    private void attachingHeadInfoWAV(){

        long mySubChunk1Size = 16;
        int myBitsPerSample= 16;
        int myFormat = 1;
        long myChannels = 1;
        long mySampleRate = RECORDER_SAMPLERATE;
        long myByteRate = mySampleRate * myChannels * myBitsPerSample/8;
        int myBlockAlign = (int) (myChannels * myBitsPerSample/8);

        long myDataSize = mSizeOfRecData;
        long myChunk2Size =  myDataSize * myChannels * myBitsPerSample/8;
        long myChunkSize = 36 + myChunk2Size;

        try {
            RandomAccessFile rf = new RandomAccessFile(new File(getExternalStorageDirectory(), RECORDED_FILE_NAME), "rw");
            rf.seek(0);

            rf.writeBytes("RIFF");                                 // 00 - RIFF
            rf.write(intToByteArray((int) myChunkSize), 0, 4);      // 04 - how big is the rest of this file?
            rf.writeBytes("WAVE");                                 // 08 - WAVE
            rf.writeBytes("fmt ");                                 // 12 - fmt
            rf.write(intToByteArray((int) mySubChunk1Size), 0, 4);  // 16 - size of this chunk
            rf.write(shortToByteArray((short)myFormat), 0, 2);     // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
            rf.write(shortToByteArray((short) myChannels), 0, 2);   // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
            rf.write(intToByteArray((int)mySampleRate), 0, 4);     // 24 - samples per second (numbers per second)
            rf.write(intToByteArray((int) myByteRate), 0, 4);       // 28 - bytes per second
            rf.write(shortToByteArray((short)myBlockAlign), 0, 2); // 32 - # of bytes in one sample, for all channels
            rf.write(shortToByteArray((short) myBitsPerSample), 0, 2);  // 34 - how many bits in a sample(number)?  usually 16 or 24
            rf.writeBytes("data");                                 // 36 - data
            rf.write(intToByteArray((int)myDataSize), 0, 4);       // 40 - how big is this data chunk
            rf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static byte[] intToByteArray(int i)
    {
        byte[] b = new byte[4];
        b[0] = (byte) (i & 0x00FF);
        b[1] = (byte) ((i >> 8) & 0x000000FF);
        b[2] = (byte) ((i >> 16) & 0x000000FF);
        b[3] = (byte) ((i >> 24) & 0x000000FF);
        return b;
    }

    // convert a short to a byte array
    private static byte[] shortToByteArray(short data)
    {
        /*
         * NB have also tried:
         * return new byte[]{(byte)(data & 0xff),(byte)((data >> 8) & 0xff)};
         *
         */

        return new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};
    }
}
