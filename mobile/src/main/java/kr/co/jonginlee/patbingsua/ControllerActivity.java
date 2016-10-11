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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.HttpsURLConnection;

// TODO Considering SensorEventListener2


public class ControllerActivity extends Activity implements SensorEventListener {


    private String SensorDataBuffer = "tag, type, x, y, z, ad1, time, real_time, acc\r\n";
    private int SensorDataBufferLength = 0;

    private static final String TAG = "WearMobile";
    private GoogleApiClient mGoogleApiClient;
    private Button mTagButton;
    private Button mSendMsgButton;
    private int mTagNum = 1;
    private boolean isStart = false;
    private TextView mStatus;
//    private GraphViewData[] mXGraphViewData1;
//    private GraphViewData[] mYGraphViewData1;
//    private GraphViewData[] mZGraphViewData1;

    GraphView mGraphView1;
    private String mDataMessage;
    private CountDownLatch mLatch1;
//
//    private GraphViewData[] mXGraphViewData2;
//    private GraphViewData[] mYGraphViewData2;
//    private GraphViewData[] mZGraphViewData2;

    GraphView mGraphView2;
    private CountDownLatch mLatch2;
    private MessageReceiver mMessageReceiver;
    private LineGraphSeries<DataPoint> Xaxis;
    private LineGraphSeries<DataPoint> Yaxis;
    private LineGraphSeries<DataPoint> Zaxis;
    private int mLength = 1;
    private Spinner mSpinner_sensor;
    private Spinner mSpinner_sampling;
    private String mSensorName = "nothing";
    private String mSamplingRate = "-1Hz";
    private Button mClearButton;
    private Button mSendButton;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private TextView mServerStatus;
    private CheckBox mSendCheckBox;
    private EditText mServeraddr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        setUpGoogleApiClient();
        setUpView();
        setUpMessageReceiver();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    private void checkSelected() {
        if ((mSpinner_sensor.getSelectedItemPosition() != 0) &&
                (mSpinner_sampling.getSelectedItemPosition() != 0)) {
            mTagButton.setEnabled(true);
            mSensorName = (String) mSpinner_sensor.getSelectedItem();
            mSamplingRate = (String) mSpinner_sampling.getSelectedItem();
            Toast.makeText(getApplicationContext(), mSensorName + ", " + mSamplingRate, Toast.LENGTH_LONG).show();
        }

    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }


        return result.toString();
    }


    public String performPostCall(String requestURL,
                                  String postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);



            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write((postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }


    public class datasender extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {

//            HashMap<String, String> params = new HashMap<String, String>();
//            params.put("logs", "1,3,4\r\n2,4,5\r\n1.4,34.5,234.0\r\n");

            String res = performPostCall("http://143.248.134.35/connect/model.php",params[0]);


//            int count = urls.length;
//            long totalSize = 0;
//            for (int i = 0; i < count; i++) {
//                totalSize += Downloader.downloadFile(urls[i]);
//                publishProgress((int) ((i / (float) count) * 100));
//                // Escape early if cancel() is called
//                if (isCancelled()) break;
//            }


            return res;
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
//            showDialog("Downloaded " + result + " bytes");
//            Toast.makeText(ControllerActivity.this, "from model.php "+ result, Toast.LENGTH_SHORT).show();
            mServerStatus.setText(result);

        }
    }


    private void setUpView() {

        mServeraddr = (EditText) findViewById(R.id.serveraddr);
        mServeraddr.setText("please enter server address");

        mSendCheckBox = (CheckBox) findViewById(R.id.sendcheckbox);
        mSendCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked == true) {
                        mServeraddr.setEnabled(true);
                        mServeraddr.setText("http://143.248.134.35/connect/model.php");
                    }
                    else {
                        mServeraddr.setText("please enter server address");
//                        mServeraddr.setText("addr");
                    }
                }
            }
        );

        mSpinner_sensor = (Spinner) findViewById(R.id.spinner1);
        mSpinner_sensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
//                Toast.makeText(getApplicationContext(), "sensor "+ "position : " + position +
//                        parent.getItemAtPosition(position), Toast.LENGTH_LONG).show();
                checkSelected();
//                tv.setText("position : " + position +
//                        parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mSpinner_sampling = (Spinner) findViewById(R.id.spinner2);
        mSpinner_sampling.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
//                Toast.makeText(getApplicationContext(), "sampling "+"position : " + position +
//                        parent.getItemAtPosition(position), Toast.LENGTH_LONG).show();
                checkSelected();
//                tv.setText("position : " + position +
//                        parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mSendButton = (Button) findViewById(R.id.send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("logs", "1,3,4\r\n2,4,5\r\n1.4,34.5,234.0\r\n");
                params.put("sensorname", mSensorName);
                params.put("samplingrate", mSamplingRate);

                String res = null;
                try {
                    res = getPostDataString(params);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                new datasender().execute(res);


//                String res = performPostCall("http://143.248.134.35/connect/model.php",params);
//                Toast.makeText(ControllerActivity.this, "from model.php "+ res, Toast.LENGTH_SHORT).show();
            }
        });

        mTagButton = (Button) findViewById(R.id.tag);
        mStatus = (TextView) findViewById(R.id.comment1);
        mServerStatus =  (TextView) findViewById(R.id.comment2);

        mTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendToDataLayerThread(SendToDataLayerThread.TYPE_MSSG, "/msg", null, "start," + mTagNum + "," + mSensorName + "," + mSamplingRate).start();
                startButtonProc(mTagNum);
            }
        });
        mClearButton = (Button) findViewById(R.id.clear);

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStatus.setText("not starting");
                if (mTagButton.isEnabled() == true) {
                    mTagButton.setEnabled(false);
                }

                mSpinner_sampling.setSelection(0);
                mSpinner_sensor.setSelection(0);
                mTagNum = 1;
                mTagButton.setText("START " + mTagNum);

                mServerStatus.setText("not starting");
                mServeraddr.setEnabled(false);
                mServeraddr.setText("please enter server address");
                mSendCheckBox.setChecked(false);
                SensorDataBuffer = "tag, type, x, y, z, ad1, time, real_time, acc\r\n";
                SensorDataBufferLength = 0;

//                new SendToDataLayerThread(SendToDataLayerThread.TYPE_MSSG, "/msg", null, "start,"+mTagNum+"," +mSensorName+ "," +mSamplingRate).start();
//                startButtonProc(mTagNum);
            }
        });

//        mSendMsgButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new SendToDataLayerThread(SendToDataLayerThread.TYPE_MSSG, "/msg", null, "Hello").start();
//            }
//        });

        // graph with dynamically genereated horizontal and vertical labels
        mGraphView1 = new GraphView(this);
//        mGraphView1.setTitle("rotation vector");
        // set legend
//        mGraphView1.setShowLegend(true);
//        mGraphView1.setManualYAxisBounds(1.25, -1.25);
        // set view port, start=2, size=40
//        mGraphView1.setViewPort(2, 150);
//        mGraphView1.setScrollable(true);
        mGraphView1.getViewport().setXAxisBoundsManual(true);
        mGraphView1.getViewport().setMinX(0);
        mGraphView1.getViewport().setMaxX(200);

        Xaxis = new LineGraphSeries<DataPoint>();
        Yaxis = new LineGraphSeries<DataPoint>();
        Zaxis = new LineGraphSeries<DataPoint>();

        mGraphView1.addSeries(Xaxis);
        mGraphView1.addSeries(Yaxis);
        mGraphView1.addSeries(Zaxis);

        Xaxis.setTitle("X-axis");
        Xaxis.setColor(Color.RED);
        Yaxis.setTitle("Y-axis");
        Yaxis.setColor(Color.GREEN);
        Zaxis.setTitle("Z-axis");
        Zaxis.setColor(Color.BLUE);

        mGraphView1.getViewport().setYAxisBoundsManual(true);
        mGraphView1.getViewport().setMinY(-10);
        mGraphView1.getViewport().setMaxY(10.0);
        mGraphView1.getLegendRenderer().setVisible(true);

//        graph.getViewport().setScrollable(true);

//        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);


        LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
        layout.addView(mGraphView1);

        mGraphView2 = new GraphView(this);
        mGraphView2.setTitle("orientation");


        // set legend
//        mGraphView2.setShowLegend(true);
//        mGraphView2.setManualYAxisBounds(360, -360);
//        // set view port, start=2, size=40
//        mGraphView2.setViewPort(2, 150);
//        mGraphView2.setScrollable(true);
//        LinearLayout layout2 = (LinearLayout) findViewById(R.id.graph2);
//        layout2.addView(mGraphView2);

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
            mClearButton.setEnabled(true);

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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
//        if (!mResolvingError) {  // more about this later //TODO
        mGoogleApiClient.connect();
//        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Controller Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://kr.co.jonginlee.patbingsua/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Controller Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://kr.co.jonginlee.patbingsua/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
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

            if (message != null) {
                Toast.makeText(getApplicationContext(), "received MSG success", Toast.LENGTH_LONG).show();

                //startButtonProc();
//                    new MyAsyncTask().execute(message);

            } else if (dataMessage != null) {

//                Toast.makeText(getApplicationContext(), "received DATA success", Toast.LENGTH_LONG).show();
//                Log.v(TAG, "dataMessage: {" + dataMessage + "}");
                mDataMessage = dataMessage;
                int idx = 0;
                char mychar = ',';

                for (int i = 2; i < mDataMessage.length(); i++) {
                    if (mychar == mDataMessage.charAt(i)) {
                        idx = i;
                        break;
                    }
                }
                String substring = mDataMessage.substring(2, idx);
                mStatus.setText("Received [Success] : " + substring);
//                mSensorName = substring;
//                Toast.makeText(getApplicationContext(), substring , Toast.LENGTH_LONG).show();

//                if(substring.equalsIgnoreCase("RotationVector")) {
                DrawGraphProc();
//                }else if(substring.equalsIgnoreCase("orientation")){
//                    DrawGraphProc2();
//                }


            }


        }


    }

//    private void DrawGraphProc2() {
//
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mLatch2 = new CountDownLatch(1);
//
//                        String[] line = mDataMessage.split("\r\n");
////                                Log.v(TAG, "a msg: {" + line[0] + "}");
//                        int prevXlen = 0;
//                        int prevYlen = 0;
//                        int prevZlen = 0;
//
//                        GraphViewData[] mXGraphViewData_prev = null;
//                        GraphViewData[] mYGraphViewData_prev = null;
//                        GraphViewData[] mZGraphViewData_prev = null;
//
//                        if (mXGraphViewData2 != null) {
//                            prevXlen = mXGraphViewData2.length;
//                            mXGraphViewData_prev = mXGraphViewData2.clone();
//                        }
//                        mXGraphViewData2 = new GraphViewData[line.length + prevXlen];
//
//                        if (mYGraphViewData2 != null) {
//                            prevYlen = mYGraphViewData2.length;
//                            mYGraphViewData_prev = mYGraphViewData2.clone();
//                        }
//                        mYGraphViewData2 = new GraphViewData[line.length + prevYlen];
//
//                        if (mZGraphViewData2 != null) {
//                            prevZlen = mZGraphViewData2.length;
//                            mZGraphViewData_prev = mZGraphViewData2.clone();
//                        }
//                        mZGraphViewData2 = new GraphViewData[line.length + prevZlen];
//
//
//                        for (int i = 0; i < line.length; i++) {
//                            String[] items = line[i].split(",");
//                            if (items.length > 3) {
//                                mXGraphViewData2[i] = new GraphViewData(i, Double.parseDouble(items[2]));
//                                mYGraphViewData2[i] = new GraphViewData(i, Double.parseDouble(items[3]));
//                                mZGraphViewData2[i] = new GraphViewData(i, Double.parseDouble(items[4]));
//                            } else
//                                Log.v(TAG, "items.length>3 " + items.toString());
//                        }
//
//                        if (mXGraphViewData_prev != null) {
//                            for (int i = 0; i < prevXlen; i++) {
//                                mXGraphViewData2[line.length + i] = new GraphViewData(line.length + i, mXGraphViewData_prev[i].getY());
//                            }
//                        }
//
//                        if (mYGraphViewData_prev != null) {
//                            for (int i = 0; i < prevYlen; i++) {
//                                mYGraphViewData2[line.length + i] = new GraphViewData(line.length + i, mYGraphViewData_prev[i].getY());
//                            }
//                        }
//
//                        if (mZGraphViewData_prev != null) {
//                            for (int i = 0; i < prevZlen; i++) {
//                                mZGraphViewData2[line.length + i] = new GraphViewData(line.length + i, mZGraphViewData_prev[i].getY());
//                            }
//                        }
//
//
//                        LineGraphSeries<DataPoint> Xaxis = new LineGraphSeries<DataPoint>("X-axis", mXGraphViewData2);
//                        LineGraphSeries<DataPoint> Yaxis = new LineGraphSeries<DataPoint>("Y-axis", new LineGraphSeries.GraphViewSeriesStyle(Color.rgb(255, 0, 255), 3), mYGraphViewData2);
//                        LineGraphSeries<DataPoint> Zaxis = new LineGraphSeries<DataPoint>("Z-axis", new LineGraphSeries.GraphViewSeriesStyle(Color.rgb(0, 255, 0), 3), mZGraphViewData2);
//
//                        mGraphView2.removeAllSeries();
//                        mGraphView2.addSeries(Xaxis);
//                        mGraphView2.addSeries(Yaxis);
//                        mGraphView2.addSeries(Zaxis);
//
//
//                        mLatch2.countDown();
//                    }
//                });
//
//
//            }
//        };
//
//        thread.start();
//    }


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
//                        int prevXlen = 0;
//                        int prevYlen = 0;
//                        int prevZlen = 0;
//
//                        GraphViewData[] mXGraphViewData_prev = null;
//                        GraphViewData[] mYGraphViewData_prev = null;
//                        GraphViewData[] mZGraphViewData_prev = null;
//
//                        if (mXGraphViewData1 != null) {
//                            prevXlen = mXGraphViewData1.length;
//                            mXGraphViewData_prev = mXGraphViewData1.clone();
//                        }
//                        mXGraphViewData1 = new GraphViewData[line.length + prevXlen];
//
//                        if (mYGraphViewData1 != null) {
//                            prevYlen = mYGraphViewData1.length;
//                            mYGraphViewData_prev = mYGraphViewData1.clone();
//                        }
//                        mYGraphViewData1 = new GraphViewData[line.length + prevYlen];
//
//                        if (mZGraphViewData1 != null) {
//                            prevZlen = mZGraphViewData1.length;
//                            mZGraphViewData_prev = mZGraphViewData1.clone();
//                        }
//                        mZGraphViewData1 = new GraphViewData[line.length + prevZlen];
//
//
//                        for (int i = 0; i < line.length; i++) {
//                            String[] items = line[i].split(",");
//                            if (items.length > 3) {
//                                mXGraphViewData1[i] = new GraphViewData(i, Double.parseDouble(items[2]));
//                                mYGraphViewData1[i] = new GraphViewData(i, Double.parseDouble(items[3]));
//                                mZGraphViewData1[i] = new GraphViewData(i, Double.parseDouble(items[4]));
//                            } else
//                                Log.v(TAG, "items.length>3 " + items.toString());
//                        }
//
//                        if (mXGraphViewData_prev != null) {
//                            for (int i = 0; i < prevXlen; i++) {
//                                mXGraphViewData1[line.length + i] = new GraphViewData(line.length + i, mXGraphViewData_prev[i].getY());
//                            }
//                        }
//
//                        if (mYGraphViewData_prev != null) {
//                            for (int i = 0; i < prevYlen; i++) {
//                                mYGraphViewData1[line.length + i] = new GraphViewData(line.length + i, mYGraphViewData_prev[i].getY());
//                            }
//                        }
//
//                        if (mZGraphViewData_prev != null) {
//                            for (int i = 0; i < prevZlen; i++) {
//                                mZGraphViewData1[line.length + i] = new GraphViewData(line.length + i, mZGraphViewData_prev[i].getY());
//                            }
//                        }


//                        GraphViewSeries Xaxis = new GraphViewSeries("X-axis", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(255, 0, 0), 3), mXGraphViewData1);
//                        GraphViewSeries Yaxis = new GraphViewSeries("Y-axis", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(255, 0, 255), 3), mYGraphViewData1);
//                        GraphViewSeries Zaxis = new GraphViewSeries("Z-axis", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(0, 255, 0), 3), mZGraphViewData1);


                        for (int i = 1; i <= line.length; i++) {
                            String[] items = line[i - 1].split(",");
                            if (items.length > 3) {
                                Log.i("appendData", "" + mLength + ", " + Double.parseDouble(items[2]));
                                Xaxis.appendData(new DataPoint(mLength, Double.parseDouble(items[2])), true, 200);
                                Yaxis.appendData(new DataPoint(mLength, Double.parseDouble(items[3])), true, 200);
                                Zaxis.appendData(new DataPoint(mLength, Double.parseDouble(items[4])), true, 200);
                                mLength = mLength + 1;
                            } else
                                Log.v(TAG, "items.length>3 " + items.toString());

                            if(SensorDataBuffer == null){
                                SensorDataBuffer = line[i - 1] + "\r\n";
                            }else
                                SensorDataBuffer = SensorDataBuffer + line[i - 1] + "\r\n";
                            SensorDataBufferLength = SensorDataBufferLength + 1;
                        }

//                        mGraphView1.removeAllSeries();
//                        mGraphView1.addSeries(Xaxis);
//                        mGraphView1.addSeries(Yaxis);
//                        mGraphView1.addSeries(Zaxis);


                        mLatch1.countDown();


                        if(mSendCheckBox.isChecked() == true){
                            if(SensorDataBufferLength > 50){
                                HashMap<String, String> params = new HashMap<String, String>();
                                params.put("logs", SensorDataBuffer);
                                params.put("sensorname", mSensorName);
                                params.put("samplingrate", mSamplingRate);

                                String res = null;
                                try {
                                    res = getPostDataString(params);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                                new datasender().execute(res);

                                SensorDataBufferLength = 0;
                                SensorDataBuffer = null;
                            }
                        }

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
