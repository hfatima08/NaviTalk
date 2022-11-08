package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static androidx.camera.core.CameraX.getContext;

public class MainActivity extends AppCompatActivity {

    private HomeFragment Home = new HomeFragment();
    private SettingsFragment settings = new SettingsFragment();
    private BottomNavigationView menu;
    DatabaseReference ref;
    String userId,userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       setFragment(Home);
        menu=findViewById(R.id.menu_item);
        menu.setSelectedItemId(R.id.menu_home);

        ref = FirebaseDatabase.getInstance().getReference().child("Users");
       userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ReceiveCall();
            }
        }, 4000);

        menu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.isChecked()){
                    return true;
                } else {
                    switch (item.getItemId()) {
                        case R.id.menu_home:
                            setFragment(Home);
                            getSupportActionBar().setTitle("NaviTalk");
                            return true;

                        case R.id.menu_setting:
                            setFragment(settings);
                          getSupportActionBar().setTitle("Settings");
                            return true;

                        default:
                            setFragment(Home);
                            getSupportActionBar().setTitle("NaviTalk");
                            return true;
                    }}
            }
        });

    }

    public void ReceiveCall() {

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userId).hasChild("Ringing")) {

                    Query refer = ref.orderByChild("Calling");
                    refer.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String Bid = snapshot.getKey();

                            Intent intent = new Intent(getApplicationContext(), ConnectingActivity2.class);
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

    private void setFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame , fragment);
        ft.commit();
    }

}