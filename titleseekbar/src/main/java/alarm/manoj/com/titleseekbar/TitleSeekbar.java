package alarm.manoj.com.titleseekbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class TitleSeekbar extends LinearLayout
{
    private SeekBar _seekbar;
    private TextView _titleView;
    private TitleSeekbarHandler _handler;

    public TitleSeekbar(Context context)
    {
        super(context);
        _seekbar = new SeekBar(context);
        _titleView = new TextView(context);
        initView();
    }

    public TitleSeekbar(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public TitleSeekbar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        _seekbar = new SeekBar(context);
        _titleView = new TextView(context);
        initView();
    }

    public void setSeekHandler(TitleSeekbarHandler handler)
    {
        _handler = handler;
    }

    public void setSeekTitle(String title)
    {
        _titleView.setText(title);
    }

    private void initView()
    {
        setOrientation(HORIZONTAL);

        LayoutParams paramsSeek = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsSeek.weight = 1;
        _seekbar.setLayoutParams(paramsSeek);
        addView(_seekbar);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _titleView.setLayoutParams(params);
        _titleView.setText("dummy");
        _titleView.setTextColor(0xFF040404);
        addView(_titleView);
        _seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                String title = _handler.getTitle(i, seekBar.getMax());
                _titleView.setText(title);
                _handler.onSeekbarChangeListener(i, seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
    }

    public SeekBar getSeekbar()
    {
        return _seekbar;
    }

    public interface TitleSeekbarHandler
    {
        public String getTitle(int progress, int maxProgress);

        public void onSeekbarChangeListener(int progress, int maxProgress);
    }
}
