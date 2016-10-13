package kr.co.jonginlee.patbingsua;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SensorService extends IntentService implements SensorEventListener2 {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SENSING = "kr.co.jonginlee.patbingsua.action.sensing";
    private static final String ACTION_BAZ = "kr.co.jonginlee.patbingsua.action.BAZ";
    // TODO: Rename parameters
    private static final String DELAY_INTERVAL = "kr.co.jonginlee.patbingsua.delay.interval";
    private static final String SENSOR_NAME = "kr.co.jonginlee.patbingsua.sensorname";
    private static final String RECORDED_FILENAME = "kr.co.jonginlee.patbingsua.extra.PARAM2";
    private static final int STATUS_SENSING_CHECKING = 0x0001;
    private static final int STATUS_SENSING_CONTINUOUS = 0x0002;
    private static final String TAG = MonitoringActivity.TAG;
    private static final int TYPE_HEARTRATE_MONITOR = 21;
    private static GoogleApiClient mGoogleApiClient;
    float[] mR = new float[9];
    float[] prevmR = new float[9];
    float[] mI = new float[9];
    float[] mV = new float[3];
    float pit = 0;
    float rol = 0;
    Float baseAzimuth;
    Float basePitch;
    Float baseRoll;
    DataMap mDataMap = new DataMap();
    DataMap mDataMap2 = new DataMap();
    private int audioTag = 0;
    private Sensor mAccelerometerSensor = null;
    private Sensor mLinearAccelSensor = null;
    private Sensor mCompassSensor = null;
    private Sensor mOrientationSensor = null;
    private Sensor mGyroSensor = null;
    private Sensor mRotationVectorSensor;
    private SensorManager mSensorManager = null;
    private long sensorTimeReference = 0l;
    private FileOutputStream mFos = null;
    private File mfile = null;
    private String mfilename = null;
    private DataOutputStream mOutFile;
    private FileOutputStream mFos2 = null;
    private File mfile2 = null;
    private String mfilename2 = null;
    private DataOutputStream mOutFile2;
    private FileOutputStream mFos3 = null;
    private File mfile3 = null;
    private String mfilename3 = null;
    private DataOutputStream mOutFile3;
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
    //    private AudioRecorderAsWave mAudioRecorder;
    private Sensor mHeartRateSensor;
    private Sensor mBarometerSensor;
    //    private AudioRecorder mAudioRecorder2;
//    private ExtAudioRecorder mExtAudioRecorder;
    private long mStartTimeMilli;
    private String mStartTimeHour;
    private long[] mVibrationPattern;
    private float[] mGravs = null;
    private float[] mGeoMags = null;
    private float[] mRotationM = new float[9];
    private Sensor mGravity = null;
    private TextView mTxtOrient;
    private float xrot;					//X Rotation
    private float yrot;					//Y Rotation
    private long myTimeReference =0;
    private float[] mRot;
    private CountDownLatch latch;
    private String mLogsBuffer = "";
    private int mLogBufferLimit = 10;
    private int mLogBufferIdx = 0;


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
private CountDownLatch latch2;

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
private String mLogsBuffer2 = "";
    private int mLogBufferLimit2 = 152;
    private int mLogBufferIdx2 = 0;

    public SensorService() {
        super("SensorService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSensing(GoogleApiClient googleApiClient, Context context, int interval, String sensorname, String filename) {
        mGoogleApiClient = googleApiClient;
        Intent intent = new Intent(context, SensorService.class);
        intent.setAction(ACTION_SENSING);
        intent.putExtra(DELAY_INTERVAL, interval);
        intent.putExtra(SENSOR_NAME, sensorname);
        intent.putExtra(RECORDED_FILENAME, filename);
//        intent.putExtra(EXTRA_PARAM2, "nothing");
        context.startService(intent);
    }

    // TODO: Customize helper method
    public static void stopActionSensing(Context context) {
        Intent intent = new Intent(context, SensorService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate(SensorService)");

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        mBarometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(TYPE_HEARTRATE_MONITOR);
        mCompassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

//        mAudioRecorder = new AudioRecorderAsWave();
//        mAudioRecorder2 = new AudioRecorder();
//        mExtAudioRecorder = ExtAudioRecorder.getInstanse(true);

//        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//        Intent batteryStatus = this.registerReceiver(null, ifilter);
//
//        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
//                status == BatteryManager.BATTERY_STATUS_FULL;
//        Log.d(TAG,"isCharging "+isCharging);
//
//        int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
//        Log.d(TAG,"temp "+temp);


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

    private void freeRegisters() {

        if (mSensorManager.flush(this))
            Log.d(TAG, "flush FIFO queue - success");

//        mSensorManager.unregisterListener(this, this.mBarometerSensor);
//        mSensorManager.unregisterListener(this, this.mHeartRateSensor);
        mSensorManager.unregisterListener(this, this.mCompassSensor);
        mSensorManager.unregisterListener(this, this.mLinearAccelSensor);
        mSensorManager.unregisterListener(this, this.mAccelerometerSensor);
        mSensorManager.unregisterListener(this, this.mGyroSensor);
        mSensorManager.unregisterListener(this, this.mOrientationSensor);
        mSensorManager.unregisterListener(this, this.mGravity);
//        stopAudioCapture();
        mSensorManager.unregisterListener(this, this.mRotationVectorSensor);
        Log.d(TAG, "freeRegisters(SensorService) - pass");
    }

    @Override
    public void onDestroy() {
        wakelock.release();
        freeRegisters();
//        mSensorManager.unregisterListener(mListener2, this.mLinearAccelSensor);
        if (mFos != null) {
            try {
                mOutFile.flush();
                mOutFile.close();
                mFos.flush();
                mFos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mFos2 != null) {
            try {
                mOutFile2.flush();
                mOutFile2.close();
                mFos2.flush();
                mFos2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mFos3 != null) {
            try {
                mOutFile3.flush();
                mOutFile3.close();
                mFos3.flush();
                mFos3.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mFos_trigger != null) {
            try {
                mOutFile_trigger.flush();
                mOutFile_trigger.close();
                mFos_trigger.flush();
                mFos_trigger.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "onDestroy(SensorService) - pass");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(SensorService)");

        mVibrationPattern = new long[]{75,25,75,25,75,25,75,525,75,25,75,25,75,25,75,25,75,25,75,25,75,225,75,25,75,25,75,25,75,225,75,25,75,25,75,25,75,525,75,25,75,25,75,25,75,25,75,25,75,25,75,225,75,25,75,25,75,25,75,225};

        String filename =  intent.getStringExtra(RECORDED_FILENAME);
        mfilename = filename +"_sensor_data";
        mfilename2 = filename+"_active_mv";
        mfilename3 = filename+"_rotaion";

        if ((mfile == null) && (mfilename !=null)) {
            mfile = new File(getExternalStorageDirectory(), mfilename+".txt");
            try {
                mFos = new FileOutputStream(mfile);
                mOutFile = new DataOutputStream(new BufferedOutputStream(mFos));
                byte[] data = new String("tag, type, x, y, z, ad1, time, real_time, acc" + "\r\n").getBytes();
                mOutFile.write(data);
                mOutFile.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if ((mfile2 == null) && (mfilename2 !=null)) {
            mfile2 = new File(getExternalStorageDirectory(), mfilename2+".txt");
            try {
                mFos2 = new FileOutputStream(mfile2);
                mOutFile2 = new DataOutputStream(new BufferedOutputStream(mFos2));
                byte[] data2 = new String("tag, milli, resl_time, hour, elapsed, epoch " + "\r\n").getBytes();
                mOutFile2.write(data2);
                mOutFile2.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if ((mfile3 == null) && (mfilename3 !=null)) {
            mfile3 = new File(getExternalStorageDirectory(), mfilename3+".txt");
            try {
                mFos3 = new FileOutputStream(mfile3);
                mOutFile3 = new DataOutputStream(new BufferedOutputStream(mFos3));
                byte[] data3 = new String("tag, azimuth, pitch, roll, pitchDiff, rollDiff, xrot, yrot, m0, m1, m2, m3, m4, m5, m6, m7, m8 , time, real_time" + "\r\n").getBytes();
                mOutFile3.write(data3);
                mOutFile3.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        PowerManager mgr = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
        wakelock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakelock.acquire();

        String action = intent.getAction();
        int interval = intent.getIntExtra(DELAY_INTERVAL, 500 * 1000);
        String sensorname = intent.getStringExtra(SENSOR_NAME);

        if (ACTION_SENSING.equals(action)) {
            Log.d(TAG, "onStartCommand(SensorService) - ACTION_SENSING - pass");
//            if (mSensorManager.registerListener(mListener2 , mLinearAccelSensor, interval, 0) == false) {
//                Log.d(TAG, "batch is not supported : " + "linearaccel cnt: "+ 0 +", "+mLinearAccelSensor.getFifoMaxEventCount());
//            }
            registerSensor(interval, sensorname, 8 * 1000 * 1000);
//            registerSensor(interval, 0);
        }
//        else if(ACTION_SAMPLING_CH.equals(action)){
//            Log.d(TAG, "ACTION_SAMPLING_CH");
//            registerSensor(20 * 1000);
//        }

        return START_STICKY;
    }

    public void registerSensor(int interval, String sensorname, int batchDelay) {
        Log.d(TAG, "registerSensor(SensorService) - interval : " + interval);
//        mBatchDelay = batchDelay;
//        interval = 50 * 1000;
        mBatchDelay = 0;
//        latch = new CountDownLatch(1);
//         mBatchDelay = 5*1000*1000;
//        mBatchDelay = 8*1000*1000;
//        mBatchDelay = 0;
        Toast.makeText(getApplicationContext(), "interval " + interval + ", sensorname" + sensorname, Toast.LENGTH_SHORT).show();

        if (sensorname.equalsIgnoreCase("not selected")) {

        } else if (sensorname.contentEquals("Accelerometer")) {
            if (mSensorManager.registerListener(this, mAccelerometerSensor, interval, mBatchDelay)) {
                Log.d(TAG, "batch is supported : " + "accel cnt: " + mBatchDelay + ", " + mAccelerometerSensor.getFifoReservedEventCount());
            } else
                Log.d(TAG, "batch is not supported " + mAccelerometerSensor.getName());

        } else if (sensorname.contentEquals("Linear_Accelerometer")) {
            if (mSensorManager.registerListener(this, mLinearAccelSensor, interval, mBatchDelay)) {
                Log.d(TAG, "batch is supported : " + "linearaccel cnt: " + mBatchDelay + ", " + mLinearAccelSensor.getFifoMaxEventCount());
            } else
                Log.d(TAG, "batch is not supported " + mLinearAccelSensor.getName());

        } else if (sensorname.contentEquals("Gyroscope")) {
            if (mSensorManager.registerListener(this, mGyroSensor, interval, mBatchDelay)) {
                Log.d(TAG, "batch is supported : " + "gyro cnt: " + mBatchDelay + ", " + mGyroSensor.getFifoReservedEventCount());
            } else {
                Log.d(TAG, "batch is not supported " + mGyroSensor.getName());
            }

        } else if (sensorname.contentEquals("Magnetometer_(compass)")) {
            if (mSensorManager.registerListener(this, mCompassSensor, interval, mBatchDelay)) {
                Log.d(TAG, "batch is supported : " + "compass cnt: " + mBatchDelay + ", " + mCompassSensor.getFifoReservedEventCount());
            } else {
                Log.d(TAG, "batch is not supported " + mCompassSensor.getName());
            }

        } else if (sensorname.contentEquals("Rotation_Vector")) {
            if (mSensorManager.registerListener(this, mRotationVectorSensor, interval, mBatchDelay)) {
                Log.d(TAG, "batch is supported : " + "mRotationVectorSensor cnt: " + mBatchDelay + ", " + mRotationVectorSensor.getFifoReservedEventCount());
            } else
                Log.d(TAG, "batch is not supported " + mRotationVectorSensor.getName());

        } else if (sensorname.contentEquals("Orientation")) {
            if (mSensorManager.registerListener(this, mOrientationSensor, interval, mBatchDelay)) {
                Log.d(TAG, "batch is supported : " + "orientation cnt: " + mBatchDelay + ", " + mOrientationSensor.getFifoMaxEventCount());
            } else
                Log.d(TAG, "batch is not supported " + mOrientationSensor.getName());

        }


//        if(mSensorManager.registerListener(this, mGravity,interval, mBatchDelay)){
//            Log.d(TAG,"batch is supported : "+"orientation cnt: "+ mBatchDelay +", "+mOrientationSensor.getFifoMaxEventCount());
//        }else
//            Log.d(TAG, "batch is not supported "+mGravity.getName());

//        if(mSensorManager.registerListener(this, mHeartRateSensor, 1*1000*1000, mBatchDelay)){
//            Log.d(TAG,"batch is supported : "+"orientation cnt: "+ mBatchDelay +", "+mHeartRateSensor.getFifoMaxEventCount());
//        }else
//            Log.d(TAG, "batch is not supported "+mHeartRateSensor.getName());

//        if(mSensorManager.registerListener(this, mBarometerSensor, 1*1000*1000, mBatchDelay)){
//            Log.d(TAG,"batch is supported : "+"gyro cnt: "+ mBatchDelay +", "+mBarometerSensor.getFifoReservedEventCount());
//        }else {
//            Log.d(TAG, "batch is not supported " + mBarometerSensor.getName());
//        }


        Log.d(TAG, "start register sensors");

//        latch.countDown();sdf
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // grab the values and timestamp
        try {

            if (sensorTimeReference == 0l&& myTimeReference == 0l) {
                sensorTimeReference = sensorEvent.timestamp;
                myTimeReference = System.currentTimeMillis();
            }
            // set event timestamp to current time in milliseconds

            long timeInMillis = Math.round((sensorEvent.timestamp - sensorTimeReference) / 1000000.0);
//            double realtimeInMillis = sensorEvent.timestamp/1000000.0;

//            if(timeInMillis % 1000*60*30 == 0)
//            {
//                myTimeReference = System.currentTimeMillis();
//            }


//            DateFormat dateFormat = new SimpleDateFormat("(yyyy-MM-dd)-HH-mm-ss");
//            Date date = new Date();
//            mStartTimeHour =dateFormat.format(date);
            long realtimeInMillis = myTimeReference + timeInMillis;

//                Log.d(TAG,"audio_start_time " + dateFormat.format(date));

//            Log.d(TAG, "time_milli : "+timeInMillis +", orignal : "+ sensorEvent.timestamp );

            if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
//                Log.d(TAG, "onSensorChanged(SensorService) - " + sensorEvent.sensor.getVendor() + " - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum+",Magnet," + sensorEvent.values[0] +","+ sensorEvent.values[1]+","+ sensorEvent.values[2] +","+0+"," +timeInMillis+ "," + realtimeInMillis +","+sensorEvent.accuracy +"\r\n").getBytes();
                mOutFile.write(data);
                insertBufferLogs2("logs", new String(tagNum + ",magnetometer," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + 0 + "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));

            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d(TAG, "onSensorChanged(SensorService) - " + sensorEvent.sensor.getVendor() + " - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",Accel," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," +0+"," + timeInMillis + "," + realtimeInMillis +","+sensorEvent.accuracy + "\r\n").getBytes();
                mOutFile.write(data);
                insertBufferLogs2("logs", new String(tagNum + ",accelerometer," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + 0 + "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));

            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
//                if (timeInMillis > 1000)
//                    if(!movBuffering(sensorEvent.values[0] + sensorEvent.values[1] + sensorEvent.values[2], 10)){
//                        boolean isMoving = isMovement();
//
//                        if(isMoving)
//                            mStateTimeReference = timeInMillis;
//
//                        if(isMoving && mSensingState == STATUS_SENSING_CHECKING)
//                            doChangeSamplingRate(20*1000, STATUS_SENSING_CONTINUOUS,1*1000*1000, timeInMillis, realtimeInMillis);
//                        else if((timeInMillis - mStateTimeReference)>3000 && (!isMoving && mSensingState == STATUS_SENSING_CONTINUOUS))
//                            doChangeSamplingRate(100*1000, STATUS_SENSING_CHECKING,0, timeInMillis, realtimeInMillis);
//                    }

//                Log.d(TAG, "onSensorChanged(SensorService) - " + sensorEvent.sensor.getVendor() + " - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",Linearaccel," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + ","+0+"," + timeInMillis + "," + realtimeInMillis +","+sensorEvent.accuracy + "\r\n").getBytes();
                mOutFile.write(data);
                insertBufferLogs2("logs", new String(tagNum + ",Linearaccel," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + 0 + "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));

//                insertBufferLogs("logs", new String(tagNum + ",Linearaccel," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + 0 + "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));
            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//                Log.d(TAG, "onSensorChanged(SensorService) - " + sensorEvent.sensor.getVendor() + " - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",Gyro," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," +0+","+ timeInMillis + "," + realtimeInMillis +","+sensorEvent.accuracy + "\r\n").getBytes();
                mOutFile.write(data);
                insertBufferLogs2("logs", new String(tagNum + ",Gyro," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + 0 + "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));

            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//                Log.d(TAG, "onSensorChanged(SensorService) - " + sensorEvent.sensor.getVendor() + " - " + sensorEvent.sensor.getName() + " - RotationVector " + sensorEvent.values[0] + ", milli " + timeInMillis);
                //Toast.makeText(getApplicationContext(), "  RotationVector " + sensorEvent.values[0]+" ", Toast.LENGTH_SHORT).show();
                float[] Q = new float[4];
                SensorManager.getQuaternionFromVector(Q, sensorEvent.values);
                byte[] data = new String(tagNum + ",RotationVector," + Q[1] + "," +  Q[2] + "," + Q[3] + ","+Q[0]+"," + timeInMillis  + "," + realtimeInMillis +","+sensorEvent.accuracy +"\r\n").getBytes();

//                byte[] data = new String(tagNum + ",RotationVector," + sensorEvent.values[0] + "," +  sensorEvent.values[1] + "," + sensorEvent.values[2] + ","+sensorEvent.values[3]+"," + timeInMillis  + "," + realtimeInMillis +","+sensorEvent.accuracy +"\r\n").getBytes();
                mOutFile.write(data);
//                insertBufferLogs2("logs", new String(tagNum + ",RotationVector," + ","+Q[0]+ "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));

                insertBufferLogs2("logs", new String(tagNum + ",RotationVector," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + 0 + "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));
//                insertBufferLogs("logs", new String(tagNum + ",RotationVector," +  Q[1] + "," +  Q[2] + "," + Q[3] + "," + Q[4] + "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));

            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
//                Log.d(TAG, "onSensorChanged(SensorService) - "+sensorEvent.sensor.getVendor()+" - "  + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",orientation," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," +0+","+ timeInMillis + "," + realtimeInMillis +","+sensorEvent.accuracy + "\r\n").getBytes();
                mOutFile.write(data);

                insertBufferLogs2("logs", new String(tagNum + ",orientation," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + 0 + "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));
            }
//            else if (sensorEvent.sensor.getType() == TYPE_HEARTRATE_MONITOR) {
//                Log.d(TAG, "onSensorChanged(SensorService) - " + sensorEvent.sensor.getVendor() + " - " + sensorEvent.sensor.getName() + " - HR " + sensorEvent.values[0] + ", milli " + timeInMillis);
////                Toast.makeText(getApplicationContext(), " * HR " + sensorEvent.values[0]+" *", Toast.LENGTH_SHORT).show();
//                byte[] data = new String(tagNum + ",HeartRate," + sensorEvent.values[0] + ", 0, 0," + timeInMillis +","+sensorEvent.accuracy + "\r\n").getBytes();
//                mOutFile.write(data);
//            }
//            else if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
//                Log.d(TAG, "onSensorChanged(SensorService) - " + sensorEvent.sensor.getVendor() + " - " + sensorEvent.sensor.getName() + " - Pressure " + sensorEvent.values[0] + ", milli " + timeInMillis);
////                Toast.makeText(getApplicationContext(), " * Pressure " + sensorEvent.values[0]+" *", Toast.LENGTH_SHORT).show();
//                byte[] data = new String(tagNum + ",Pressure," + sensorEvent.values[0] + ", 0, 0," + timeInMillis +","+sensorEvent.accuracy + "\r\n").getBytes();
//                mOutFile.write(data);
//            }

            synchronized (this) {


                if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                    mGeoMags = sensorEvent.values.clone();
                } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mGravs = sensorEvent.values.clone();
                }
//                else if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//                    mRot = sensorEvent.values.clone();
//
//                    SensorManager.getRotationMatrixFromVector(Rx, mRot[0]);
//                    SensorManager.getRotationMatrixFromVector(Ry, mRot[1]);
//                    SensorManager.getRotationMatrixFromVector(Rz, mRot[2]);
//                }

                if (mGravs != null && mGeoMags != null) {
                    boolean bSuccess = SensorManager.getRotationMatrix(mR, null, mGravs, mGeoMags);
                    if(bSuccess)
                    {
                        float pitchDifference ;
                        float rollDifference;
                        SensorManager.getOrientation(mR, mV);
                        if (null == baseAzimuth) {
                            baseAzimuth =  mV[0];
                        }
                        if (null == basePitch) {
                            basePitch = mV[1];
                        }
                        if (null == baseRoll) {
                            baseRoll = mV[2];
                        }

                        pitchDifference = mV[1] - basePitch;
                        rollDifference = mV[2] - baseRoll;

                        yrot -= rollDifference;
                        xrot -= pitchDifference;
                        baseRoll = mV[2];
                        basePitch = mV[1];


//                        StringBuilder result = new StringBuilder();
//
////                        result.append("회수 = " + mOrientCount/FREQ + "회\n");
//                        result.append("azimuth:" + String.format("%.2f, %.2f", mV[0], Radian2Degree(mV[0])));
//                        result.append("\npitch:" + String.format("%.2f, %.2f", mV[1], Radian2Degree(mV[1])));
//                        result.append("\nroll:" + String.format("%.2f, %.2f", mV[2], Radian2Degree(mV[2])));
//                        result.append("\n\n");

                        pit = Radian2Degree(mV[1]);
                        rol = Radian2Degree(mV[2]);

                        // left hand watch
//                        if (1< pit  && pit <=90) {
//                            result.append("up \n");
//                        } else if (-1 <= rol && rol <=1) {
//                            result.append("upright\n");
//                        } else if (-90 <= pit  && pit <1) {
//                            result.append("down \n");
//                        }
//                        else {
//                            result.append("not sure\n");
//                        }
//                        // left hand watch
//                        if (-180 <=rol && rol < -90) {
//                            result.append("left & face-down\n");
//                        }
//                        if (-90 <=rol && rol < -1) {
//                            result.append("left & face-up\n");
//                        }
//                        else if (-1 <= rol && rol <=1) {
//                            result.append("upright\n");
//                        }
//                        else if (1 <rol && rol <= 90){
//                            result.append("right normal \n");
//                        }
//                        else if (90 <rol && rol <= 180){
//                            result.append("right abnormal \n");
//                        }
//                        else {
//                            result.append("not sure\n");
//                        }

                        //result.append("x " + "pitchDifference = " + pitchDifference + "\n");
                        //result.append("y " + "rollDifference = " + rollDifference + "\n\n");

//                        result.append("xrot = " + xrot + "\n");
//                        result.append("yrot = " + yrot + "\n");
//                        result.append("R:\n" + dumpMatrix(mR));

//   new String("tag, azimuth, azimuth_rad, pitch, pitch_rad, roll, roll_rad, pitchDiff, rollDiff, m0, m1, m2, m3, m4, m5, m6, m7, m8 " + "\r\n").getBytes();

                        byte[] data3 = new String(tagNum  + "," + Radian2Degree(mV[0]) +"," +
                               + Radian2Degree(mV[1]) + "," +
                               + Radian2Degree(mV[2]) + "," + pitchDifference +","+rollDifference + "," + xrot  +"," + yrot + ","+
                                        mR[0] +","+ mR[1] +","+ mR[2]+","+ mR[3]+","+ mR[4]+","+ mR[5]+","+ mR[6]+","+ mR[7]+","+ mR[8]+","+ timeInMillis + "," + realtimeInMillis + "\r\n").getBytes();

                        mOutFile3.write(data3);
                        //mTxtOrient.setText(result.toString());

//                        Log.d(TAG, "getRotationmatrix called");
//                        byte[] data = new String(tagNum + ",Roationmatrix," +
//                                mRotationM[0] + "," +mRotationM[1] + "," + mRotationM[2] + "," +
//                                mRotationM[3] + "," +mRotationM[4] + "," + mRotationM[5] + "," +
//                                mRotationM[6] + "," +mRotationM[7] + "," + mRotationM[8] + "," +
//                                timeInMillis +","+sensorEvent.accuracy + "\r\n").getBytes();
//                        mOutFile.write(data);
                        mGravs = null;
                        mGeoMags =null;

//                        if(prevmR!=null){
//                            float[] ac = new float[3];
//                            SensorManager.getAngleChange(ac, mR, prevmR);
//                            insertBufferLogs2("logs", new String(tagNum + ",RotationVector," + ac[0] + "," + ac[1] + "," + ac[2] + "," + 0 + "," + timeInMillis + "," + realtimeInMillis + "," + sensorEvent.accuracy + "\r\n"));
//                        }
//
//                        prevmR = mR.clone();

                    }

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
//        new SensorEventLoggerTask().execute(event);
//        stopSelf();
    }

    String dumpMatrix(float[] m) {
        return String.format("%.2f, %.2f, %.2f\n%.2f, %.2f, %.2f\n%.2f, %.2f, %.2f\n",
                m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]);
    }

    float Radian2Degree(float radian) {
        return radian * 180 / (float)Math.PI;
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

        if(variance > 0.1 ){ // 원래 0.1임
            Log.d(TAG, "===================> IS movement!! ,"+variance);
            return true;
        }
        else{
//            Log.d(TAG, "===================> NO movement!! ,"+variance);
            return false;
        }
    }

    private boolean movBuffering(double value, int limit) {

        mMovBuffer[mMovBufferIndex] = value;
        mMovBufferIndex++;

//        Log.d(TAG, "===================> MOV Buffering!!, " + mMovBufferIndex);

        return mMovBufferIndex < limit;
    }

    private void insertBufferLogs(String key, String log) {

        latch = new CountDownLatch(1);

        mLogsBuffer += log;

        if(mLogBufferIdx < mLogBufferLimit)
            mLogBufferIdx++;
        else{
            mDataMap.putString(key, mLogsBuffer);
            new SendToDataLayerThread(mGoogleApiClient,SendToDataLayerThread.TYPE_DATA_SYNC, "/data", mDataMap, null).start();
//            Log.v(TAG, "sendToDataLayerThread");

            mLogsBuffer="";
            mLogBufferIdx=0;
        }

        latch.countDown();
        //Requires a new thread to avoid blocking the UI
    }

    private void insertBufferLogs2(String key, String log) {

        latch2 = new CountDownLatch(1);

        mLogsBuffer2 += log;

        if(mLogBufferIdx2 < mLogBufferLimit2)
            mLogBufferIdx2++;
        else{
            mDataMap2.putString(key, mLogsBuffer2);
            new SendToDataLayerThread(mGoogleApiClient,SendToDataLayerThread.TYPE_DATA_SYNC, "/data", mDataMap2, null).start();
//            Log.v(TAG, "sendToDataLayerThread");

            mLogsBuffer2="";
            mLogBufferIdx2=0;
        }

        latch2.countDown();
        //Requires a new thread to avoid blocking the UI
    }

    private void doChangeSamplingRate(int interval, int status, int batchdelay, long timeInMillis, long realtimeInMillis) {

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
//################################ case 1 #############
//            if(mAudioRecorder!=null) {
                DateFormat dateFormat = new SimpleDateFormat("(yyyy-MM-dd)-HH-mm-ss");
                Date date = new Date();
//                Log.d(TAG,"audio_start_time " + dateFormat.format(date));
                mStartTimeMilli = timeInMillis;
                mStartTimeHour =dateFormat.format(date);
//                mAudioRecorder.startAudioCapture(mfilename + "-" + mStartTimeHour + "-" + audioTag + ".wav");
//                audioTag++;
//            }

//################################ case 2 #############
//            if(mAudioRecorder2!=null){
//                DateFormat dateFormat = new SimpleDateFormat("(yyyy-MM-dd)-HH-mm-ss");
//                Date date = new Date();
//                Log.d(TAG, "audio_start_time " + dateFormat.format(date));
//                mAudioRecorder2.startRecording(mfilename + "-" + dateFormat.format(date) + "-" + audioTag + ".3gp");
//                audioTag++;
//            }
//################################ case 3 #############
//            if(mExtAudioRecorder!=null)
//            {
//                DateFormat dateFormat = new SimpleDateFormat("(yyyy-MM-dd)-HH-mm-ss");
//                Date date = new Date();
//                Log.d(TAG, "audio_start_time " + dateFormat.format(date));
//                mExtAudioRecorder.setOutputFile("/sdcard/"+mfilename + "-" + dateFormat.format(date) + "-" + audioTag + ".3gp");
//                audioTag++;
//
//                mExtAudioRecorder.prepare();
//
//                mExtAudioRecorder.start();
//            }
//################################ end #############

            Log.d(TAG," STATUS --> [CONTINUOUS] ");
//            Toast.makeText(getApplicationContext(), "STATUS --> [CONTINUOUS]", Toast.LENGTH_SHORT).show();
//            MonitoringActivity.setBackgroundColor(Color.RED);

//            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            //-1 - don't repeat
//            final int indexInPatternToRepeat = 3;
//            AudioAttributes att = new AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_MEDIA)
//                    .build();
//
//            vibrator.vibrate(mVibrationPattern, indexInPatternToRepeat, att );

        }
        else if(status == STATUS_SENSING_CHECKING)
        {
            mSensingState = status;
//            freeRegisters();
            long elapsed = (timeInMillis-mStartTimeMilli);
            int epoch = 0;
            if(elapsed>3000)
                epoch = 1;

            byte[] data2 = new String(tagNum + ", "+mStartTimeMilli+" , " +realtimeInMillis +" , "+mStartTimeHour+", " +elapsed + ","+epoch + "\r\n").getBytes();

            try {
                mOutFile2.write(data2);
                mOutFile2.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

//################################ case 1 #############
//            if(mAudioRecorder!=null)
//                mAudioRecorder.stopAudioCapture();
//################################ case 2 #############
//            if(mAudioRecorder2!=null)
//                mAudioRecorder2.stopRecording();
//################################ case 3 #############
//            if(mExtAudioRecorder!=null){
//                mExtAudioRecorder.stop();
//                mExtAudioRecorder.release();
//            }
//            mExtAudioRecorder = mExtAudioRecorder.getInstanse(true);
//################################ end #############
//            if (mSensorManager.registerListener(mListener2, mLinearAccelSensor, interval, batchdelay) == false) {
//                Log.d(TAG, "batch is not supported : " + "linearaccel cnt: "+ batchdelay +", "+mLinearAccelSensor.getFifoMaxEventCount());
//            }

            Log.d(TAG," STATUS --> [CHECKING] ");
//            Toast.makeText(getApplicationContext(), "STATUS --> [CHECKING]", Toast.LENGTH_SHORT).show();
//            MonitoringActivity.setBackgroundColor(Color.BLACK);
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
