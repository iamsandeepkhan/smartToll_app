package com.example.android.metro.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.metro.R;
import com.example.android.metro.Utilities;
import com.example.android.metro.login.EnterActivity;

/**
 * MainActivity class allows the user to view his/her current journey,
 * past journeys and balance by showing the respective fragments.
 *
 * @author Sandeep Khan
 */
public class MainActivity extends AppCompatActivity{

    public static int NUM_PAGES = 3;
    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(NUM_PAGES);
        TabLayout tab = (TabLayout)findViewById(R.id.sliding_tab);
        tab.setupWithViewPager(mPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                new Logout().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0)
                return new JourneyFragment();
            else if(position==1)
                return new HistoryFragment();
            else if(position==2)
                return new BalanceFragment();
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0: return "Journey";
                case 1: return "History";
                case 2: return "Balance";
                default: return null;
            }
        }
    }

    /**
     * It deletes all the data of logged in user saved in SharedPreference and
     * redirects the user to EnterActivity class.
     */
    public class Logout extends AsyncTask<Void,Void,Void>{
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Logging out...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected Void doInBackground(Void... voids){
            try{
                Thread.sleep(2000);
            }catch (Exception e){e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            SharedPreferences.Editor editor = getSharedPreferences(Utilities.PREF_NAME, Context.MODE_PRIVATE).edit();
            editor.clear();
            editor.commit();
            progressDialog.dismiss();
            Intent intent = new Intent(MainActivity.this, EnterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
