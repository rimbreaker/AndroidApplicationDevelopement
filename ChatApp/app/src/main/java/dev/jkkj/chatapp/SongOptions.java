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

    //Creating the View
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_optioins);
        Intent intent = getIntent();

        //assigning all buttons to the layout
        synchButton=findViewById(R.id.synchButton);
        playButton=findViewById(R.id.playButton);
        searchButton=findViewById(R.id.searchButton);
        //making play button invisible
        playButton.setVisibility(View.INVISIBLE);

        //getting the messages from the main view
        song = intent.getStringExtra(MainActivity.SONG_MESSAGE);
        String sender = intent.getStringExtra(MainActivity.SENDER_MESSAGE);
        String user=intent.getStringExtra(MainActivity.USER_MESSAGE);

        //sending the song info to the view
        TextView textView = findViewById(R.id.textView);
        textView.setText(song);
        //options if user opened a song that was not sent by them
        if(!user.equals(sender)){
            playButton.setBackgroundColor(Color.parseColor("red"));
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
        if(!ifCanPlay()){
        fileExists=false;
        searchButton.setBackgroundColor(Color.parseColor("red"));
        Toast.makeText(this,"File Not Found",Toast.LENGTH_SHORT).show();
        }
    }

    //checking whether the file is present on a device and whether the device is synchronized
    public boolean ifCanPlay() {
        //if both booleans say "true" then display the "Play" button
        if(isSynched && fileExists){
            playButton.setVisibility(View.VISIBLE);
            playButton.setBackgroundColor(Color.parseColor("green"));
            searchButton.setVisibility(View.INVISIBLE);
            synchButton.setVisibility(View.INVISIBLE);
            return true;
        }
        return false;
    }

    //action for "Synchronize" button
    public void synchBtn(View view){
        //I don't have any action for this yet
        isSynched=false;
        synchButton.setBackgroundColor(Color.parseColor("red"));
        Toast.makeText(this,"Cannot synchronize",Toast.LENGTH_SHORT).show();
    }

    //action for "Play" button
    public void playBtn(View view) throws IOException {

        //sending message to the Firebase with "PLAY" in front of the user, so that it can be interpreted differently by the app
        FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(song, "PLAY" + FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        //stopping if music is already playing
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        //setting up the MediaPlayer
        else{
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), contentUri);
        mediaPlayer.prepare();
        mediaPlayer.start();}
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

