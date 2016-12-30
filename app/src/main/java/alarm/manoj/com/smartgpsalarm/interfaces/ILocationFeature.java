package alarm.manoj.com.smartgpsalarm.interfaces;


import alarm.manoj.com.smartgpsalarm.models.DefaultGeoFenceRequest;
import android.location.Location;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

public interface ILocationFeature
{
    public Location getLastLocation();

    public void addLocationListener(int freqSeconds, int priority, LocationListener listener);

    public void removeLocationListener(LocationListener listener);

    public void addGeoFence(DefaultGeoFenceRequest request);

    public void removeGeoFence(String requestId);
}
