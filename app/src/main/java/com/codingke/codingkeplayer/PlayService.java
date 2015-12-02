package com.codingke.codingkeplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.codingke.codingkeplayer.utils.MediaUtils;
import com.codingke.codingkeplayer.vo.Mp3Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音乐播放的服务组件
 * 实现功能
 * 1.播放
 * 2.上一首
 * 3.下一首
 * 4.暂停
 * 5.获取当前进度
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private MediaPlayer mediaPlayer;
    private int currentPosition;
    ArrayList<Mp3Info> mp3Infos;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private MusicUpdateListener musicUpdateListener;
    private boolean isPause = false;
    public static final int ORDER_PLAY = 1;
    public static final int RUNDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;
    private int play_mode = ORDER_PLAY;

    public int getPlay_mode() {
        return play_mode;
    }

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public boolean isPause() {
        return isPause;
    }

    public PlayService() {
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    private Random random = new Random();

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case ORDER_PLAY:
                next();
                break;
            case RUNDOM_PLAY:
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGLE_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    class PlayBinder extends Binder {
        public PlayService getPlayService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mp3Infos = MediaUtils.getMp3Infos(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        es.execute(updateStatusRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (es != null && es.isShutdown()) {
            es.shutdown();
            es = null;
        }
    }

    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (musicUpdateListener != null && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public void play(int position) {
        if (position >= 0 && position < mp3Infos.size()) {
            Mp3Info mp3Info = mp3Infos.get(position);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
                mediaPlayer.prepare();
                mediaPlayer.start();
                currentPosition = position;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (musicUpdateListener != null) {
                musicUpdateListener.onChange(currentPosition);
            }
        }
    }

    public void puase() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    public void prev() {
        if (currentPosition - 1 < 0) {
            currentPosition = mp3Infos.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);
    }

    public void next() {
        if (currentPosition + 1 >= mp3Infos.size() - 1) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);
    }

    public void start() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();

        }
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        mediaPlayer.seekTo(msec);
    }

    public interface MusicUpdateListener {
        public void onPublish(int progress);

        public void onChange(int position);
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }
}
