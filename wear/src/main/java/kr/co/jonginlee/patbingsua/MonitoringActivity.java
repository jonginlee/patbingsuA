package kr.co.jonginlee.patbingsua;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;

public class MonitoringActivity extends Activity {

    private Button mTagButton;
    private boolean isStart = false;
    private LinearLayout mTopLayout;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "WearWatch";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        setUpGoogleApiClient();
        setUpView();
        setUpMessageReceiver();

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

    private void setUpView() {
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTagButton = (Button) findViewById(R.id.tag);
                mTopLayout = (LinearLayout) stub.findViewById(R.id.toplayout);

                if (!mTagButton.hasOnClickListeners()) {
                    mTagButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (isStart == false) {
                                SensingASAService.startActionSensing(getApplicationContext(),1000*1000);
                                mTagButton.setText("STOP");
                                mTagButton.setBackgroundColor(Color.DKGRAY);
                                mTopLayout.setBackgroundColor(Color.BLACK);
                                isStart = true;

                            } else if (isStart == true) {
                                SensingASAService.stopActionSensing(getApplicationContext());
                                mTagButton.setText("START");
                                mTopLayout.setBackgroundColor(Color.BLUE);
                                isStart = false;

                            }
                        }

                    });
                }
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
}
