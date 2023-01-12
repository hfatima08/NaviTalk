package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Locale;
import juw.fyp.navitalk.detection.DetectorActivity;


public class ConnectingActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    String checker=" ",volid=" ",uid;
    ImageView endCall,acceptCall;
    MediaPlayer mediaPlayer;
    LinearLayout linearLayout;
    SwipeListener swipeListener;
    Intent intent;
    TextToSpeech t1;
    TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);

        // Resource ID
        endCall = findViewById(R.id.endCall);
        acceptCall = findViewById(R.id.startCall);
        linearLayout = findViewById(R.id.layout);
        name = findViewById(R.id.volname);

        // Firebase Instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = auth.getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        // Get volunteer id from bundle
        volid = getIntent().getStringExtra("vol");

        // Play ringtone
        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);

        // Initialize swipe gesture on layout
        swipeListener = new SwipeListener(linearLayout);

        // Assigning the volunteer's name who the blind has called
        reference.child(volid).child("userName").addValueEventListener(new ValueEventListener() {
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

        // Speech to endcall
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                    t1.speak("when you want to end your call, swipe right. Swipe left to listen again",TextToSpeech.QUEUE_ADD, null);
                }
            }
        });

        // calling google intent for speech
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // endcall button
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                checker="clicked";
                cancelCall();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                connectCall();
            }
        }, 1000);

    // end call if the volunteer end's call
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.child(uid).hasChild("Calling") && !snapshot.child(volid).hasChild("Ringing")){
                            cancelCall();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }, 4000);

    }// end of onCreate()

    // Swipe gesture
    private class SwipeListener implements View.OnTouchListener{
        GestureDetector gestureDetector;

        SwipeListener(View view){
            int threshold= 100;
            int velocity_threshold=100;

            GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    float xDiff = e2.getX() - e1.getX();
                    float yDiff = e2.getY() - e1.getY();
                    try {
                        if(Math.abs(xDiff) > Math.abs(yDiff)){
                            if(Math.abs(xDiff) > threshold && Math.abs(velocityX) > velocity_threshold){
                                if(xDiff>0){
                                    // Swipe Right
                                    cancelCall();
                                }
                                else{
                                    // Swipe Left
                                    t1.speak("when you want to end your call, swipe right.",TextToSpeech.QUEUE_ADD, null);
                                }
                                return true;
                            }
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return false;
                }
            };
            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
    } // end of swipe gesture code

    //Connect Call
    public void connectCall(){
      mediaPlayer.start();
      reference.child(volid).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              if (!checker.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing")) {

                  final HashMap<String, Object> callingInfo = new HashMap<>();
                  callingInfo.put("calling", volid);

                  reference.child(uid)
                          .child("Calling")
                          .updateChildren(callingInfo)
                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task) {
                                  if (task.isSuccessful()) {
                                      final HashMap<String, Object> ringingInfo = new HashMap<>();
                                      ringingInfo.put("ringing", uid);
                                      reference.child(volid)
                                              .child("Ringing")
                                              .updateChildren(ringingInfo);
                                  }
                              }
                          });
              }
          }
          @Override
          public void onCancelled(@NonNull DatabaseError error) {
          }
      });

      // Go To Call Activity When volunteer picks up
      reference.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              if(!volid.equals(null)){
                  if (snapshot.child(volid).child("Ringing").hasChild("picked")) {
                      mediaPlayer.stop();
                      Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                      intent.putExtra("volId",volid);
                      intent.putExtra("uid",uid);
                      startActivity(intent);
                  }
              }
          }
          @Override
          public void onCancelled(@NonNull DatabaseError error) {
          }
      });
  }

// End Call method
    private void cancelCall() {
        mediaPlayer.stop();
        reference.child(volid).child("Ringing").removeValue();
        reference.child(uid).child("Calling").removeValue();
        startActivity(new Intent(getApplicationContext(), DetectorActivity.class));
        finish();
   }

}