package alarm.manoj.com.smartgpsalarm.ui.adapters;

import alarm.manoj.com.smartgpsalarm.features.AlarmFeature;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.ui.view.AlarmInfoView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class AlarmViewAdapter extends BaseAdapter
{
    private Context _context;
    private List<GPSAlarm> _alarmList;

    public AlarmViewAdapter(Context context)
    {
        _context = context;
        _alarmList = AlarmFeature.getInstance(context).getAlarmHistory();
    }

    @Override
    public int getCount()
    {
        return _alarmList.size();
    }

    @Override
    public Object getItem(int i)
    {
        return _alarmList.get(i);
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

    @Override
    public void notifyDataSetChanged()
    {
        _alarmList = AlarmFeature.getInstance(_context).getAlarmHistory();
        notifyDataSetInvalidated();
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated()
    {
        _alarmList = AlarmFeature.getInstance(_context).getAlarmHistory();
        super.notifyDataSetInvalidated();
    }
}
