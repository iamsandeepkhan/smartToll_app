package com.example.android.metro.main;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
 * A fragment to show user his/her remaining balance and providing user
 * UI to update his/her balance.
 * @author Sandeep Khan
 */
public class BalanceFragment extends Fragment {

    private double balance = 0;
    private TextView balanceText;
    private EditText amountText;
    private String username;

    public BalanceFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                SharedPreferences pref = getActivity().getSharedPreferences(Utilities.PREF_NAME, Context.MODE_PRIVATE);
                username = pref.getString("username","");
                new FetchBalance(username).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_balance, container, false);
        balanceText = (TextView)view.findViewById(R.id.balance);
        amountText = (EditText)view.findViewById(R.id.amount);
        SharedPreferences pref = getActivity().getSharedPreferences(Utilities.PREF_NAME, Context.MODE_PRIVATE);
        username = pref.getString("username","");

        new FetchBalance(username).execute();

        Button update = (Button)view.findViewById(R.id.update_button);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount = amountText.getText().toString().trim();
                if(!TextUtils.isEmpty(amount)){
                    new UpdateBalance(username,new Double(amount)).execute();
                }else{
                    amountText.setError("Amount needed");
                }
            }
        });
        return view;
    }

    /**
     * Fetches and displays balance of a user from server.
     */
    public class FetchBalance extends AsyncTask<Void,Void,String>{
        String username;
        private ProgressDialog progressDialog;
        public FetchBalance(String name){
            username = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Fetching balance...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        protected String  doInBackground(Void... voids){
            String response="";
            try{
                URL url = new URL(getActivity().getString(R.string.host) + "/cus.manageBankAccount.smartToll.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", username)
                        .appendQueryParameter("option","get");

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
            try{
                JSONObject jsonObject = new JSONObject(result);
                balance = jsonObject.getDouble("money");
                balanceText.setText("Balance: Rs"+String.format("%.2f", balance));
            }catch (JSONException e){e.printStackTrace();}

            progressDialog.dismiss();
        }
    }

    /**
     * Updates user balance by sending the amount to server.
     */
    public class UpdateBalance extends AsyncTask<Void,Void,String>{
        String username;
        double amount;
        private ProgressDialog progressDialog;
        public UpdateBalance(String name,double amt){
            username = name;
            amount = amt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Updating balance...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        protected String doInBackground(Void... voids){
            String response="";
            try{
                URL url = new URL(getActivity().getString(R.string.host) + "/cus.manageBankAccount.smartToll.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", username)
                        .appendQueryParameter("option","set")
                        .appendQueryParameter("money",amount+"");

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
            try {
                JSONObject jsonObject = new JSONObject(result);
                balanceText.setText("Balance: Rs"+String.format("%.2f",(jsonObject.getDouble("money"))));
            }catch (Exception e){e.printStackTrace();}

            progressDialog.dismiss();

            amountText.setText("");
        }
    }

}
