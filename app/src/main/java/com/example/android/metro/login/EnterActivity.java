package com.example.android.metro.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.android.metro.R;

/**
 * EnterActivity class act as an entry point through which users
 * can go to either login page or registration page.
 *
 * @author Sandeep Khan
 */
public class EnterActivity extends AppCompatActivity {


    Button login,register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        login=(Button) findViewById(R.id.loginbut);
        register=(Button) findViewById(R.id.registerbut);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(EnterActivity.this, LoginActivity.class);
                startActivity(myIntent);
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(EnterActivity.this, RegisterActivity.class);
                startActivity(myIntent);
            }
        });
    }

}
