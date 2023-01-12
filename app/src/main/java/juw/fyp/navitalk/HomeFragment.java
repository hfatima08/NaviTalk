package juw.fyp.navitalk;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import juw.fyp.navitalk.models.Users;


public class HomeFragment extends Fragment {
    ImageView img;
    TextView volName;
    DatabaseReference ref;
    String userId,userName;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ref = FirebaseDatabase.getInstance().getReference().child("Users");

        // Resource ID
        img = view.findViewById(R.id.img);
        img = view.findViewById(R.id.img);
        volName = view.findViewById(R.id.vol_name);

        // Get volunteer's username from database function call
        getUsername();

        // Image animation function call
        startAnimation();

        return view;
    } // end of onCreate view

    // Function to get volunteer's username from database
    private void getUsername() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users/"+userId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users users = dataSnapshot.getValue(Users.class);
                    userName = users.getUserName();
                    volName.setText("Hey "+userName+"!");
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    // Image Animation function
    private void startAnimation() {
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
    }

}