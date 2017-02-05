package alarm.manoj.com.smartgpsalarm.ui.view;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlarmWarningView extends LinearLayout
{
    public AlarmWarningView(Context context)
    {
        super(context);
        init(context);
    }

    public AlarmWarningView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public AlarmWarningView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context)
    {
        TextView textView = new TextView(context);
        textView.setText("This is overlay");
        addView(textView);
    }


}
