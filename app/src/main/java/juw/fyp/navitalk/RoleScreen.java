package juw.fyp.navitalk;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Locale;

public class RoleScreen extends AppCompatActivity implements View.OnClickListener {
    ImageView img;
    Button vol, blind;
    LinearLayout linearLayout;
    SwipeListener swipeListener;
    TextToSpeech t1;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_screen);

        img = findViewById(R.id.img);
        vol = findViewById(R.id.btn_vol);
        blind = findViewById(R.id.btn_blind);
        linearLayout = findViewById(R.id.linearlayout);


        vol.setOnClickListener(this);
        blind.setOnClickListener(this);




        startAnimation();

        swipeListener = new SwipeListener(linearLayout);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                    t1.speak("You are on the role screen, If you want to register as a blind person swipe right.", TextToSpeech.QUEUE_FLUSH, null);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            t1.speak("swipe left to listen again", TextToSpeech.QUEUE_ADD, null);
                        }
                    }, 1000);
                }
            }
        });

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

    }//end of onCreate()

    //BackButton Code
    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

    //stop voice when activity is paused
    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    //Assigning role on button click
    @Override
    public void onClick(View view) {
        Intent intent1 = new Intent(getApplicationContext(), LoginScreen.class);
        if (view == vol) {
            intent1.putExtra("Role", "volunteer");
        } else {
            intent1.putExtra("Role", "blind");
        }
        startActivity(intent1);
    }

    //Swipe Gesture Code
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
                                    Intent intent1 = new Intent(getApplicationContext(), LoginScreen.class);
                                    intent1.putExtra("Role", "blind");
                                    startActivity(intent1);
                                } else {
                                    //     textView.setText("swiped left");
                                    t1.speak("If you want to register as a blind person swipe right", TextToSpeech.QUEUE_FLUSH, null);

                                }
                                return true;
                            }
                        }

                    } catch (Exception e) {
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


    }