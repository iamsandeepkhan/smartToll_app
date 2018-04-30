package com.example.android.metro.main;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.metro.BillActivity;
import com.example.android.metro.R;
import com.example.android.metro.Utilities;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * A fragment to show user all of his/her previous journeys.
 *
 * @author Sandeep Khan
 */
public class HistoryFragment extends Fragment {


    private HistoryAdapter adapter;
    private String username;
    public HistoryFragment() {
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
                new FetchHistory().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_history, container, false);
        ListView listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new HistoryAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               HistoryItem item = (HistoryItem) adapterView.getAdapter().getItem(i);
                Intent intent = new Intent(getActivity(), BillActivity.class);
                intent.putExtra("tourid",item.tourID);
                startActivity(intent);
            }
        });
        listView.setEmptyView((TextView)view.findViewById(R.id.empty_list));
        SharedPreferences pref = getActivity().getSharedPreferences(Utilities.PREF_NAME,Context.MODE_PRIVATE);
        username = pref.getString("username","subho040995");
        new FetchHistory().execute();
        return view;
    }

    /**
     * Fetches and displays all the previous journeys of a user.
     */
    public class FetchHistory extends AsyncTask<Void,Void,ArrayList<HistoryItem>>{
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Fetchig history...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        protected ArrayList<HistoryItem> doInBackground(Void... voids){
            String response="";
            try{
                URL url = new URL(getActivity().getString(R.string.host) + "/cus.getHistoryEntry.smartToll.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", username);
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

            ArrayList<HistoryItem> items = new ArrayList<HistoryItem>();
            Log.e("Track ME2",response);
            if(response.length()>0){
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    int tours = jsonObject.getInt("nooftours");
                    for(int i=1;i<=tours;i++){
                        JSONObject item = jsonObject.getJSONObject(i+"");
                        items.add(new HistoryItem(
                                item.getString("station_from"),
                                item.getString("station_to"),
                                item.getString("entry_time"),
                                item.getString("exit_time"),
                                item.getDouble("tourcost"),
                                item.getString("tourid")
                        ));
                    }
                }catch (Exception e){e.printStackTrace();}
            }
            return items;
        }

        @Override
        protected void onPostExecute(ArrayList<HistoryItem> s) {
            progressDialog.dismiss();
            if(s.size()>0){
                adapter.clear();
                adapter.addAll(s);
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Custom Adapter class to populate listview.
     */
    public class HistoryAdapter extends ArrayAdapter<HistoryItem>{
        Context mContext;
        public HistoryAdapter(@NonNull Context context) {
            super(context, 0);
            mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            HistoryItem item = getItem(position);
            if(convertView==null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.history_item, parent, false);
            }
            TextView startStation = convertView.findViewById(R.id.start_station);
            TextView endStation = convertView.findViewById(R.id.end_station);
            TextView amount = convertView.findViewById(R.id.amount);

            startStation.setText(item.startStation);
            endStation.setText(item.endStation);
            amount.setText("Rs "+String.format("%.2f", item.amount));

            return convertView;
        }
    }
}
