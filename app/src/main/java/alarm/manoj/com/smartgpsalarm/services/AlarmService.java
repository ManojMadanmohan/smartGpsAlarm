package alarm.manoj.com.smartgpsalarm.services;

import alarm.manoj.com.smartgpsalarm.features.AlarmFeature;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.GeofencingEvent;

import java.io.IOException;

public class AlarmService extends IntentService
{
    public static final String KEY_ALARM = "alarm_id";

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
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        String alarmId = intent.getStringExtra(KEY_ALARM);
        triggerAlarm();
        AlarmFeature.getInstance(this).unsetAlarm(alarmId);
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
}
