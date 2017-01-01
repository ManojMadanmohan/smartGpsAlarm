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

    public String getAlarmId()
    {
        return String.valueOf(hashCode());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GPSAlarm gpsAlarm = (GPSAlarm) o;

        if (_alarmTimeAbsMillis != gpsAlarm._alarmTimeAbsMillis) return false;
        return _geofenceRequest.equals(gpsAlarm._geofenceRequest);

    }

    @Override
    public int hashCode()
    {
        int result = _geofenceRequest.hashCode();
        result = 31 * result + (int) (_alarmTimeAbsMillis ^ (_alarmTimeAbsMillis >>> 32));
        return result;
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
