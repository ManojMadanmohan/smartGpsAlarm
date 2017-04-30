package alarm.manoj.com.smartgpsalarm.features;

import alarm.manoj.com.smartgpsalarm.R;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.services.AlarmService;
import alarm.manoj.com.smartgpsalarm.ui.activities.GPSAlarmActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;
import org.json.JSONException;

import java.io.IOException;

import static android.content.Context.VIBRATOR_SERVICE;

public class AlarmRinger
{
    private View _alarmWarningView;
    private Context _context;
    private MediaPlayer _player;

    private static AlarmRinger _instance;

    private AlarmRinger(Context context)
    {
        _context = context;
        _player = new MediaPlayer();
    }

    public void setContext(Context context)
    {
        _context = context;
    }

    public static AlarmRinger getInstance(Context context)
    {
        if(_instance == null)
        {
            _instance = new AlarmRinger(context);
        } else
        {
            _instance.setContext(context);
        }
        return _instance;
    }

    public void ringAlarm(GPSAlarm alarm)
    {
        ringAlarm();
        showAlarmView(alarm);
        vibrate();
        showAlarmNotification(alarm);
    }

    public void stopAlarm()
    {
        stopRinging();
        clearWindow();
        stopVibrating();
        dismissAlarmNotif();
    }

    public void showAlarmView(GPSAlarm alarm)
    {
        Intent intent = new Intent(_context, GPSAlarmActivity.class);
        try
        {
            intent.putExtra(GPSAlarmActivity.SHOW_ALARM_RINGING_STATE, GPSAlarm.toJson(alarm));
        } catch (JSONException j)
        {

        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(intent);
    }

    public void ringAlarm()
    {
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
            Log.d("AlarmRinger", "player code called");
            _player.reset();
            _player.setDataSource(_context, alert);
            final AudioManager audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0)
            {
                _player.setAudioStreamType(AudioManager.STREAM_ALARM);
                _player.setLooping(false);
                _player.prepare();
                _player.start();
            }
        }catch (IOException ioex)
        {
            Toast.makeText(_context, "IO EXP",Toast.LENGTH_LONG).show();
        }
    }

    private void vibrate()
    {
        long[] pattern = {0, 1000, 500};
        ((Vibrator)_context.getSystemService(VIBRATOR_SERVICE)).vibrate(pattern, 0);
    }

    private void stopRinging()
    {
        _player.stop();
    }

    private void clearWindow()
    {
        Intent intent = new Intent(_context, GPSAlarmActivity.class);
        intent.putExtra(GPSAlarmActivity.DISMISS_ALARM_RINGING_STATE, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(intent);
    }

    private void stopVibrating()
    {
        ((Vibrator)_context.getSystemService(VIBRATOR_SERVICE)).cancel();
    }

    private void showAlarmNotification(GPSAlarm alarm)
    {
        PendingIntent dismissIntent = PendingIntent.getService(_context, 121,
                AlarmService.getLaunchIntent(_context, alarm.getAlarmId(), true), 0);
        PendingIntent activityIntent = PendingIntent.getActivity(_context, 1322,
                new Intent(_context, GPSAlarmActivity.class), 0);
        Notification notification = new Notification.Builder(_context)
                .setContentTitle("GPS alarm")
                .setContentText(alarm.getTitle())
                .addAction(R.drawable.common_google_signin_btn_icon_dark, "DISMISS", dismissIntent)
                .setContentIntent(activityIntent)
                .build();
        ((NotificationManager)_context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(111222, notification);
    }

    private void dismissAlarmNotif()
    {
        ((NotificationManager)_context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(111222);
    }
}
