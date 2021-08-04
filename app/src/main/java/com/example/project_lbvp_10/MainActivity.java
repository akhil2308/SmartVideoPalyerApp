package com.example.project_lbvp_10;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity   {
    private static final String TAG = "MainActivity";
    //video
    VideoView videoView;

    // SpeechRecognizer
   /* private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;*/
    private String keeper="";
    private SpeechRecognizerProcesser srp;

    AudioManager audioManager;
    //For looking logs
    ArrayAdapter<String> adapter;
    ArrayList<String> list = new ArrayList<>();

    //CameraSource
    CameraSource cameraSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
        }
        else {
            videoView = findViewById(R.id.videoView);
             audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
           /* speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());*/

            srp = new SpeechRecognizerProcesser(this);


            adapter = new ArrayAdapter<>(this,   android.R.layout.simple_list_item_1, list);
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.a));
            MediaController med=new MediaController(this);
            videoView.setMediaController(med);
            med.setAnchorView(videoView);
            videoView.start();
            createCameraSource();
            videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction())
                    {
                        case  MotionEvent.ACTION_DOWN:
                            Log.i(TAG, "ACTION_DOWN ");
                            srp.startRecording();
                            keeper="";


                            break;
                        case  MotionEvent.ACTION_UP:
                            Log.i(TAG, "ACTION_UP ");
                            srp.stopRecording();


                            break;
                    }
                    return false;
                }
            });
        }
    }


    private class SpeechRecognizerProcesser  {
        private android.speech.SpeechRecognizer speechRecognizer;
        private Intent intentRecognizer;

        SpeechRecognizerProcesser(MainActivity m) {
            intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(m);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                    String string = "";
                    if (matches != null) {
                        keeper = matches.get(0);
                        if(keeper.equals("volume up") || keeper.equals("volume increase") || keeper.equals("increase volume")){
                            //To increase media player volume
                            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                            // Log.i(TAG, "volume increase ");
                            Toast.makeText(MainActivity.this, "volume increase", Toast.LENGTH_LONG).show();
                        }
                        else if(keeper.equals("volume decrease") ||keeper.equals("volume down")  || keeper.equals("decrease volume")){
                            //To decrease media player volume
                            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                            // Log.i(TAG, "volume decrease ");
                            Toast.makeText(MainActivity.this, "volume decrease", Toast.LENGTH_LONG).show();


                        }
                        else if(keeper.equals("rewind") || keeper.equals("backward")){
                            videoView.seekTo(videoView.getCurrentPosition()-10000);
                            Toast.makeText(MainActivity.this, "rewind", Toast.LENGTH_LONG).show();
                        }
                        else if(keeper.equals("forward") ){
                            videoView.seekTo(videoView.getCurrentPosition()+11000);
                            Toast.makeText(MainActivity.this, "forward", Toast.LENGTH_LONG).show();
                        }

                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });

        }

        public void startRecording() {
            speechRecognizer.startListening(intentRecognizer);

        }

        public void stopRecording() {
            speechRecognizer.stopListening();


        }
    }







    //This class will use google vision api to detect eyes
    private class EyesTracker extends Tracker<Face> {
        float THRESHOLD = 0.75f;
        private EyesTracker() {

        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face) {

            if (face.getIsLeftEyeOpenProbability() > THRESHOLD && face.getIsRightEyeOpenProbability() > THRESHOLD) {
                Log.i(TAG, "onUpdate: Eyes Detected");

                //Toast.makeText(MainActivity.this, "Eyes Detected and open, so video continues", Toast.LENGTH_SHORT).show();
                if (!videoView.isPlaying())
                    videoView.start();

            }
            else {
                if (videoView.isPlaying())
                    videoView.pause();
                //Toast.makeText(MainActivity.this, "Eyes Detected and closed, so video paused", Toast.LENGTH_SHORT).show();

            }


        }



        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);

            //Toast.makeText(MainActivity.this, "Face Not Detected yet!, so video paused", Toast.LENGTH_SHORT).show();
            videoView.pause();
        }

        @Override
        public void onDone() {
            super.onDone();
        }
    }


    //FaceTracker
    private class FaceTrackerFactory implements MultiProcessor.Factory<Face> {

        private FaceTrackerFactory() {

        }

        @Override
        public Tracker<Face> create(Face face) {
            return new EyesTracker();
        }
    }


    //CameraSource
    public void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        detector.setProcessor(new MultiProcessor.Builder(new FaceTrackerFactory()).build());

        cameraSource = new CameraSource.Builder(this, detector)
                .setRequestedPreviewSize(1024, 768)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraSource.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraSource != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraSource.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraSource!=null) {
            cameraSource.stop();
            Log.i(TAG, "cameraSource is stoped");

        }
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource!=null) {
            cameraSource.release();
        }
    }
}