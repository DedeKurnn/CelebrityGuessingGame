package com.dedekurnn.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
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

    ImageView imageCeleb;
    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    String[] answers = new String[4];
    int locationOfCorrectAnswer = 0;
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public class ImageDownloader extends  AsyncTask<String, Void, Bitmap> {

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
                return null;
            }

        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageCeleb = findViewById(R.id.celebImageView);
        DownloadTask task = new DownloadTask();
        String result = null;
        button0 = findViewById(R.id.celebNameBtn1);
        button1 = findViewById(R.id.celebNameBtn2);
        button2 = findViewById(R.id.celebNameBtn3);
        button3 = findViewById(R.id.celebNameBtn4);

        try {
            result = task.execute("https://www.amiannoying.com/(S(y3fo0h0lfzn1jd0ii4j34jj5))/collection.aspx?collection=78").get();
            String[] splitResult = result.split("<tbody>");

            Pattern p = Pattern.compile("collection=78\">(.*?)</a>");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebNames.add(m.group(1));
            }

            p = Pattern.compile("annoy-photo-34k3-1/(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebURLs.add("https://www.amiannoying.com/annoy-photo-34k3-1/" + m.group(1));
            }
            newQuestion();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void newQuestion() {
        try {
            Random random = new Random();
            chosenCeleb = random.nextInt(celebURLs.size());

            ImageDownloader imageTask = new ImageDownloader();
            Bitmap celebImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();
            imageCeleb.setImageBitmap(celebImage);

            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectLocation;

            //FIXME:    `Some celebrity name in the button is set to the image link instead
            //           of their actual name. Consider fixing the bug in the next release.
            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectLocation = random.nextInt(celebURLs.size());

                    while (incorrectLocation == chosenCeleb) {
                        incorrectLocation = random.nextInt(celebURLs.size());
                    }
                    answers[i] = celebNames.get(incorrectLocation);
                }
                button0.setText(answers[0]);
                button1.setText(answers[1]);
                button2.setText(answers[2]);
                button3.setText(answers[3]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void celebChosen(View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong! is was " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }
        newQuestion();
    }
}