package alarm.manoj.com.smartgpsalarm.features;

import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.ui.view.AlarmWarningView;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

import static android.content.Context.CARRIER_CONFIG_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class AlarmRinger
{
    private AlarmWarningView _alarmWarningView;
    private Context _context;

    private static AlarmRinger _instance;

    private AlarmRinger(Context context)
    {
        _context = context;
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
    }

    public void stopAlarm()
    {
        //TODO stop music
        clearWindow();
        stopVibrating();
    }

    public void showAlarmView(GPSAlarm alarm)
    {
        _alarmWarningView = new AlarmWarningView(_context, alarm);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.OPAQUE);

        params.gravity = Gravity.CENTER;
        ((WindowManager) _context.getSystemService(WINDOW_SERVICE)).addView(_alarmWarningView, params);
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

    private void vibrate()
    {
        long[] pattern = {0, 1000, 500};
        ((Vibrator)_context.getSystemService(VIBRATOR_SERVICE)).vibrate(pattern, 0);
    }

    private void clearWindow()
    {
        ((WindowManager) _context.getSystemService(WINDOW_SERVICE)).removeView(_alarmWarningView);
    }

    private void stopVibrating()
    {
        ((Vibrator)_context.getSystemService(VIBRATOR_SERVICE)).cancel();
    }
}
