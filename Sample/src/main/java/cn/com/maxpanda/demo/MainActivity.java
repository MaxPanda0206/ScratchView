package cn.com.maxpanda.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import java.text.DecimalFormat;

import cn.com.maxpanda.demo.databinding.ActivityMainBinding;
import cn.com.maxpanda.view.ScratchView;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, ScratchView.OnEraseStatusListener {

    private ActivityMainBinding mBinding;

    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        mActivity = this;
        mBinding.mode.setOnCheckedChangeListener(this);
        mBinding.eraserSize.setOnSeekBarChangeListener(this);
        mBinding.maxPercent.setOnSeekBarChangeListener(this);
        mBinding.scratchView.setOnEraseStatusListener(this);
        mBinding.percent.setText(String.format("0/%s", mBinding.scratchView.getMaxPercent()));
        int max = (int) (mBinding.scratchView.getMaxPercent() * 100);
        mBinding.maxPercent.setProgress(max);

    }

    public void reset(View view) {
        Toast.makeText(mActivity, "reset", Toast.LENGTH_SHORT).show();
        mBinding.scratchView.reset();
        mBinding.percent.setText(String.format("0/%s", mBinding.scratchView.getMaxPercent()));
    }

    public void clear(View view) {
        Toast.makeText(mActivity, "clear", Toast.LENGTH_SHORT).show();
        mBinding.scratchView.clear();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_image:
                mBinding.scratchView.setSurfaceDrawMode(ScratchView.Mode.IMAGE);
                mBinding.scratchView.setSurfaceImageSrcId(R.mipmap.scratch_bg);
                break;
            case R.id.rb_watermark:
                mBinding.scratchView.setSurfaceDrawMode(ScratchView.Mode.WATERMARK);
                mBinding.scratchView.setSurfaceImageSrcId(R.mipmap.watermark);
                break;
            case R.id.rb_normal:
                mBinding.scratchView.setSurfaceDrawMode(ScratchView.Mode.NORMAL);
                break;
        }
        mBinding.scratchView.reset();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.eraser_size:
                mBinding.scratchView.setEraseSize(progress);
                break;
            case R.id.max_percent:
                mBinding.scratchView.setMaxPercent(progress / 100f);
                String current = new DecimalFormat("#.##").format(mBinding.scratchView.getPercent());
                String max = new DecimalFormat("#.##").format(mBinding.scratchView.getMaxPercent());
                mBinding.percent.setText(String.format("%s / %s", current, max));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onProgress(float percent) {
        String current = new DecimalFormat("#.##").format(percent);
        String max = new DecimalFormat("#.##").format(mBinding.scratchView.getMaxPercent());
        mBinding.percent.setText(String.format("%s / %s", current, max));
    }

    @Override
    public void onCompleted(View view) {
        mBinding.scratchView.clear();
    }
}