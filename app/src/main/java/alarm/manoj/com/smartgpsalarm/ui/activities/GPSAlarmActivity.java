package alarm.manoj.com.smartgpsalarm.ui.activities;

import alarm.manoj.com.smartgpsalarm.R;
import alarm.manoj.com.smartgpsalarm.features.LocationFeature;
import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.ui.adapters.AlarmViewAdapter;
import alarm.manoj.com.smartgpsalarm.ui.contracts.GPSAlarmHomeContract;
import alarm.manoj.com.smartgpsalarm.ui.dialogs.AddAlarmDialog;
import alarm.manoj.com.smartgpsalarm.ui.presenters.GPSAlarmActivityPresenter;
import alarm.manoj.com.smartgpsalarm.ui.view.AlarmWarningView;
import alarm.manoj.com.titleseekbar.TitleSeekbar;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONException;

public class GPSAlarmActivity extends AppCompatActivity implements OnMapReadyCallback, GPSAlarmHomeContract.GPSAlarmView
{

    private GPSAlarmHomeContract.GPSAlarmHomePresenter _presenter;
    private AlarmWarningView _alarmWarningView;
    private GoogleMap _googleMap;
    private AlarmViewAdapter _adapter;
    private ListView _alarmList;
    private TitleSeekbar _seekbar;
    private static final String ADD_ALARM_TAG = "add_alarm_tag";

    private static final int PLACE_SEARCH_CODE = 1222;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _presenter = new GPSAlarmActivityPresenter(this, this);
        setContentView(R.layout.activity_gpsalarm);
        ((MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment)).getMapAsync(this);
        setActionBar();
        findViewById(R.id.add_alarm).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LatLng alarmLoc = _presenter.getMapCenter(_googleMap);
                AddAlarmDialog dialog = AddAlarmDialog.newInstance(alarmLoc, "", _presenter.getRadiusM());
                dialog.show(getFragmentManager(), ADD_ALARM_TAG);
            }
        });
        _adapter = new AlarmViewAdapter(this);
        _seekbar = (TitleSeekbar) findViewById(R.id.seekbar);
        _alarmList = (ListView)findViewById(R.id.alarm_list);
        _alarmList.setAdapter(_adapter);
        _seekbar.setSeekHandler(_presenter.getSeekHandler());
        _seekbar.setSeekTitle(_presenter.getRadiusM()+" metres"); //default
        _presenter.onCreate();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        _presenter.onStart();
        _adapter.notifyDataSetChanged();
        _presenter.resetActiveAlarmsOnMap(_googleMap, this);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        _presenter.onNewIntent(intent);
    }

    @Override
    protected void onStop()
    {
        _presenter.onStop();
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
                _presenter.onPlaceAutocompleteResult(place);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showAddAlarmDialog(LatLng latng, String name, int radiusM)
    {
        AddAlarmDialog dialog = AddAlarmDialog.newInstance(latng, name, radiusM);
        dialog.show(getFragmentManager(), ADD_ALARM_TAG);
    }

    @Override
    public void onGpsAlarmsChanged()
    {
        _adapter.notifyDataSetChanged();
        _presenter.resetActiveAlarmsOnMap(_googleMap, this);
    }

    @Override
    public void initAlarmWarningView()
    {
        _alarmWarningView = new AlarmWarningView(this);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        ((RelativeLayout)findViewById(R.id.content_root)).addView(_alarmWarningView, params);
        _alarmWarningView.setVisibility(View.GONE);
    }

    @Override
    public void showAlarmWarningView(GPSAlarm alarm)
    {
        _alarmWarningView.init(alarm);
        _alarmWarningView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideAlarmWarningView()
    {
        _alarmWarningView.setVisibility(View.GONE);
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
                updateSetMarkerRadius(_presenter.getRadiusM());
            }
        });
        zoomToCurrentLocation();
        _presenter.resetActiveAlarmsOnMap(_googleMap, this);
        _presenter.initSeekbarProgress();
    }

    @Override
    public void setSeekbarProgress(int progress)
    {
        _seekbar.getSeekbar().setProgress(progress);
    }

    @Override
    public int getMaxSeekbarProgress()
    {
        return _seekbar.getSeekbar().getMax();
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

    @Override
    public void zoomToLocation(LatLng latLng)
    {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to Mountain View
                    .zoom(14)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
        _googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void addActiveAlarmOnUI(GPSAlarm alarm)
    {
        _googleMap.addMarker(getAlarmMarker(alarm));
        _googleMap.addCircle(getAlarmCircle(alarm));
    }

    @Override
    public void updateSetMarkerRadius(int radiusM)
    {
        if(_googleMap != null)
        {
            View circleView = findViewById(R.id.set_alarm_radius_circle);
            int screenDistM = (int) _presenter.getScreenDistMetres(_googleMap);
            int screenWidthPx = _presenter.getScreenWidthPx(_googleMap);
            int screenHeightPx = _presenter.getScreenHeightPx(_googleMap);

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
