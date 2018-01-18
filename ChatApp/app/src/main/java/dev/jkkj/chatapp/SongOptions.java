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
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;


public class SongOptions extends AppCompatActivity {

    private boolean isSynched = false;
    private boolean fileExists = false;
    String song;
    String starter;
    Button searchButton;
    Button playButton;
    String delay;
    long startTime;
   // Button synchButton;
    MediaPlayer mediaPlayer;
    long songId;

    //Creating the View
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_optioins);
        Intent intent = getIntent();
        songId=0;

        //assigning all buttons to the layout
        //synchButton=findViewById(R.id.synchButton);
        playButton=findViewById(R.id.playButton);
        searchButton=findViewById(R.id.searchButton);
        //making play button invisible
        playButton.setVisibility(View.INVISIBLE);

        //getting the messages from the main view
        song = intent.getStringExtra(MainActivity.SONG_MESSAGE);
        String sender = intent.getStringExtra(MainActivity.SENDER_MESSAGE);
        String user=intent.getStringExtra(MainActivity.USER_MESSAGE);
        starter=intent.getStringExtra(MainActivity.START_MESSAGE);
        startTime=Long.parseLong(intent.getStringExtra(MainActivity.DELAY_MESSAGE));

        if(starter.equals("1"))
        {
            searchButton.performClick();
            if(fileExists)
             playButton.performClick();
        }
        //sending the song info to the view
        TextView textView = findViewById(R.id.textView);
        textView.setText(song);
        //options if user opened a song that was not sent by them
        if(!user.equals(sender)){
            TextView textView2=findViewById(R.id.textView2);
            textView2.setText(sender);
        }
        //options if user has opened a file that was sent by them
        else {
            getFileId();
            isSynched=true;
            fileExists=true;
            ifCanPlay();
        }
    }

    //action for "Search For Files" button
    public void searchForFile(View view){
        getFileId();
        //reacting to having not found the file
        if(songId==0){
        fileExists=false;
        searchButton.setBackgroundColor(Color.parseColor("red"));
        Toast.makeText(this,"File Not Found",Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this,"File Found",Toast.LENGTH_SHORT).show();

    }

    //checking whether the file is present on a device and whether the device is synchronized
    public boolean ifCanPlay() {
        //if both booleans say "true" then display the "Play" button
        if(isSynched && fileExists){
            playButton.setVisibility(View.VISIBLE);
            playButton.setBackgroundColor(Color.parseColor("green"));
            searchButton.setVisibility(View.INVISIBLE);
           // synchButton.setVisibility(View.INVISIBLE);
            return true;
        }
        return true;
    }

   public void deleteSong(View view){
       FirebaseDatabase.getInstance().getReference().child(song.replace("\n","-")).removeValue();
       Toast.makeText(this,"Song deleted",Toast.LENGTH_SHORT).show();
       finish();
   }

    //action for "Play" button
    public void playBtn(View view) throws IOException, InterruptedException {



        //sending message to the Firebase with "PLAY" in front of the user, so that it can be interpreted differently by the app
        if(starter.equals("0")) {

            startTime=System.currentTimeMillis()+startTime/4+5000;
            FirebaseDatabase.getInstance().getReference().child("toPlay").setValue(new ChatMessage(song,"PLAY" + FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        }

        if(starter.equals("0"))
           FirebaseDatabase.getInstance().getReference().child("toPlay").removeValue();

        //stopping if music is already playing
        if(mediaPlayer != null){

            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
            starter="1";
        }
        //setting up the MediaPlayer
        else{
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), contentUri);
        mediaPlayer.prepare();


            new Thread(new Runnable() {
                public void run() {
                    if(System.currentTimeMillis()<startTime)
                        while(System.currentTimeMillis()!=startTime){}
                    mediaPlayer.start();
                }
            }).start();
        starter="1";
        }

    }

    //destroying MediaPlayer on closing the View
    @Override
    public void onDestroy() {
        if (mediaPlayer != null) mediaPlayer.release();
        super.onDestroy();
    }

    //searching through the device's files; setting File ID for the MediaPlayer
    public void getFileId(){
        //searching through the device basically just like in MusicSelector.class
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null,null,null,null);

        if(songCursor != null && songCursor.moveToFirst()){
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                //reacting to having found the file
                if(song.equals(currentTitle +"\n" + currentArtist))
                {fileExists=true;
                    searchButton.setBackgroundColor(Color.parseColor("green"));
                    ifCanPlay();
                    int idColumn = songCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
                    songId = songCursor.getLong(idColumn);
                    playButton.setVisibility(View.VISIBLE);
                    playButton.setBackgroundColor(Color.parseColor("green"));
                    return;
                    }
            }while(songCursor.moveToNext());
        }
    }
}

