package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import juw.fyp.navitalk.detection.CameraActivity;
import juw.fyp.navitalk.detection.CameraConnectionFragment;
import juw.fyp.navitalk.detection.DetectorActivity;
import juw.fyp.navitalk.detection.tflite.Detector;

public class ConnectingActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    ArrayList<String> vol_list;
    String username,checker=" ",callingId,ringingId;
    ImageView endCall,acceptCall;
    MediaPlayer mediaPlayer;
    String volid=" ";
    LinearLayout linearLayout;
    SwipeListener swipeListener;
    Intent intent;
    TextToSpeech t1;
    TextView name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);

        endCall = findViewById(R.id.endCall);
        acceptCall = findViewById(R.id.startCall);
        linearLayout = findViewById(R.id.layout);
        name = findViewById(R.id.volname);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
      //  vol_list = (ArrayList<String>) getIntent().getSerializableExtra("volList");
        volid = getIntent().getStringExtra("vol");
        username = auth.getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);

        swipeListener = new SwipeListener(linearLayout);

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

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                    t1.speak("when you want to end your call, swipe right",TextToSpeech.QUEUE_ADD, null);
                }
            }
        });

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

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


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.child(username).hasChild("Calling") && !snapshot.child(volid).hasChild("Ringing")){
                            cancelCall();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }, 4000);

    }//oncreate


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
                                    cancelCall();
 //                                   textView.setText("swiped right");
//                                    t1.speak("Start speaking",TextToSpeech.QUEUE_ADD, null);
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//                                            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start Speaking");
//                                            if(intent.resolveActivity(getPackageManager())!=null){
//                                                startActivityForResult(intent,10);
//                                            }else{
//                                                t1.speak("Your device does not support speech input", TextToSpeech.QUEUE_ADD, null);
//                                            }
//
//                                        }
//                                    }, 1000);

                                }
                                else{
//                                    textView.setText("swiped left");
                                    t1.speak("when you want to end your call, swipe right and say end call.",TextToSpeech.QUEUE_ADD, null);
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

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 10:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> result =  data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                   if(result.get(0).equals("end call")){
                       cancelCall();
                   }

                }else{
                    t1.speak("Sorry, I didn't hear anything", TextToSpeech.QUEUE_ADD, null);
                }
        }
    }


    public void connectCall(){
      mediaPlayer.start();

    //  volid = vol_list.get(0);
      reference.child(volid).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              if (!checker.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing")) {

                  final HashMap<String, Object> callingInfo = new HashMap<>();
                  callingInfo.put("calling", volid);

                  reference.child(username)
                          .child("Calling")
                          .updateChildren(callingInfo)
                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task) {
                                  if (task.isSuccessful()) {
                                      final HashMap<String, Object> ringingInfo = new HashMap<>();
                                      ringingInfo.put("ringing", username);
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

      reference.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              if(!volid.equals(null)){
                  if (snapshot.child(volid).child("Ringing").hasChild("picked")) {
                      mediaPlayer.stop();
                      Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                      intent.putExtra("volId",volid);
                      startActivity(intent);
                  }
              }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {

          }
      });




  }


    private void cancelCall() {

        mediaPlayer.stop();
        reference.child(volid).child("Ringing").removeValue();
        reference.child(username).child("Calling").removeValue();

        startActivity(new Intent(getApplicationContext(), DetectorActivity.class));
        finish();

        //senders call
//        reference.child(username)
//                .child("Calling")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if(snapshot.exists() && snapshot.hasChild("calling")) {
//                            callingId = snapshot.child("calling").getValue().toString();
//
//                            reference.child(callingId)
//                                    .child("Ringing")
//                                    .removeValue()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful()){
//                                                reference.child(username)
//                                                        .child("Calling")
//                                                        .removeValue();
//
//                                            }
//                                        }
//                                    });
//                        }
//                        else{
//                            startActivity(new Intent(getApplicationContext(),DetectorActivity.class));
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
//        reference.child(volid)
//                .child("Ringing")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if(snapshot.exists() && snapshot.hasChild("ringing")) {
//                            ringingId = snapshot.child("ringing").getValue().toString();
//
//                            reference.child(ringingId)
//                                    .child("Calling")
//                                    .removeValue()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful()){
//                                                reference.child(username)
//                                                        .child("Ringing")
//                                                        .removeValue();
//
//                                                startActivity(new Intent(getApplicationContext(), DetectorActivity.class));
//                                                finish();
//                                            }
//                                        }
//                                    });
//                        }
//                        else{
//                            startActivity(new Intent(getApplicationContext(), DetectorActivity.class));
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

}