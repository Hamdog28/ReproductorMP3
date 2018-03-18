package com.example.anthony_pc.reproductormpp3;

import android.graphics.Canvas;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    private StringBuilder text = new StringBuilder();
    final String[] canciones = {"Queen - Bohemian Rhapsody","Neon Trees - Animal",
            "Lana de Rey - Young and Beautiful","Bruno Mars - Just the way you are"};
    final int[] resID = {R.raw.bohemian_rhapsody, R.raw.animal, R.raw.young_and_beautiful, R.raw.just_the_way_you_are};
    final String[] letras ={"bohemian_letra","animal_letra","beautiful_letra","just_letra"};

    Button play,next,previous;
    Animation animation;
    TextView lyric;
    MediaPlayer mediaPlayer;
    SeekBar progreso;
    ListView listCanciones;
    ScrollView myScroll;
    Handler handler;
    Runnable runnable;
    int numeroCancion = 0;
    int duracionCancion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface font = Typeface.createFromAsset( getAssets(), "fontawesome-webfont.ttf" );


        mediaPlayer = MediaPlayer.create(this, resID[0]);
        duracionCancion = mediaPlayer.getDuration();

        myScroll = (ScrollView) findViewById(R.id.scroll);
        progreso = findViewById(R.id.seekBar);
        progreso.setMax(mediaPlayer.getDuration());
        listCanciones = findViewById(R.id.ListCanciones);
        handler = new Handler();
        progresoCancion();



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, canciones);
        listCanciones.setAdapter(adapter);

        listCanciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                numeroCancion = i;
                cambiarCancion();
                progreso.setMax(mediaPlayer.getDuration());
                progresoCancion();
                lyric.clearAnimation();
                ajustarScroll();
            }
        });

        listCanciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                numeroCancion = i;
                cambiarCancion();
            }
        });

        progreso.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mediaPlayer.seekTo(i);
                    lyric.clearAnimation();
                    ajustarScroll();
                    if(mediaPlayer.isPlaying())
                        lyric.startAnimation(animation);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        myScroll.setOnTouchListener( new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });



        lyric = findViewById(R.id.letra);



        animation = AnimationUtils.loadAnimation(this, R.anim.animationfile);
        animation.setDuration(mediaPlayer.getDuration());
        setLetra();

        next = (Button)findViewById( R.id.next);
        next.setTypeface(font);

        play = (Button)findViewById( R.id.play );
        play.setTypeface(font);

        previous = (Button)findViewById( R.id.previous);
        previous.setTypeface(font);



    }
    public void progresoCancion(){
        progreso.setProgress(mediaPlayer.getCurrentPosition());

        if(mediaPlayer.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    progresoCancion();
                }
            };
            handler.postDelayed(runnable,1000);
        }
    }



    public void clickPlay(View view){
        //ajustarScroll(500);
        if(mediaPlayer.isPlaying()){
            Log.e("playing","playing");
            lyric.clearAnimation();
            mediaPlayer.pause();
            play.setText(String.valueOf((char) 0xf04b));

            ajustarScroll();

        }else{
            Log.e("playing","stop");

            play.setText(String.valueOf((char) 0xf04c));
            lyric.startAnimation(animation);

            //progreso.setMax(mediaPlayer.);
            mediaPlayer.start();

            animation.start();
            progresoCancion();

        }
    }

    public void clickNext(View view){
        if(numeroCancion == resID.length-1) {
            numeroCancion = 0;
        }else{
            numeroCancion++;

        }
        cambiarCancion();
    }
    public void clickPrevious(View view){
        if(numeroCancion == 0) {
            numeroCancion = resID.length-1;

        }else{
            numeroCancion--;

        }
        cambiarCancion();
    }

    public void ajustarScroll(){
        final int porcentaje = (mediaPlayer.getCurrentPosition()*100)/duracionCancion;
        final int largo = myScroll.getChildAt(0).getHeight();
        int x=0;
        if(mediaPlayer.isPlaying())
            x=200;

        final int i = x;

        myScroll.post(new Runnable() {
            @Override
            public void run() {
                myScroll.scrollTo(0, (((largo*porcentaje) /100)+40-i));
            }
        });
    }
    public void setLetra(){

        BufferedReader reader = null;
        text = new StringBuilder();

        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(letras[numeroCancion]+".txt")));

            String mLine;
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"Error reading file!",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        lyric.setText((CharSequence) text);
    }
    public void cambiarCancion(){

        mediaPlayer.stop();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), resID[numeroCancion]);

        progreso.setMax(mediaPlayer.getDuration());

        setLetra();
        mediaPlayer.start();
        progresoCancion();
        lyric.clearAnimation();
        ajustarScroll();
        lyric.startAnimation(animation);
        animation.setDuration(mediaPlayer.getDuration()+40);
        play.setText(String.valueOf((char) 0xf04c));
    }
}
