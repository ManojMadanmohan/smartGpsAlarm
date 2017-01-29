package alarm.manoj.com.smartgpsalarm.ui.view;

import alarm.manoj.com.smartgpsalarm.R;
import alarm.manoj.com.smartgpsalarm.features.AlarmFeature;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlarmInfoView extends CardView
{
    private GPSAlarm _alarm;


    public AlarmInfoView(Context context)
    {
        super(context);
        initView(context);
    }

    public AlarmInfoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView(context);
    }

    public AlarmInfoView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context)
    {
        LayoutInflater.from(context).inflate(R.layout.alarm_info_view, this, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            setElevation(context.getResources().getDimension(R.dimen.card_elevation));
        }
    }

    public void init(final GPSAlarm alarm)
    {
        _alarm = alarm;
        ((TextView)findViewById(R.id.alarm_title)).setText(alarm.getTitle());
        final ImageView stateView = (ImageView) findViewById(R.id.alarm_state);
        if(alarm.isActive())
        {
            stateView.setImageResource(R.drawable.ic_stop_black_24dp);
        } else
        {
            stateView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
        stateView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(_alarm.isActive())
                {
                    _alarm.setActive(false);
                    AlarmFeature.getInstance(getContext()).unsetAlarm(_alarm.getAlarmId());
                    stateView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                } else
                {
                    _alarm.setActive(true);
                    AlarmFeature.getInstance(getContext()).setAlarm(_alarm);
                    stateView.setImageResource(R.drawable.ic_stop_black_24dp);
                }
            }
        });
        findViewById(R.id.alarm_remove).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AlarmFeature.getInstance(getContext()).removeAlarmFromHistory(_alarm.getAlarmId());
            }
        });
    }
}
