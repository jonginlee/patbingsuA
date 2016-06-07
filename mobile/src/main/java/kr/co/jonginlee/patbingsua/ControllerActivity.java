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
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.concurrent.CountDownLatch;

// TODO Considering SensorEventListener2


public class ControllerActivity extends Activity implements SensorEventListener {

    private static final String TAG = "WearMobile";
    private GoogleApiClient mGoogleApiClient;
    private Button mTagButton;
    private Button mSendMsgButton;
    private int mTagNum = 1;
    private boolean isStart = false;
    private TextView mStatus;
    private GraphViewData[] mXGraphViewData1;
    private GraphViewData[] mYGraphViewData1;
    private GraphViewData[] mZGraphViewData1;

    LineGraphView mGraphView1;
    private String mDataMessage;
    private CountDownLatch mLatch1;

    private GraphViewData[] mXGraphViewData2;
    private GraphViewData[] mYGraphViewData2;
    private GraphViewData[] mZGraphViewData2;

    LineGraphView mGraphView2;
    private CountDownLatch mLatch2;
    private MessageReceiver mMessageReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        setUpGoogleApiClient();
        setUpView();
        setUpMessageReceiver();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }




    private void setUpView() {
        mTagButton = (Button) findViewById(R.id.tag);
//        mSendMsgButton = (Button) findViewById(R.id.sendmsg);
        mStatus = (TextView) findViewById(R.id.comment1);

        mTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendToDataLayerThread(SendToDataLayerThread.TYPE_MSSG, "/msg", null, "start,"+mTagNum).start();
                startButtonProc(mTagNum);
            }
        });

//        mSendMsgButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new SendToDataLayerThread(SendToDataLayerThread.TYPE_MSSG, "/msg", null, "Hello").start();
//            }
//        });

        // graph with dynamically genereated horizontal and vertical labels
        mGraphView1 = new LineGraphView(this, "rotation vector");
        // set legend
        mGraphView1.setShowLegend(true);
        mGraphView1.setManualYAxisBounds(1.25, -1.25);
        // set view port, start=2, size=40
        mGraphView1.setViewPort(2, 150);
        mGraphView1.setScrollable(true);
        LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
        layout.addView(mGraphView1);

        mGraphView2 = new LineGraphView(this, "orientation");
        // set legend
        mGraphView2.setShowLegend(true);
        mGraphView2.setManualYAxisBounds(360, -360);
        // set view port, start=2, size=40
        mGraphView2.setViewPort(2, 150);
        mGraphView2.setScrollable(true);
        LinearLayout layout2 = (LinearLayout) findViewById(R.id.graph2);
        layout2.addView(mGraphView2);

    }

    private void startButtonProc(int tagnum) {
        mTagNum = tagnum;
        if (isStart == false) {
//          TrigerSensorService.startActionSensing(getApplicationContext(), 100 * 1000,"data_watch_intentservice_at_"+mTagNum);
           // SensorService.startActionSensing(getApplicationContext(), 20 * 1000, "watch" + mTagNum);
            mTagButton.setText("STOP");
            //mTagButton.setBackgroundColor(Color.DKGRAY);
            //mTopLayout.setBackgroundColor(Color.WHITE);
            isStart = true;

        } else if (isStart) {
            mTagNum++;
//          TrigerSensorService.stopActionSensing(getApplicationContext());
           // SensorService.stopActionSensing(getApplicationContext());
            mTagButton.setText("START " + mTagNum);
            //mTagButton.setBackgroundColor(Color.BLUE);
            //mTopLayout.setBackgroundColor(Color.WHITE);
            isStart = false;
        }
    }

    private void setUpGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (!mResolvingError) {  // more about this later //TODO
        mGoogleApiClient.connect();
//        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onStop();
    }

    private void setUpMessageReceiver() {
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        mMessageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, messageFilter);
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");
            String dataMessage = intent.getStringExtra("dataMessage");
            // Display message in UI

            if(message!=null){
                Toast.makeText(getApplicationContext(), "received MSG success", Toast.LENGTH_LONG).show();

                //startButtonProc();
//                    new MyAsyncTask().execute(message);

            }
            else if(dataMessage!=null){

//                Toast.makeText(getApplicationContext(), "received DATA success", Toast.LENGTH_LONG).show();
//                Log.v(TAG, "dataMessage: {" + dataMessage + "}");
                mDataMessage = dataMessage;
                int idx = 0;
                char mychar = ',';

                for(int i = 2 ; i < mDataMessage.length(); i++){
                    if(mychar == mDataMessage.charAt(i)) {
                        idx = i;
                        break;
                    }
                }
                String substring = mDataMessage.substring(2, idx);
                mStatus.setText("Received Message : " + "Success : " +substring);

//                Toast.makeText(getApplicationContext(), substring , Toast.LENGTH_LONG).show();

                if(substring.equalsIgnoreCase("RotationVector")) {
                    DrawGraphProc();
                }else if(substring.equalsIgnoreCase("orientation")){
                    DrawGraphProc2();
                }


            }


        }


    }

    private void DrawGraphProc2() {

        Thread thread = new Thread() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLatch2 = new CountDownLatch(1);

                        String[] line = mDataMessage.split("\r\n");
//                                Log.v(TAG, "a msg: {" + line[0] + "}");
                        int prevXlen = 0;
                        int prevYlen = 0;
                        int prevZlen = 0;

                        GraphViewData[] mXGraphViewData_prev = null;
                        GraphViewData[] mYGraphViewData_prev = null;
                        GraphViewData[] mZGraphViewData_prev = null;

                        if (mXGraphViewData2 != null) {
                            prevXlen = mXGraphViewData2.length;
                            mXGraphViewData_prev = mXGraphViewData2.clone();
                        }
                        mXGraphViewData2 = new GraphViewData[line.length + prevXlen];

                        if (mYGraphViewData2 != null) {
                            prevYlen = mYGraphViewData2.length;
                            mYGraphViewData_prev = mYGraphViewData2.clone();
                        }
                        mYGraphViewData2 = new GraphViewData[line.length + prevYlen];

                        if (mZGraphViewData2 != null) {
                            prevZlen = mZGraphViewData2.length;
                            mZGraphViewData_prev = mZGraphViewData2.clone();
                        }
                        mZGraphViewData2 = new GraphViewData[line.length + prevZlen];


                        for (int i = 0; i < line.length; i++) {
                            String[] items = line[i].split(",");
                            if (items.length > 3) {
                                mXGraphViewData2[i] = new GraphViewData(i, Double.parseDouble(items[2]));
                                mYGraphViewData2[i] = new GraphViewData(i, Double.parseDouble(items[3]));
                                mZGraphViewData2[i] = new GraphViewData(i, Double.parseDouble(items[4]));
                            } else
                                Log.v(TAG, "items.length>3 " + items.toString());
                        }

                        if (mXGraphViewData_prev != null) {
                            for (int i = 0; i < prevXlen; i++) {
                                mXGraphViewData2[line.length + i] = new GraphViewData(line.length + i, mXGraphViewData_prev[i].getY());
                            }
                        }

                        if (mYGraphViewData_prev != null) {
                            for (int i = 0; i < prevYlen; i++) {
                                mYGraphViewData2[line.length + i] = new GraphViewData(line.length + i, mYGraphViewData_prev[i].getY());
                            }
                        }

                        if (mZGraphViewData_prev != null) {
                            for (int i = 0; i < prevZlen; i++) {
                                mZGraphViewData2[line.length + i] = new GraphViewData(line.length + i, mZGraphViewData_prev[i].getY());
                            }
                        }


                        GraphViewSeries Xaxis = new GraphViewSeries("X-axis", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(255, 0, 0), 3), mXGraphViewData2);
                        GraphViewSeries Yaxis = new GraphViewSeries("Y-axis", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(255, 0, 255), 3), mYGraphViewData2);
                        GraphViewSeries Zaxis = new GraphViewSeries("Z-axis", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(0, 255, 0), 3), mZGraphViewData2);

                        mGraphView2.removeAllSeries();
                        mGraphView2.addSeries(Xaxis);
                        mGraphView2.addSeries(Yaxis);
                        mGraphView2.addSeries(Zaxis);


                        mLatch2.countDown();
                    }
                });


            }
        };

        thread.start();
    }


    private void DrawGraphProc() {

        Thread thread = new Thread() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLatch1 = new CountDownLatch(1);

                        String[] line = mDataMessage.split("\r\n");
//                                Log.v(TAG, "a msg: {" + line[0] + "}");
                        int prevXlen = 0;
                        int prevYlen = 0;
                        int prevZlen = 0;

                        GraphViewData[] mXGraphViewData_prev = null;
                        GraphViewData[] mYGraphViewData_prev = null;
                        GraphViewData[] mZGraphViewData_prev = null;

                        if (mXGraphViewData1 != null) {
                            prevXlen = mXGraphViewData1.length;
                            mXGraphViewData_prev = mXGraphViewData1.clone();
                        }
                        mXGraphViewData1 = new GraphViewData[line.length + prevXlen];

                        if (mYGraphViewData1 != null) {
                            prevYlen = mYGraphViewData1.length;
                            mYGraphViewData_prev = mYGraphViewData1.clone();
                        }
                        mYGraphViewData1 = new GraphViewData[line.length + prevYlen];

                        if (mZGraphViewData1 != null) {
                            prevZlen = mZGraphViewData1.length;
                            mZGraphViewData_prev = mZGraphViewData1.clone();
                        }
                        mZGraphViewData1 = new GraphViewData[line.length + prevZlen];


                        for (int i = 0; i < line.length; i++) {
                            String[] items = line[i].split(",");
                            if (items.length > 3) {
                                mXGraphViewData1[i] = new GraphViewData(i, Double.parseDouble(items[2]));
                                mYGraphViewData1[i] = new GraphViewData(i, Double.parseDouble(items[3]));
                                mZGraphViewData1[i] = new GraphViewData(i, Double.parseDouble(items[4]));
                            } else
                                Log.v(TAG, "items.length>3 " + items.toString());
                        }

                        if (mXGraphViewData_prev != null) {
                            for (int i = 0; i < prevXlen; i++) {
                                mXGraphViewData1[line.length + i] = new GraphViewData(line.length + i, mXGraphViewData_prev[i].getY());
                            }
                        }

                        if (mYGraphViewData_prev != null) {
                            for (int i = 0; i < prevYlen; i++) {
                                mYGraphViewData1[line.length + i] = new GraphViewData(line.length + i, mYGraphViewData_prev[i].getY());
                            }
                        }

                        if (mZGraphViewData_prev != null) {
                            for (int i = 0; i < prevZlen; i++) {
                                mZGraphViewData1[line.length + i] = new GraphViewData(line.length + i, mZGraphViewData_prev[i].getY());
                            }
                        }


                        GraphViewSeries Xaxis = new GraphViewSeries("X-axis", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(255, 0, 0), 3), mXGraphViewData1);
                        GraphViewSeries Yaxis = new GraphViewSeries("Y-axis", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(255, 0, 255), 3), mYGraphViewData1);
                        GraphViewSeries Zaxis = new GraphViewSeries("Z-axis", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(0, 255, 0), 3), mZGraphViewData1);

                        mGraphView1.removeAllSeries();
                        mGraphView1.addSeries(Xaxis);
                        mGraphView1.addSeries(Yaxis);
                        mGraphView1.addSeries(Zaxis);


                        mLatch1.countDown();
                    }
                });


            }
        };

        thread.start();
    }


    private class SendToDataLayerThread extends Thread {
        String uniqePath;
        DataMap dataMap;
        int messageType;
        String message;
        public final static int TYPE_MSSG = 0x001;
        public final static int TYPE_DATA_SYNC = 0x002;


        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(int type, String path, DataMap data, String m) {
            messageType = type;
            uniqePath = path;
            dataMap = data;
            message = m;
        }

        public void run() {

            switch (messageType) {
                case TYPE_MSSG:
                    sendingMessage();
                    break;
                case TYPE_DATA_SYNC:
                    sendingDATA();
                    break;
                default:
                    //error
                    break;
            }

        }

        private void sendingDATA() {

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {

                // Construct a DataRequest and send over the data layer
                PutDataMapRequest putDMR = PutDataMapRequest.create(uniqePath);
//                putDMR.get
                putDMR.getDataMap().putAll(dataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
                if (result.getStatus().isSuccess()) {
                    Log.v(TAG, "DataMap: " + dataMap + " sent to: " + node.getDisplayName());
//                    mDataMap.clear();
                } else {
                    // Log an error
                    Log.v(TAG, "ERROR: failed to send DataMap");
                }

            }

        }

        private void sendingMessage() {

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), uniqePath, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v(TAG, "Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v(TAG, "ERROR: failed to send Message");
                }
            }

        }

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {  // implemnts of SensorEventListener

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {   // implemnts of SensorEventListener

    }

}
