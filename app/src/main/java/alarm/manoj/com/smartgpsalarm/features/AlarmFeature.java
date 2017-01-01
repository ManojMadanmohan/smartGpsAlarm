package alarm.manoj.com.smartgpsalarm.features;

import alarm.manoj.com.smartgpsalarm.interfaces.IAlarmFeature;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import android.content.Context;
import org.json.JSONException;

import java.util.List;

public class AlarmFeature implements IAlarmFeature
{
    private static final String KEY_FILE_SYSTEM = "alarm_store";
    private FileSystem _fileSystem;

    public AlarmFeature(Context context)
    {
        _fileSystem = new FileSystem(context, KEY_FILE_SYSTEM);
    }

    @Override
    public boolean setAlarm(GPSAlarm alarm)
    {
        //TODO
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
                alarm.setActive(false);
                addAlarmToHistory(alarm);
            }
        }
    }

    @Override
    public List<GPSAlarm> getAlarmHistory()
    {
        return null;
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
}
