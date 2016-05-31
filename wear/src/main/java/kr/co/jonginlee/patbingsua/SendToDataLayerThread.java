package kr.co.jonginlee.patbingsua;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by jonginlee on 15. 10. 13..
 */
public class SendToDataLayerThread extends Thread {
    private String uniqePath;
    private DataMap dataMap;
    private int messageType;
    private String message;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = MonitoringActivity.TAG;

    public final static int TYPE_MSSG = 0x001;
    public final static int TYPE_DATA_SYNC = 0x002;

    // Constructor for sending data objects to the data layer
    SendToDataLayerThread(GoogleApiClient googleApiClient, int type, String path, DataMap data, String m) {
        mGoogleApiClient = googleApiClient;
        messageType = type;
        uniqePath = path;
        dataMap = data;
        message = m;
    }

    public void run() {

        switch (messageType) {
            case TYPE_MSSG:
//                    Log.d(TAG,"send_TYPE_MSSG");
                sendingMessage();
                break;
            case TYPE_DATA_SYNC:
//                    Log.d(TAG,"send_TYPE_DATA");
                sendingDATA();
                break;
            default:
                //error
                break;
        }

    }

    private void sendingDATA() {

        if(!mGoogleApiClient.hasConnectedApi(Wearable.API)){
            Log.d(TAG, "[FALSE] - hasConnectedApi Wearaable API");
            return;
        }
//        Log.d(TAG, "send_TYPE_DATA");

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//        for (Node node : nodes.getNodes()) {
//            Log.d(TAG, "node id " + node.getId() +", "+node.getDisplayName());
            // Construct a DataRequest and send over the data layer
            PutDataMapRequest putDMR = PutDataMapRequest.create(uniqePath);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
            if (result.getStatus().isSuccess()) {
//                Log.d(TAG, "DataMap: " + dataMap + " sent to: " + node.getDisplayName());
//                Log.d(TAG, "DataMap: " + dataMap);
              dataMap.clear();
            } else {
            // Log an error
                Log.d(TAG, "ERROR: failed to send DataMap");
            }
//        }

    }

    private void sendingMessage() {


        if (!mGoogleApiClient.hasConnectedApi(Wearable.API)) {
            Log.d(TAG, "[FALSE] - hasConnectedApi Wearaable API");
            return;
        }
        Log.d(TAG, "send_TYPE_MSSG");

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
