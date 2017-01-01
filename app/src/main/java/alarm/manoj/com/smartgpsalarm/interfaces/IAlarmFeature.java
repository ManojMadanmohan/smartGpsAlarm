package alarm.manoj.com.smartgpsalarm.interfaces;

import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;

import java.util.List;

public interface IAlarmFeature
{
    /*
    Returns whether alarm was set successfully.
    Alarm will not be set, if any other alarm with same time OR same geofence is already set
     */
    public boolean setAlarm(GPSAlarm alarm);

    public void unsetAlarm(String alarmId);

    public List<GPSAlarm> getAlarmHistory();
    /*
    Returns whether alarm was removed successfully.
    Alarm will not be removed, if the alarm is currently set
     */
    public boolean removeAlarmFromHistory(String alarmId);
}
