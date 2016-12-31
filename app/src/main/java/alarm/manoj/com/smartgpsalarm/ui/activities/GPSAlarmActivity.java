package alarm.manoj.com.smartgpsalarm.ui.activities;

import alarm.manoj.com.smartgpsalarm.R;
import alarm.manoj.com.smartgpsalarm.features.LocationFeature;
import alarm.manoj.com.smartgpsalarm.ui.dialogs.AddAlarmDialog;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class GPSAlarmActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private GoogleMap _googleMap;
    private static final String ADD_ALARM_TAG = "add_alarm_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsalarm);
        ((MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment)).getMapAsync(this);
        setActionBar();
        findViewById(R.id.add_alarm).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LatLng alarmLoc = getMapCenter();
                AddAlarmDialog dialog = AddAlarmDialog.newInstance(alarmLoc, "");
                dialog.show(getFragmentManager(), ADD_ALARM_TAG);
            }
        });
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
        zoomToCurrentLocation();
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
                Toast.makeText(GPSAlarmActivity.this, "got location", Toast.LENGTH_SHORT).show();
                zoomToLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                LocationFeature.getInstance(GPSAlarmActivity.this).removeLocationListener(this);
            }
        };
    }

    private void zoomToLocation(LatLng latLng)
    {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to Mountain View
                    .zoom(17)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
        _googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
