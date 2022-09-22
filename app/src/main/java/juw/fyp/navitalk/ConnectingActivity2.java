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

import juw.fyp.navitalk.detection.DetectorActivity;

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

        reference = FirebaseDatabase.getInstance().getReference("Users");

        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        BId = getIntent().getStringExtra("BId");

        endCall = findViewById(R.id.endCall);
        acceptCall = findViewById(R.id.startCall);
        name = findViewById(R.id.blindname);
        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.start();

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

        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();

                cancelCall();
            }
        });

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
                                startActivity(intent);
                            }
                        });
            }
        });

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


    private void cancelCall() {
        mediaPlayer.stop();
        reference.child(userid).child("Ringing").removeValue();
        reference.child(BId).child("Calling").removeValue();

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
//        //senders call
//        reference.child(BId)
//                .child("Calling")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists() && snapshot.hasChild("calling")) {
//                            callingId = snapshot.child("calling").getValue().toString();
//
//                            reference.child(callingId)
//                                    .child("Ringing")
//                                    .removeValue()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                reference.child(userid)
//                                                        .child("Calling")
//                                                        .removeValue();
//
//                                            }
//                                        }
//                                    });
//                        } else {
//                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                            finish();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//
//        //receiver's call
//        reference.child(userid)
//                .child("Ringing")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists() && snapshot.hasChild("ringing")) {
//                            ringingId = snapshot.child("ringing").getValue().toString();
//
//                            reference.child(ringingId)
//                                    .child("Calling")
//                                    .removeValue()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                reference.child(userid)
//                                                        .child("Ringing")
//                                                        .removeValue();
//                                            }
//                                        }
//                                    });
//                        } else {
//                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                            finish();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
    }




}//end of class
