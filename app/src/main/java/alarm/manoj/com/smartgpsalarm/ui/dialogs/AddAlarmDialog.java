package alarm.manoj.com.smartgpsalarm.ui.dialogs;

import alarm.manoj.com.smartgpsalarm.R;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.gms.maps.model.LatLng;

public class AddAlarmDialog extends DialogFragment
{
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_TITLE = "title";

    private LatLng _latlng;

    @BindView(R.id.add_alarm_button)
    Button _button;
    @BindView(R.id.add_dialog_lat_lng)
    TextView _latlngView;
    @BindView(R.id.add_dialog_time_picker)
    TimePicker _timePicker;
    @BindView(R.id.add_dialog_title_input)
    EditText _titleInput;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.add_alarm_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initView();

    }

    private void initView()
    {
        double lat = getArguments().getDouble(KEY_LAT);
        double lon = getArguments().getDouble(KEY_LON);
        _latlng = new LatLng(lat, lon);
        _latlngView.setText(format(_latlng));
        String title = getArguments().getString(KEY_TITLE);
        _titleInput.setText(title);
        _button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                addAlarm();
            }
        });
    }

    private String format(LatLng latLng)
    {
        String formatted = String.format("%.4f",Math.abs(latLng.latitude))+getDegreeChar();
        if(latLng.latitude > 0)
        {
            formatted += "N";
        } else
        {
            formatted += "S";
        }
        formatted+= ", "+String.format("%.4f",Math.abs(latLng.longitude))+getDegreeChar();
        if(latLng.longitude > 0)
        {
            formatted += "W";
        } else
        {
            formatted += "E";
        }
        return formatted;
    }

    private String getDegreeChar()
    {
        return Character.toString((char) 176);
    }

    private void addAlarm()
    {

    }
}
