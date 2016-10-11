/**
 * Created by jonginlee on 04/10/2016.
 */
package kr.co.jonginlee.patbingsua;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {

    private static final String TAG = "WearWatch";

//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {
//
//        DataMap dataMap;
//        for (DataEvent event : dataEvents) {
//            if (event.getType() == DataEvent.TYPE_CHANGED) {
//                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
//                Log.v(TAG, "DataMap received on Watch: " + dataMap);
//
//                for (String key : dataMap.keySet()) {
//                    String values = dataMap.get(key);
//                    Log.v(TAG, "key : "+key+" values : "+values);
//                }
//
//                Intent messageIntent = new Intent();
//                messageIntent.setAction(Intent.ACTION_SEND);
//                messageIntent.putExtra("dataMessage", "Recieved dataMap "+dataMap.toString());
//                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
//            }
//        }
//    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/msg")) {
            final String message = new String(messageEvent.getData());
            Log.v(TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.v(TAG, "Message received on watch is: " + message);

            String[] words = message.split(",");
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);

            if(words[0].equalsIgnoreCase("start"))
            {
                messageIntent.putExtra("message", words[0]);
                messageIntent.putExtra("mTagnum", words[1]);
                messageIntent.putExtra("mSensorName", words[2]);
                messageIntent.putExtra("mSamplingRate", words[1]);

            }else{
                messageIntent.putExtra("message", "no_start");
            }
            // Broadcast message to wearable activity for display
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}