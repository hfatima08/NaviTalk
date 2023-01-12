package juw.fyp.navitalk.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import juw.fyp.navitalk.R;
import juw.fyp.navitalk.models.Users;


public class codeAdapter extends RecyclerView.Adapter<codeAdapter.ViewHolder> {

    ArrayList<String> arrayList;
    Context context;
    String username,userId;

    private OnItemClickListener listener;

    DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users");

    // Constructor
    public codeAdapter(ArrayList<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    // Inflate row layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.codes_list,parent,false);
        userId =  FirebaseAuth.getInstance().getCurrentUser().getUid();

        return new ViewHolder(view,listener);
    }

    // get blind user's name and code
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String Acode = arrayList.get(position);
       holder.code.setText(Acode);

        ref.orderByChild("role").equalTo("Blind User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        Users users = snapshot.getValue(Users.class);
                        List<String> coded = users.getCode();
                        for(int i=0;i<=coded.size();i++){
                            if(coded.get(i).equals(Acode)){
                                username = users.getUserName();
                                break;
                            }
                            break;
                        }
                        holder.name.setText(username);

                    }}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public interface OnItemClickListener{
        void onItemClick(int position,String cd);
    }

    public void setOnItemClickListener(OnItemClickListener clickListener){
        listener = clickListener;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name,code;
        Button delete;

        public ViewHolder(@NonNull View itemView,OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            code = itemView.findViewById(R.id.code);
            delete = itemView.findViewById(R.id.delete);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cd = code.getText().toString();

                    // on Item click Listener to delete the code
                    listener.onItemClick(getAdapterPosition(), cd);
                }
            });
        }
    }}

