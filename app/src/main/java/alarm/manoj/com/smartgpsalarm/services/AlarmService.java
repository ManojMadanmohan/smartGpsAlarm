package alarm.manoj.com.smartgpsalarm.services;

import alarm.manoj.com.smartgpsalarm.R;
import alarm.manoj.com.smartgpsalarm.features.AlarmFeature;
import alarm.manoj.com.smartgpsalarm.features.AlarmRinger;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.MainThread;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class AlarmService extends IntentService
{
    public static final String KEY_ALARM = "alarm_id";
    public static final String KEY_DISMISS = "dismiss";

    private static final int LOC_FREQ_MILLIS = 5000;

    private Handler _handler;

    public static Intent getLaunchIntent(Context context, String alarmId, boolean dismiss)
    {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra(KEY_ALARM, alarmId);
        intent.putExtra(KEY_DISMISS, dismiss);
        return intent;
    }

    public AlarmService()
    {
        this("Default");
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public AlarmService(String name)
    {
        super(name);
        _handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //Runs in background thread
        String alarmId = intent.getStringExtra(KEY_ALARM);
        GPSAlarm alarm = AlarmFeature.getInstance(this).getAlarm(alarmId);
        boolean dismiss = intent.getBooleanExtra(KEY_DISMISS, false);
        if(!dismiss)
        {
            checkAlarm(alarm);
        } else
        {
            AlarmRinger.getInstance(this).stopAlarm();
            AlarmFeature.getInstance(this).unsetAlarm(alarmId);
        }
    }

    private void checkAlarm(final GPSAlarm alarm)
    {
        long alarmTime = alarm.getAlarmTimeAbsMillis();
        final DefaultGeoFenceRequest request = alarm.getGeofenceRequest();
        if(alarmTime > System.currentTimeMillis())
        {
            //Alarm triggered by location
            triggerAlarmOnUIThread(alarm.getAlarmId());
        } else
        {
            //still time to go, check location
            Location currentLoc = LocationFeature.getInstance(this).getLastLocation();
            if(currentLoc == null || inGeofence(currentLoc, request))
            {
                //Loc not avl or loc in geofence already, trigger alarm
                triggerAlarmOnUIThread(alarm.getAlarmId());
            } else
            {
                _handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        startForeground(alarm);
                        LocationFeature.getInstance(AlarmService.this).addLocationListener(LOC_FREQ_MILLIS, LocationRequest.PRIORITY_HIGH_ACCURACY, new LocationListener()
                        {
                            @Override
                            public void onLocationChanged(Location location)
                            {
                                if(inGeofence(location, request))
                                {
                                    LocationFeature.getInstance(AlarmService.this).removeLocationListener(this);
                                    triggerAlarm(alarm.getAlarmId());
                                    stopForeground(true);
                                }
                            }
                        });
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

    private void triggerAlarmOnUIThread(final String alarmId)
    {
        _handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                triggerAlarm(alarmId);
            }
        });

    }

    @MainThread
    private void triggerAlarm(String alarmId)
    {
        GPSAlarm alarm = AlarmFeature.getInstance(AlarmService.this).getAlarm(alarmId);
        AlarmRinger.getInstance(AlarmService.this).ringAlarm(alarm);
        AlarmFeature.getInstance(AlarmService.this).unsetAlarm(alarmId);
    }

    private void startForeground(GPSAlarm alarm)
    {
        startForeground(1337, buildStickyNotification(this, alarm));
    }

    public static Notification buildStickyNotification(Context context, GPSAlarm alarm)
    {
        return new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_gps_fixed_black_24dp)
                .setContentTitle("gps alarm")
                .setContentText(alarm.getTitle())
                .setContentIntent(getContentIntent(context))
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .build();
    }

    private static PendingIntent getContentIntent(Context context)
    {
        return PendingIntent.getActivity(context, 1322,
                new Intent(context, GPSAlarmActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
