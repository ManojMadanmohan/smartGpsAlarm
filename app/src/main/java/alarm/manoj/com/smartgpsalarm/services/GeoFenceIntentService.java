package alarm.manoj.com.smartgpsalarm.services;

import alarm.manoj.com.smartgpsalarm.features.LocationFeature;
import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeoFenceIntentService extends IntentService
{
    public GeoFenceIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //Runs in background thread
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError())
        {
            Toast.makeText(this, "geofence error", Toast.LENGTH_SHORT).show();
        } else
        {
            Toast.makeText(this, "got geofence alarm! Yay :)", Toast.LENGTH_SHORT).show();
            for(Geofence geofence: geofencingEvent.getTriggeringGeofences())
            {
                LocationFeature.getInstance(this).removeGeoFence(geofence.getRequestId());
            }
        }
    }
}
