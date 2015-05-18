package kr.co.jonginlee.patbingsua;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

public class MonitoringActivity extends Activity {

    private int mTagNum = 1;

    private Button mTagButton;
    private boolean isStart = false;
    private static LinearLayout mTopLayout;
    private GoogleApiClient mGoogleApiClient;
    private static int SENSOR_TYPE_TILT_DETECTOR = 65536;

    private static final String TAG = "WearWatch";
    private final TriggerEventListener mListener = new TriggerEventListener() {
        @Override
        public void onTrigger(TriggerEvent event) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            Log.d(TAG," @@@@@ onTrigger - " + event.sensor.getName() + " , "+dateFormat.format(date));
            Toast.makeText(getApplicationContext(), "- motion detected -\r\n- "+dateFormat.format(date)+" -", Toast.LENGTH_SHORT).show();
        }
    };

    private final SensorEventListener mListener2 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            Toast.makeText(getApplicationContext(), "- tilt detected -\r\n- "+dateFormat.format(date)+" -", Toast.LENGTH_SHORT).show();
            Log.d(TAG, event.values[0] +" , "+event.sensor.getType() +", "+event.sensor.getName() +", "+event.timestamp);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "onAccuracyChanged(Tilt Detector) - " + sensor.getName() + ", accuracy - " + accuracy);
            if(SensorManager.SENSOR_STATUS_ACCURACY_HIGH == accuracy){
                Log.d(TAG, "accuracy - high");
            }else if(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM == accuracy){
                Log.d(TAG, "accuracy - medium");
            }else if(SensorManager.SENSOR_STATUS_ACCURACY_LOW == accuracy){
                Log.d(TAG, "accuracy - low");
            }else if(SensorManager.SENSOR_STATUS_NO_CONTACT == accuracy){
                Log.d(TAG, "accuracy - no contact");
            }else if(SensorManager.SENSOR_STATUS_UNRELIABLE == accuracy){
                Log.d(TAG, "accuracy - unreliable");
            }
        }
    };

    private Sensor mSigMotion;
    private SensorManager mSensorManager;
    private Sensor mTiltDetector;

    // for recording functions below
    private static final int SPEECH_REQUEST_CODE = 1;
    private static final int RECORDER_SAMPLERATE = 22100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private volatile boolean isRecording;
    private FileOutputStream mFos;
    private int mNumOfBlock = 0;
    private String RECORDED_FILE_NAME = "recorded.wav";
    private long mSizeOfRecData = 0;
    private DataOutputStream outFile;
    private AudioRecorderAsWave mAudioRecorder = null;
//    private DataOutputStream dos;
//    private byte[] clipData;
//    private ByteArrayOutputStream recData;


    public void handleRecordButtonClick(View view) {
        if(mAudioRecorder!=null)
            mAudioRecorder.startAudioCapture(null);
    }

    public void handleStopButtonClick(View view) {
        if(mAudioRecorder!=null)
            mAudioRecorder.stopAudioCapture();
    }

//    private void startAudioCapture() {
//        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
//        Log.v(TAG, "Starting audio capture"+" buffer size ("+bufferSize+")");
//
//        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
//        byte[] header = new byte[44];
//        try {
//            mFos = new FileOutputStream(new File(getExternalStorageDirectory(), RECORDED_FILE_NAME));
//            BufferedOutputStream bos = new BufferedOutputStream(mFos);
//            outFile = new DataOutputStream(bos);
//            outFile.write(header, 0, 44);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
////        recData = new ByteArrayOutputStream();
////        dos = new DataOutputStream(recData);
//
//        if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
//            recorder.startRecording();
//            isRecording = true;
//            Log.v(TAG, "Successfully started recording " + bufferSize + " byte");
//
//            recordingThread = new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    processRawAudioData();
//                }
//            }, "AudioRecorder Thread");
//
//            recordingThread.start();
//        } else {
//            Log.v(TAG, "Failed to started recording");
//        }
//    }
//
//    private void processRawAudioData() {
//        short data[] = new short[bufferSize];
//        int read = 0;
//        while(isRecording) {
//            read = recorder.read(data, 0, bufferSize);
//
//            if(AudioRecord.ERROR_INVALID_OPERATION != read) {
//                Log.v(TAG, "Successfully read " + data.length + " bytes of audio , read "+read);
//                for(int i = 0; i < read;i++) {
//                    try {
////                        dos.writeShort(data[i]);
//                        writeShortLE(outFile, data[i]);
////                        outFile.write(data[i]);
//                        mSizeOfRecData+=1;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//
//    public static void writeShortLE(DataOutputStream out, short value) throws IOException {
//        out.writeByte(value & 0xFF);
//        out.writeByte((value >> 8) & 0xFF);
//    }
//
//    private void stopAudioCapture() {
//        Log.v(TAG, "Stop audio capture");
//
//
//
//        if(recorder.getState() != AudioRecord.STATE_UNINITIALIZED ){
//            recorder.stop();
//
////            clipData = mFos;
////            Log.v(TAG, "clipData length "+clipData.length);
//
//            isRecording = false;
//            recorder.release();
//
//            try {
//                outFile.flush();
//                outFile.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            attachingHeadInfoWAV();
//        }
//
//
//        //int totalDataLen, int longSampleRate, int byteRate, byte channels, byte RECORDER_BPP, int totalAudioLen, int blockAlign
//
////
//
//
//    }
//
//    private void attachingHeadInfoWAV(){
//        try {
//            long mySubChunk1Size = 16;
//            int myBitsPerSample= 16;
//            int myFormat = 1;
//            long myChannels = 1;
//            long mySampleRate = 22100;
//            long myByteRate = mySampleRate * myChannels * myBitsPerSample/8;
//            int myBlockAlign = (int) (myChannels * myBitsPerSample/8);
//
////            byte[] clipData = getBytesFromFile(fileToConvert);
//
////            long myDataSize = clipData.length;
//            long myDataSize = mSizeOfRecData;
//            long myChunk2Size =  myDataSize * myChannels * myBitsPerSample/8;
//            long myChunkSize = 36 + myChunk2Size;
//
////            OutputStream os;
////            os = new FileOutputStream(new File(getExternalStorageDirectory(), RECORDED_FILE_NAME));
////            BufferedOutputStream bos = new BufferedOutputStream(os);
////            DataOutputStream outFile = new DataOutputStream(bos);
//
//            RandomAccessFile rf = new RandomAccessFile(new File(getExternalStorageDirectory(), RECORDED_FILE_NAME), "rw");
//            rf.seek(0);
//
//
//            rf.writeBytes("RIFF");                                 // 00 - RIFF
//            rf.write(intToByteArray((int) myChunkSize), 0, 4);      // 04 - how big is the rest of this file?
//            rf.writeBytes("WAVE");                                 // 08 - WAVE
//            rf.writeBytes("fmt ");                                 // 12 - fmt
//            rf.write(intToByteArray((int) mySubChunk1Size), 0, 4);  // 16 - size of this chunk
//            rf.write(shortToByteArray((short)myFormat), 0, 2);     // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
//            rf.write(shortToByteArray((short) myChannels), 0, 2);   // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
//            rf.write(intToByteArray((int)mySampleRate), 0, 4);     // 24 - samples per second (numbers per second)
//            rf.write(intToByteArray((int) myByteRate), 0, 4);       // 28 - bytes per second
//            rf.write(shortToByteArray((short)myBlockAlign), 0, 2); // 32 - # of bytes in one sample, for all channels
//            rf.write(shortToByteArray((short) myBitsPerSample), 0, 2);  // 34 - how many bits in a sample(number)?  usually 16 or 24
//            rf.writeBytes("data");                                 // 36 - data
//            rf.write(intToByteArray((int)myDataSize), 0, 4);       // 40 - how big is this data chunk
////            outFile.write(clipData);                                    // 44 - the actual data itself - just a long string of numbers
//
//
////            rf.flush();
//            rf.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private static byte[] intToByteArray(int i)
//    {
//        byte[] b = new byte[4];
//        b[0] = (byte) (i & 0x00FF);
//        b[1] = (byte) ((i >> 8) & 0x000000FF);
//        b[2] = (byte) ((i >> 16) & 0x000000FF);
//        b[3] = (byte) ((i >> 24) & 0x000000FF);
//        return b;
//    }
//
//    // convert a short to a byte array
//    public static byte[] shortToByteArray(short data)
//    {
//        /*
//         * NB have also tried:
//         * return new byte[]{(byte)(data & 0xff),(byte)((data >> 8) & 0xff)};
//         *
//         */
//
//        return new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(MonitoringActivity)");
        setContentView(R.layout.activity_monitoring);
        setUpGoogleApiClient();
        setUpView();
        setUpMessageReceiver();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mAudioRecorder = new AudioRecorderAsWave();
        try {
            setUpGettingSensorList();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        setUpWakeupSensor();

        Log.d(TAG, "onCreate(MonitoringActivity) - pass");
    }

    private void setUpGettingSensorList() throws IOException {
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));


        String mfilename = "sensorInfo.txt";
        File mfile = new File(getExternalStorageDirectory(), mfilename);
        FileOutputStream mfos = null;

        mfos = new FileOutputStream(mfile);
        byte[] data = ("sensor_name, type, vendor(moduleVer), maximumRange, delaymin~max(microsec), power(mA), resolution, FIFO_Rserved, FIFO_max" + "\r\n").getBytes();
        mfos.write(data);


        Log.d(TAG,"name \t type \t vendor \t moduleVer \t maximumRange \t delay(min~max) \t power(mA) \t resolution \t FIFO_Rserved \t FIFO_max");
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(int i = 0 ; i<sensorList.size() ; i++)
        {
            Sensor sensor = sensorList.get(i);
//            Log.d(TAG, sensor.getName()+" FIFO_event_count : "+sensor.getFifoMaxEventCount() +" , "+sensor.getType());
//            byte[] data = new String("sensor_name, type, vendor(moduleVer), maximumRange, delaymin~max(microsec), power(mA), resolution, FIFO_Rserved, FIFO_max"+"\r\n").getBytes();
            data = (sensor.getName() + "," + sensor.getType() + "," + sensor.getVendor() + "(" + sensor.getVersion() + ")," +
                    sensor.getMaximumRange() + "," +
                    sensor.getMinDelay() + "~" + sensor.getMaxDelay() + "," + sensor.getPower() + "," + sensor.getResolution() +
                    "," + sensor.getFifoReservedEventCount() + "," + sensor.getFifoMaxEventCount() + "\r\n").getBytes();
            mfos.write(data);

            Log.d(TAG, sensor.getName() + "\t" + sensor.getType() + "\t" + sensor.getVendor() + "\t" + sensor.getVersion() + "\t" +
                            sensor.getMaximumRange() + "\t" +
                            sensor.getMinDelay()+"~"+sensor.getMaxDelay() + "\t" + sensor.getPower() + "\t" + sensor.getResolution() +"\t"+sensor.getFifoReservedEventCount() +
                            "\t" + sensor.getFifoMaxEventCount()
            );

            if(sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                Log.d(TAG," accel "+sensor.getType());
            if(sensor.isWakeUpSensor())
                Log.d(TAG, "    - wakeupsensor");
        }

        mfos.flush();
        mfos.close();
        Log.d(TAG, "write sensorInfo.txt - success");
    }

    private void setUpWakeupSensor() {
        // test - significant motion sensor

//        getDefaultSensor(SENSOR_TYPE_TILT_DETECTOR)
        mSigMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        if(mSigMotion!=null){
            Log.d(TAG,"name "+mSigMotion.getName()+ ", "+mSigMotion.isWakeUpSensor() +", "+mSigMotion.getFifoMaxEventCount());
            mSensorManager.requestTriggerSensor(mListener, mSigMotion);
        }
        else
            Log.d(TAG,"sigmotion sensor is null");

        mTiltDetector = mSensorManager.getDefaultSensor(SENSOR_TYPE_TILT_DETECTOR,true);

        if(mTiltDetector!=null){
            Log.d(TAG,"name "+mTiltDetector.getName()+ ", "+mTiltDetector.isWakeUpSensor() +", "+mTiltDetector.getFifoMaxEventCount());
            mSensorManager.registerListener(mListener2,mTiltDetector,SensorManager.SENSOR_DELAY_NORMAL);
        }
        else{
            Log.d(TAG, "tilt sensor is null");
        }
    }

    private void setUpMessageReceiver() {
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        Log.d(TAG, "setUpMessageReceiver(MonitoringActivity) - pass");
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive(MessageReceiver)");

            String message = intent.getStringExtra("message");
            String dataMessage = intent.getStringExtra("dataMessage");
            // Display message in UI

            if(message!=null){

                Toast.makeText(getApplicationContext(), "received MSG success", Toast.LENGTH_LONG).show();

                //startButtonProc();
//                    new MyAsyncTask().execute(message);

            }
            else if(dataMessage!=null){

                Toast.makeText(getApplicationContext(), "received DATA success", Toast.LENGTH_LONG).show();
                //mStatus.setText("Recived Message : " + "Success");
//                new MyAsyncTask().execute(dataMessage);
            }


        }
    }


    public static void setBackgroundColor(int color)
    {
        if(mTopLayout!=null)
        {
            mTopLayout.setBackgroundColor(color);
        }

    }
    private void setUpView() {
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTagButton = (Button) findViewById(R.id.tag);
                mTagButton.setText("START "+mTagNum);
                mTopLayout = (LinearLayout) stub.findViewById(R.id.toplayout);
//                mStartRecordingButton = ()

                if (!mTagButton.hasOnClickListeners()) {
                    mTagButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (isStart == false) {
                                SensorService.startActionSensing(getApplicationContext(), 100 * 1000,"data_watch_intentservice"+mTagNum);
                                mTagButton.setText("STOP");
//                                mTagButton.setBackgroundColor(Color.DKGRAY);
                                mTopLayout.setBackgroundColor(Color.BLACK);
                                isStart = true;

                            } else if (isStart) {
                                mTagNum++;
                                SensorService.stopActionSensing(getApplicationContext());
                                mTagButton.setText("START "+mTagNum);
                                mTopLayout.setBackgroundColor(Color.BLUE);
                                isStart = false;

                            }
                        }

                    });
                }
            }
        });
        Log.d(TAG, "setUpView(MonitoringActivity) - pass");

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy(MonitoringActivity) - pass");
        SensorService.stopActionSensing(getApplicationContext());

//        mSensorManager.cancelTriggerSensor(mListener, mSigMotion);
//        mSensorManager.unregisterListener(mListener2);

        super.onDestroy();
    }

    private void setUpGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected(mGoogleApiClient): " + connectionHint);
                        // Now you can use the Data Layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended(mGoogleApiClient): " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed(mGoogleApiClient): " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        Log.d(TAG, "setUpGoogleApiClient(MonitoringActivity) - pass");
    }
}
