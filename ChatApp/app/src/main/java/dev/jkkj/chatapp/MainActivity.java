package dev.jkkj.chatapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String SONG_MESSAGE = "com.example.AndroidApplicationDevelopement.MESSAGE1";
    public static final String SENDER_MESSAGE = "com.example.AndroidApplicationDevelopement.MESSAGE";
    public static final String USER_MESSAGE = "com.example.AndroidApplicationDevelopement.MESSAGE2";
    private static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_main;
    //FloatingActionButton fab;
   // Intent intent2 = new Intent(this, SongOptions.class);
    Context sth=this;



    public void searchFile(View view){
        Intent intent = new Intent(this, MusicSelector.class);
        startActivity(intent);
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main=(RelativeLayout)findViewById(R.id.activity_main);
       // fab=(FloatingActionButton)findViewById(R.id.fab);
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input);
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(input.getText().toString(),FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                input.setText("");
            }
        });*/
        //Check if not sign-in then  navigate Sign-in page
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

    private void displayChatMessage() {
        final ArrayList<String> songs=new ArrayList<>();
        final ArrayList<String> users=new ArrayList<>();
        final ListView listOfMessage=(ListView)findViewById(R.id.list_of_message);
        adapter = new FirebaseListAdapter<ChatMessage>(this,ChatMessage.class,R.layout.list_item,FirebaseDatabase.getInstance().getReference())
        {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //get references to the views of list_item.xml
                TextView messageText,messageUser,messageTime;
                messageText=(TextView) v.findViewById(R.id.message_text);
                messageUser=(TextView) v.findViewById(R.id.message_user);
                messageTime=(TextView) v.findViewById(R.id.message_time);

                if(!model.getMessageUser().equals("PLAY")) {

                    messageText.setText(model.getMessageText());
                    songs.add(model.getMessageText());
                    users.add(model.getMessageUser());
                    messageUser.setText(model.getMessageUser());
                    messageTime.setText(android.text.format.DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));
                }else{
                    Toast.makeText(sth,"YO, PLAY A SONG",Toast.LENGTH_LONG).show();
                }
            }
        };
        listOfMessage.setAdapter(adapter);

        listOfMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String[] songsArr=songs.toArray(new String[0]);
                String[] usersArr=users.toArray(new String[0]);
                Intent intent =new Intent(sth,SongOptions.class);
                String song=songsArr[i];
                String user=usersArr[i];
                String currentUser=FirebaseAuth.getInstance().getCurrentUser().getEmail();

                intent.putExtra(USER_MESSAGE,currentUser);
                intent.putExtra(SONG_MESSAGE,song);
                intent.putExtra(SENDER_MESSAGE,user);
                startActivity(intent);
            }
        });

    }
}
