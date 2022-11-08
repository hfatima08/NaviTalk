package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import juw.fyp.navitalk.detection.DetectorActivity;
import juw.fyp.navitalk.models.Users;

public class LoginScreen extends AppCompatActivity {
    ImageView img;
    Button login,submit;
    EditText name;
    TextInputEditText code;
    GoogleSignInClient signInClient;
    GoogleSignInOptions signInOptions;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseUser user;
    String str,userName="",cmd="";
    Users users = new Users();
    AlertDialog dialog;
    LinearLayout linearLayout;
    SwipeListener swipeListener;
    TextToSpeech t1;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        //id assigned
        img = findViewById(R.id.img);
        login = findViewById(R.id.btn_login);
        linearLayout = findViewById(R.id.linearlayout);
        name = findViewById(R.id.usernameTF);


        //Login onclick method call
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });
                //firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //google signin
        signInOptions= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this,signInOptions);

        //start image animation
        startAnimation();

        //swipe gesture
        swipeListener = new SwipeListener(linearLayout);

        //voice integration
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                    t1.speak("You are on the login screen, You can only login with your g-mail account. Swipe up for available accounts. ", TextToSpeech.QUEUE_FLUSH, null);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            t1.speak("swipe left to listen again", TextToSpeech.QUEUE_ADD,null, null);
                        }
                    }, 1000);
                }
            }
        });

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);



    }//end oncreate


   // Swipe Gesture code
    private class SwipeListener implements View.OnTouchListener {
        GestureDetector gestureDetector;

        SwipeListener(View view) {
            int threshold = 100;
            int velocity_threshold = 100;

            GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    float xDiff = e2.getX() - e1.getX();
                    float yDiff = e2.getY() - e1.getY();
                    try {
                        if (Math.abs(xDiff) > Math.abs(yDiff)) {
                            if (Math.abs(xDiff) > threshold && Math.abs(velocityX) > velocity_threshold) {
                                if (xDiff > 0) {
                                    //textView.setText("swiped right");
                                    } else {
                                    //     textView.setText("swiped left");
                                    t1.speak("You are on the login screen, You can only login with your g-mail account. Swipe up for available accounts.", TextToSpeech.QUEUE_ADD, null);
                                }
                                return true;
                            }
                        }

                        else{
                            if(Math.abs(yDiff) > threshold && Math.abs(velocityY) > velocity_threshold){
                                if(yDiff>0){
                                   // textView.setText("swiped down");
                                }
                                else{
                                  //  textView.setText("swiped up");
                                 SignIn();
                                }
                                return true;
                            }
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return false;
                }
            };

            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

    }

//   // Google SignIn Start
//    @Override
//    public void onClick(View view) {
//        SignIn();
//    }



    private void SignIn() {
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent,100);
  //      t1.speak(signInOptions.getExtensions().toString(), TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account= task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                Toast.makeText(this, "Google SignIn Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            user = auth.getCurrentUser();

                            users.setUserId(user.getUid());
                            users.setUserName(user.getDisplayName());
                            users.setMail(user.getEmail());
                            Intent intent = getIntent();
                            str = intent.getStringExtra("Role");
                            AddUser();

                        } else {
                            Toast.makeText(LoginScreen.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void AddUser(){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Users/" + user.getUid())) {
                        OpenActivity();
                } else {
                    addRole(str);
                    if(str.equals("volunteer")){
                        getBlindCode();
                    }else{
                    database.getReference().child("Users").child(user.getUid()).setValue(users);
                    OpenActivity();}
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addRole(String val){
            if( val.equals("volunteer")){
                 users.setRole("Volunteer");
             }else{
                users.setRole("Blind User");
                Long code = getRandom();
            //    users.setCode(code);
            }
    }


    private void VolunteerActivity() {
        finish();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void BlindActivity() {
        finish();
        Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }

    //Google Signin End

    //random number for blind assistance code
    public Long getRandom() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999 );

        // this will convert any number sequence into 6 character.
        return Long.parseLong(String.format("%06d", number));
    }

//Signout if user is already logged-in as a different role
    private void SignOut(){
        signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),RoleScreen.class);
                finishAffinity();
                startActivity(intent);

            }
        });
    }

    private void OpenActivity(){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Users/" + user.getUid()+"/role");
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String role = dataSnapshot.getValue().toString();
                if( str.equals("volunteer") && role.equals("Volunteer")){
                  VolunteerActivity();
                }else if(str.equals("blind") && role.equals("Blind User")){
                    BlindActivity();
                }else{
                    Toast.makeText(LoginScreen.this, "This User is already registered as a "+str+"!", Toast.LENGTH_SHORT).show();
                     SignOut();

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getBlindCode(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Assistance Code");
        View view = getLayoutInflater().inflate(R.layout.custom_dialog,null);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        code = view.findViewById(R.id.code);
        submit = view.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long num = Long.parseLong(code.getText().toString());
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users");
                ref.orderByChild("code").equalTo(num).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                                Boolean role = dataSnapshot.getValue().toString().contains("Blind User");
                                 if (role.equals(true)) {
                                       //     users.setCode(num);
                                     Toast.makeText(LoginScreen.this, users.getUserName(), Toast.LENGTH_SHORT).show();
                                     database.getReference().child("Users").child(user.getUid()).setValue(users);
                                           OpenActivity();
                                        }
                                }
                        else {
                            Toast.makeText(getApplicationContext(), "ERROR: Invalid Code! ", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

   }

    //Image Animation
    private void startAnimation() {
        Animation zoomin =new TranslateAnimation(1, 1, 0, -50);
        zoomin.setDuration(1000);
        zoomin.setFillEnabled(true);
        zoomin.setFillAfter(true);

        Animation zoomout =  new TranslateAnimation(1, 1, -50, 0);
        zoomout.setDuration(1000);
        zoomout.setFillEnabled(true);
        zoomout.setFillAfter(true);

        img.startAnimation(zoomin);

        zoomin.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                img.startAnimation(zoomout);
            }
        });

        zoomout.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation arg0) {

                img.startAnimation(zoomin);


            }

        });
    }

    //Stop voice when the activity is paused
    public void onPause(){
        if(t1 !=null){
            t1.stop();
          //  t1.shutdown();
        }
        super.onPause();
    }

}//end class