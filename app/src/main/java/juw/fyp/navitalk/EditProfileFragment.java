package juw.fyp.navitalk;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
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

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import juw.fyp.navitalk.Adapter.UserAdapter;
import juw.fyp.navitalk.Adapter.codeAdapter;
import juw.fyp.navitalk.models.Users;

public class EditProfileFragment extends Fragment {
    private CodeListFragment codeListFragment = new CodeListFragment();
    String userId,userEmail,userName=null;
    List<String> code;
    String Bcode;
    ListView lv;
    TextInputEditText email,name,Acode;
    Button update,viewList;
    FirebaseDatabase database;
    Users users = new Users();
    int i=1;
    ArrayList<String> codeList;


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
        viewList = view.findViewById(R.id.viewList);
        // lv = view.findViewById(R.id.clist);

        codeList = new ArrayList<String>();

        //get user details and fill text fields
        userId =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String role = users.getRole();

        getUserData();



        viewList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction= getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_frame, codeListFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        //Update data
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = name.getText().toString();
                Bcode = Acode.getText().toString();

                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("Users/" + userId)) {
                            if (!newName.isEmpty()) {
                                if(Bcode.length() ==6 ){
                                    if (!codeList.contains(Bcode)) {
                                        uploadCodeList();
                                        final HashMap<String, Object> userName = new HashMap<>();
                                        userName.put("userName", newName);
                                        rootRef.child("Users").child(userId).updateChildren(userName);

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

                                    }else{
                                        Toast.makeText(getContext(), "ERROR: Code Already Exists!", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Toast.makeText(getContext(), "ERROR: Code Should Consists of 6 Digits!", Toast.LENGTH_SHORT).show();
                                }

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



    private void uploadCodeList() {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Users").orderByChild("role").equalTo("Blind User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean Acode = dataSnapshot.getValue().toString().contains(String.valueOf(Bcode));
                    if (Acode.equals(true)) {
                        codeList.add(Bcode.toString());
                        rootRef.child("Users").child(userId).child("code").setValue(codeList).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "ERROR: Profile Didn't Update!", Toast.LENGTH_SHORT).show();
                                }
                            }});
                    } else {
                        Toast.makeText(getContext(), "ERROR: Invalid Code! ", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getContext(), "ERROR: Doesn't Exists ", Toast.LENGTH_SHORT).show();
                }}
            @Override
            public void onCancelled (@NonNull DatabaseError error){
            }

        });

    }

    private void getUserData() {
        email.setText(userEmail);
        DatabaseReference reference = database.getReference("Users/"+userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                userName = users.getUserName();
                //      code = users.getCode();
                name.setText(userName);
//                    codeList.add(code.toString());

                //       Acode.setText(code.toString());

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("code").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    codeList.clear();

                    for(DataSnapshot ds:snapshot.getChildren()){
                        String codes = ds.getValue(String.class);
                        codeList.add(codes);
                    }
                    FirebaseDatabase.getInstance().getReference("Users").child(userId).child("code").setValue(codeList);


                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}