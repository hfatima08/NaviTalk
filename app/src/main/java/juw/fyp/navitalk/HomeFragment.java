package juw.fyp.navitalk;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    ImageView img;
    DatabaseReference ref;
    String userid;
    ArrayList<String> vol_list = new ArrayList<String>();
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref= FirebaseDatabase.getInstance().getReference().child("Users");
        img=view.findViewById(R.id.img);

        Animation zoomin =new TranslateAnimation(1, 1, 0, -50);
        zoomin.setDuration(1000);
        zoomin.setFillEnabled(true);
        zoomin.setFillAfter(true);

        Animation zoomout =  new TranslateAnimation(1, 1, -50, 0);
        zoomout.setDuration(1000);
        zoomout.setFillEnabled(true);
        zoomout.setFillAfter(true);

        img.startAnimation(zoomin);

        zoomin.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                img.startAnimation(zoomout);
            }
        });

        zoomout.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation arg0) {

                img.startAnimation(zoomin);


            }
            
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ReceiveCall();
            }
        }, 4000);

        return view;
    }


    public void ReceiveCall() {

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userid).hasChild("Ringing")) {

                    Query refer = ref.orderByChild("Calling");
                    refer.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String Bid = snapshot.getKey();

                                Intent intent = new Intent(getContext(), ConnectingActivity2.class);
                                intent.putExtra("BId", Bid);
                                startActivity(intent);
                            }


                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //end
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}