package com.example.android.metro.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.metro.main.MainActivity;
import com.example.android.metro.R;
import com.example.android.metro.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A login screen that offers login via email/password.
 *
 * @author Sandeep Khan
 */
public class LoginActivity extends AppCompatActivity{
    Button login;
    EditText username,password;
    TextView fplink;
    String usernamevalue,passwordvalue;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login=(Button) findViewById(R.id.loginbut);
        username=(EditText) findViewById(R.id.login_username);
        password=(EditText) findViewById(R.id.login_password);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInput(username.getText().toString(),password.getText().toString())){
                    usernamevalue=username.getText().toString().trim();
                    passwordvalue=password.getText().toString();
                    if(Utilities.isNetworkAvailable(LoginActivity.this))
                        new FullAuthenticate(LoginActivity.this).execute();
                }
            }
        });

    }

    /**
     * The method checks whether the email id and password enterd by the user
     * are valid or not.
     * @param username Email id of user
     * @param password  Password of user
     * @return true if parameters are valid and false if parameters are invalid
     */
    private boolean validateInput(String username, String password){
        if(username.trim().equals("") || password.length()==0){
            Toast.makeText(LoginActivity.this,"Empty field",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!Utilities.isEmailIDValid(username)){
            this.username.setError("Invalid emailid");
            return false;
        }
        return true;
    }


    /**
     * FullAuthenticate class checks whether the user is registered or not
     * by querying the server. After verifying user credentials it opens the
     * MainActivity class.
     */
    private class FullAuthenticate extends AsyncTask<String, Void, String> {

        Context context;
        private FullAuthenticate(Context context){
            this.context=context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!(progressDialog!=null && progressDialog.isShowing())){
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Please wait, while we authenticate your credentials.");
                progressDialog.setTitle("Signing in");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try {
                URL url = new URL(getString(R.string.host)+"/cus.login.smartToll.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", usernamevalue)
                        .appendQueryParameter("password", passwordvalue);;
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
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            final JSONObject jbj;
            Log.e("TRACK ME",s);
            try {
                jbj = new JSONObject(s);
                JSONObject obj;

                if (!s.equals("") && jbj.getString("success").equals("true") && jbj.getString("errorCode").equals("null")) {
                    makeProgress(usernamevalue,jbj.getString("name"));
                }
                else if(!s.equals("") && jbj.getString("success").equals("false")){
                    if(jbj.getString("errorCode").equals("1")){
                        Toast.makeText(context,"App Build Failed. Update version.",Toast.LENGTH_SHORT).show();
                    }
                    if(jbj.getString("errorCode").equals("2")){
                        Toast.makeText(context,"Invalid emaild format",Toast.LENGTH_SHORT).show();
                    }

                    else if(jbj.getString("errorCode").equals("3")){
                        Toast.makeText(context,"Server not responsding. Try again after sometime.",Toast.LENGTH_SHORT).show();
                    }
                    else if(jbj.getString("errorCode").equals("4")){
                        Toast.makeText(context,"Wrong Credentials",Toast.LENGTH_SHORT).show();
                    }
                    else if(jbj.getString("errorCode").equals("5")){
                        Toast.makeText(context,"Server not responsding. Try again after sometime.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }
    }


    /**
     * The method saves email id and name of the user in SharedPreferences and opens the
     * MainActivity class.
     * @param username Email id of user
     * @param name  Name of user
     */
    private void makeProgress(String username,String name){
        SharedPreferences.Editor editor = getSharedPreferences(Utilities.PREF_NAME, MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("username", username);
        editor.commit();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}

