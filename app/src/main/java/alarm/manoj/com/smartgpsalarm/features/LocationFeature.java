package alarm.manoj.com.smartgpsalarm.features;

import alarm.manoj.com.smartgpsalarm.interfaces.ILocationFeature;
import alarm.manoj.com.smartgpsalarm.models.DefaultGeoFenceRequest;
import alarm.manoj.com.smartgpsalarm.services.GeoFenceIntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LocationFeature implements ILocationFeature
{
    private GoogleApiClient _googleApiClient;

    private List<DefaultLocationRequest> _pendingLocationRequests;
    private List<DefaultGeoFenceRequest> _pendingGeofencingRequests;
    private PendingIntent _geofencingIntent;

    private static LocationFeature _instance;

    private static final long FASTEST_LOCATION_INTERVAL = 1000;
    private static final long GEOFENCE_EXPIRY = 6 * 60 * 60 * 1000;

    private LocationFeature(Context context)
    {
        _googleApiClient = new GoogleApiClient.Builder(context).
                addApi(LocationServices.API)
                .addConnectionCallbacks(getGApiConnectionCallback())
                .addOnConnectionFailedListener(getGApiFailedListener())
                .build();
        _googleApiClient.connect();
        _pendingLocationRequests = new ArrayList<>();
        _pendingGeofencingRequests = new ArrayList<>();
    }

    public static LocationFeature getInstance(Context context)
    {
        if(_instance == null)
        {
            _instance = new LocationFeature(context);
        }
        return _instance;
    }

    @Override
    public Location getLastLocation()
    {
        return _googleApiClient.isConnected() ?
            LocationServices.FusedLocationApi.getLastLocation(_googleApiClient) : null;
    }

    @Override
    public void addLocationListener(int freqMillis, int priority, LocationListener listener)
    {
        if(_googleApiClient.isConnected())
        {
            LocationRequest locationRequest = new LocationRequest()
                    .setInterval(freqMillis)
                    .setPriority(priority)
                    .setFastestInterval(FASTEST_LOCATION_INTERVAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(_googleApiClient, locationRequest, listener);
        } else
        {
            _pendingLocationRequests.add(new DefaultLocationRequest(freqMillis, listener, priority));
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
        if(_googleApiClient.isConnected())
        {
            GeofencingRequest geofencingRequest = getGeofencingRequest(request);
            LocationServices.GeofencingApi.addGeofences(_googleApiClient, geofencingRequest, getGeofencePendingIntent());
        } else
        {
            _pendingGeofencingRequests.add(request);
        }
    }

    @Override
    public void removeGeoFence(String geofenceRequestId)
    {
        if(_googleApiClient.isConnected())
        {

            Toast.makeText(_googleApiClient.getContext(), "GEOFENCE REMOVED!!", Toast.LENGTH_LONG).show();
            LocationServices.GeofencingApi.removeGeofences(_googleApiClient, Arrays.asList(geofenceRequestId));
        } else
        {
            Iterator<DefaultGeoFenceRequest> iterator = _pendingGeofencingRequests.iterator();
            while(iterator.hasNext())
            {
                DefaultGeoFenceRequest request = iterator.next();
                if(request.getRequestId().equals(geofenceRequestId))
                {
                    iterator.remove();
                }
            }
        }
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

    private class DefaultGeofenceAlarmRequest
    {
        private DefaultGeoFenceRequest _request;
        private PendingIntent _deliveryIntent;

        public DefaultGeofenceAlarmRequest(PendingIntent _deliveryIntent, DefaultGeoFenceRequest _request)
        {
            this._deliveryIntent = _deliveryIntent;
            this._request = _request;
        }

        public PendingIntent getDeliveryIntent()
        {
            return _deliveryIntent;
        }

        public DefaultGeoFenceRequest getRequest()
        {
            return _request;
        }
    }

    private GeofencingRequest getGeofencingRequest(DefaultGeoFenceRequest request)
    {
        Geofence geofence = new Geofence.Builder().setCircularRegion(request.getLatLng().latitude, request.getLatLng().longitude, request.getRadiusMeters())
                .setRequestId(request.getRequestId())
                .setLoiteringDelay(1)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setExpirationDuration(GEOFENCE_EXPIRY)
                .build();
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    private PendingIntent getGeofencePendingIntent()
    {
        // Reuse the PendingIntent if we already have it.
        if (_geofencingIntent != null)
        {
            return _geofencingIntent;
        }
        Intent intent = new Intent(_googleApiClient.getContext(), GeoFenceIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        _geofencingIntent = PendingIntent.getService(_googleApiClient.getContext(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return getGeofencePendingIntent();
    }

    @NonNull
    private GoogleApiClient.OnConnectionFailedListener getGApiFailedListener()
    {
        return new GoogleApiClient.OnConnectionFailedListener()
        {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
            {

            }
        };
    }

    @NonNull
    private GoogleApiClient.ConnectionCallbacks getGApiConnectionCallback()
    {
        return new GoogleApiClient.ConnectionCallbacks()
        {
            @Override
            public void onConnected(@Nullable Bundle bundle)
            {
                for(DefaultLocationRequest request: _pendingLocationRequests)
                {
                    addLocationListener(request.getFreq(), request.getPriority(), request.getListener());
                }
                _pendingLocationRequests.clear();
                for(DefaultGeoFenceRequest request: _pendingGeofencingRequests)
                {
                    addGeoFence(request);
                }
                _pendingGeofencingRequests.clear();
            }

            @Override
            public void onConnectionSuspended(int i)
            {

            }
        };
    }

}
