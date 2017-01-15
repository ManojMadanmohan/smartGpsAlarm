package alarm.manoj.com.smartgpsalarm.services;

import alarm.manoj.com.smartgpsalarm.features.AlarmFeature;
import alarm.manoj.com.smartgpsalarm.features.LocationFeature;
import alarm.manoj.com.smartgpsalarm.models.DefaultGeoFenceRequest;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.ui.activities.GPSAlarmActivity;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

public class AlarmService extends IntentService
{
    public static final String KEY_ALARM = "alarm_id";

    private static final int LOC_FREQ_MILLIS = 5000;

    public static Intent getLaunchIntent(Context context, String alarmId)
    {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra(KEY_ALARM, alarmId);
        return intent;
    }

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
        String alarmId = intent.getStringExtra(KEY_ALARM);
        GPSAlarm alarm = AlarmFeature.getInstance(this).getAlarm(alarmId);
        checkAlarm(alarm);
        AlarmFeature.getInstance(this).unsetAlarm(alarmId);
    }

    private void checkAlarm(GPSAlarm alarm)
    {
        long alarmTime = alarm.getAlarmTimeAbsMillis();
        final DefaultGeoFenceRequest request = alarm.getGeofenceRequest();
        if(alarmTime <= System.currentTimeMillis())
        {
            //Alarm triggered by time
            triggerAlarm();
        } else
        {
            //still time to go, check location
            Location currentLoc = LocationFeature.getInstance(this).getLastLocation();
            if(currentLoc == null || inGeofence(currentLoc, request))
            {
                //Loc not avl or loc in geofence already, trigger alarm
                triggerAlarm();
            } else
            {
                startForeground(alarm.getTitle());
                LocationFeature.getInstance(this).addLocationListener(LOC_FREQ_MILLIS, LocationRequest.PRIORITY_HIGH_ACCURACY, new LocationListener()
                {
                    @Override
                    public void onLocationChanged(Location location)
                    {
                        if(inGeofence(location, request))
                        {
                            LocationFeature.getInstance(AlarmService.this).removeLocationListener(this);
                            triggerAlarm();
                            stopForeground(true);
                        }
                    }
                });
            }
        }
    }

    private boolean inGeofence(Location location, DefaultGeoFenceRequest geoFenceRequest)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        float []results = new float[1];
        Location.distanceBetween(latLng.latitude, latLng.longitude, geoFenceRequest.getLatLng().latitude, geoFenceRequest.getLatLng().longitude, results);
        float dist = results[0];
        if(dist <= geoFenceRequest.getRadiusMeters())
        {
            return true;
        } else
        {
            return false;
        }
    }

    private void triggerAlarm()
    {
        Toast.makeText(this, "GEOFENCE FOUND!!", Toast.LENGTH_LONG).show();
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if(alert == null)
        {
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // I can't see this ever being null (as always have a default notification)
            // but just incase
            if(alert == null)
            {
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        try
        {
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(this, alert);
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0)
            {
                player.setAudioStreamType(AudioManager.STREAM_ALARM);
                player.setLooping(false);
                player.prepare();
                player.start();
            }
        }catch (IOException ioex)
        {
            Toast.makeText(this, "IO EXP",Toast.LENGTH_LONG).show();
        }
    }

    private void startForeground(String alarmTitle)
    {
        Intent intent = new Intent(this, GPSAlarmActivity.class);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Smart GPS Alarm")
                .setContentText("Actively checkin location for "+alarmTitle)
                .setContentIntent(PendingIntent.getActivity(this, 1234, intent, 0))
                .build();
        startForeground(1234, notification);
    }
}
