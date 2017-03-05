package alarm.manoj.com.smartgpsalarm.ui.activities;

import alarm.manoj.com.smartgpsalarm.R;
import alarm.manoj.com.smartgpsalarm.events.GPSAlarmChangeEvent;
import alarm.manoj.com.smartgpsalarm.features.AlarmFeature;
import alarm.manoj.com.smartgpsalarm.features.FileSystem;
import alarm.manoj.com.smartgpsalarm.features.LocationFeature;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.ui.adapters.AlarmViewAdapter;
import alarm.manoj.com.smartgpsalarm.ui.dialogs.AddAlarmDialog;
import alarm.manoj.com.smartgpsalarm.ui.view.AlarmWarningView;
import alarm.manoj.com.titleseekbar.TitleSeekbar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GPSAlarmActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private static Activity _instance;
    private static AlarmWarningView _alarmWarningView;
    private GoogleMap _googleMap;
    private AlarmViewAdapter _adapter;
    private ListView _alarmList;
    private TitleSeekbar _seekbar;
    private FileSystem _radiusStore;
    private int _radiusM = MINIMUM_RADIUS;
    private static final String ADD_ALARM_TAG = "add_alarm_tag";
    private static final String RADIUS_KEY = "radius_key";
    private static final String RADIUS_STORE = "radius";
    private static final int MINIMUM_RADIUS = 100;
    private static final int MAXIMUM_RADIUS = 2000;

    private static final int PLACE_SEARCH_CODE = 1222;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsalarm);
        _radiusStore = new FileSystem(this, RADIUS_STORE);
        ((MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment)).getMapAsync(this);
        setActionBar();
        findViewById(R.id.add_alarm).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LatLng alarmLoc = getMapCenter();
                AddAlarmDialog dialog = AddAlarmDialog.newInstance(alarmLoc, "", _radiusM);
                dialog.show(getFragmentManager(), ADD_ALARM_TAG);
            }
        });
        _adapter = new AlarmViewAdapter(this);
        _seekbar = (TitleSeekbar) findViewById(R.id.seekbar);
        _alarmList = (ListView)findViewById(R.id.alarm_list);
        _alarmList.setAdapter(_adapter);
        _seekbar.setSeekHandler(new TitleSeekbar.TitleSeekbarHandler()
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
                updateSetMarkerRadius(_radiusM);
            }
        });
        _seekbar.setSeekTitle(_radiusM+" metres"); //default
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        _instance = this;
        _radiusM = Integer.valueOf(_radiusStore.read(RADIUS_KEY, String.valueOf(MINIMUM_RADIUS)));
        initSeekbarProgress();
        EventBus.getDefault().register(this);
        _adapter.notifyDataSetChanged();
        resetActiveAlarmsOnMap();
    }

    @Override
    protected void onStop()
    {
        EventBus.getDefault().unregister(this);
        _radiusStore.write(RADIUS_KEY, String.valueOf(_radiusM));
        _instance = null;
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.menu_search)
        {
            try
            {
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
                startActivityForResult(intent, PLACE_SEARCH_CODE);
            } catch (GooglePlayServicesRepairableException e)
            {
                // TODO: Handle the error.
            } catch (GooglePlayServicesNotAvailableException e)
            {
                // TODO: Handle the error.
            }
            return true;
        }
        if(item.getItemId() == R.id.menu_favorites)
        {
            if(_alarmList.getVisibility() == View.VISIBLE)
            {
                _alarmList.setVisibility(View.GONE);
            } else
            {
                _alarmList.setVisibility(View.VISIBLE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == PLACE_SEARCH_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                Place place = PlaceAutocomplete.getPlace(this, data);
                zoomToLocation(place.getLatLng());
                AddAlarmDialog dialog = AddAlarmDialog.newInstance(place.getLatLng(), place.getName().toString(), _radiusM);
                dialog.show(getFragmentManager(), ADD_ALARM_TAG);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(GPSAlarmChangeEvent event)
    {
        _adapter.notifyDataSetChanged();
        resetActiveAlarmsOnMap();
    }

    public static boolean showAlarmIfVisible(GPSAlarm alarm)
    {
        if(_instance != null)
        {
            _alarmWarningView = new AlarmWarningView(_instance.getApplicationContext(), alarm);
            final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            _instance.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    ((RelativeLayout)_instance.findViewById(R.id.content_root)).addView(_alarmWarningView, params);
                }
            });
            return true;
        } else
        {
            return false;
        }
    }

    public static boolean hideAlarmIfVisible()
    {
        if(_instance != null && _alarmWarningView != null)
        {
            _instance.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    ((RelativeLayout)_instance.findViewById(R.id.content_root)).removeView(_alarmWarningView);
                }
            });
            return true;
        } else
        {
            return false;
        }
    }

    private void setActionBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        _googleMap = googleMap;
        _googleMap.setMyLocationEnabled(true);
        _googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener()
        {
            @Override
            public void onCameraMove()
            {
                updateSetMarkerRadius(_radiusM);
            }
        });
        zoomToCurrentLocation();
        resetActiveAlarmsOnMap();
        initSeekbarProgress();
    }

    private void initSeekbarProgress()
    {
        _radiusM = Integer.valueOf(_radiusStore.read(RADIUS_KEY, String.valueOf(MINIMUM_RADIUS)));
        int maxProgress = _seekbar.getSeekbar().getMax();
        int progress = (int)(((_radiusM - MINIMUM_RADIUS)*1.0/(MAXIMUM_RADIUS-MINIMUM_RADIUS))*maxProgress);
        _seekbar.getSeekbar().setProgress(progress);
    }

    private LatLng getMapCenter()
    {
        return _googleMap.getCameraPosition().target;
    }

    private void zoomToCurrentLocation()
    {
        LocationFeature.getInstance(this).addLocationListener(1000, LocationRequest.PRIORITY_HIGH_ACCURACY, getZoomToCurrentLocationListener());
    }

    private LocationListener getZoomToCurrentLocationListener()
    {
        return new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                zoomToLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                LocationFeature.getInstance(GPSAlarmActivity.this).removeLocationListener(this);
            }
        };
    }

    private void zoomToLocation(LatLng latLng)
    {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to Mountain View
                    .zoom(14)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
        _googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void resetActiveAlarmsOnMap()
    {
        if(_googleMap != null)
        {
            _googleMap.clear();
            for (GPSAlarm alarm : AlarmFeature.getInstance(this).getAlarmHistory())
            {
                if (alarm.isActive())
                {
                    _googleMap.addMarker(getAlarmMarker(alarm));
                    _googleMap.addCircle(getAlarmCircle(alarm));
                }
            }

        }
    }

    private void updateSetMarkerRadius(int radiusM)
    {
        if(_googleMap != null)
        {
            View circleView = findViewById(R.id.set_alarm_radius_circle);
            LatLng leftLoc = _googleMap.getProjection().getVisibleRegion().farLeft;
            LatLng rightLoc = _googleMap.getProjection().getVisibleRegion().farRight;
            LatLng nearLeftLoc = _googleMap.getProjection().getVisibleRegion().nearLeft;

            float results[] = new float[2];
            Location.distanceBetween(leftLoc.latitude, leftLoc.longitude, rightLoc.latitude, rightLoc.longitude, results);
            int screenDistM = (int) results[0];
            Point leftEndPoint = _googleMap.getProjection().toScreenLocation(leftLoc);
            Point rightEndPoint = _googleMap.getProjection().toScreenLocation(rightLoc);
            Point nearLeftEndPoint = _googleMap.getProjection().toScreenLocation(nearLeftLoc);
            int screenWidthPx = Math.abs(leftEndPoint.x - rightEndPoint.x);
            int screenHeightPx = Math.abs(leftEndPoint.y - nearLeftEndPoint.y);

            int radiusWidthPx = (int) ((radiusM * 1.0 / screenDistM) * screenWidthPx);
            if (radiusWidthPx * 2 < Math.min(screenWidthPx, screenHeightPx))
            {
                circleView.getLayoutParams().width = 2 * radiusWidthPx;
                circleView.getLayoutParams().height = 2 * radiusWidthPx;
            } else
            {

                int diffHeightPx = radiusWidthPx * 2 - screenHeightPx;

                int diffWidthPx = radiusWidthPx * 2 - screenWidthPx;

                if (diffHeightPx <= 0) diffHeightPx = 0;
                if (diffWidthPx <= 0) diffWidthPx = 0;

                circleView.getLayoutParams().width = 2 * radiusWidthPx;
                circleView.getLayoutParams().height = 2 * radiusWidthPx;

                ((RelativeLayout.LayoutParams) circleView.getLayoutParams()).setMargins((-diffWidthPx / 2), (-diffHeightPx / 2), (-diffWidthPx / 2), (-diffHeightPx / 2));
//            circleView.setRight(diffWidthPx / 2 + screenWidthPx);
//
//            circleView.setTop(-diffHeightPx / 2);
//            circleView.setTop(diffHeightPx / 2 + screenHeightPx);
            }
            circleView.requestLayout();
        }
    }

    private CircleOptions getAlarmCircle(GPSAlarm alarm)
    {
        return getMapCircle(alarm.getGeofenceRequest().getLatLng(), alarm.getGeofenceRequest().getRadiusMeters(), getResources().getColor(R.color.AliceBlue));
    }

    private CircleOptions getMapCircle(LatLng latLng, double radiusM, @ColorInt int color)
    {
        return new CircleOptions().center(latLng).radius(radiusM).fillColor(color);
    }

    private MarkerOptions getAlarmMarker(GPSAlarm alarm)
    {
        return new MarkerOptions().position(alarm.getGeofenceRequest().getLatLng()).title(alarm.getTitle());
    }
}
