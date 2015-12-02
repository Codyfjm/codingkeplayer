package com.codingke.codingkeplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Mr.Wang on 2015/11/3.
 */
public abstract class BaseActivity extends FragmentActivity {
    protected PlayService playService;
    private boolean isBind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.PlayBinder playBinder = (PlayService.PlayBinder) service;
            playService = playBinder.getPlayService();
            playService.setMusicUpdateListener(musicUpdateListener);
            musicUpdateListener.onChange(playService.getCurrentPosition());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService = null;
            isBind = false;
        }
    };
    private PlayService.MusicUpdateListener musicUpdateListener = new PlayService.MusicUpdateListener() {
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    public abstract void publish(int progress);

    public abstract void change(int position);

    //绑定服务
    public void bindPlayService() {
        if (!isBind) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            isBind = true;
        }
    }

    //解除绑定服务
    public void unbindPlayService() {
        if (isBind) {
            unbindService(conn);
            isBind = false;
        }
    }
}
