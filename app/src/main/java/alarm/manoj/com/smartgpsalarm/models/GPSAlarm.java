package alarm.manoj.com.smartgpsalarm.models;

import org.json.JSONException;
import org.json.JSONObject;

public class GPSAlarm
{
    private DefaultGeoFenceRequest _geofenceRequest;
    private long _alarmTimeAbsMillis;
    private boolean _active;
    private String _title;

    //This is the time the alarm was set
    private long _timestamp;

    private static final String KEY_GEOFENCE_REQUEST = "geofence_request";
    private static final String KEY_ALARM_TIME = "alarm_time";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_TITLE = "title";

    public GPSAlarm(long _alarmTimeAbsMillis, DefaultGeoFenceRequest _geofenceRequest, long timestamp, String title)
    {
        this._alarmTimeAbsMillis = _alarmTimeAbsMillis;
        this._geofenceRequest = _geofenceRequest;
        this._timestamp = timestamp;
        this._active = false;
        this._title = title;
    }

    public long getAlarmTimeAbsMillis()
    {
        return _alarmTimeAbsMillis;
    }

    public DefaultGeoFenceRequest getGeofenceRequest()
    {
        return _geofenceRequest;
    }

    public long getTimeStamp()
    {
        return _timestamp;
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean active)
    {
        _active = active;
    }

    public String getAlarmId()
    {
        return String.valueOf(hashCode());
    }

    public String getTitle()
    {
        return _title;
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
        jsonObject.put(KEY_TIMESTAMP, alarm.getTimeStamp());
        jsonObject.put(KEY_ACTIVE, alarm.isActive());
        jsonObject.put(KEY_TITLE, alarm.getTitle());
        return jsonObject.toString();
    }

    public static GPSAlarm fromJson(String json) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(json);
        DefaultGeoFenceRequest request = DefaultGeoFenceRequest.fromJson(jsonObject.getString(KEY_GEOFENCE_REQUEST));
        long alarmTime = jsonObject.getLong(KEY_ALARM_TIME);
        long timestamp = jsonObject.getLong(KEY_TIMESTAMP);
        boolean active = jsonObject.getBoolean(KEY_ACTIVE);
        String title = jsonObject.getString(KEY_TITLE);
        GPSAlarm alarm = new GPSAlarm(alarmTime, request, timestamp, title);
        alarm.setActive(active);
        return alarm;
    }
}
