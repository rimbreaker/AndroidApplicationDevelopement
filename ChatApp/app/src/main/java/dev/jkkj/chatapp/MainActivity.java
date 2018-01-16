package dev.jkkj.chatapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    long startTime=0;
    long difference=0;
    public static final String SONG_MESSAGE = "com.example.AndroidApplicationDevelopement.MESSAGE1";
    public static final String SENDER_MESSAGE = "com.example.AndroidApplicationDevelopement.MESSAGE";
    public static final String USER_MESSAGE = "com.example.AndroidApplicationDevelopement.MESSAGE2";
    public static final String START_MESSAGE = "com.example.AndroidApplicationDevelopement.MESSAGE3";
    public static final String DELAY_MESSAGE = "com.example.AndroidApplicationDevelopement.MESSAGE4";
    private static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_main;
    Button synchBtn;


    //action for 'Search File' button
    public void searchFile(View view){
        //opening different activity
        Intent intent = new Intent(this, MusicSelector.class);
        startActivity(intent);
    }

    public void synchronize(View view){
        startTime=System.currentTimeMillis();
        FirebaseDatabase.getInstance().getReference().child("Synchronize1").setValue(new ChatMessage(null, "SYNCH" + FirebaseAuth.getInstance().getCurrentUser().getEmail()));
    }

    //signing out; entirely from tutorial
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.menu_sign_out)
        {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main,"You have been signed out.",Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        return true;
    }

    //entirely from tutorial
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    //handling signing in; entirely from tutorial
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==SIGN_IN_REQUEST_CODE)
        {
            if(resultCode==RESULT_OK)
            {
                Snackbar.make(activity_main,"Succesfully signed in. Welcome!",Snackbar.LENGTH_SHORT).show();
                displayChatMessage();
            }
            else{
                Snackbar.make(activity_main,"We couldn't sign you in. Please try again later",Snackbar.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    //starting MainActivity;from tutorial, but deleted editText field and floating button
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        synchBtn=findViewById(R.id.synchronize);

        activity_main=(RelativeLayout)findViewById(R.id.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_REQUEST_CODE);
        }
        else
        {
            Snackbar.make(activity_main,"Welcome "+FirebaseAuth.getInstance().getCurrentUser().getEmail(),Snackbar.LENGTH_SHORT).show();
            //Load Content
            displayChatMessage();
        }


    }

    //displaying messages from database in real-time
    private void displayChatMessage() {

        final ArrayList<String> songs=new ArrayList<>();
        final ArrayList<String> users=new ArrayList<>();
        final ListView listOfMessage=(ListView)findViewById(R.id.list_of_message);
        //sth=this;

        adapter = new FirebaseListAdapter<ChatMessage>(this,ChatMessage.class,R.layout.list_item,FirebaseDatabase.getInstance().getReference())
        {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //get references to the views of list_item.xml
                TextView messageText,messageUser,messageTime;
                messageText=(TextView) v.findViewById(R.id.message_text);
                messageUser=(TextView) v.findViewById(R.id.message_user);
                messageTime=(TextView) v.findViewById(R.id.message_time);

                if(!model.getMessageUser().contains("PLAY")&&!model.getMessageUser().contains("SYNCH")) {
                    messageText.setText(model.getMessageText());
                    songs.add(model.getMessageText());
                    users.add(model.getMessageUser());
                    messageUser.setText(model.getMessageUser());
                    messageTime.setText(android.text.format.DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));
                }
            }
        };
        listOfMessage.setAdapter(adapter);

        listOfMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent =new Intent(MainActivity.this,SongOptions.class);
                String song=songs.get(i);
                String user=users.get(i);
                String currentUser=FirebaseAuth.getInstance().getCurrentUser().getEmail();

                intent.putExtra(DELAY_MESSAGE,Long.toString(difference));
                intent.putExtra(START_MESSAGE,"0");
                intent.putExtra(USER_MESSAGE,currentUser);
                intent.putExtra(SONG_MESSAGE,song);
                intent.putExtra(SENDER_MESSAGE,user);
                startActivity(intent);
            }
        });

        FirebaseDatabase.getInstance().getReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessage msg= dataSnapshot.getValue(ChatMessage.class);
                if(!msg.getMessageUser().contains("PLAY")&& !msg.getMessageUser().contains("SYNCH")){
                    listOfMessage.setAdapter(adapter);
                }
                if(msg.getMessageUser().contains("PLAY")&& !msg.getMessageUser().contains(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    Intent intent =new Intent(MainActivity.this,SongOptions.class);
                    String currentUser=FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    intent.putExtra(DELAY_MESSAGE,Long.toString(difference));
                    intent.putExtra(START_MESSAGE,"1");
                    intent.putExtra(USER_MESSAGE,currentUser);
                    intent.putExtra(SONG_MESSAGE,msg.getMessageText());
                    intent.putExtra(SENDER_MESSAGE,msg.getMessageUser());
                    startActivity(intent);
                    }
                if(msg.getMessageUser().contains("SYNCH")&& !msg.getMessageUser().contains(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    Toast.makeText(MainActivity.this,"pong",Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference().child("Synchronize1").removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Synchronize2").setValue(new ChatMessage(null, msg.getMessageUser()));
                }
                if(msg.getMessageUser().contains("SYNCH") && msg.getMessageUser().contains(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    difference = System.currentTimeMillis() - startTime;
                    Toast.makeText(MainActivity.this,"Synchronized",Toast.LENGTH_SHORT).show();
                    synchBtn.setBackgroundColor(Color.parseColor("green"));
                    FirebaseDatabase.getInstance().getReference().child("Synchronize2").removeValue();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
