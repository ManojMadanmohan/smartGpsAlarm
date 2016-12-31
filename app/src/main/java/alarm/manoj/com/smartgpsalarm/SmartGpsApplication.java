package alarm.manoj.com.smartgpsalarm;

import alarm.manoj.com.smartgpsalarm.features.LocationFeature;
import android.app.Application;

public class SmartGpsApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        LocationFeature.getInstance(this);
    }
}
