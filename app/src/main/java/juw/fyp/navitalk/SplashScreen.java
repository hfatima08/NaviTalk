package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.HashMap;

import juw.fyp.navitalk.detection.DetectorActivity;
import juw.fyp.navitalk.models.Users;

public class SplashScreen extends AppCompatActivity {
String userName=" ";
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

//                    File f = new File("/data/data/juw.fyp.navitalk/shared_prefs/NaviTalkSharedPref.xml");
//                    if (f.exists()){
//                        //Log.d("TAG", "SharedPreferences Name_of_your_preference : exist");
//                    SharedPreferences sh = getSharedPreferences("NaviTalkSharedPref", MODE_PRIVATE);
//                    userName = sh.getString("userName", "");
//                    if(!userName.isEmpty()){
//                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//                        Query query = reference.child("Users").orderByChild("userName").equalTo(userName);
//                        query.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                for(DataSnapshot datas: dataSnapshot.getChildren()){
//                                    //  String keys=datas.getKey();
//                                    String role = datas.child("role").getValue().toString();
//                                    if(role.equals("Volunteer")){
//                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//                                    startActivity(intent);
//                                }
//                                else if(role.equals("Blind User")){
//                                    Intent intent2 = new Intent(getApplicationContext(), DetectorActivity.class);
//                                    startActivity(intent2);
//                                }
//                                else{
//                                    Intent it= new Intent(getApplicationContext(),RoleScreen.class);
//                                    startActivity(it);
//                                }
//                                }
//
//                            }
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//                                Toast.makeText(getApplicationContext(), "canceled", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    } else{
//                        Intent it= new Intent(getApplicationContext(),RoleScreen.class);
//                        startActivity(it);
//                    }}
//                    else{
//                       Intent it= new Intent(getApplicationContext(),RoleScreen.class);
//                       startActivity(it);
//                   }

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