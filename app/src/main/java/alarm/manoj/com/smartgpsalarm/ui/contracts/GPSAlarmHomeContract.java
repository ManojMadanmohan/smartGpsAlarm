package alarm.manoj.com.smartgpsalarm.ui.contracts;

import alarm.manoj.com.smartgpsalarm.models.GPSAlarm;
import alarm.manoj.com.smartgpsalarm.ui.base.BasePresenter;
import alarm.manoj.com.smartgpsalarm.ui.base.BaseView;
import alarm.manoj.com.titleseekbar.TitleSeekbar;

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.places.Place;

public class GPSAlarmHomeContract
{
    public interface GPSAlarmView extends BaseView<GPSAlarmHomePresenter>
    {
        public void setSeekbarProgress(int progress);

        public int getMaxSeekbarProgress();

        public void onGpsAlarmsChanged();

        public void zoomToLocation(LatLng Latng);

        public void showAddAlarmDialog(LatLng latlng, String placeName, int radius);

        public void addActiveAlarmOnUI(GPSAlarm alarm);

        public void updateSetMarkerRadius(int radiusM);
    }

    public interface GPSAlarmHomePresenter extends BasePresenter
    {
        public int getScreenDistMetres(GoogleMap map);

        public int getScreenWidthPx(GoogleMap map);

        public int getScreenHeightPx(GoogleMap map);

        public LatLng getMapCenter(GoogleMap map);

        public void resetActiveAlarmsOnMap(GoogleMap map, Context context);

        public TitleSeekbar.TitleSeekbarHandler getSeekHandler();

        public int getRadiusM();

        public void onPlaceAutocompleteResult(Place place);

        public void initSeekbarProgress();
    }
}
