package com.codingke.codingkeplayer;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.codingke.codingkeplayer.adapter.MyMusicListAdapter;
import com.codingke.codingkeplayer.utils.MediaUtils;
import com.codingke.codingkeplayer.vo.Mp3Info;

import java.util.ArrayList;

/**
 * Created by Mr.Wang on 2015/11/2.
 */
public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener ,View.OnClickListener{
    private ListView listView_my_music;
    private ArrayList<Mp3Info> mp3Infos;
    private MyMusicListAdapter myMusicListAdapter;
    private MainActivity mainActivity;
    private ImageView imageView_album, imageView_play_pause, imageView_play_next;
    private TextView textView_songName, textView_songer;
   // private boolean isPause = false;
    private int position = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mymusic_list_layout, null);
        listView_my_music = (ListView) view.findViewById(R.id.listView_my_music);
        imageView_album = (ImageView) view.findViewById(R.id.imageView_album);
        imageView_play_pause = (ImageView) view.findViewById(R.id.imageView_play_pause);
        imageView_play_next = (ImageView) view.findViewById(R.id.imageView_play_next);
        textView_songName = (TextView) view.findViewById(R.id.textView_songName);
        textView_songer = (TextView) view.findViewById(R.id.textView_songer);

        listView_my_music.setOnItemClickListener(this);
        imageView_play_pause.setOnClickListener(this);
        imageView_play_next.setOnClickListener(this);
        imageView_album.setOnClickListener(this);
        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        mainActivity.unbindPlayService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void loadData() {
        mp3Infos = MediaUtils.getMp3Infos(mainActivity);
        myMusicListAdapter = new MyMusicListAdapter(mainActivity, mp3Infos);
        listView_my_music.setAdapter(myMusicListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mainActivity.playService.play(position);
    }

    public void changeUIStatusOnPlay(int position) {
        if (position >= 0 && position < mp3Infos.size()) {
            Mp3Info mp3Info = mp3Infos.get(position);
            textView_songName.setText(mp3Info.getTitle());
            textView_songer.setText(mp3Info.getArtist());
            if (mainActivity.playService.isPlaying()) {
                imageView_play_pause.setImageResource(R.mipmap.pause);
            } else {
                imageView_play_pause.setImageResource(R.mipmap.play);
            }
            Bitmap albumBitmap = MediaUtils.getArtwork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            imageView_album.setImageBitmap(albumBitmap);
            this.position = position;
        }
    }

    @Override
    public void onClick(View v) {
      switch (v.getId()) {
          case R.id.imageView_play_pause:{
              if (mainActivity.playService.isPlaying()) {
                  imageView_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
                  mainActivity.playService.puase();
                 // isPause = true;
              } else {
                  if (mainActivity.playService.isPause()) {
                      imageView_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                      mainActivity.playService.start();
                  } else {
                      mainActivity.playService.play(0);
                  }
                  //isPause = false;
              }
              break;
      }
          case R.id.imageView_play_next: {
              mainActivity.playService.next();
              break;
          }
          case R.id.imageView_album:{
             Intent intent = new Intent(mainActivity, PlayActivity.class);
             // intent.putExtra("isPause",isPause);
             startActivity(intent);
              break;
          }
          default:
              break;
      }
    }
}
