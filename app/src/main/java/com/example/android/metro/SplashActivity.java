package com.example.android.metro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.android.metro.login.EnterActivity;
import com.example.android.metro.main.MainActivity;

/**
 * Class to display splash screen
 * @author Sandeep Khan
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash2);
        new Splash().execute();
    }



    public class Splash extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(2000);
            }catch (Exception e){e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            SharedPreferences preferences = getSharedPreferences(Utilities.PREF_NAME, Context.MODE_PRIVATE);
            String username = preferences.getString("username","");
            Intent intent;
            if(username.length()==0){
                intent = new Intent(SplashActivity.this, EnterActivity.class);
            }else{
                intent = new Intent(SplashActivity.this,MainActivity.class);
            }
            finish();
            startActivity(intent);

        }
    }
}
