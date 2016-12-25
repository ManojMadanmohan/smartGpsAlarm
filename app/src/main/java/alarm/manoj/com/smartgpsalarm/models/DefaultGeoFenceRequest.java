package alarm.manoj.com.smartgpsalarm.models;

import com.google.android.gms.maps.model.LatLng;

public class DefaultGeoFenceRequest
{
    private LatLng _latLng;
    private int _radiusMeters;

    private static final int LOCATION_MULTIPLIER = 1000;

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

    public String getKey()
    {
        return String.valueOf(hashCode());
    }

    @Override
    public int hashCode()
    {
        return (int)Math.round(_latLng.latitude*LOCATION_MULTIPLIER + _latLng.longitude*LOCATION_MULTIPLIER + _radiusMeters);
    }
}
