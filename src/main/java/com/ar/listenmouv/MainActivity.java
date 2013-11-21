package com.ar.listenmouv;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.Calendar;

public class MainActivity extends ActionBarActivity {

    PlayService pService;
    boolean mBound = false;
    boolean launchAlarm = false;
    Context context;
    boolean playing = false;
    boolean hq = true;
    Button buttonControl;
    Button toggle;
    ProgressBar progressBar;

    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = new Intent(this, PlayService.class);
        Log.d("onStart", "pService");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        toggle = (Button)findViewById(R.id.toggle_button);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(hq){
                    toggle.setBackground(getResources().getDrawable(R.drawable.sd_selector));
                }
                else
                {
                    toggle.setBackground(getResources().getDrawable(R.drawable.hd_selector));
                }
                hq = !hq;
            }
        });

        buttonControl = (Button)findViewById(R.id.play);
        buttonControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (playing) {
                    buttonControl.setBackground(getResources().getDrawable(R.drawable.play_selector));
                    toggle.setEnabled(true);
                    new controlRadio().execute();
                } else {
                    buttonControl.setBackground(getResources().getDrawable(R.drawable.stop_selector));
                    toggle.setEnabled(false);
                    buttonControl.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    new controlRadio().execute();
                }
            }
        });

        Log.d("ALARM", "outside savedInstanceState");
        if(getIntent().getExtras() != null){
            Log.d("ALARM", "in savedInstanceState");
//            Boolean alarm = savedInstanceState.getBoolean("alarm");
//            Log.d("ALARM", "in savedInstanceState " + alarm);
            launchAlarm = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            //Home button
            case(R.id.item_alarm):
                Intent intent = new Intent(this, AlarmActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.LocalBinder binder = (PlayService.LocalBinder)service;
            Log.d("onServiceConnected", "binder " + binder);
            pService = binder.getService();
            Log.d("onServiceConnected", "pService " + pService);
            mBound = true;

            if(launchAlarm){
                buttonControl.setBackground(getResources().getDrawable(R.drawable.stop_selector));
                toggle.setEnabled(false);
                buttonControl.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                new controlRadio().execute();
                launchAlarm = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    public class controlRadio extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if(playing){
                pService.stop();
            }
            else{
                Log.d("doInBackground", "pService " + pService);
                pService.play(hq);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!playing){
                progressBar.setVisibility(View.INVISIBLE);
                buttonControl.setEnabled(true);
            }
            playing = !playing;
        }

    }
}
