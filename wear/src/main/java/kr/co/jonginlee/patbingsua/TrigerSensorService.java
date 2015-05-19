package kr.co.jonginlee.patbingsua;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TrigerSensorService extends IntentService implements SensorEventListener2 {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SENSING = "kr.co.jonginlee.patbingsua.action.sensing2";
    private static final String ACTION_BAZ = "kr.co.jonginlee.patbingsua.action.BAZ";

    // TODO: Rename parameters
    private static final String DELAY_INTERVAL = "kr.co.jonginlee.patbingsua.delay.interval2";
    private static final String RECORDED_FILENAME = "kr.co.jonginlee.patbingsua.extra.PARAM2";

    private static final int STATUS_SENSING_CHECKING = 0x0001;
    private static final int STATUS_SENSING_CONTINUOUS = 0x0002;
    private int audioTag = 0;

    private Sensor mAccelerometerSensor = null;
    private Sensor mLinearAccelSensor = null;
    private Sensor mCompassSensor = null;
    private Sensor mOrientationSensor = null;
    private Sensor mGyroSensor = null;
    private SensorManager mSensorManager = null;

    private static final String TAG = "WearWatch";
    private long sensorTimeReference = 0l;

    private FileOutputStream mFos = null;
    private File mfile = null;
    private String mfilename = null;
    private DataOutputStream mOutFile;

    private File mfile_trigger = null;
    private FileOutputStream mFos_trigger = null;
    private DataOutputStream mOutFile_trigger;


    private PowerManager.WakeLock wakelock = null;
    private int tagNum = 1;
    private double mPrevAccelValue = 0;
    private double mThreshold = 0.8;
    private int mSensingState = STATUS_SENSING_CHECKING;
    private long mStateTimeReference = 0;
    private int mBatchDelay = 0;
    private double[] mMovBuffer = new double[50];
    private int mMovBufferIndex = 0;
    private AudioRecorderAsWave mAudioRecorder;



    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate(SensorService)");

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        mCompassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mAudioRecorder = new AudioRecorderAsWave();

        super.onCreate();
        Log.d(TAG, "onCreate(SensorService) - pass");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "onHandleIntent(SensorService) - not null");
        } else
            Log.d(TAG, "onHandleIntent(SensorService) - null");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSensing(Context context, int interval, String filename) {
        Intent intent = new Intent(context, TrigerSensorService.class);
        intent.setAction(ACTION_SENSING);
        intent.putExtra(DELAY_INTERVAL, interval);
        intent.putExtra(RECORDED_FILENAME, filename);
//        intent.putExtra(EXTRA_PARAM2, "nothing");
        context.startService(intent);
    }






    private void freeRegisters() {

        if(mSensorManager.flush(this))
            Log.d(TAG, "flush FIFO queue - success");

//        mSensorManager.unregisterListener(this, this.mHeartRateSensor);
        mSensorManager.unregisterListener(this, this.mCompassSensor);
        mSensorManager.unregisterListener(this, this.mLinearAccelSensor);
        mSensorManager.unregisterListener(this, this.mAccelerometerSensor);
        mSensorManager.unregisterListener(this, this.mGyroSensor);
        mSensorManager.unregisterListener(this, this.mOrientationSensor);

//        stopAudioCapture();
//        mSensorManager.unregisterListener(this, this.mRotationVector);
        Log.d(TAG, "freeRegisters(TrigerSensorService) - pass");
    }


    @Override
    public void onDestroy() {
        wakelock.release();
        freeRegisters();
        mSensorManager.unregisterListener(mListener2, this.mLinearAccelSensor);
        if(mFos !=null){
            try {
                mOutFile.flush();
                mOutFile.close();
                mFos.flush();
                mFos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(mFos_trigger !=null){
            try {
                mOutFile_trigger.flush();
                mOutFile_trigger.close();
                mFos_trigger.flush();
                mFos_trigger.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "onDestroy(TrigerSensorService) - pass");

        super.onDestroy();
    }


    // TODO: Customize helper method
    public static void stopActionSensing(Context context) {
        Intent intent = new Intent(context, TrigerSensorService.class);
        context.stopService(intent);
    }


//    /**
//     * Starts this service to perform action Baz with the given parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    // TODO: Customize helper method
//    public static void startActionBaz(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, SensorService.class);
//        intent.setAction(ACTION_BAZ);
//        intent.putExtra(DELAY_INTERVAL, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }

    public TrigerSensorService() {
        super("TrigerSensorService");
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            final String action = intent.getAction();
//            if (ACTION_SENSING.equals(action)) {
//                final String param1 = intent.getStringExtra(DELAY_INTERVAL);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//
//
//                handleActionFoo(param1, param2);
//            }
////            else if (ACTION_BAZ.equals(action)) {
////                final String param1 = intent.getStringExtra(DELAY_INTERVAL);
////                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
////                handleActionBaz(param1, param2);
////            }
//        }
//    }


    private long sensorTimeReference2 = 0;

    private final SensorEventListener mListener2 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            if (sensorTimeReference2 == 0l)
                sensorTimeReference2 = sensorEvent.timestamp;

            long timeInMillis = Math.round((sensorEvent.timestamp - sensorTimeReference2) / 1000000.0);

            if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                if (timeInMillis > 1000)
                    if(!movBuffering(sensorEvent.values[0] + sensorEvent.values[1] + sensorEvent.values[2], 5)){
                        boolean isMoving = isMovement();

                        if(isMoving)
                            mStateTimeReference = timeInMillis;

                        if(isMoving && mSensingState == STATUS_SENSING_CHECKING)
                            doChangeSamplingRate(20*1000, STATUS_SENSING_CONTINUOUS,1*1000*1000);
                        else if((timeInMillis - mStateTimeReference)>5000 && (!isMoving && mSensingState == STATUS_SENSING_CONTINUOUS))
                            doChangeSamplingRate(100*1000, STATUS_SENSING_CHECKING,0);
                    }

                Log.d(TAG, " * trigger sensor - " + sensorEvent.sensor.getVendor() + " - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",Linearaccel," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + timeInMillis + "\r\n").getBytes();
                try {
                    mOutFile_trigger.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, " * triger sensor - " + sensor.getName() + ", accuracy - " + accuracy);
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



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(SensorService)");
        if(intent!=null){
            Log.d(TAG,""+intent.getFlags());
        }else
            Log.d(TAG,""+"d???");
//        super.onStartCommand(intent, flags, startId);

        mfilename = intent.getStringExtra(RECORDED_FILENAME);
//
//        if ((mfile == null) && (mfilename !=null)) {
//            mfile = new File(getExternalStorageDirectory(), mfilename+".txt");
//            try {
//                mFos = new FileOutputStream(mfile);
//                mOutFile = new DataOutputStream(new BufferedOutputStream(mFos));
//                byte[] data = new String("tag, type, x, y, z, time" + "\r\n").getBytes();
//                mOutFile.write(data);
//                mOutFile.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        if ((mfile_trigger == null) && (mfilename !=null)) {
            mfile_trigger = new File(getExternalStorageDirectory(), mfilename+"_triger_sensor.txt");
            try {
                mFos_trigger = new FileOutputStream(mfile_trigger);
                mOutFile_trigger = new DataOutputStream(new BufferedOutputStream(mFos_trigger));
                byte[] data = new String("tag, type, x, y, z, time" + "\r\n").getBytes();
                mOutFile_trigger.write(data);
                mOutFile_trigger.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        PowerManager mgr = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
        wakelock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakelock.acquire();

        String action = intent.getAction();
        int interval = intent.getIntExtra(DELAY_INTERVAL, 500 * 1000);
        if (ACTION_SENSING.equals(action)) {
            Log.d(TAG, "onStartCommand(SensorService) - ACTION_SENSING - pass");
            if (mSensorManager.registerListener(mListener2 , mLinearAccelSensor, interval, 0) == false) {
                Log.d(TAG, "batch is not supported : " + "linearaccel cnt: "+ 0 +", "+mLinearAccelSensor.getFifoMaxEventCount());
            }
//            registerSensor(20*1000, 8*1000*1000);
        }
//        else if(ACTION_SAMPLING_CH.equals(action)){
//            Log.d(TAG, "ACTION_SAMPLING_CH");
//            registerSensor(20 * 1000);
//        }

        return START_STICKY;
    }

    public void registerSensor(int interval, int batchDelay) {
        Log.d(TAG, "registerSensor(SensorService) - interval : " + interval);
        mBatchDelay = batchDelay;
//        latch = new CountDownLatch(1);
//         mBatchDelay = 5*1000*1000;
//        mBatchDelay = 8*1000*1000;
//        mBatchDelay = 0;
        if (mSensorManager.registerListener(this, mAccelerometerSensor, interval, mBatchDelay)) {
            Log.d(TAG, "batch is supported : " + "accel cnt: "+ mBatchDelay +", "+mAccelerometerSensor.getFifoReservedEventCount());
        }else
            Log.d(TAG, "batch is not supported "+mAccelerometerSensor.getName());

        if(mSensorManager.registerListener(this, mGyroSensor, interval, mBatchDelay)){
            Log.d(TAG,"batch is supported : "+"gyro cnt: "+ mBatchDelay +", "+mGyroSensor.getFifoReservedEventCount());
        }else
            Log.d(TAG, "batch is not supported "+mGyroSensor.getName());

        if(mSensorManager.registerListener(this, mCompassSensor, interval, mBatchDelay)){
            Log.d(TAG, "batch is supported : " + "compass cnt: "+ mBatchDelay +", "+mCompassSensor.getFifoReservedEventCount());
        }else
            Log.d(TAG, "batch is not supported "+mCompassSensor.getName());

        if (mSensorManager.registerListener(this, mLinearAccelSensor, interval, mBatchDelay)) {
            Log.d(TAG, "batch is supported : " + "linearaccel cnt: "+ mBatchDelay +", "+mLinearAccelSensor.getFifoMaxEventCount());
        }else
            Log.d(TAG, "batch is not supported "+mLinearAccelSensor.getName());

        if(mSensorManager.registerListener(this, mOrientationSensor,interval, mBatchDelay)){
            Log.d(TAG,"batch is supported : "+"orientation cnt: "+ mBatchDelay +", "+mOrientationSensor.getFifoMaxEventCount());
        }else
            Log.d(TAG, "batch is not supported "+mOrientationSensor.getName());

        Log.d(TAG, "start register sensors");

//        latch.countDown();
    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // grab the values and timestamp
        try {
            if (sensorTimeReference == 0l) {
                sensorTimeReference = sensorEvent.timestamp;
            }
            // set event timestamp to current time in milliseconds

            long timeInMillis = Math.round((sensorEvent.timestamp - sensorTimeReference) / 1000000.0);

//            Log.d(TAG, "time_milli : "+timeInMillis +", orignal : "+ sensorEvent.timestamp );

            if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                Log.d(TAG, "onSensorChanged(SensorService) - "+sensorEvent.sensor.getVendor()+" - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum+",Magnet," + sensorEvent.values[0] +","+ sensorEvent.values[1]+","+ sensorEvent.values[2] +","+timeInMillis+"\r\n").getBytes();
                mOutFile.write(data);
            } else
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d(TAG, "onSensorChanged(SensorService) - "+sensorEvent.sensor.getVendor()+" - "  + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",Accel," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + timeInMillis + "\r\n").getBytes();
                mOutFile.write(data);
            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                Log.d(TAG, "onSensorChanged(SensorService) - "+sensorEvent.sensor.getVendor()+" - "  + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",Linearaccel," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + timeInMillis + "\r\n").getBytes();
                mOutFile.write(data);
            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.d(TAG, "onSensorChanged(SensorService) - "+sensorEvent.sensor.getVendor()+" - "  + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",Gyro," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + timeInMillis + "\r\n").getBytes();
                mOutFile.write(data);
            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                Log.d(TAG, "onSensorChanged(SensorService) - "+sensorEvent.sensor.getVendor()+" - "  + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",orientation," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + timeInMillis + "\r\n").getBytes();
                mOutFile.write(data);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
//        new SensorEventLoggerTask().execute(event);
//        stopSelf();
    }

    private boolean isMovement() {
        double sum = 0 ;
        for(double v : mMovBuffer)
            sum += v;
        double mean = sum/mMovBufferIndex;
        sum = 0;
        for(double v :mMovBuffer)
            sum += (mean-v)*(mean-v);
        double variance = sum/mMovBufferIndex;

        mMovBufferIndex = 0;

        if(variance > 0.2){
            Log.d(TAG, "===================> IS movement!! ,"+variance);
            return true;
        }
        else{
            Log.d(TAG, "===================> NO movement!! ,"+variance);
            return false;
        }
    }

    private boolean movBuffering(double value, int limit) {

        mMovBuffer[mMovBufferIndex] = value;
        mMovBufferIndex++;

        Log.d(TAG, "===================> MOV Buffering!!, " + mMovBufferIndex);

        if (mMovBufferIndex >= limit){
            return false;
        }
        else
            return true;
    }

    private void doChangeSamplingRate(int interval, int status, int batchdelay) {

        Log.d(TAG, "(in) doChangeSamplingRate");
//        if(mSensingState == STATUS_SENSING_CHECKING)
//        {
//            mBatchDelay = 0;
//            if (mSensorManager.registerListener(this, mLinearAccelSensor, interval, mBatchDelay) == false) {
//                Log.d(TAG, "batch is not supported : " + "linearaccel cnt: "+ mBatchDelay +", "+mLinearAccelSensor.getFifoMaxEventCount());
//            }
//        }
//        else
//
        if(status == STATUS_SENSING_CONTINUOUS)
        {
            mSensingState = status;
//            mSensorManager.unregisterListener(mListener2, this.mLinearAccelSensor);
//            registerSensor(interval, 2 * 1000 * 1000);
//            if (mSensorManager.registerListener(this, mLinearAccelSensor, interval, batchdelay) == false) {
//                Log.d(TAG, "batch is not supported : " + "linearaccel cnt: "+ batchdelay +", "+mLinearAccelSensor.getFifoMaxEventCount());
//            }

            if(mAudioRecorder!=null) {
                DateFormat dateFormat = new SimpleDateFormat("HH_mm_ss");
                Date date = new Date();
                Log.d(TAG,"audio_start_time " + dateFormat.format(date));
                mAudioRecorder.startAudioCapture(mfilename + "_" + dateFormat.format(date) + "_" + audioTag + ".wav");
                audioTag++;
            }

            Log.d(TAG," STATUS --> [CONTINUOUS] ");
            Toast.makeText(getApplicationContext(), "STATUS --> [CONTINUOUS]", Toast.LENGTH_SHORT).show();
            MonitoringActivity.setBackgroundColor(Color.RED);
        }
        else if(status == STATUS_SENSING_CHECKING)
        {
            mSensingState = status;
//            freeRegisters();
            if(mAudioRecorder!=null)
                mAudioRecorder.stopAudioCapture();

//            if (mSensorManager.registerListener(mListener2, mLinearAccelSensor, interval, batchdelay) == false) {
//                Log.d(TAG, "batch is not supported : " + "linearaccel cnt: "+ batchdelay +", "+mLinearAccelSensor.getFifoMaxEventCount());
//            }

            Log.d(TAG," STATUS --> [CHECKING] ");
            Toast.makeText(getApplicationContext(), "STATUS --> [CHECKING]", Toast.LENGTH_SHORT).show();
            MonitoringActivity.setBackgroundColor(Color.BLACK);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged(SensorService) - " + sensor.getName() + ", accuracy - " + accuracy);
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


    @Override
    public void onFlushCompleted(Sensor sensor) {
        Log.d(TAG, "onFlushCompleted(SensorService) - " + sensor.getName());
    }
}
