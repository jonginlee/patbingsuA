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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
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
public class SensorService extends IntentService implements SensorEventListener2 {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SENSING = "kr.co.jonginlee.patbingsua.action.sensing";
    private static final String ACTION_BAZ = "kr.co.jonginlee.patbingsua.action.BAZ";

    // TODO: Rename parameters
    private static final String DELAY_INTERVAL = "kr.co.jonginlee.patbingsua.delay.interval";
    private static final String RECORDED_FILENAME = "kr.co.jonginlee.patbingsua.extra.PARAM2";

    private static final int STATUS_SENSING_CHECKING = 0x0001;
    private static final int STATUS_SENSING_CONTINUOUS = 0x0002;


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
    private PowerManager.WakeLock wakelock = null;



    private int tagNum = 1;
    private DataOutputStream mOutFile;
    private double mPrevAccelValue = 0;
    private double mThreshold = 0.8;
    private int mSensingState = STATUS_SENSING_CHECKING;
    private long mStateTimeReference = 0;
    private int mBatchDelay = 0;
    private double[] mMovBuffer = new double[50];
    private int mMovBufferIndex = 0;


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate(SensorService)");

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        mCompassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

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
        Intent intent = new Intent(context, SensorService.class);
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
//        mSensorManager.unregisterListener(this, this.mCompassSensor);
        mSensorManager.unregisterListener(this, this.mLinearAccelSensor);
//        mSensorManager.unregisterListener(this, this.mAccelerometerSensor);
//        mSensorManager.unregisterListener(this, this.mGyroSensor);
//        mSensorManager.unregisterListener(this, this.mOrientationSensor);

//        stopAudioCapture();
//        mSensorManager.unregisterListener(this, this.mRotationVector);
        Log.d(TAG, "freeRegisters(SensorService) - pass");
    }


    @Override
    public void onDestroy() {
        wakelock.release();
        freeRegisters();
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
        Log.d(TAG, "onDestroy(SensorService) - pass");


        super.onDestroy();
    }


    // TODO: Customize helper method
    public static void stopActionSensing(Context context) {
        Intent intent = new Intent(context, SensorService.class);
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

    public SensorService() {
        super("SensorService");
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
        Log.d(TAG, "onStartCommand(SensorService)");
//        super.onStartCommand(intent, flags, startId);

        mfilename = intent.getStringExtra(RECORDED_FILENAME);

        if ((mfile == null) && (mfilename !=null)) {
            mfile = new File(getExternalStorageDirectory(), mfilename);
            try {
                mFos = new FileOutputStream(mfile);
                mOutFile = new DataOutputStream(new BufferedOutputStream(mFos));
                byte[] data = new String("tag, type, x, y, z, time" + "\r\n").getBytes();
                mOutFile.write(data);
                mOutFile.flush();
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
//            registerSensor(interval);
            if (mSensorManager.registerListener(this, mLinearAccelSensor, interval, mBatchDelay) == false) {
                Log.d(TAG, "batch is not supported : " + "linearaccel cnt: "+ mBatchDelay +", "+mLinearAccelSensor.getFifoMaxEventCount());
            }
        }
//        else if(ACTION_SAMPLING_CH.equals(action)){
//            Log.d(TAG, "ACTION_SAMPLING_CH");
//            registerSensor(20 * 1000);
//        }


        return START_STICKY;
    }

    public void registerSensor(int interval) {
        Log.d(TAG, "registerSensor(SensorService) - interval : " + interval);

//        latch = new CountDownLatch(1);
//         mBatchDelay = 5*1000*1000;
        mBatchDelay = 8*1000*1000;
//        mBatchDelay = 0;

//
//        if (mSensorManager.registerListener(this, mAccelerometerSensor, interval, mBatchDelay)) {
//            Log.d(TAG, "batch is supported : " + "accel cnt: "+ mBatchDelay +", "+mAccelerometerSensor.getFifoReservedEventCount());
//        }else
//            Log.d(TAG, "batch is not supported "+mAccelerometerSensor.getName());
//
//        if(mSensorManager.registerListener(this, mGyroSensor, interval, mBatchDelay)){
//            Log.d(TAG,"batch is supported : "+"gyro cnt: "+ mBatchDelay +", "+mGyroSensor.getFifoReservedEventCount());
//        }else
//            Log.d(TAG, "batch is not supported "+mGyroSensor.getName());
//
//        if(mSensorManager.registerListener(this, mCompassSensor, interval, mBatchDelay)){
//            Log.d(TAG, "batch is supported : " + "compass cnt: "+ mBatchDelay +", "+mCompassSensor.getFifoReservedEventCount());
//        }else
//            Log.d(TAG, "batch is not supported "+mCompassSensor.getName());

//        if(mSensorManager.registerListener(this, ))


        if (mSensorManager.registerListener(this, mLinearAccelSensor, interval, mBatchDelay) == false) {
            Log.d(TAG, "batch is not supported : " + "linearaccel cnt: "+ mBatchDelay +", "+mLinearAccelSensor.getFifoMaxEventCount());
        }
//
//        if(mSensorManager.registerListener(this, mOrientationSensor,interval, mBatchDelay)==false){
//            Log.d(TAG,"batch is not supported : "+"orientation cnt: "+ mBatchDelay +", "+mOrientationSensor.getFifoMaxEventCount());
//        }
////        Log.d(TAG, "start register sensors");


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
                //    gravSensorVals = lowPass(sensorEvent.values.clone(), gravSensorVals);
                //                if(mSensingState == 1)
//                if( mPrevAccelValue == 0)
//                    mPrevAccelValue = Math.sqrt(Math.pow(sensorEvent.values[0],2) + Math.pow(sensorEvent.values[1],2) + Math.pow(sensorEvent.values[2],2));
//                double currentValue = Math.sqrt(Math.pow(sensorEvent.values[0],2) + Math.pow(sensorEvent.values[1],2) + Math.pow(sensorEvent.values[2],2));

//                Log.d(TAG, "accel - " + Math.abs(mPrevAccelValue - currentValue));

                if (timeInMillis > 1000)
                    if(!movBuffering(sensorEvent.values[0] + sensorEvent.values[1] + sensorEvent.values[2], 5)){
                        boolean isMoving = isMovement();
                        if(isMoving && mSensingState == STATUS_SENSING_CHECKING)
                            doChangeSamplingRate(20*1000, STATUS_SENSING_CONTINUOUS);
                        else if(!isMoving && mSensingState == STATUS_SENSING_CONTINUOUS)
                            doChangeSamplingRate(100*1000, STATUS_SENSING_CHECKING);
                    }

//                if((mSensingState==STATUS_SENSING_CHECKING) && (Math.abs(mPrevAccelValue - currentValue)>mThreshold)){
//                    Log.d(TAG, "checking..");
//                    if(mStateTimeReference == 0 ) {
//                        mStateTimeReference = timeInMillis;
//                    }
//                    if(timeInMillis - mStateTimeReference > 100 ){
//                        Log.d(TAG, "timeRef..");
//                        doChangeSamplingRate(false);
//                    }
//                }
//                else
//                    mStateTimeReference = 0;
//
//                if((mSensingState==STATUS_SENSING_CONTINUOUS) && (Math.abs(mPrevAccelValue - currentValue)<mThreshold)){
//                    if(mStateTimeReference == 0 )
//                        mStateTimeReference = timeInMillis;
//                    if(timeInMillis - mStateTimeReference > 3000 ){
//                        doChangeSamplingRate(true);
//                    }
//                    Log.d(TAG, "station..");
//                }
//                else
//                    mStateTimeReference = 0;
//
//                mPrevAccelValue = currentValue;

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

        if(variance > 0.5){
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

    private void doChangeSamplingRate(int interval, int status) {


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
            freeRegisters();
            mSensorManager.unregisterListener(this, this.mLinearAccelSensor);
            mBatchDelay = 1*1000*1000;
            registerSensor(interval);
        }
        //        registerSensor((int) interval);

    }

    private void doChangeSamplingRate(boolean checking) {
        int interval = 0;
        freeRegisters();

        if(checking){
            interval = 100*1000;
            mSensingState = STATUS_SENSING_CHECKING;
            if (mSensorManager.registerListener(this, mLinearAccelSensor, interval, mBatchDelay) == false) {
                Log.d(TAG, "batch is not supported : " + "linearaccel cnt: "+ mBatchDelay +", "+mLinearAccelSensor.getFifoMaxEventCount());
            }

            Log.d(TAG, "STATUS_SENSING_CHECKING");
        }else{
//            interval =  20*1000;
//            mSensingState = STATUS_SENSING_CONTINUOUS;
//            registerSensor(interval);
            Log.d(TAG, "STATUS_SENSING_CONTINUOS");
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
