package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class ConnectingActivity2 extends AppCompatActivity {
    DatabaseReference reference;
    String userid,BId="";
    ImageView endCall, acceptCall;
    MediaPlayer mediaPlayer;
    TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting2);

        // Firebase Instances
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        BId = getIntent().getStringExtra("BId");

        // Resource ID
        endCall = findViewById(R.id.endCall);
        acceptCall = findViewById(R.id.startCall);
        name = findViewById(R.id.blindname);

        // Play Ringtone
        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.start();

        // Assigning Blind User name who's calling
        reference.child(BId).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                name.setText(data);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "canceled", Toast.LENGTH_SHORT).show();
            }
        });

        // End Call button
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                cancelCall();
            }
        });

        // Accept Call Button
        acceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                final HashMap<String, Object> callPickUpMap = new HashMap<>();
                callPickUpMap.put("picked", "picked");
                reference.child(userid).child("Ringing")
                        .updateChildren(callPickUpMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mediaPlayer.stop();
                                Intent intent = new Intent(getApplicationContext(), CallActivity2.class);
                                intent.putExtra("BId",BId);
                                intent.putExtra("uid",userid);
                                startActivity(intent);
                            }
                        });
            }
        });

        // end call if the blind user end's call
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.child(BId).hasChild("Calling") && !snapshot.child(userid).hasChild("Ringing")){
                            mediaPlayer.stop();
                            cancelCall();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }, 4000);


    }// end of oncreate()

// End Call Function
    private void cancelCall() {
        mediaPlayer.stop();
        reference.child(userid).child("Ringing").removeValue();
        reference.child(BId).child("Calling").removeValue();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

}//end of class
