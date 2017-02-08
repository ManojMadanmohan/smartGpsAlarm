package alarm.manoj.com.smartgpsalarm.ui.view;

import alarm.manoj.com.smartgpsalarm.R;
import alarm.manoj.com.smartgpsalarm.events.GPSAlarmChangeEvent;
import alarm.manoj.com.smartgpsalarm.events.GPSAlarmDismissed;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;

public class AlarmWarningView extends LinearLayout
{
    public AlarmWarningView(Context context, GPSAlarm alarm)
    {
        super(context);
        init(context, alarm);
    }

    public AlarmWarningView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, null);
    }

    public AlarmWarningView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, null);
    }

    private void init(Context context, GPSAlarm alarm)
    {
        setOrientation(VERTICAL);
        setBackgroundColor(getResources().getColor(R.color.AliceBlue));
        TextView textView = new TextView(context);
        textView.setText("GPS Alarm");

        TextView titleView = new TextView(context);
        titleView.setText(alarm.getTitle());

        CardView titleCard = new CardView(context);
        titleCard.addView(titleView);
        int padding = getResources().getDimensionPixelSize(R.dimen.place_autocomplete_button_padding);
        titleCard.setPadding(padding, padding, padding, padding);

        TextView dissmiss = new TextView(context);
        dissmiss.setText("dismiss");

        dissmiss.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EventBus.getDefault().postSticky(new GPSAlarmDismissed());
            }
        });

        addView(textView);

        addView(titleCard);

        addView(dissmiss);
    }


}
