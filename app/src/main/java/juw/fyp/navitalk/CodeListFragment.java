package juw.fyp.navitalk;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cazaea.sweetalert.SweetAlertDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import juw.fyp.navitalk.Adapter.codeAdapter;

public class CodeListFragment extends Fragment {

    String userId;
    ArrayList<String> displayList;
    codeAdapter adapter;
    RecyclerView rv;

    public CodeListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_code_list, container, false);
        rv = view.findViewById(R.id.codeList);

        // Get  current user ID from firebase database
        userId =  FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Function to display volunteer's assistance code list
        displayCodeList();

        //Setting up Codes Listview
        displayList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        adapter = new codeAdapter(displayList,getActivity());
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(new codeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position,String cd) {
                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE);
                dialog.setTitleText("Are you sure?");
                dialog.setContentText("Once deleted, you would have to add the code again!");
                dialog.setConfirmText("Yes,delete it!");
                dialog.setCancelText("No, cancel!");
                dialog.showCancelButton(true);
                dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        // reuse previous dialog instance, keep widget user state, reset them if you need
                        sDialog.setTitleText("Cancelled!")
                                .setContentText("Code has not been deleted!")
                                .setConfirmText("OK")
                                .showCancelButton(false)
                                .setCancelClickListener(null)
                                .setConfirmClickListener(null)
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    }
                });
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {

                        displayList.remove(position);
                        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("code").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String dataKeys = "";
                                int counter = 0;
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (counter == position) {
                                        dataKeys = child.getKey();
                                        break;
                                    }
                                    counter++;
                                }
                                FirebaseDatabase.getInstance().getReference("Users").child(userId).child("code").child(dataKeys).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sDialog.setTitleText("Deleted!")
                                                .setContentText("Code has been deleted!")
                                                .setConfirmText("OK")
                                                .showCancelButton(false)
                                                .setConfirmClickListener(null)
                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        adapter.notifyItemRemoved(position);
                    }
                });

                dialog.show();

                Button btn = dialog.findViewById(R.id.confirm_button);
                Button btn2 = dialog.findViewById(R.id.cancel_button);
                btn.setPadding(10, 10, 10, 10);
                btn2.setPadding(10, 10, 10, 10);
            }
        });

        return view;

    }// end of onCreate view


    // Display volunteer's Assistance code list function
    private void displayCodeList() {
        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("code").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    displayList.clear();
                    for(DataSnapshot ds:snapshot.getChildren()){
                        String codes = ds.getValue(String.class);
                        displayList.add(codes);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}