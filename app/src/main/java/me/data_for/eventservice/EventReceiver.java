package me.data_for.eventservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import org.json.JSONException;
import org.json.JSONObject;

public class EventReceiver extends BroadcastReceiver {
    final EventSender sender = new EventSender();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String intentAction = intent.getAction();
        String eventActionType = null;
        JSONObject eventActionAttributes = new JSONObject();

        if (intentAction != null) {
            if (intentAction.equals(Intent.ACTION_SCREEN_ON)) {
                eventActionType = "screen_on";
            } else if (intentAction.equals(Intent.ACTION_SCREEN_OFF)) {
                eventActionType = "screen_off";
            } else if (intentAction.equals(Intent.ACTION_USER_PRESENT)) {
                eventActionType = "user_present";
            } else if (intentAction.equals(Intent.ACTION_BOOT_COMPLETED)) {
                eventActionType = "boot_completed";
            } else if (intentAction.equals(Intent.ACTION_REBOOT)) {
                eventActionType = "reboot";
            } else if (intentAction.equals(Intent.ACTION_SHUTDOWN)) {
                eventActionType = "shutdown";
            } else if (intentAction.equals(Intent.ACTION_BATTERY_CHANGED)) {
                eventActionType = "battery_changed";
                try {
                    eventActionAttributes.put("battery_level", String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (intentAction.equals(Intent.ACTION_POWER_CONNECTED)) {
                eventActionType = "power_connected";
            } else if (intentAction.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                eventActionType = "power_disconnected";
            } else if (intentAction.equals(Intent.ACTION_HEADSET_PLUG)) {
                eventActionType = "headset_plug";
            }

            if (eventActionType != null) {
                sender.sendEvent(context, null, null, eventActionType, eventActionAttributes);
            }
        }
    }
}
