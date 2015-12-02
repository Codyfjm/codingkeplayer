package com.codingke.codingkeplayer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codingke.codingkeplayer.utils.MediaUtils;
import com.codingke.codingkeplayer.vo.Mp3Info;

import java.util.ArrayList;

/**
 * Created by Mr.Wang on 2015/11/3.
 */
public class PlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private TextView textView1_title, textView_start_time, textView_end_time;
    private ImageView imageView1_album, imageView_play_mode, imageView_previous, imageView1_play_pause, imageView_next;
    private SeekBar seekBar;
    private ViewPager viewPager;
    private ArrayList<View> views = new ArrayList<>();
    private ArrayList<Mp3Info> mp3Infos;
    private static final int UPDATE_TIME = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
      //  textView1_title = (TextView) findViewById(R.id.textView1_title);
        textView_end_time = (TextView) findViewById(R.id.textView_end_time);
        textView_start_time = (TextView) findViewById(R.id.textView_start_time);
      //  imageView1_album = (ImageView) findViewById(R.id.imageView1_album);
        imageView_play_mode = (ImageView) findViewById(R.id.imageView_play_mode);
        imageView1_play_pause = (ImageView) findViewById(R.id.imageView1_play_pause);
        imageView_previous = (ImageView) findViewById(R.id.imageView_previous);
        imageView_next = (ImageView) findViewById(R.id.imageView_next);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        initViewPager();
        imageView1_play_pause.setOnClickListener(this);
        imageView_next.setOnClickListener(this);
        imageView_previous.setOnClickListener(this);
        imageView_play_mode.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        mp3Infos = MediaUtils.getMp3Infos(this);
        //bindPlayService();
        myHandler = new MyHandler(this);
        //isPause = getIntent().getBooleanExtra("isPause",false);
    }

    private static MyHandler myHandler;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            playService.puase();
            playService.seekTo(progress);
            playService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    static class MyHandler extends Handler {
        private PlayActivity playActivity;

        public MyHandler(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (playActivity != null) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.textView_start_time.setText(MediaUtils.formatTime(msg.arg1));
                        break;
                }
            }
        }
    }

    public void initViewPager() {
        View album_image_layout = getLayoutInflater().inflate(R.layout.album_image_layout, null);
        imageView1_album = (ImageView) album_image_layout.findViewById(R.id.imageView1_album);
        textView1_title = (TextView) album_image_layout.findViewById(R.id.textView1_title);
        views.add(album_image_layout);
        views.add(getLayoutInflater().inflate(R.layout.lrc_layout, null));
        viewPager.setAdapter(new MyPagerAdapter());
    }
class MyPagerAdapter extends PagerAdapter{
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = views.get(position);
        container.addView(v);
        return v;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void publish(int progress) {
//textView_end_time.setText(MediaUtils.formatTime(progress));
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        myHandler.sendMessage(msg);
        seekBar.setProgress(progress);
    }

    @Override
    public void change(int position) {
        //if (this.playService.isPlaying()) {
        Mp3Info mp3Info = mp3Infos.get(position);
        textView1_title.setText(mp3Info.getTitle());
        Bitmap alnumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
        imageView1_album.setImageBitmap(alnumBitmap);
        textView_end_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        seekBar.setProgress(0);
        seekBar.setMax((int) mp3Info.getDuration());
        if (this.playService.isPlaying()) {
            imageView1_play_pause.setImageResource(R.mipmap.pause);
        } else {
            imageView1_play_pause.setImageResource(R.mipmap.play);
        }
        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.order);
                imageView_play_mode.setTag(PlayService.ORDER_PLAY);
                break;
            case PlayService.RUNDOM_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.random);
                imageView_play_mode.setTag(PlayService.RUNDOM_PLAY);
                break;
            case PlayService.SINGLE_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.single);
                imageView_play_mode.setTag(PlayService.SINGLE_PLAY);
                break;
        }
        // }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView1_play_pause: {
                if (playService.isPlaying()) {
                    imageView1_play_pause.setImageResource(R.mipmap.play);
                    playService.puase();
                } else {
                    if (playService.isPause()) {
                        imageView1_play_pause.setImageResource(R.mipmap.pause);
                        this.playService.start();
                    } else {
                        this.playService.play(0);
                    }
                }

                break;
            }
            case R.id.imageView_next: {
                playService.next();
                break;
            }
            case R.id.imageView_previous: {
                playService.prev();
                break;
            }
            case R.id.imageView_play_mode: {
                int mode = (int) imageView_play_mode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.random);
                        imageView_play_mode.setTag(PlayService.RUNDOM_PLAY);
                        playService.setPlay_mode(PlayService.RUNDOM_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.rundom_play), Toast.LENGTH_LONG).show();
                        break;
                    case PlayService.RUNDOM_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.single);
                        imageView_play_mode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.single_play), Toast.LENGTH_LONG).show();
                        break;
                    case PlayService.SINGLE_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.order);
                        imageView_play_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.order_play), Toast.LENGTH_LONG).show();

                        break;
                }
                break;
            }
            default:
                break;
        }
    }
}
