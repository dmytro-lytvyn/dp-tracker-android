package me.data_for.eventservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class MainService extends Service {
    final BroadcastReceiver mReceiver = new EventReceiver();
    final EventSender sender = new EventSender();

    String eventContext = "ANDROID";
    String eventObject = "EVENT_SERVICE";
    String eventObjectId = null;
    String eventAction = null;
    String eventSchemaVersion = null;

    @Nullable
    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_REBOOT);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mReceiver, filter);

        eventAction = "START";
        eventSchemaVersion = "v.1";
        sender.sendEvent(this, eventContext, eventObject, eventObjectId, eventAction, eventSchemaVersion);

        return START_STICKY;

        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        eventAction = "STOP";
        eventSchemaVersion = "v.1";
        sender.sendEvent(this, eventContext, eventObject, eventObjectId, eventAction, eventSchemaVersion);

        super.onDestroy();
    }
}
