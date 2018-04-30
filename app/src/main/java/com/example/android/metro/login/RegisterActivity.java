package com.example.android.metro.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

/**RegisterActivity class handles user registration.
 *
 * @author Sandeep Khan
 *
 */
public class RegisterActivity extends AppCompatActivity {
    EditText name,username,password,conpassword;
    Button register;
    String namevalue,usernamevalue,passwordvalue;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name=(EditText) findViewById(R.id.register_name);
        username=(EditText) findViewById(R.id.register_username);
        password=(EditText) findViewById(R.id.register_password);
        conpassword=(EditText) findViewById(R.id.register_conpassword);

        register=(Button) findViewById(R.id.registerbut);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validateInput(name.getText().toString().trim(),username.getText().toString().trim(),password.getText().toString().trim(),conpassword.getText().toString().trim())){
                    //  Toast.makeText(RegisterActivity.this,"Ready to process",Toast.LENGTH_SHORT).show();
                    namevalue=name.getText().toString().trim();
                    usernamevalue=username.getText().toString().trim();
                    passwordvalue=password.getText().toString().trim();

                    new RegisterUser(RegisterActivity.this).execute();
                }

            }
        });
    }

    /**
     * This method checks whether name, username and password entered by user
     * are valid or not.
     * @param name Name of user
     * @param username  Users email id
     * @param password Password entered by user
     * @param conpassword Confirm password entered by user
     * @return true if the parameters are valid and false if parameters are invalid
     */
    private boolean validateInput(String name,String username, String password, String conpassword){
        if(name.equals("") || username.equals("") || password.equals("") || conpassword.equals("")){

            Toast.makeText(this,"Cannot accept any empty field",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!Utilities.isNameValid(name)){
            this.username.setError("Invalid name format");
            return false;
        }
        else if(!Utilities.isEmailIDValid(username)){
            this.name.setError("Invalid emailid");
            return false;
        }
        else if(!password.equals(conpassword)){
            this.conpassword.setError("Password and confirm password mismatch");
            return false;
        }

        return true;
    }

    /**
     * RegisterUser class sends user data to server to register user.
     *
     */
    private class RegisterUser extends AsyncTask<String, Void, String> {

        Context context;
        private RegisterUser(Context context){
            this.context=context;
        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if(!(progressDialog!=null && progressDialog.isShowing())){
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Please wait, while we process registration.");
                progressDialog.setTitle("Registering user");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try {
                URL url = new URL(context.getString(R.string.host) + "/cus.register.smartToll.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(1000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("name", namevalue)
                        .appendQueryParameter("username", usernamevalue)
                        .appendQueryParameter("password", passwordvalue);

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

                    Toast.makeText(context,"Successfully registered",Toast.LENGTH_SHORT).show();
                    finish();
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
                        Toast.makeText(context,"Emaild already registered",Toast.LENGTH_SHORT).show();
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
}
