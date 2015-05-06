package kr.co.jonginlee.patbingsua;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
// TODO Considering SensorEventListener2
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class ControllerActivity extends Activity implements SensorEventListener {

    private static final String TAG = "WearMobile";
    private GoogleApiClient mGoogleApiClient;
    private Button mTagButton;
    private Button mSendMsgButton;

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
        mSendMsgButton = (Button) findViewById(R.id.sendmsg);

        mTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mSendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendToDataLayerThread(SendToDataLayerThread.TYPE_MSSG, "/msg", null, "Hello").start();
            }
        });
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

    private void setUpMessageReceiver() {
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
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

                Toast.makeText(getApplicationContext(), "received DATA success", Toast.LENGTH_LONG).show();
                //mStatus.setText("Recived Message : " + "Success");
//                new MyAsyncTask().execute(dataMessage);
            }


        }
    }


    private class SendToDataLayerThread extends Thread {
        String uniqePath;
        DataMap dataMap;
        int messageType;
        String message;
        public final static int TYPE_MSSG = 0x001;
        public final static int TYPE_DATA = 0x002;


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
                    Log.d(TAG,"send_TYPE_MSSG");
                    sendingMessage();
                    break;
                case TYPE_DATA:
                    Log.d(TAG,"send_TYPE_DATA");
                    sendingDATA();
                    break;
                default:
                    //error
                    break;
            }

        }

        private void sendingDATA() {

            if(!mGoogleApiClient.hasConnectedApi(Wearable.API)){
                Log.d(TAG,"[FALSE] - hasConnectedApi Wearaable API");
                return;
            }

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {

                // Construct a DataRequest and send over the data layer
                PutDataMapRequest putDMR = PutDataMapRequest.create(uniqePath);
                putDMR.getDataMap().putAll(dataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
                if (result.getStatus().isSuccess()) {
                    Log.d(TAG, "DataMap: " + dataMap + " sent to: " + node.getDisplayName());
//                    dataMap.clear();
                } else {
                    // Log an error
                    Log.d(TAG, "ERROR: failed to send DataMap");
                }
            }

        }

        private void sendingMessage() {

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {
                Log.d(TAG, "node id " + node.getId() +", "+node.getDisplayName());
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), uniqePath, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.d(TAG, "Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.d(TAG, "ERROR: failed to send Message");
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
