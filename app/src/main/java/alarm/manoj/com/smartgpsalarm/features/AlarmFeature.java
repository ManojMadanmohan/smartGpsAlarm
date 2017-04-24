package alarm.manoj.com.smartgpsalarm.features;

import alarm.manoj.com.smartgpsalarm.events.GPSAlarmChangeEvent;
import alarm.manoj.com.smartgpsalarm.interfaces.IAlarmFeature;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.services.AlarmService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
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
        PendingIntent pendingIntent = getPendingIntent(alarm);
        LocationFeature.getInstance(_context).addGeoFence(alarm.getGeofenceRequest(), pendingIntent);
        AlarmManager manager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
        {
            manager.set(AlarmManager.RTC, alarm.getAlarmTimeAbsMillis(), pendingIntent);
        } else
        {
            manager.setExact(AlarmManager.RTC, alarm.getAlarmTimeAbsMillis(), pendingIntent);
        }
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
                PendingIntent intent = getPendingIntent(alarm);
                AlarmManager manager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
                manager.cancel(intent);
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
                alarmList.add(GPSAlarm.fromJson(_fileSystem.read(alarmId, null)));
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
        unsetAlarm(alarmId);
        _fileSystem.clear(alarmId);
        EventBus.getDefault().postSticky(new GPSAlarmChangeEvent());
        return false;
    }

    public GPSAlarm getAlarm(String alarmId)
    {
        if(!_fileSystem.hasKey(alarmId))
        {
            return null;
        }
        try
        {
            return GPSAlarm.fromJson(_fileSystem.read(alarmId, null));
        } catch (JSONException jsonExp)
        {
            //TODO;
            jsonExp.printStackTrace();
            return null;
        }
    }

    private void addAlarmToHistory(GPSAlarm alarm)
    {
        try
        {
            _fileSystem.write(alarm.getAlarmId(), GPSAlarm.toJson(alarm));
            EventBus.getDefault().postSticky(new GPSAlarmChangeEvent());
        } catch (JSONException jsonExp)
        {
            //TODO;
        }
    }

    private PendingIntent getPendingIntent(GPSAlarm alarm)
    {
        Intent intent = AlarmService.getLaunchIntent(_context, alarm.getAlarmId(), false);
        PendingIntent pendingIntent = PendingIntent.getService(_context, alarm.hashCode(), intent, 0);
        return pendingIntent;
    }
}
