package juw.fyp.navitalk;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        userId =  FirebaseAuth.getInstance().getCurrentUser().getUid();

        displayCodeList();


        //Settingup Codes Listview

        displayList = new ArrayList<>();
//        adapter = new ArrayAdapter<String>(getActivity(),R.layout.codes_list,displayList);
//        lv.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        adapter = new codeAdapter(displayList,getActivity());
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(new codeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position,String cd) {
                displayList.remove(position);
                FirebaseDatabase.getInstance().getReference("Users").child(userId).child("code").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = dataSnapshot.getKey();
                        String dataKeys="";
                        int counter=0;
                        for (DataSnapshot child: dataSnapshot.getChildren()){
                            //Object object = child.getKey();
                            if(counter==position){
                                dataKeys=child.getKey();
                                break;
                            }
                            counter++;
                        }

                        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("code").child(dataKeys).removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                adapter.notifyItemRemoved(position);

            }
        });

        return view;
    }

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