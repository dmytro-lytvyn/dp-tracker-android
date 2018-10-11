package me.data_for.eventservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EventSender {
    private static final String PREFERENCES_NAME = "EventService";

    private void savePreference(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_NAME, context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String loadPreference(Context context, String key, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, context.MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
    }

    public void sendEvent(Context context, String eventContext, String eventObject, String eventObjectId, String eventAction, String eventSchemaVersion) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://input.data-for.me:1337/events";

        JSONObject jsonBody = new JSONObject();
        JSONObject jsonContent = new JSONObject();
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US); // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            String nowAsISO = df.format(new Date());

            int mGMTOffset = new GregorianCalendar().getTimeZone().getRawOffset();
            long timezoneOffset = TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);

            String eventId = UUID.randomUUID().toString();

            String sessionId = loadPreference(context, "sessionId", "");
            if (sessionId.equals("")) {
                sessionId = eventId;
                savePreference(context, "sessionId", sessionId);
            }

            String deviceId = loadPreference(context, "deviceId", "");
            if (deviceId.equals("")) {
                deviceId = eventId;
                savePreference(context, "deviceId", deviceId);
            }

            String userAgent = android.os.Build.FINGERPRINT;
            String deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;

            String phoneNumber = null;
            if (context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tm = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
                phoneNumber = tm.getLine1Number();
            }

            if (eventSchemaVersion == null) eventSchemaVersion = "v.1";

                jsonBody.put("context", eventContext);
            jsonBody.put("object", eventObject);
            if (eventObjectId != null) jsonBody.put("object_id", eventObjectId);
            jsonBody.put("action", eventAction);
            if (phoneNumber != null) jsonBody.put("actor", "PHONE");
            if (phoneNumber != null) jsonBody.put("actor_id", phoneNumber);
            jsonBody.put("origin", "DEVICE");
            jsonBody.put("origin_id", deviceId);
            jsonBody.put("session_id", sessionId);
            jsonBody.put("event_id", eventId);
            jsonBody.put("event_timestamp", nowAsISO);
            jsonBody.put("timezone_offset", String.valueOf(timezoneOffset));
            jsonBody.put("schema_version", eventSchemaVersion);
            jsonBody.put("content_type", "application/json");

            jsonContent.put("app_user_agent", userAgent);
            jsonContent.put("device_name", deviceName);

            jsonBody.put("content", jsonContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        byte[] mRequestBody = null;
        try {
            mRequestBody = jsonBody.toString().getBytes("utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final byte[] requestBody = mRequestBody;

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/vnd.kafka.json.v1+json");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return requestBody;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(postRequest);
    }
}