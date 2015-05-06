package kr.co.jonginlee.patbingsua;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {

    private static final String TAG = "WearMobile";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {


        DataMap dataMap;
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                Log.v(TAG, "DataMap received on mobile: " + dataMap);

                String logs=null;
                String keys = null;
                for (String key : dataMap.keySet()) {
                    logs = dataMap.get(key);
                    keys = key;
                    Log.v(TAG, "key : "+key+" values : "+logs);
                }

                Intent messageIntent = new Intent();
                messageIntent.setAction(Intent.ACTION_SEND);

                messageIntent.putExtra("dataMessage", logs);


                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/msg")) {
            final String message = new String(messageEvent.getData());
            Log.v(TAG, "Message path received on mobile is: " + messageEvent.getPath());
            Log.v(TAG, "Message received on mobile is: " + message);

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}