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
import com.cazaea.sweetalert.SweetAlertDialog;
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
    String Bcode;
    TextInputEditText email,name,Acode;
    Button update,viewList;
    FirebaseDatabase database;
    ArrayList<String> codeList;
    SweetAlertDialog errorDialog,successDialog;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Firebase Instance
        database = FirebaseDatabase.getInstance();

        // Layout Id's
        email = view.findViewById(R.id.emailTF);
        name = view.findViewById(R.id.usernameTF);
        Acode = view.findViewById(R.id.codeTF);
        update = view.findViewById(R.id.update);
        viewList = view.findViewById(R.id.viewList);

        // Code List Initialization
        codeList = new ArrayList<String>();

        // Get user details and fill text fields
        userId =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        getUserData();

        // Open Code List Activity on button click
        viewList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction= getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_frame, codeListFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Update data
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
                                final HashMap<String, Object> userName = new HashMap<>();
                                userName.put("userName", newName);
                                if(Bcode.length() == 6){
                                    if (!codeList.contains(Bcode)) {
                                        uploadCodeList();
                                        rootRef.child("Users").child(userId).updateChildren(userName);
                                    } else{
                                        errorDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                                        errorDialog.setTitleText("Error!");
                                        errorDialog.setContentText("Code already exists!");
                                        errorDialog.setConfirmText("OK" );
                                        errorDialog.showConfirmButton(true);
                                        errorDialog.show();

                                        Button btn = errorDialog.findViewById(R.id.confirm_button);
                                        btn.setPadding(10,10,10,10);
                                    }
                                } else if(Bcode.length() == 0){
                                    rootRef.child("Users").child(userId).updateChildren(userName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                successDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE);
                                                successDialog.setTitleText("Success!");
                                                successDialog.setContentText("Your profile has been updated!");
                                                successDialog.setConfirmText("OK" );
                                                successDialog.showConfirmButton(true);
                                                successDialog.show();

                                                Button btn = successDialog.findViewById(R.id.confirm_button);
                                                btn.setPadding(10,10,10,10);

                                            } else {
                                                errorDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                                                errorDialog.setTitleText("Error!");
                                                errorDialog.setContentText("Profile has not been updated!");
                                                errorDialog.setConfirmText("OK" );
                                                errorDialog.showConfirmButton(true);
                                                errorDialog.show();

                                                Button btn = errorDialog.findViewById(R.id.confirm_button);
                                                btn.setPadding(10,10,10,10);
                                            }
                                        }});
                                }else{
                                    errorDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                                    errorDialog.setTitleText("Error!");
                                    errorDialog.setContentText("Code should consists of 6 digits!");
                                    errorDialog.setConfirmText("OK" );
                                    errorDialog.showConfirmButton(true);
                                    errorDialog.show();

                                    Button btn = errorDialog.findViewById(R.id.confirm_button);
                                    btn.setPadding(10,10,10,10);

                                }
                            }
                            else{
                                errorDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                                errorDialog.setTitleText("Error!");
                                errorDialog.setContentText("Username cannot be empty!");
                                errorDialog.setConfirmText("OK" );
                                errorDialog.showConfirmButton(true);
                                errorDialog.show();

                                Button btn = errorDialog.findViewById(R.id.confirm_button);
                                btn.setPadding(10,10,10,10);
                            }
                        }}
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        // end of update data code

        return view;

    } // end onCreate view


    // Upload code list
    private void uploadCodeList() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Users").orderByChild("role").equalTo("Blind User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean Acode = dataSnapshot.getValue().toString().contains(String.valueOf(Bcode));
                    if (Acode.equals(true)) {
                        codeList.add(Bcode);
                        rootRef.child("Users").child(userId).child("code").setValue(codeList).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    successDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE);
                                    successDialog.setTitleText("Success!");
                                    successDialog.setContentText("Your profile has been updated!");
                                    successDialog.setConfirmText("OK" );
                                    successDialog.showConfirmButton(true);
                                    successDialog.show();

                                    Button btn = successDialog.findViewById(R.id.confirm_button);
                                    btn.setPadding(10,10,10,10);

                                } else {
                                    errorDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                                    errorDialog.setTitleText("Error!");
                                    errorDialog.setContentText("Profile has not been updated!");
                                    errorDialog.setConfirmText("OK" );
                                    errorDialog.showConfirmButton(true);
                                    errorDialog.show();

                                    Button btn = errorDialog.findViewById(R.id.confirm_button);
                                    btn.setPadding(10,10,10,10);

                                }
                            }});
                    } else {
                        errorDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                        errorDialog.setTitleText("Error!");
                        errorDialog.setContentText("You entered an invalid assistance code!");
                        errorDialog.setConfirmText("OK" );
                        errorDialog.showConfirmButton(true);
                        errorDialog.show();

                        Button btn = errorDialog.findViewById(R.id.confirm_button);
                        btn.setPadding(10,10,10,10);

                    }
                }else {
                    errorDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                    errorDialog.setTitleText("Error!");
                    errorDialog.setContentText("User doesn't Exists");
                    errorDialog.setConfirmText("OK" );
                    errorDialog.showConfirmButton(true);
                    errorDialog.show();

                    Button btn = errorDialog.findViewById(R.id.confirm_button);
                    btn.setPadding(10,10,10,10);

                }}
            @Override
            public void onCancelled (@NonNull DatabaseError error){
            }

        });
    } // end of upload code list code

    //get user data function
    private void getUserData() {
        email.setText(userEmail);
        DatabaseReference reference = database.getReference("Users/"+userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                userName = users.getUserName();
                name.setText(userName);
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

    } // end of get user data data
}