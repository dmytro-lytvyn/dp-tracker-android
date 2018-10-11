package me.data_for.eventservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class EventReceiver extends BroadcastReceiver {
    final EventSender sender = new EventSender();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String intentAction = intent.getAction();
        String eventContext = "ANDROID";
        String eventObject = null;
        String eventObjectId = null;
        String eventAction = null;
        String eventSchemaVersion = null;

        if (intentAction != null) {
            if (intentAction.equals(Intent.ACTION_SCREEN_ON)) {
                eventObject = "SCREEN";
                eventAction = "TURN_ON";
                eventSchemaVersion = "v.1";
            } else if (intentAction.equals(Intent.ACTION_SCREEN_OFF)) {
                eventObject = "SCREEN";
                eventAction = "TURN_OFF";
                eventSchemaVersion = "v.1";
            } else if (intentAction.equals(Intent.ACTION_USER_PRESENT)) {
                eventObject = "USER";
                eventAction = "PRESENT";
                eventSchemaVersion = "v.1";
            } else if (intentAction.equals(Intent.ACTION_BOOT_COMPLETED)) {
                eventObject = "SYSTEM";
                eventAction = "BOOT_COMPLETED";
                eventSchemaVersion = "v.1";
            } else if (intentAction.equals(Intent.ACTION_REBOOT)) {
                eventObject = "SYSTEM";
                eventAction = "REBOOT";
                eventSchemaVersion = "v.1";
            } else if (intentAction.equals(Intent.ACTION_SHUTDOWN)) {
                eventObject = "SYSTEM";
                eventAction = "SHUTDOWN";
                eventSchemaVersion = "v.1";
            } else if (intentAction.equals(Intent.ACTION_BATTERY_CHANGED)) {
                eventObject = "BATTERY_LEVEL";
                eventObjectId = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));
                eventAction = "CHANGE";
                eventSchemaVersion = "v.1";
            } else if (intentAction.equals(Intent.ACTION_POWER_CONNECTED)) {
                eventObject = "POWER";
                eventAction = "CONNECT";
                eventSchemaVersion = "v.1";
            } else if (intentAction.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                eventObject = "POWER";
                eventAction = "DISCONNECT";
                eventSchemaVersion = "v.1";
            } else if (intentAction.equals(Intent.ACTION_HEADSET_PLUG)) {
                eventObject = "HEADSET";
                eventAction = "PLUG";
                eventSchemaVersion = "v.1";
            }

            if (eventObject != null) {
                sender.sendEvent(context, eventContext, eventObject, eventObjectId, eventAction, eventSchemaVersion);
            }
        }
    }
}
