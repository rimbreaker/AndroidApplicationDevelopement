package dev.jkkj.chatapp;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;



public class MusicSelector extends AppCompatActivity{
    private static final int MY_PERMISSION_REQUEST = 1;

    ArrayList<String> arrayList;

    String[] toSearch;

    ListView listView2;

    EditText editText;

    ArrayAdapter<String> adapter;

    //checking permission at the beginning of the View
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selector_list);
        //checking if there is permission to search through the files
        if(ContextCompat.checkSelfPermission(MusicSelector.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MusicSelector.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(MusicSelector.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }else {
                ActivityCompat.requestPermissions(MusicSelector.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }
        }else{
            //see below
            doStuff();
        }
    }

    //populating ListView with file list and uploading file info to the database on click
    public void doStuff(){
        listView2=(ListView)findViewById(R.id.listView2);
        arrayList = new ArrayList<>();
        editText=(EditText) findViewById(R.id.txtsearch);
        //see below
        getMusic();
        //populating the ListView with the songs
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,arrayList);
        listView2.setAdapter(adapter);
        //interactions for the search engine
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().equals("")){
                    getMusic();
                    listView2.setAdapter(adapter);
                }
                else {
                    searchItem(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //reacting to songs being clicked
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(arrayList.get(i), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                finish();
            }
        });
    }

    //getting all music files from the device
    public void getMusic(){
        //using the method from the internet
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null,null,null,null);

        //adding every song title and song artist to the arrayList
        if(songCursor != null && songCursor.moveToFirst()){
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                arrayList.add(currentTitle + "\n" + currentArtist );
            }while(songCursor.moveToNext());
        }
        //populating the array that is later used for search engine
        toSearch=arrayList.toArray(new String[0]);
    }

    //work of search-bar
    public void searchItem(String txtToSearch){
        for(String item:toSearch){
            if(!item.contains(txtToSearch)){
                arrayList.remove(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    //reacting for granting permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSION_REQUEST: {
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(MusicSelector.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this,"Permission Granted!",Toast.LENGTH_SHORT).show();

                        doStuff();
                    }
                }else{
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }
}
