package com.example.android.metro;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

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
 * Display bill for a completed journey.
 * @author Sandeep Khan
 */
public class BillActivity extends AppCompatActivity {

    private String tourID="";
    private String username;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        SharedPreferences pref = getSharedPreferences(Utilities.PREF_NAME, Context.MODE_PRIVATE);
        username = pref.getString("username","");
        Intent intent = getIntent();
        tourID = intent.getStringExtra("tourid");
        new FetchBill().execute();
    }

    /**
     * Fetch the details of a completed journey from server.
     */
    public class FetchBill extends AsyncTask<Void,Void,String>{
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(BillActivity.this);
            progressDialog.setMessage("Fetching bill...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(Void... voids){
            String response = "";
            try{
                URL url = new URL(getString(R.string.host) + "/cus.getEntryInfo.smartToll.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("tourid",tourID+"")
                        .appendQueryParameter("username",username);

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
            Log.e("Track ME",result);
            TextView amount = (TextView)findViewById(R.id.amount);
            TextView startTime = (TextView)findViewById(R.id.start_time);
            TextView endTime = (TextView)findViewById(R.id.end_time);
            TextView startFrom = (TextView)findViewById(R.id.start_from);
            TextView endAt = (TextView)findViewById(R.id.end_at);
            TextView tourid = (TextView)findViewById(R.id.tourid);

            try{
                JSONObject jsonObject = new JSONObject(result);
                amount.setText("Rs "+String.format("%.2f", jsonObject.getDouble("tourcost")));
                startTime.setText(jsonObject.getString("entry_time"));
                endTime.setText(jsonObject.getString("exit_time"));
                startFrom.setText(jsonObject.getString("station_from"));
                endAt.setText(jsonObject.getString("station_to"));
                tourid.setText(tourID);

            }catch (Exception e){e.printStackTrace();}
            progressDialog.dismiss();
        }
    }
}
