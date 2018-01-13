package dev.jkkj.chatapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;


public class SongOptions extends AppCompatActivity {

    private boolean isSynched = false;
    private boolean fileExists = false;
    String song;
    Button searchButton;
    Button playButton;
    Button synchButton;
    MediaPlayer mediaPlayer;
    long songId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_optioins);
        Intent intent = getIntent();

        synchButton=findViewById(R.id.synchButton);
        playButton=findViewById(R.id.playButton);
        searchButton=findViewById(R.id.searchButton);
        playButton.setVisibility(View.INVISIBLE);

        song = intent.getStringExtra(MainActivity.SONG_MESSAGE);
        String sender = intent.getStringExtra(MainActivity.SENDER_MESSAGE);
        String user=intent.getStringExtra(MainActivity.USER_MESSAGE);

        TextView textView = findViewById(R.id.textView);
        textView.setText(song);
        if(!user.equals(sender)){
            playButton.setBackgroundColor(Color.parseColor("red"));
            TextView textView2=findViewById(R.id.textView2);
            textView2.setText(sender);
        }else {
            getFileId();
            isSynched=true;
            fileExists=true;
            ifCanPlay();
        }
    }

    public void searchForFile(View view){
        getFileId();
        if(!ifCanPlay()){
        fileExists=false;
        searchButton.setBackgroundColor(Color.parseColor("red"));
        Toast.makeText(this,"File Not Found",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean ifCanPlay() {
        if(isSynched && fileExists){
            playButton.setVisibility(View.VISIBLE);
            playButton.setBackgroundColor(Color.parseColor("green"));
            searchButton.setVisibility(View.INVISIBLE);
            synchButton.setVisibility(View.INVISIBLE);
            return true;
        }
        return false;
    }

    public void synchBtn(View view){
        isSynched=false;
        synchButton.setBackgroundColor(Color.parseColor("red"));
        Toast.makeText(this,"Cannot synchronize",Toast.LENGTH_SHORT).show();
    }

    public void playBtn(View view) throws IOException {
        FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(song, "PLAY"));

        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), contentUri);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) mediaPlayer.release();
        super.onDestroy();
    }

    public void getFileId(){
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null,null,null,null);

        if(songCursor != null && songCursor.moveToFirst()){
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                if(song.equals(currentTitle +"\n" + currentArtist));
                {fileExists=true;
                    searchButton.setBackgroundColor(Color.parseColor("green"));
                    Toast.makeText(this,"File Found",Toast.LENGTH_SHORT).show();
                    ifCanPlay();
                    int idColumn = songCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
                    songId = songCursor.getLong(idColumn);
                    return;}
            }while(songCursor.moveToNext());
        }
    }
}

