package alarm.manoj.com.smartgpsalarm.features;

import alarm.manoj.com.smartgpsalarm.interfaces.ILocationFeature;
import alarm.manoj.com.smartgpsalarm.models.DefaultGeoFenceRequest;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationFeature implements ILocationFeature
{
    private GoogleApiClient _googleApiClient;

    private static final long FASTEST_LOCATION_INTERVAL = 1000;

    public LocationFeature(Context context)
    {
        _googleApiClient = new GoogleApiClient.Builder(context).
                addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
                {
                    @Override
                    public void onConnected(@Nullable Bundle bundle)
                    {

                    }

                    @Override
                    public void onConnectionSuspended(int i)
                    {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener()
                {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {

                    }
                })
                .build();
    }

    @Override
    public Location getLastLocation()
    {
        return _googleApiClient.isConnected() ?
            LocationServices.FusedLocationApi.getLastLocation(_googleApiClient) : null;
    }

    @Override
    public void addLocationListener(int freqSeconds, int priority, LocationListener listener)
    {
        if(_googleApiClient.isConnected())
        {
            LocationRequest locationRequest = new LocationRequest()
                    .setInterval(freqSeconds)
                    .setPriority(priority)
                    .setFastestInterval(FASTEST_LOCATION_INTERVAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(_googleApiClient, locationRequest, listener);
        } else
        {
            //TODO
        }
    }

    @Override
    public void removeLocationListener(LocationListener listener)
    {
        if(_googleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(_googleApiClient, listener);
    }

    @Override
    public void addGeoFence(DefaultGeoFenceRequest request)
    {
        //TODO
    }

    @Override
    public void removeGeoFence(DefaultGeoFenceRequest request)
    {
        //TODO
    }
}
