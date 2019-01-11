package com.feed.owner.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    RelativeLayout guessLayout;
    Button startButton;
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    ImageView celebImage;
    TextView scoreTextView;

    ArrayList<String> celebNames = new ArrayList<String>();
    ArrayList<String> celebURLs = new ArrayList<String>();
    int chosenCeleb = 0;

    int locationOfCorrectAnswer = 0;
    int incorrectAnswer = 0;
    String[] answers = new String[4];

    int numberOfQuestions;
    int score = 0;

    DownloadTask task;
    DownloadImage imageTask;

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data != -1){
                    char current = (char) data;

                    result += current;

                    data = reader.read();
                }

                return result;
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void startGame(View view){

        startButton.setVisibility(View.INVISIBLE);
        guessLayout.setVisibility(View.VISIBLE);

    }

    public void generateQuestion(){

        Bitmap myImage;

        Random random = new Random();
        chosenCeleb = random.nextInt(celebURLs.size());

        imageTask = new DownloadImage();

        try {
            myImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();
            celebImage.setImageBitmap(myImage);

            locationOfCorrectAnswer = random.nextInt(4);

            for (int i = 0; i < 4; i++){
                if (i == locationOfCorrectAnswer){
                    answers[i] = celebNames.get(chosenCeleb);
                }else {
                    incorrectAnswer = random.nextInt(celebURLs.size());

                    while (incorrectAnswer == chosenCeleb) {
                        incorrectAnswer = random.nextInt(celebURLs.size());
                    }
                    answers[i] = celebNames.get(incorrectAnswer);
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void guessClick(View view){

        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            score++;
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(), "Wrong! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_LONG).show();
        }

        numberOfQuestions++;
        scoreTextView.setText(Integer.toString(score) + "/" + Integer.toString(numberOfQuestions));
        generateQuestion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.startButton);
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        guessLayout = (RelativeLayout)findViewById(R.id.guessLayout);
        celebImage = (ImageView) findViewById(R.id.celebImage);
        scoreTextView = (TextView) findViewById(R.id.scoreTextView);

        task = new DownloadTask();
        //DownloadTask task2 = new DownloadTask();

        String result = null;

        try{
            result = task.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()){
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()){
                celebNames.add(m.group(1));
            }

            generateQuestion();

        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
