 package juw.fyp.navitalk;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import juw.fyp.navitalk.models.Users;

public class EditProfileFragment extends Fragment {
    String userId,userEmail,userName;
    Long code;
    TextInputEditText email,name,Acode;
    Button update;
    FirebaseDatabase database;
    Users users = new Users();
    int i=1;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_edit_profile, container, false);

        database = FirebaseDatabase.getInstance();

        //layout Id's
        email = view.findViewById(R.id.emailTF);
        name = view.findViewById(R.id.usernameTF);
        Acode = view.findViewById(R.id.codeTF);
        update = view.findViewById(R.id.update);

        //get user details and fill text fields
        userId =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String role = users.getRole();

        getUserData();

        //Update data
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = name.getText().toString();
                Long Bcode = Long.parseLong(Acode.getText().toString());

                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("Users/" + userId)) {
                            if (!newName.isEmpty()) {
                                final HashMap<String, Object> userName = new HashMap<>();
                                userName.put("userName", newName);
                                rootRef.child("Users").child(userId).updateChildren(userName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "ERROR: Profile Didn't Update!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                //--------- update Assistance code -----------

//                                rootRef.child("Users").orderByChild("code").equalTo(Bcode).addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        if (dataSnapshot.exists()) {
//                                            Boolean role = dataSnapshot.getValue().toString().contains("Blind User");
//                                            if (role.equals(true)) {
//                                                final HashMap<String, Object> codes = new HashMap<>();
//                                                codes.put("code"+i, Bcode);
//                                                rootRef.child("Users").child(userId).child("code").addChildEventListener(new ChildEventListener() {
//                                                    @Override
//                                                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                                                        Toast.makeText(getContext(), "code added", Toast.LENGTH_SHORT).show();
//                                                    }
//
//                                                    @Override
//                                                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                                                    }
//
//                                                    @Override
//                                                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//                                                    }
//
//                                                    @Override
//                                                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                                    }
//                                                });
//                                                i++;
//                                              //  users.setCode(Bcode);
//                                              //  rootRef.child("Users").child(userId).updateChildren(code);
//                                          //   rootRef.child("Users").child(userId).child("code").setValue(Bcode);
//
//                                            }
//                                        }
//                                        else {
//                                            Toast.makeText(getContext(), "ERROR: Invalid Code! ", Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError error) {
//                                    }
//                                });

                            }
                        else{
                            Toast.makeText(getContext(), "ERROR: User Name cannot be Empty!", Toast.LENGTH_SHORT).show();
                        }
                        }}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        return view;
}

    private void getUserData() {
        email.setText(userEmail);
        DatabaseReference reference = database.getReference("Users/"+userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                userName = users.getUserName();
               // code = users.getCode();
                name.setText(userName);
         //       Acode.setText(code.toString());

                }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}