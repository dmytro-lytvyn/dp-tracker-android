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
import java.util.Iterator;
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

    public void sendEvent(Context context, String eventSubjectId, String eventSubjectType, String eventActionType, JSONObject eventActionAttributes) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://input.data-for.me:1337/events";

        JSONObject jsonEvent = new JSONObject();
        JSONObject jsonOrigin = new JSONObject();
        JSONObject jsonActor = new JSONObject();
        JSONObject jsonSubject = new JSONObject();
        JSONObject jsonSubjectAttributes = new JSONObject();
        JSONObject jsonAction = new JSONObject();
        JSONObject jsonActionAttributes = new JSONObject();
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
            String deviceManufacturer = android.os.Build.MANUFACTURER;
            String deviceBrand = android.os.Build.BRAND;
            String deviceModel = android.os.Build.MODEL;

            String phoneNumber = null;
            if (context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tm = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
                phoneNumber = tm.getLine1Number();
            }

            // Putting together JSON event
            jsonEvent.put("event_id", eventId);
            jsonEvent.put("event_timestamp", nowAsISO);

            // Origin
            jsonOrigin.put("id", deviceId);
            jsonOrigin.put("type", "android_device");
            jsonEvent.put("origin", jsonOrigin);

            // Actor
            if (phoneNumber != null) {
                jsonActor.put("id", phoneNumber);
                jsonActor.put("type", "phone_number");
            } else {
                jsonActor.put("id", deviceId);
                jsonActor.put("type", "android_device");
            }
            jsonEvent.put("actor", jsonActor);

            // Subject

            if (eventSubjectId != null) {
                jsonSubject.put("id", eventSubjectId);
            } else {
                jsonSubject.put("id", deviceId);
            }
            if (eventSubjectType != null) {
                jsonSubject.put("type", eventSubjectType);
            } else {
                jsonSubject.put("type", "device");
            }
            if (jsonSubjectAttributes.length() != 0) jsonSubject.put("attributes", jsonSubjectAttributes);
            jsonEvent.put("subject", jsonSubject);

            // Action
            jsonAction.put("type", eventActionType);
            jsonActionAttributes.put("session_id", sessionId);
            jsonActionAttributes.put("action_timestamp", nowAsISO);
            jsonActionAttributes.put("timezone_offset", String.valueOf(timezoneOffset));
            jsonActionAttributes.put("app_user_agent", userAgent);
            jsonActionAttributes.put("device_manufacturer", deviceManufacturer);
            jsonActionAttributes.put("device_brand", deviceBrand);
            jsonActionAttributes.put("device_model", deviceModel);
            // Adding more action attributes
            if (eventActionAttributes != null) {
                Iterator<?> i = eventActionAttributes.keys();
                String attributeKey;
                while (i.hasNext()) {
                    attributeKey = (String) i.next();
                    jsonActionAttributes.put(attributeKey, eventActionAttributes.get(attributeKey));
                }
            }
            if (jsonActionAttributes.length() != 0) jsonAction.put("attributes", jsonActionAttributes);
            jsonEvent.put("action", jsonAction);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        byte[] mRequestBody = null;
        try {
            mRequestBody = jsonEvent.toString().getBytes("utf-8");
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
