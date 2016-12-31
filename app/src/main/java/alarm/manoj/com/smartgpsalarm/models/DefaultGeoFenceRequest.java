package alarm.manoj.com.smartgpsalarm.models;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultGeoFenceRequest
{
    private LatLng _latLng;
    private int _radiusMeters;

    private static final int LOCATION_MULTIPLIER = 1000;

    private static final String KEY_RADIUS = "radius";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LONG = "lon";

    public DefaultGeoFenceRequest(LatLng latLng, int radius)
    {
        _latLng = latLng;
        _radiusMeters = radius;
    }

    public int getRadiusMeters()
    {
        return _radiusMeters;
    }

    public LatLng getLatLng()
    {
        return _latLng;
    }

    public String getRequestId()
    {
        return String.valueOf(hashCode());
    }

    @Override
    public int hashCode()
    {
        return (int)Math.round(_latLng.latitude*LOCATION_MULTIPLIER + _latLng.longitude*LOCATION_MULTIPLIER + _radiusMeters);
    }

    public static String toJson(DefaultGeoFenceRequest defaultGeoFenceRequest) throws JSONException
    {
        JSONObject object = new JSONObject();
        object.put(KEY_RADIUS, defaultGeoFenceRequest.getRadiusMeters());
        object.put(KEY_LAT, defaultGeoFenceRequest.getLatLng().latitude);
        object.put(KEY_LONG, defaultGeoFenceRequest.getLatLng().longitude);
        return object.toString();
    }

    public static DefaultGeoFenceRequest fromJson(String json) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(json);
        LatLng latLng = new LatLng(jsonObject.getDouble(KEY_LAT), jsonObject.getDouble(KEY_LONG));
        int radius = jsonObject.getInt(KEY_RADIUS);
        return new DefaultGeoFenceRequest(latLng, radius);
    }
}
