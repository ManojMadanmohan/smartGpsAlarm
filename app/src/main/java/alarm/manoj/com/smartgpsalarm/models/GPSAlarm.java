package alarm.manoj.com.smartgpsalarm.models;

import org.json.JSONException;
import org.json.JSONObject;

public class GPSAlarm
{
    private DefaultGeoFenceRequest _geofenceRequest;
    private long _alarmTimeAbsMillis;

    private static final String KEY_GEOFENCE_REQUEST = "geofence_request";
    private static final String KEY_ALARM_TIME = "alarm_time";

    public GPSAlarm(long _alarmTimeAbsMillis, DefaultGeoFenceRequest _geofenceRequest)
    {
        this._alarmTimeAbsMillis = _alarmTimeAbsMillis;
        this._geofenceRequest = _geofenceRequest;
    }

    public long getAlarmTimeAbsMillis()
    {
        return _alarmTimeAbsMillis;
    }

    public DefaultGeoFenceRequest getGeofenceRequest()
    {
        return _geofenceRequest;
    }

    public static String toJson(GPSAlarm alarm) throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_ALARM_TIME, alarm.getAlarmTimeAbsMillis());
        jsonObject.put(KEY_GEOFENCE_REQUEST, DefaultGeoFenceRequest.toJson(alarm.getGeofenceRequest()));
        return jsonObject.toString();
    }

    public static GPSAlarm fromJson(String json) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(json);
        DefaultGeoFenceRequest request = DefaultGeoFenceRequest.fromJson(jsonObject.getString(KEY_GEOFENCE_REQUEST));
        long alarmTime = jsonObject.getLong(KEY_ALARM_TIME);
        return new GPSAlarm(alarmTime, request);
    }
}
