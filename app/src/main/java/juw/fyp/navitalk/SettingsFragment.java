package juw.fyp.navitalk;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {
    private EditProfileFragment editProfile = new EditProfileFragment();
    Button logout;
    GoogleSignInClient signInClient;
    GoogleSignInOptions signInOptions;
    FirebaseAuth auth;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_settings, container, false);

        TextView textView= (TextView) view.findViewById(R.id.EditProfile);
        logout = view.findViewById(R.id.btn_logout);

        auth = FirebaseAuth.getInstance();

        signInOptions= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        signInClient = GoogleSignIn.getClient(getContext(),signInOptions);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction= getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_frame, editProfile);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        return view;
    }

    private void SignOut() {
        signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(),RoleScreen.class);
                getActivity().finishAffinity();
                startActivity(intent);
            }
        });
    }
}