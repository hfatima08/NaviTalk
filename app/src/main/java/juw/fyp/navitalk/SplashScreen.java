package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import juw.fyp.navitalk.detection.DetectorActivity;
import juw.fyp.navitalk.models.Users;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Thread td= new Thread(){
            public void run()
            {
                try {
                    sleep(3000);

                }

                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                finally {
                   DatabaseReference reference;
                   FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                   if(user!=null){
                  reference = FirebaseDatabase.getInstance().getReference("Users/"+user.getUid()+"/role");
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String data = dataSnapshot.getValue(String.class);
                                if((data.equals("Volunteer")) && (user!=null)){
                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(intent);
                                }
                                else if((data.equals("Blind User")) && (user!=null)){
                                    Intent intent2 = new Intent(getApplicationContext(), DetectorActivity.class);
                                    startActivity(intent2);
                                }
                                else{
                                    Intent it= new Intent(getApplicationContext(),RoleScreen.class);
                                    startActivity(it);
                                }
                            }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(), "canceled", Toast.LENGTH_SHORT).show();
                        }
                    });}  else{
                       Intent it= new Intent(getApplicationContext(),RoleScreen.class);
                       startActivity(it);
                   }
          }}
        }; td.start();

    }
}