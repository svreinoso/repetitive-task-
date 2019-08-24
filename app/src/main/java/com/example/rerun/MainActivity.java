package com.example.rerun;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Console;

public class MainActivity extends AppCompatActivity {
    private String tag = "MainActivity";
    EditText minutesEditText;
    SharedPreferences sharedpreferences;
    AlarmManager alarmMgr;
    Button cancelAlarmBtn, setAlarmBtn;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        minutesEditText = (EditText) findViewById(R.id.editText2);
        cancelAlarmBtn = (Button) findViewById(R.id.button3);
        setAlarmBtn = (Button) findViewById(R.id.button2);
        textView = (TextView) findViewById(R.id.textView);

        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);

        int minutes = sharedpreferences.getInt("minutes", 0);
        Log.i(tag, String.valueOf(minutes));
        minutesEditText.setText(String.valueOf(minutes));

        String lastUpdateDate = sharedpreferences.getString("lastRunDate", "");
        textView.setText(lastUpdateDate);

        if (isAlarmSet()){
            cancelAlarmBtn.setVisibility(View.VISIBLE);
            setAlarmBtn.setVisibility(View.GONE);
        } else {
            setAlarmBtn.setVisibility(View.VISIBLE);
            cancelAlarmBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void SetAlarm (int minutes) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 1001, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000,
                minutes * 1000, //AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                sender);
        Log.i(tag, "Alarm set");
        cancelAlarmBtn.setVisibility(View.VISIBLE);
        setAlarmBtn.setVisibility(View.GONE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("minutes", minutes);
        editor.apply();
    }

    private boolean isAlarmSet() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        return  (PendingIntent.getBroadcast(this, 1001, intent,
                PendingIntent.FLAG_NO_CREATE) != null);
    }

    public void setAlarmClick(View view) {
        String minutesString = minutesEditText.getText().toString();
        try {
            int minutes = Integer.parseInt(minutesString);
            if (minutes < 1) {
                Snackbar.make(view, "Minutes should be greater than 0.", Snackbar.LENGTH_LONG)
                        .setAction("Ok", null).show();
                return;
            }
            SetAlarm(minutes);
        } catch (Exception ex){
            Snackbar.make(view, "Please set a valid number", Snackbar.LENGTH_LONG)
                    .setAction("Ok", null).show();
            Log.e(tag, "Error parsing number");
        }
    }

    public void cancelAlarmClick(View view) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1001,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pendingIntent);
        pendingIntent.cancel();
        setAlarmBtn.setVisibility(View.VISIBLE);
        cancelAlarmBtn.setVisibility(View.GONE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove("minutes");
        editor.remove("lastRunDate");
        textView.setText("");
        editor.apply();
    }
}
