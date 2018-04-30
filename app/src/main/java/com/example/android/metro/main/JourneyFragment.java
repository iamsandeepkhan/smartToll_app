package com.example.android.metro.main;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.metro.BillActivity;
import com.example.android.metro.QRScanner;
import com.example.android.metro.R;
import com.example.android.metro.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A fragment to show user the status of his/her current journey.
 *
 * @author Sandeep Khan
 */
public class JourneyFragment extends Fragment {


    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    public static final String APP_NAME = "appname";

    private boolean infoFetched = false;
    private boolean travelling = false;

    private TextView startStation;
    private TextView currentStatus;
    private Button button;


    private String tourID = "";
    private String usernamevalue;


    private static int CAMERA_REQ = 2;

    public JourneyFragment() {
        // Required empty public constructor
    }

    /**
     * It checks if Camera permission has been granted by user.
     * If permission has been granted it opens the QRScanner class else
     * it asks for Camera permission.
     */
    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.CAMERA},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        Intent intent = new Intent(getActivity(),QRScanner.class);
        startActivityForResult(intent,CAMERA_REQ);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getActivity(),QRScanner.class);
                    startActivityForResult(intent,CAMERA_REQ);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_journey, container, false);
        startStation = (TextView)view.findViewById(R.id.start_station);
        currentStatus = (TextView)view.findViewById(R.id.current_status);
        button = (Button)view.findViewById(R.id.journey_button);
        TextView welcome = (TextView)view.findViewById(R.id.name_text);

        SharedPreferences pref = getActivity().getSharedPreferences(Utilities.PREF_NAME, Context.MODE_PRIVATE);
        welcome.setText("Welcome "+pref.getString("name","").trim()+"!");
        usernamevalue = pref.getString("username","");

        if(!infoFetched) {
            infoFetched = true;
            new JourneyInfo().execute();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    checkPermission();

            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode==CAMERA_REQ){
            if(resultCode== Activity.RESULT_OK){
                String data = intent.getStringExtra("qrcode");
                if(data!=null) {
                    try{
                        JSONObject jsonObject = new JSONObject(data);
                        String appname = jsonObject.getString(APP_NAME);
                        if(appname.equals("smarttoll")) {
                            String station = jsonObject.getString("station");
                            String gate = jsonObject.getString("gate");
                            if (travelling) {
                                new StopJourney(station,gate).execute();
                            } else {
                                new StartJourney(station,gate).execute();
                            }
                        }else{
                            Toast.makeText(getActivity(),"Invalid Qrcode",Toast.LENGTH_SHORT).show();
                        }
                    }catch (JSONException e){
                        Toast.makeText(getActivity(),"Invalid Qrcode",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * Fetches and displays the current journey status of user.
     */
    public class JourneyInfo extends AsyncTask<Void,Void,String>{
        //private String mUsername;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Getting info...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response="";
            try{
                URL url = new URL(getActivity().getString(R.string.host) + "/cus.getLastEntry.smartToll.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", usernamevalue);
                String query = builder.build().getEncodedQuery();


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
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
            }catch (Exception e){e.printStackTrace();}
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if(result.length()>0){
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.getInt("status");
                    if(status == 1){
                        tourID = jsonObject.getString("tourid");
                        travelling = true;
                        updateUI(jsonObject.getString("station_from"));

                    }else{
                        travelling = false;
                        tourID = "";
                        updateUI("");
                    }
                }catch (Exception e){e.printStackTrace();}
            }
        }
    }

    /**
     * Updates the UI of JourneyFragment by setting the start station name,
     * travelling status of user and setting the button name.
     * @param station Name of station from which journey has started
     */
    public void updateUI(String station){
        if(travelling){
            startStation.setText(station);
            currentStatus.setText("Travelling");
            button.setText("End Journey");
            button.setBackgroundResource(R.drawable.btn_end_journey);
        }else{
            startStation.setText("Not set");
            currentStatus.setText("Halt");
            button.setText("Start Journey");
            button.setBackgroundResource(R.drawable.button_style);
        }
    }

    /**
     * Starts user journey by sending data to server.
     */
    public class StartJourney extends AsyncTask<Void,Void,String>{

        private ProgressDialog progressDialog;
        private String station,gate;
        public StartJourney(String st, String g){
            station = st;
            gate=g;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Starting journey...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String response="";
            try{
                URL url = new URL(getActivity().getString(R.string.host) + "/cus.setEntry.smartToll.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", usernamevalue)
                        .appendQueryParameter("station_from",station)
                        .appendQueryParameter("status",1+"")
                        .appendQueryParameter("gate",gate);
                String query = builder.build().getEncodedQuery();


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
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


            }catch (Exception e){e.printStackTrace();}
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Log.e("Track ME",result);
            if(result.length()>0){
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean success = jsonObject.getBoolean("success");
                    if(!success && jsonObject.getInt("errorCode")==7){
                        Toast.makeText(getActivity(),"Not enough balance",Toast.LENGTH_SHORT).show();
                        travelling = false;
                        tourID = "";
                    }else{
                        tourID = jsonObject.getString("tourid");
                        travelling = true;
                        updateUI(station);
                    }
                }catch (Exception e){e.printStackTrace();}
            }
        }
    }

    /**
     * Finishes user journey by sending data to server.
     */
    public class StopJourney extends AsyncTask<Void,Void,String>{
        private ProgressDialog progressDialog;
        private String destination,gate;

        public StopJourney(String dest, String g){
            destination = dest;
            gate = g;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Finishing journey...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... integers) {
            String response="";
            try{
                URL url = new URL(getActivity().getString(R.string.host) + "/cus.setExit.smartToll.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", usernamevalue)
                        .appendQueryParameter("tourid",tourID+"")
                        .appendQueryParameter("station_to",destination)
                        .appendQueryParameter("status",2+"")
                        .appendQueryParameter("gate",gate);
                String query = builder.build().getEncodedQuery();


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
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


            }catch (Exception e){e.printStackTrace();}
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
           progressDialog.dismiss();
            Intent intent = new Intent(getActivity(), BillActivity.class);
            intent.putExtra("tourid",tourID);
            startActivity(intent);

            travelling = false;
            tourID="";
            updateUI("");

        }
    }
}
