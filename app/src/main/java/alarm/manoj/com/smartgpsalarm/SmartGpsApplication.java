package alarm.manoj.com.smartgpsalarm;

import alarm.manoj.com.smartgpsalarm.features.LocationFeature;
import android.app.Application;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class SmartGpsApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        LocationFeature.getInstance(this);
    }
}
