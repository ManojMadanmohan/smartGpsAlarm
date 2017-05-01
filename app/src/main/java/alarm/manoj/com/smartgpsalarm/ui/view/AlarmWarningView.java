package alarm.manoj.com.smartgpsalarm.ui.view;

import alarm.manoj.com.smartgpsalarm.R;
import alarm.manoj.com.smartgpsalarm.features.AlarmRinger;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AlarmWarningView extends RelativeLayout
{

    public AlarmWarningView(final Context context)
    {
        super(context);
        View alarmWarningView = LayoutInflater.from(context).inflate(R.layout.alarm_warning_overlay, null);
        alarmWarningView.findViewById(R.id.alarm_warning_dismiss).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AlarmRinger.getInstance(context).stopAlarm();
            }
        });
        addView(alarmWarningView);
    }

    public void init(GPSAlarm alarm)
    {
        ((TextView)findViewById(R.id.alarm_warning_title)).setText(alarm.getTitle());
    }
}
