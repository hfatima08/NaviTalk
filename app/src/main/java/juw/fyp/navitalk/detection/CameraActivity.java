/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juw.fyp.navitalk.detection;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import juw.fyp.navitalk.Adapter.UserAdapter;
import juw.fyp.navitalk.ConnectingActivity;
import juw.fyp.navitalk.R;
import juw.fyp.navitalk.RoleScreen;
import juw.fyp.navitalk.detection.env.ImageUtils;
import juw.fyp.navitalk.detection.env.Logger;
import juw.fyp.navitalk.models.Users;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class CameraActivity extends AppCompatActivity
    implements OnImageAvailableListener,
        Camera.PreviewCallback{
  private static final Logger LOGGER = new Logger();
  GoogleSignInClient signInClient;
  GoogleSignInOptions signInOptions;
  TextView code,vol;
  Button logout;
  FirebaseAuth auth;
  UserAdapter userAdapter;
  ArrayList<Users> Alist;
  RecyclerView rv;
  RelativeLayout relativeLayout;
  SwipeListener swipeListener;
  public TextToSpeech t1;
  DatabaseReference reference;
  String bcode;
  String uid,volid,obj=null,userName;
  String[] labels = {"person","bicycle","car", "motorcycle","airplane","bus","train","truck", "boat","traffic light", "fire hydrant","stop sign","parking meter", "bench",
          "bird","cat","dog","horse","sheep","cow","elephant", "bear","zebra", "giraffe","backpack","umbrella","handbag","tie","suitcase","frisbee","skis","snowboard","sports ball",
          "kite", "baseball bat","baseball glove","skateboard","surfboard","tennis racket","bottle","wine glass","cup","fork","knife","spoon","bowl" ,"banana","apple",
          "sandwich","orange","broccoli","carrot","hot dog","pizza","donut","cake","chair","couch","potted plant","bed","dining table","toilet","tv","laptop","mouse", "remote",
          "keyboard","cell phone","microwave","oven","toaster","sink","refrigerator","book","clock","vase","scissors","teddy bear","hair drier","toothbrush"};
  Intent intent;

  private static final int PERMISSIONS_REQUEST = 1;
  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  protected int previewWidth = 0;
  protected int previewHeight = 0;
  private boolean debug = false;
  private Handler handler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;
  private Runnable postInferenceCallback;
  private Runnable imageConverter;
  private LinearLayout bottomSheetLayout;
  private LinearLayout gestureLayout;
  private BottomSheetBehavior<LinearLayout> sheetBehavior;
  protected ImageView bottomSheetArrowImageView;
  private Object ArrayIndexOutOfBoundsException;
  SharedPreferences sh;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    LOGGER.d("onCreate " + this);
    super.onCreate(null);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    setContentView(R.layout.tfe_od_activity_camera);

    auth = FirebaseAuth.getInstance();
    uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    signInOptions= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
    signInClient = GoogleSignIn.getClient(this,signInOptions);

    reference = FirebaseDatabase.getInstance().getReference().child("Users");

    if (hasPermission()) {
      setFragment();
    } else {
      requestPermission();
    }

    relativeLayout = findViewById(R.id.layout);
    bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
    gestureLayout = findViewById(R.id.gesture_layout);
    sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
    bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);
    code = findViewById(R.id.code);
    rv = findViewById(R.id.list);
    logout = findViewById(R.id.btn_logout);
    Alist = new ArrayList<>();
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    rv.setLayoutManager(layoutManager);
    userAdapter = new UserAdapter(Alist,this);
    rv.setAdapter(userAdapter);
    vol = findViewById(R.id.vol);

   getCode();
  getVolunteer();

//BottomSheet code
    ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
              gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
              gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
            int height = gestureLayout.getMeasuredHeight();

            sheetBehavior.setPeekHeight(height);
          }
        });
    sheetBehavior.setHideable(false);

    sheetBehavior.setBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
              case BottomSheetBehavior.STATE_HIDDEN:
                break;
              case BottomSheetBehavior.STATE_EXPANDED:
                {
                  bottomSheetArrowImageView.setImageResource(R.drawable.arrow_down);
                }
                break;
              case BottomSheetBehavior.STATE_COLLAPSED:
                {
                  bottomSheetArrowImageView.setImageResource(R.drawable.arrow_up);
                }
                break;
              case BottomSheetBehavior.STATE_DRAGGING:
                break;
              case BottomSheetBehavior.STATE_SETTLING:
                bottomSheetArrowImageView.setImageResource(R.drawable.arrow_up);
                break;
            }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });


    //textToSpeech Code
    swipeListener = new SwipeListener(relativeLayout);

    t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
          t1.setLanguage(Locale.US);
          t1.speak("You are on the Main Screen, swipe left to listen the features and swipe right to say something.",TextToSpeech.QUEUE_ADD, null);
        }
      }
    });

   intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
   intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

   //Logout Button Code
    logout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SignOut();
      }
    });

  }//end of onCreate()

//BackButton Code
  @Override
  public void onBackPressed()
  {
   // finish();
    moveTaskToBack(true);
  }

  //Swipe Gesture Code
  private class SwipeListener implements View.OnTouchListener{
    GestureDetector gestureDetector;

    SwipeListener(View view){
      int threshold= 100;
      int velocity_threshold=100;

      GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDown(MotionEvent e) {
          return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
          float xDiff = e2.getX() - e1.getX();
          float yDiff = e2.getY() - e1.getY();
          try {
            if(Math.abs(xDiff) > Math.abs(yDiff)){
              if(Math.abs(xDiff) > threshold && Math.abs(velocityX) > velocity_threshold){
                if(xDiff>0){
                  //textView.setText("swiped right");
                  t1.speak(" ",TextToSpeech.QUEUE_FLUSH,null);

                  t1.speak("Start speaking",TextToSpeech.QUEUE_ADD, null);
                  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start Speaking");
                      if(intent.resolveActivity(getPackageManager())!=null){
                        startActivityForResult(intent,10);
                        new Handler().postDelayed(new Runnable() {
                          @Override
                          public void run() {
                            finishActivity(10);
                          }
                        },5000);
                      }else{

                        t1.speak("Your device does not support speech input", TextToSpeech.QUEUE_ADD, null);
                      }


                    }
                  }, 1000);

                }
                else{
             //     textView.setText("swiped left");
                  t1.speak("If you want to detect an object say detection. You are assigned an assistance code. The volunteer will need this code to help you through video call. To know your code just say code. Anytime you require visual assistance, say video call. Simply say logout to get logged out from your account.", TextToSpeech.QUEUE_ADD, null);
                  t1.speak("Swipe left to listen again and swipe right to say something.", TextToSpeech.QUEUE_ADD, null);
                }
                return true;
              }
            }

            else{
              if(Math.abs(yDiff) > threshold && Math.abs(velocityY) > velocity_threshold){
                if(yDiff>0){
                  // textView.setText("swiped down");
                  obj=" ";
                  t1.stop();
                }
                else{
                  //  textView.setText("swiped up");
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

  //Responses of Voice Commands
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(resultCode == RESULT_OK && data != null) {
      ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
    switch(requestCode){
        case 10:
          String cmd = result.get(0);
          switch (cmd) {
            case "logout":
              SignOut();
              break;

            case "detection":
              t1.speak("Tell me the object you want to detect when i say start speaking", TextToSpeech.QUEUE_ADD, null);
              new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
              t1.speak("start speaking", TextToSpeech.QUEUE_ADD, null);
                  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                  intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What do you want to detect");
                  if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 30);
                    new Handler().postDelayed(new Runnable() {
                      @Override
                      public void run() {
                        finishActivity(30);
                      }
                    },5000);
                  } else {
                    t1.speak("Your device does not support speech input", TextToSpeech.QUEUE_ADD, null);
                  }
                    }
                  }, 2000);
                }
              }, 3000);
                  break;

            case "video call":
              //Toast.makeText(this, Alist.get(0).getUserName(), Toast.LENGTH_SHORT).show();
              if(!Alist.isEmpty()){
                t1.speak("who do you want to call? Tell me the number for the respective volunteer when i say start speaking", TextToSpeech.QUEUE_ADD, null);
                int i = 1;
              for (Users vol : Alist) {
                t1.speak("say"+ i + "for" + vol.getUserName(), TextToSpeech.QUEUE_ADD, null);
                i++;
              }

                new Handler().postDelayed(new Runnable() {
                  @Override
                  public void run() {
                    t1.speak("start speaking", TextToSpeech.QUEUE_ADD, null);
                    new Handler().postDelayed(new Runnable() {
                      @Override
                      public void run() {
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "tell the number");
                        if (intent.resolveActivity(getPackageManager()) != null) {
                          startActivityForResult(intent, 20);
                        } else {
                          t1.speak("Your device does not support speech input", TextToSpeech.QUEUE_ADD, null);
                        }
                      }
                    }, 3000);
                  }
                }, 7000);
              }else{
                t1.speak("No Volunteer Registered. You have to give your assistance code to your volunteer. To know your code swipe right and say code ", TextToSpeech.QUEUE_ADD, null);
              }

              break;

            case "code":
              t1.speak("Your Assistance code is :", TextToSpeech.QUEUE_ADD, null);
              for(int i=0;i<=bcode.length()-1;i++) {
                t1.speak(String.valueOf(bcode.charAt(i)), TextToSpeech.QUEUE_ADD, null);
              }
              break;

            default:
              t1.speak("Sorry! I didn't understand what you said. Swipe left to listen the features.", TextToSpeech.QUEUE_ADD, null);
          }
    break;

        case 20:
          String cmd2 = result.get(0);
          if(cmd2.equals("one")){
            cmd2="1";
          }if(cmd2.equals("two") || cmd2.equals("to")){
          cmd2="2";
        }
         int num = Integer.parseInt(cmd2);
         num--;
          String id= (String) Alist.get(num).getUserId();
          String name = (String) Alist.get(num).getUserName();
          t1.speak("Calling"+name, TextToSpeech.QUEUE_ADD, null);
          Intent intent = new Intent(getApplicationContext(), ConnectingActivity.class);
          intent.putExtra("vol",id);
          startActivity(intent);
          break;

      case 30:
       obj = result.get(0);
          try{
            for(int i=0; i<=labels.length;i++){
          if(obj.equals(labels[i])){

            t1.speak("detecting "+obj+"if you want to stop detection swipe down", TextToSpeech.QUEUE_ADD, null);
            break;
          } }
          }catch(Exception e){
             t1.speak("I cannot detect a "+obj, TextToSpeech.QUEUE_ADD, null);
          }
          break;


      default:
        t1.speak("Sorry! I didn't understand what you said. Swipe left to listen the features.", TextToSpeech.QUEUE_ADD, null);

      }
    }

    else{
      t1.speak("Sorry, I didn't hear anything", TextToSpeech.QUEUE_ADD, null);
    }
  }



  //Fetch Blind's Assistance Code
  private void getCode() {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users/"+user.getUid()+"/code"+"/0");
    reference.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
         bcode = dataSnapshot.getValue(String.class);
        code.setText("ASSISTANCE CODE: "+ bcode);
      }
      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Toast.makeText(getApplicationContext(), "canceled", Toast.LENGTH_SHORT).show();
      }
    });
  }

  //Fetch Volunteer's Data
  private void getVolunteer() {

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
    reference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Alist.clear();
        for(DataSnapshot dataSnapshot: snapshot.getChildren()){

       Users users = dataSnapshot.getValue(Users.class);
         //  if(users.getRole().equals("Volunteer") && users.getCode().equals(bcode)){
          if(users.getRole().equals("Volunteer") && users.getCode().contains(bcode)){
              vol.setText("VOLUNTEERS");
              Alist.add(users);
          }
         }
        userAdapter.notifyDataSetChanged();
      }


      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });

    if(Alist.isEmpty()){
      vol.setText("No Volunteers Registered!");
    }

 }

  //Logout Code
  private void SignOut() {
//    FirebaseDatabase.getInstance().getReference("Users").child(uid).removeValue();
//        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//          @Override
//          public void onComplete(@NonNull Task<Void> task) {
//            if(task.isSuccessful()){
//            Intent intent = new Intent(getApplicationContext(), RoleScreen.class);
//            finishAffinity();
//            startActivity(intent);}
//          }
//        });

    signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(@NonNull Task<Void> task) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), RoleScreen.class);
        finishAffinity();
        startActivity(intent);
      }
    });
  }

  //TensorFlow Api Detection and Camera Code
  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }

  /** Callback for android.hardware.Camera API */
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
        new Runnable() {
          @Override
          public void run() {
            ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
          }
        };

    postInferenceCallback =
        new Runnable() {
          @Override
          public void run() {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
          }
        };


    processImage(obj);
  }

  /** Callback for Camera2 API */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    // We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
          new Runnable() {
            @Override
            public void run() {
              ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0],
                  yuvBytes[1],
                  yuvBytes[2],
                  previewWidth,
                  previewHeight,
                  yRowStride,
                  uvRowStride,
                  uvPixelStride,
                  rgbBytes);
            }
          };

      postInferenceCallback =
          new Runnable() {
            @Override
            public void run() {
              image.close();
              isProcessingFrame = false;
            }
          };

    // processImage(obj);
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();

  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);
    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;

    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    LOGGER.d("onDestroy " + this);
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @Override
  public void onRequestPermissionsResult(
          final int requestCode, final String[] permissions, final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSIONS_REQUEST) {
      if (allPermissionsGranted(grantResults)) {
        setFragment();
      } else {
        requestPermission();
      }
    }
  }

  private static boolean allPermissionsGranted(final int[] grantResults) {
    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
        Toast.makeText(
               CameraActivity.this,
                "Camera permission is required for this demo",
                Toast.LENGTH_LONG)
            .show();
      }
      requestPermissions(new String[] {PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  private boolean isHardwareLevelSupported(
          CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  private String chooseCamera() {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        final StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
          continue;
        }

        // Fallback to camera1 API for internal cameras that don't have full support.
        // This should help with legacy situations where using the camera2 API causes
        // distorted or otherwise broken previews.
        useCamera2API =
            (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                || isHardwareLevelSupported(
                    characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
        LOGGER.i("Camera API lv2?: %s", useCamera2API);
        return cameraId;
      }
    } catch (CameraAccessException e) {
      LOGGER.e(e, "Not allowed to access camera");
    }

    return null;
  }

  protected void setFragment() {
    String cameraId = chooseCamera();

    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
          CameraConnectionFragment.newInstance(
              new CameraConnectionFragment.ConnectionCallback() {
                @Override
                public void onPreviewSizeChosen(final Size size, final int rotation) {
                  previewHeight = size.getHeight();
                  previewWidth = size.getWidth();
                  juw.fyp.navitalk.detection.CameraActivity.this.onPreviewSizeChosen(size, rotation);
                }
              },
              this,
              getLayoutId(),
              getDesiredPreviewFrameSize());

      camera2Fragment.setCamera(cameraId);
      fragment = camera2Fragment;
    } else {
      fragment =
          new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
    }

    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
  }

  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getScreenOrientation() {
    switch (getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }


  protected abstract void processImage(String obj);

  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

  protected abstract int getLayoutId();

  protected abstract Size getDesiredPreviewFrameSize();


}
