package me.data_for.eventservice;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button;
    private static final String PREFERENCES_NAME = "EventService";

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void setServiceRunningButton() {
        if (isMyServiceRunning(MainService.class)) {
            button.setText("Stop Service");
            button.setTextColor(Color.RED);
        } else {
            button.setText("Start Service");
            button.setTextColor(Color.BLACK);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.buttonStartStop);
        button.setOnClickListener(this);

        TextView text = findViewById(R.id.textView);
        text.setText("android.os.Build.MANUFACTURER: " + Build.MANUFACTURER +
                "\nandroid.os.Build.BRAND: " + Build.BRAND +
                "\nandroid.os.Build.MODEL: " + Build.MODEL +
                "\nandroid.os.Build.PRODUCT: " + Build.PRODUCT +
                "\nandroid.os.Build.DEVICE: " + Build.DEVICE +
                "\nandroid.os.Build.ID: " + Build.ID +
                "\nandroid.os.Build.USER " + Build.USER +
                "\nandroid.os.Build.FINGERPRINT: " + Build.FINGERPRINT
        );

        SharedPreferences.Editor editor = this.getSharedPreferences(PREFERENCES_NAME, this.MODE_PRIVATE).edit();
        editor.remove("sessionId"); //Start a new session on app restart (new sessionId will be geenrated on the first event)
        editor.apply();

        setServiceRunningButton();

        if (this.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.READ_PHONE_STATE}, 0);
        }
    }

    @Override
    protected void onResume() {
        setServiceRunningButton();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == button) {
            if (isMyServiceRunning(MainService.class)) {
                stopService(new Intent(this, MainService.class));
            } else {
                startService(new Intent(this, MainService.class));
            }
            setServiceRunningButton();
        }
    }
}
