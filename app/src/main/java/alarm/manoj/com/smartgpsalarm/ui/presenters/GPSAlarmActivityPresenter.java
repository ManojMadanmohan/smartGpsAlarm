package alarm.manoj.com.smartgpsalarm.ui.presenters;

import alarm.manoj.com.smartgpsalarm.R;
import alarm.manoj.com.smartgpsalarm.events.GPSAlarmChangeEvent;
import alarm.manoj.com.smartgpsalarm.features.AlarmFeature;
import alarm.manoj.com.smartgpsalarm.features.FileSystem;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.ui.contracts.GPSAlarmHomeContract;
import alarm.manoj.com.titleseekbar.TitleSeekbar;
import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.view.View;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class GPSAlarmActivityPresenter implements GPSAlarmHomeContract.GPSAlarmHomePresenter
{

    private static final int MINIMUM_RADIUS = 100;
    private static final int MAXIMUM_RADIUS = 2000;
    private static final String RADIUS_KEY = "radius_key";
    private static final String RADIUS_STORE = "radius";
    public static final String SHOW_ALARM_RINGING_STATE = "warning_alarm";
    public static final String DISMISS_ALARM_RINGING_STATE = "warning_alarm_dismiss";

    private GPSAlarmHomeContract.GPSAlarmView _view;
    private int _radiusM = MINIMUM_RADIUS;
    private FileSystem _radiusStore;

    public GPSAlarmActivityPresenter(GPSAlarmHomeContract.GPSAlarmView view, Context context)
    {
        _view = view;
        _radiusStore = new FileSystem(context, RADIUS_STORE);
    }

    @Override
    public void onCreate()
    {
        _view.initAlarmWarningView();
    }

    @Override
    public void onStart()
    {
        _radiusM = Integer.valueOf(_radiusStore.read(RADIUS_KEY, String.valueOf(MINIMUM_RADIUS)));
        initSeekbarProgress();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume()
    {

    }

    @Override
    public void onPause()
    {

    }

    @Override
    public void onStop()
    {
        EventBus.getDefault().unregister(this);
        _radiusStore.write(RADIUS_KEY, String.valueOf(_radiusM));
    }

    @Override
    public void onDestroy()
    {

    }

    @Override
    public void onNewIntent(Intent intent)
    {
        if(intent.hasExtra(SHOW_ALARM_RINGING_STATE))
        {
            try
            {
                GPSAlarm alarm = GPSAlarm.fromJson(intent.getStringExtra(SHOW_ALARM_RINGING_STATE));
                _view.showAlarmWarningView(alarm);
            } catch (JSONException j)
            {

            }
        } else if(intent.hasExtra(DISMISS_ALARM_RINGING_STATE))
        {
            _view.hideAlarmWarningView();
        }
    }

    @Override
    public void resetActiveAlarmsOnMap(GoogleMap _googleMap, Context context)
    {
        if(_googleMap != null)
        {
            _googleMap.clear();
            for (GPSAlarm alarm : AlarmFeature.getInstance(context).getAlarmHistory())
            {
                if (alarm.isActive())
                {
                    _view.addActiveAlarmOnUI(alarm);
                }
            }

        }
    }

    @Override
    public int getScreenDistMetres(GoogleMap googleMap)
    {
        LatLng leftLoc = googleMap.getProjection().getVisibleRegion().farLeft;
        LatLng rightLoc = googleMap.getProjection().getVisibleRegion().farRight;
        LatLng nearLeftLoc = googleMap.getProjection().getVisibleRegion().nearLeft;

        float results[] = new float[2];
        Location.distanceBetween(leftLoc.latitude, leftLoc.longitude, rightLoc.latitude, rightLoc.longitude, results);
        return (int) results[0];
    }

    @Override
    public int getScreenWidthPx(GoogleMap googleMap)
    {
        LatLng leftLoc = googleMap.getProjection().getVisibleRegion().farLeft;
        LatLng rightLoc = googleMap.getProjection().getVisibleRegion().farRight;
        LatLng nearLeftLoc = googleMap.getProjection().getVisibleRegion().nearLeft;

        Point leftEndPoint = googleMap.getProjection().toScreenLocation(leftLoc);
        Point rightEndPoint = googleMap.getProjection().toScreenLocation(rightLoc);
        Point nearLeftEndPoint = googleMap.getProjection().toScreenLocation(nearLeftLoc);
        int screenWidthPx = Math.abs(leftEndPoint.x - rightEndPoint.x);
        return screenWidthPx;
    }

    @Override
    public int getScreenHeightPx(GoogleMap googleMap)
    {
        LatLng leftLoc = googleMap.getProjection().getVisibleRegion().farLeft;
        LatLng rightLoc = googleMap.getProjection().getVisibleRegion().farRight;
        LatLng nearLeftLoc = googleMap.getProjection().getVisibleRegion().nearLeft;

        Point leftEndPoint = googleMap.getProjection().toScreenLocation(leftLoc);
        Point rightEndPoint = googleMap.getProjection().toScreenLocation(rightLoc);
        Point nearLeftEndPoint = googleMap.getProjection().toScreenLocation(nearLeftLoc);
        int screenHeightPx = Math.abs(leftEndPoint.y - nearLeftEndPoint.y);
        return screenHeightPx;
    }

    @Override
    public LatLng getMapCenter(GoogleMap map)
    {
        return map.getCameraPosition().target;
    }

    @Override
    public TitleSeekbar.TitleSeekbarHandler getSeekHandler()
    {
        return new TitleSeekbar.TitleSeekbarHandler()
        {
            @Override
            public String getTitle(int progress, int maxProgress)
            {
                double ratio = progress*1.0/maxProgress;
                _radiusM = (int)((MAXIMUM_RADIUS-MINIMUM_RADIUS)*ratio)+MINIMUM_RADIUS;
                return _radiusM+" metres";
            }

            @Override
            public void onSeekbarChangeListener(int progress, int maxProgress)
            {
                double ratio = progress*1.0/maxProgress;
                _radiusM = (int)((MAXIMUM_RADIUS-MINIMUM_RADIUS)*ratio)+MINIMUM_RADIUS;
                _view.updateSetMarkerRadius(_radiusM);
            }
        };
    }

    @Override
    public int getRadiusM()
    {
        return _radiusM;
    }

    @Override
    public void onPlaceAutocompleteResult(Place place)
    {
        _view.zoomToLocation(place.getLatLng());
        _view.showAddAlarmDialog(place.getLatLng(), place.getName().toString(), getRadiusM());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(GPSAlarmChangeEvent event)
    {
        _view.onGpsAlarmsChanged();
    }

    @Override
    public void initSeekbarProgress()
    {
        _radiusM = Integer.valueOf(_radiusStore.read(RADIUS_KEY, String.valueOf(MINIMUM_RADIUS)));
        int maxProgress = _view.getMaxSeekbarProgress();
        int progress = (int)(((_radiusM - MINIMUM_RADIUS)*1.0/(MAXIMUM_RADIUS-MINIMUM_RADIUS))*maxProgress);
        _view.setSeekbarProgress(progress);
    }
}
