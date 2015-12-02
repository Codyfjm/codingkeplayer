package com.codingke.codingkeplayer.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;


import com.codingke.codingkeplayer.R;
import com.codingke.codingkeplayer.vo.Mp3Info;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Mr.Wang on 2015/11/2.
 */
public class MediaUtils {
    private static final Uri albumArtUrl = Uri.parse("content://media/external/audio/albumart");

    /**
     * 根据歌曲id查询歌曲信息
     *
     * @param context
     * @param _id
     * @return
     */
    public static Mp3Info getMp3Info(Context context, long _id) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null
                , MediaStore.Audio.Media._ID + "=" + _id, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        Mp3Info mp3Info = null;
        if (cursor.moveToNext()) {
            mp3Info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != 0) {
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(url);
            }

        }
        cursor.close();
        return mp3Info;
    }

    public static ArrayList<Mp3Info> getMp3Infos(Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null
                , MediaStore.Audio.Media.DURATION + ">=180000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        ArrayList<Mp3Info> mp3Infos = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            Mp3Info mp3Info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != 0) {
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(url);
                mp3Infos.add(mp3Info);
            }

        }
        cursor.close();
        return mp3Infos;
    }

    /**
     * 格式转换，毫秒变为分：秒
     *
     * @param time
     * @return
     */
    public static String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + (time / (1000 * 60)) + "";
        } else {
            min = (time / (1000 * 60)) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);

    }

    /**
     * 获取专辑图片
     *
     * @param context
     * @param small
     * @return
     */
    public static Bitmap getDefaultArtwork(Context context, boolean small) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (small) {
            return BitmapFactory.decodeStream(context.getResources().openRawResource(R.mipmap.music_album), null, options);
        }
        return BitmapFactory.decodeStream(context.getResources().openRawResource(R.mipmap.music_album), null, options);
    }

    /**
     *
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefault
     * @param small
     * @return
     */
    public static Bitmap getArtwork(Context context, long song_id
            , long album_id, boolean allowdefault, boolean small) {
        if (album_id < 0) {
            if (song_id < 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver resolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUrl, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = resolver.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //先制定原始大小
                options.inSampleSize = 1;
                //只进行大小判断
                options.inJustDecodeBounds = true;
                //调用此方法得到options图片的大小
                BitmapFactory.decodeStream(in, null, options);
                if (small) {
                    options.inSampleSize = computeSampleSize(options, 40);
                } else {
                    options.inSampleSize = computeSampleSize(options, 600);
                }
                //我们得到了缩放比例，现在开始正式读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = resolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context, small);

                        }
                    }

                } else if (allowdefault) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {

                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return null;
    }

    /**
     * 缩放图片尺寸
     * @param options
     * @param target
     * @return
     */
    private static int computeSampleSize(BitmapFactory.Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if (candidate == 0){
            return 1;
        }
        if (candidate > 1){
            if ((w > target)&&(w / candidate) < target){
                candidate -= 1;
            }
        }
        if (candidate > 1){
            if ((h > target)&&(h / candidate) < target){
                candidate -= 1;
            }
        }
        return candidate;
    }

    /**
     * 从文件当中获取专辑封面
     *
     * @param context
     * @param song_id
     * @param album_id
     * @return
     */
    private static Bitmap getArtworkFromFile(Context context, long song_id, long album_id) {
        Bitmap bm = null;
        if (album_id < 0 && song_id < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {


            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if (album_id < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + song_id + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUrl, album_id);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            options.inSampleSize = 100;
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
        return bm;
    }

}

