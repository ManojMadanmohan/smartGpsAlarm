package alarm.manoj.com.smartgpsalarm.features;

import alarm.manoj.com.smartgpsalarm.interfaces.IAlarmFeature;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlarmFeature implements IAlarmFeature
{
    private static AlarmFeature _instance;

    private static final String KEY_FILE_SYSTEM = "alarm_store";
    private FileSystem _fileSystem;
    private Context _context;

    private AlarmFeature(Context context)
    {
        _fileSystem = new FileSystem(context, KEY_FILE_SYSTEM);
        _context = context;
        EventBus.getDefault().register(this);
    }

    public static AlarmFeature getInstance(Context context)
    {
        if(_instance == null)
        {
            _instance = new AlarmFeature(context);
        }
        return _instance;
    }

    @Override
    public boolean setAlarm(GPSAlarm alarm)
    {
        //TODO
        LocationFeature.getInstance(_context).addGeoFence(alarm.getGeofenceRequest());
        alarm.setActive(true);
        addAlarmToHistory(alarm);
        return true;
    }

    @Override
    public void unsetAlarm(String alarmId)
    {
        for(GPSAlarm alarm: getAlarmHistory())
        {
            if(alarm.getAlarmId().equals(alarmId))
            {
                //TODO
                LocationFeature.getInstance(_context).removeGeoFence(alarm.getGeofenceRequest().getRequestId());
                alarm.setActive(false);
                addAlarmToHistory(alarm);
            }
        }
    }

    @Override
    public List<GPSAlarm> getAlarmHistory()
    {
        List<GPSAlarm> alarmList = new ArrayList<>();
        for(String alarmId: _fileSystem.keyList())
        {
            try
            {
                alarmList.add(GPSAlarm.fromJson(_fileSystem.read(alarmId)));
            } catch (JSONException jsonExp)
            {
                //corrupted entry, remove
                _fileSystem.clear(alarmId);
                //TODO: log this error
            }
        }
        return alarmList;
    }

    @Override
    public boolean removeAlarmFromHistory(String alarmId)
    {
        return false;
    }

    private GPSAlarm getAlarm(String alarmId)
    {
        if(!_fileSystem.hasKey(alarmId))
        {
            return null;
        }
        try
        {
            return GPSAlarm.fromJson(_fileSystem.read(alarmId));
        } catch (JSONException jsonExp)
        {
            //TODO;
            return null;
        }
    }

    private void addAlarmToHistory(GPSAlarm alarm)
    {
        try
        {
            _fileSystem.write(alarm.getAlarmId(), GPSAlarm.toJson(alarm));
        } catch (JSONException jsonExp)
        {
            //TODO;
        }
    }

    @Subscribe
    public void onEvent(GeofencingEvent event)
    {
        for(Geofence geofence: event.getTriggeringGeofences())
        {
            for (GPSAlarm alarm : getAlarmHistory())
            {
                if (alarm.getGeofenceRequest().getRequestId().equals(geofence.getRequestId()))
                {
                    triggerAlarm();
                    unsetAlarm(alarm.getAlarmId());
                }
            }
        }
    }

    private void triggerAlarm()
    {
        Toast.makeText(_context, "GEOFENCE FOUND!!", Toast.LENGTH_LONG).show();
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
            player.setDataSource(_context, alert);
            final AudioManager audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0)
            {
                player.setAudioStreamType(AudioManager.STREAM_ALARM);
                player.setLooping(false);
                player.prepare();
                player.start();
            }
        }catch (IOException ioex)
        {
            Toast.makeText(_context, "IO EXP",Toast.LENGTH_LONG).show();
        }
    }
}
