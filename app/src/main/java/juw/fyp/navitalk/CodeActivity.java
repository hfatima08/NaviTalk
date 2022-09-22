package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import juw.fyp.navitalk.models.Users;

public class CodeActivity extends AppCompatActivity {
EditText code;
Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        code = findViewById(R.id.code);
        submit = findViewById(R.id.button);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = code.getText().toString();
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users");

                ref.orderByChild("code").equalTo(num).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        String id = snapshot.getKey();
                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Users/" +id+"/role");
                        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String roled=dataSnapshot.getValue(String.class);
                               if(roled.equals("Blind User")){
                                   Toast.makeText(CodeActivity.this, "Blind User code:"+num, Toast.LENGTH_SHORT).show();
                                }


                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
//                        code.setText( );
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
                });}
                });

//                ref.orderByChild("code").equalTo(num).addValueEventListener(new ValueEventListener(){
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot){
//                        if(dataSnapshot.exists()){
//                          dataSnapshot
//                            code.setText(dataSnapshot.getChildren().toString());
//                            Toast.makeText(CodeActivity.this,ref.push().toString() , Toast.LENGTH_SHORT).show();
//                        }else{
//                            Toast.makeText(CodeActivity.this, "No Matched", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
            }
 //       });

//}
}