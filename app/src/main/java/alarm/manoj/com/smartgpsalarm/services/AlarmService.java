package alarm.manoj.com.smartgpsalarm.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.GeofencingEvent;

public class AlarmService extends IntentService
{
    public AlarmService()
    {
        this("Default");
    }

    public AlarmService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //Runs in background thread
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        String val = intent.getStringExtra("dummy");
        Log.d("test",val);
        Toast.makeText(this, val, Toast.LENGTH_SHORT).show();
        if(geofencingEvent.hasError())
        {
            Toast.makeText(this, "geofence error", Toast.LENGTH_SHORT).show();
        } else
        {
            Toast.makeText(this, "got geofence alarm! Yay :)", Toast.LENGTH_SHORT).show();
            //EventBus.getDefault().post(geofencingEvent);
        }
    }
}
