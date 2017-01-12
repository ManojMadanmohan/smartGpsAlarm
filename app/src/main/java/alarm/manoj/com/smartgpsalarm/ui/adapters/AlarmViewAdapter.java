package alarm.manoj.com.smartgpsalarm.ui.adapters;

import alarm.manoj.com.smartgpsalarm.features.AlarmFeature;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.ui.view.AlarmInfoView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class AlarmViewAdapter extends BaseAdapter
{
    private Context _context;

    public AlarmViewAdapter(Context context)
    {
        _context = context;
    }

    @Override
    public int getCount()
    {
        return AlarmFeature.getInstance(_context).getAlarmHistory().size();
    }

    @Override
    public Object getItem(int i)
    {
        return AlarmFeature.getInstance(_context).getAlarmHistory().get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return ((GPSAlarm)getItem(i)).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        if(view == null)
        {
            view = new AlarmInfoView(_context);
        }
        ((AlarmInfoView) view).init((GPSAlarm) getItem(i));
        return view;
    }
}
