package alarm.manoj.com.smartgpsalarm.features;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class FileSystem
{
    private SharedPreferences _preferences;
    private SharedPreferences.Editor _editor;

    public FileSystem(Context context, String key)
    {
        _preferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        _editor = _preferences.edit();
    }
    
    public void write(String key, String value)
    {
        _editor.putString(key, value);
        _editor.commit();
    }
    
    public String read(String key)
    {
        return _preferences.getString(key, null);
    }
    
    public void clear(String key)
    {
        _editor.remove(key);
        _editor.commit();
    }

    public void clearAll()
    {
        _editor.clear();
        _editor.commit();
    }

    public boolean hasKey(String key)
    {
        return _preferences.contains(key);
    }

    
    public List<String> keyList()
    {
        return new ArrayList<String>(_preferences.getAll().keySet());
    }

    
    public List<String> values()
    {
        List<String> values = new ArrayList<String>();
        for (String key : keyList())
        {
            String value = read(key);
            values.add(value);
        }
        return values;
    }
}
