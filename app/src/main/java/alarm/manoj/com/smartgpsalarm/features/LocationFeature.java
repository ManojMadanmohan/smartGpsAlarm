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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LocationFeature implements ILocationFeature
{
    private GoogleApiClient _googleApiClient;

    private List<DefaultLocationRequest> _pendingLocationRequests;

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
                        for(DefaultLocationRequest request: _pendingLocationRequests)
                        {
                            addLocationListener(request.getFreq(), request.getPriority(), request.getListener());
                        }
                        _pendingLocationRequests.clear();
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
        _pendingLocationRequests = new ArrayList<>();
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
            _pendingLocationRequests.add(new DefaultLocationRequest(freqSeconds, listener, priority));
        }
    }

    @Override
    public void removeLocationListener(LocationListener listener)
    {
        if(_googleApiClient.isConnected())
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(_googleApiClient, listener);
        } else
        {
            Iterator<DefaultLocationRequest> iterator = _pendingLocationRequests.iterator();
            while(iterator.hasNext())
            {
                DefaultLocationRequest request = iterator.next();
                if(request.getListener().equals(listener))
                {
                    iterator.remove();
                }
            }
        }
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

    private class DefaultLocationRequest
    {
        private int _freq;
        private int priority;
        private LocationListener _listener;

        public DefaultLocationRequest(int _freq, LocationListener _listener, int priority)
        {
            this._freq = _freq;
            this._listener = _listener;
            this.priority = priority;
        }

        public int getFreq()
        {
            return _freq;
        }

        public LocationListener getListener()
        {
            return _listener;
        }

        public int getPriority()
        {
            return priority;
        }
    }
}
