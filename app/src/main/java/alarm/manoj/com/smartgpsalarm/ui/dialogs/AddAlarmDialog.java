package alarm.manoj.com.smartgpsalarm.ui.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import com.google.android.gms.maps.model.LatLng;

public class AddAlarmDialog extends DialogFragment
{
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_TITLE = "title";

    public AddAlarmDialog()
    {

    }

    public static AddAlarmDialog newInstance(LatLng latLng, String title)
    {
        Bundle bundle = new Bundle();
        bundle.putDouble(KEY_LAT, latLng.latitude);
        bundle.putDouble(KEY_LON, latLng.longitude);
        bundle.putString(KEY_TITLE, title);
        AddAlarmDialog alarmDialog = new AddAlarmDialog();
        alarmDialog.setArguments(bundle);
        return alarmDialog;
    }


}
