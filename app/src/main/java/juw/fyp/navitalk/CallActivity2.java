package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import juw.fyp.navitalk.detection.DetectorActivity;
import juw.fyp.navitalk.models.Users;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CallActivity2 extends AppCompatActivity  implements Session.SessionListener,
        PublisherKit.PublisherListener {

    private static String API_KEY = "47555231";
    private static String SESSION_ID = "2_MX40NzU1NTIzMX5-MTY2NDE4OTM1ODUyNX5LZ1hCK0IxalRQeHRUVjhIRlFudUhtQ3J-UH4";
    private static String TOKEN ="T1==cGFydG5lcl9pZD00NzU1NTIzMSZzaWc9YWIwMGZlNTRhZjliMzhjY2U5YjExY2QxOTZhMGRiOTFkZGY2NDY0ZTpzZXNzaW9uX2lkPTJfTVg0ME56VTFOVEl6TVg1LU1UWTJOREU0T1RNMU9EVXlOWDVMWjFoQ0swSXhhbFJRZUhSVVZqaElSbEZ1ZFVodFEzSi1VSDQmY3JlYXRlX3RpbWU9MTY2NDE4OTM3NSZub25jZT0wLjEwMTM2NDEzOTUwNjY2MDIzJnJvbGU9cHVibGlzaGVyJmV4cGlyZV90aW1lPTE2NjY3ODEzNzQmaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0=";
    private static final String LOG_TAG = CallActivity2.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    ImageView endCall, mic;
    FrameLayout container1, container2;
    DatabaseReference ref;
    String userId = "",BId=" ";
    Boolean isAudio = true;
    Session session;
    Publisher publisher;
    Subscriber subscriber;
    TextView name;
    LinearLayout progess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call2);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        BId = getIntent().getStringExtra("BId");

        endCall = findViewById(R.id.endCall);
        name = findViewById(R.id.bname);
        mic = findViewById(R.id.micBtn);
        progess = findViewById(R.id.progress);

        //assigning callers name on call screen
        ref.child(BId).child("userName").addValueEventListener(new ValueEventListener() {
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

        //end call button code
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                            ref.child(userId).child("Ringing").removeValue();
                            ref.child(BId).child("Calling").removeValue();

                            session.unpublish(publisher);

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();

                               }
        });

        requestPermission();

        //mic mute and un-mute code
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (publisher != null) {
                isAudio = !isAudio;
                if(isAudio){

                    mic.setImageResource(R.drawable.btn_unmute_normal);
                    publisher.setPublishAudio(true);

                }else{

                    mic.setImageResource(R.drawable.btn_mute_pressed);
                    publisher.setPublishAudio(false);

                }
            }
            }
        });

        //end call if blind user ends call
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.child(userId).hasChild("Ringing")){
                            endCall.setClickable(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }, 2000);

    }

//permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,CallActivity2.this);

    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission(){
        String[] perms = {Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};

        if(EasyPermissions.hasPermissions(this,perms)){
            container1 = findViewById(R.id.cont1);
            container2 = findViewById(R.id.cont2);

            session =  new Session.Builder(this,API_KEY,SESSION_ID).build();
            session.setSessionListener(CallActivity2.this);

            session.connect(TOKEN);
        }
        else{
            EasyPermissions.requestPermissions(this,"Need Camera & Mic Permissions ....",RC_VIDEO_APP_PERM,perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    ///stream connection
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG,"Session Connected");

        publisher = new Publisher.Builder(this).build();
        publisher.setPublisherListener(CallActivity2.this);
        publisher.setCameraId(1);
        publisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        container2.addView(publisher.getView());

        if(publisher.getView() instanceof GLSurfaceView){

            ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
        }

        session.publish(publisher);
        progess.setVisibility(View.GONE);
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Received");

        if(subscriber == null){
            subscriber = new Subscriber.Builder(this,stream).build();;
            session.subscribe(subscriber);

            container1.addView(subscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");

        session.unpublish(publisher);
        if(subscriber != null){
            subscriber = null;
            container1.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }
}