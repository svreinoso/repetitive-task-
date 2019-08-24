package com.example.rerun;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "1";
    private String tag = "AlarmReceiver";
    private String url;

    @Override
    public void onReceive(Context context, Intent intent) {
        sendRequest(context);
    }

    private void createNotificationChannel(Context context) {
        Log.i(tag, "channel created");
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(Context context, boolean success) {
        createNotificationChannel(context);
        Log.i(tag, "Alarm received");
        String date = date = Calendar.getInstance().getTime().toString();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Re Run")
                .setContentText("Last Update : " + date + " ,\nIs online: " + success)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Last Update : " + date + " ,\nIs online: " + success))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        int notificationId = 1;
        notificationManager.notify(notificationId, builder.build());
        Log.i(tag, "notification sent");
        setLastRunDate(context, success);
    }

    private void setLastRunDate(Context context, boolean isOnline){
        SharedPreferences sharedpreferences = context.getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        Date date = Calendar.getInstance().getTime();
        editor.putString("lastRunDate", date.toString());
        editor.putBoolean("isOnline", isOnline);
        editor.apply();
    }

    private void sendRequest(final Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        SharedPreferences sharedpreferences = context.getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        url = sharedpreferences.getString("url", "");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       sendNotification(context, true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(tag, "Error sending request: " + error.getMessage());
                sendNotification(context, false);
            }
        });

        queue.add(stringRequest);
    }
}
