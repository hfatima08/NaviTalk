package juw.fyp.navitalk;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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
import com.google.firebase.database.FirebaseDatabase;


public class SettingsFragment extends Fragment {

    Button logout;
    GoogleSignInClient signInClient;
    GoogleSignInOptions signInOptions;
    FirebaseAuth auth;
    String uid;
    TextView profile,fb,insta;

    // Fragment object
    private EditProfileFragment editProfile = new EditProfileFragment();

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_settings, container, false);

        profile = view.findViewById(R.id.EditProfile);
        logout = view.findViewById(R.id.btn_logout);
        fb = view.findViewById(R.id.fb);
        insta = view.findViewById(R.id.insta);

        // Firebase instance
        auth = FirebaseAuth.getInstance();

        //Google signin
        signInOptions= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        signInClient = GoogleSignIn.getClient(getContext(),signInOptions);

        // Underline Edit Profile text
        SpannableString content = new SpannableString(profile.getText());
        content.setSpan(new UnderlineSpan(), 0, profile.getText().length(), 0);
        profile.setText(content);

        // Open Facebook Page on logo click
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appLink="fb://page/237564710351658";
                String webLink="https://www.facebook.com/NaviTalk-101457775805953/";
                String appPackage="com.facebook.katana ";

                openLink(appLink,appPackage,webLink);

            }
        });

        // Open Instagram Page on logo click
        insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appLink="https://instagram.com/navi_talk?igshid=YmMyMTA2M2Y=";
                String appPackage="com.instagram.android ";

                openLink(appLink,appPackage,appLink);

            }
        });

        // Logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
            }
        });

        // Open Edit profile fragment on click
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction= getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_frame, editProfile);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        return view;
    } // end of onCreate

    // Open social pages link function
    private void openLink(String appLink, String appPackage, String webLink) {
        try{
            Uri uri = Uri.parse(appLink);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setPackage(appPackage);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException){
            Uri uri = Uri.parse(webLink);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    // Logout / Signout Function
    private void SignOut() {
        signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseAuth.getInstance().getCurrentUser().delete();
                FirebaseDatabase.getInstance().getReference("Users").child(uid).removeValue();
                Intent intent = new Intent(getActivity(),RoleScreen.class);
                getActivity().finishAffinity();
                startActivity(intent);
            }
        });
    }

}