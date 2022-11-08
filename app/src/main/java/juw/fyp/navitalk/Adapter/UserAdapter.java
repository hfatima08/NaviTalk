package juw.fyp.navitalk.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import juw.fyp.navitalk.ConnectingActivity;
import juw.fyp.navitalk.R;
import juw.fyp.navitalk.detection.CameraActivity;
import juw.fyp.navitalk.models.Users;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

  ArrayList<Users> arrayList;
  Context context;
  String id;

    public UserAdapter(ArrayList<Users> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = arrayList.get(position);
        holder.name.setText(user.getUserName());
        holder.num.setText(user.getMail());

        id=user.getUserId();

    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name,num;
        Button call;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            num=itemView.findViewById(R.id.number);
            call=itemView.findViewById(R.id.video);

            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     Intent intent = new Intent(context, ConnectingActivity.class);
                     String vol= (String) name.getText();
                        intent.putExtra("vol",id);
                        context.startActivity(intent);
                }
            });
        }
    }
}
