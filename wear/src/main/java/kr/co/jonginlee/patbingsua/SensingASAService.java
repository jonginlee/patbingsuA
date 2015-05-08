package kr.co.jonginlee.patbingsua;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SensingASAService extends IntentService implements SensorEventListener {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SENSING = "kr.co.jonginlee.patbingsua.action.sensing";
    private static final String ACTION_BAZ = "kr.co.jonginlee.patbingsua.action.BAZ";

    // TODO: Rename parameters
    private static final String DELAY_INTERVAL = "kr.co.jonginlee.patbingsua.delay.interval";
    private static final String EXTRA_PARAM2 = "kr.co.jonginlee.patbingsua.extra.PARAM2";

    private Sensor mAccelerometerSensor = null;
    private Sensor mLinearAccelSensor = null;
    private Sensor mCompassSensor = null;
    private Sensor mOrientationSensor = null;
    private Sensor mGyroSensor = null;
    private SensorManager mSensorManager = null;

    private static final String TAG = "WearWatch";
    private long sensorTimeReference = 0l;
    private FileOutputStream mfos = null;
    private File mfile = null;
    private String mfilename = null;
    private PowerManager.WakeLock wakelock = null;

    private int tagNum = 1;



    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate(SensingASAService)");


        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mCompassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);


        if (mfile == null) {
            mfilename = "data_watch_intentservice.txt";
            mfile = new File(getExternalStorageDirectory(), mfilename);
            try {
                mfos = new FileOutputStream(mfile);
                byte[] data = new String("tag, type, x, y, z, time" + "\r\n").getBytes();
                mfos.write(data);
                mfos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onCreate();
        Log.d(TAG, "onCreate(SensingASAService) - pass");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "onHandleIntent(SensingASAService) - not null");
        } else
            Log.d(TAG, "onHandleIntent(SensingASAService) - null");

    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSensing(Context context, int interval) {
        Intent intent = new Intent(context, SensingASAService.class);
        intent.setAction(ACTION_SENSING);
        intent.putExtra(DELAY_INTERVAL, interval);
        intent.putExtra(EXTRA_PARAM2, "nothing");
        context.startService(intent);
    }






    private void freeRegisters() {

//        mSensorManager.unregisterListener(this, this.mHeartRateSensor);
        mSensorManager.unregisterListener(this, this.mCompassSensor);
        mSensorManager.unregisterListener(this, this.mLinearAccelSensor);
        mSensorManager.unregisterListener(this, this.mAccelerometerSensor);
        mSensorManager.unregisterListener(this, this.mGyroSensor);
        mSensorManager.unregisterListener(this, this.mOrientationSensor);

//        stopAudioCapture();
//        mSensorManager.unregisterListener(this, this.mRotationVector);
        Log.d(TAG, "freeRegisters(SensingASAService) - pass");

    }


    @Override
    public void onDestroy() {
        wakelock.release();
        freeRegisters();
        if(mfos!=null){
            try {
                mfos.flush();
                mfos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "onDestroy(SensingASAService) - pass");


        super.onDestroy();
    }


    // TODO: Customize helper method
    public static void stopActionSensing(Context context) {
        Intent intent = new Intent(context, SensingASAService.class);
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
//        Intent intent = new Intent(context, SensingASAService.class);
//        intent.setAction(ACTION_BAZ);
//        intent.putExtra(DELAY_INTERVAL, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }

    public SensingASAService() {
        super("SensingASAService");
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(SensingASAService)");
//        super.onStartCommand(intent, flags, startId);

        PowerManager mgr = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
        wakelock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakelock.acquire();
        String action = intent.getAction();
        int interval = intent.getIntExtra(DELAY_INTERVAL, 50 * 1000);
        if (ACTION_SENSING.equals(action)) {
            Log.d(TAG, "onStartCommand(SensingASAService) - ACTION_SENSING - pass");
            registerSensor(interval);
        }
//        else if(ACTION_SAMPLING_CH.equals(action)){
//            Log.d(TAG, "ACTION_SAMPLING_CH");
//            registerSensor(20 * 1000);
//        }


        return START_STICKY;
    }

    public void registerSensor(int interval) {
        Log.d(TAG, "registerSensor(SensingASAService) - interval : " + interval);

//        latch = new CountDownLatch(1);

        if(mSensorManager.registerListener(this, mCompassSensor,interval)==false){
            Log.d(TAG, "batch is not supported : " + "compass");
        }
        if (mSensorManager.registerListener(this, mAccelerometerSensor, interval) == false) {
            Log.d(TAG, "registerSensor(SensingASAService) - fail , batch is not supported : " + "accel");
        }
        if (mSensorManager.registerListener(this, mLinearAccelSensor, interval) == false) {
            Log.d(TAG, "batch is not supported : " + "linearaccel");
        }
        if(mSensorManager.registerListener(this, mGyroSensor,interval)==false){
            Log.d(TAG,"batch is not supported : "+"gyro");
        }
        if(mSensorManager.registerListener(this, mOrientationSensor,interval)==false){
            Log.d(TAG,"batch is not supported : "+"orientation");
        }
//        Log.d(TAG, "start register sensors");


//        latch.countDown();
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        // grab the values and timestamp
        SensorEvent sensorEvent = event;

        try {

            if (sensorTimeReference == 0l) {
                sensorTimeReference = sensorEvent.timestamp;
            }

            // set event timestamp to current time in milliseconds

            long timeInMillis =
                    Math.round((sensorEvent.timestamp - sensorTimeReference) / 1000000.0);

//            Log.d(TAG, "time_milli : "+timeInMillis +", orignal : "+ sensorEvent.timestamp );


            if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
//                    Log.d(TAG, "Magnet : " +"x "+sensorEvent.values[0]+", y "+sensorEvent.values[1]+", z "+sensorEvent.values[2]+", milli"+timeInMillis);
                Log.d(TAG, "onSensorChanged(SensingASAService) - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);

                byte[] data = new String(tagNum+",Magnet," + sensorEvent.values[0] +","+ sensorEvent.values[1]+","+ sensorEvent.values[2] +","+timeInMillis+"\r\n").getBytes();

                mfos.write(data);
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d(TAG, "onSensorChanged(SensingASAService) - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",Accel," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + timeInMillis + "\r\n").getBytes();
                mfos.write(data);
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                //    gravSensorVals = lowPass(sensorEvent.values.clone(), gravSensorVals);
                Log.d(TAG, "is - TYPE_LINEAR_ACCELERATION : " + "x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli" + timeInMillis);
                byte[] data = new String(tagNum + ",Linearaccel," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + timeInMillis + "\r\n").getBytes();
                mfos.write(data);
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.d(TAG, "onSensorChanged(SensingASAService) - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);

                byte[] data = new String(tagNum + ",Gyro," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + timeInMillis + "\r\n").getBytes();
                mfos.write(data);
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                Log.d(TAG, "onSensorChanged(SensingASAService) - " + sensorEvent.sensor.getName() + " - x " + sensorEvent.values[0] + ", y " + sensorEvent.values[1] + ", z " + sensorEvent.values[2] + ", milli " + timeInMillis);
                byte[] data = new String(tagNum + ",orientation," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + "," + timeInMillis + "\r\n").getBytes();
                mfos.write(data);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
//        new SensorEventLoggerTask().execute(event);
//        stopSelf();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged(SensingASAService) - " + sensor.getName() + ", accuracy - " + accuracy);
    }


}
