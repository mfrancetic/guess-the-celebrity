package com.mfrancetic.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    private String websiteContent;

    private Pattern pattern;

    private Matcher matcher;

    private ArrayList<String> celebrityPhotoUrlList = new ArrayList<>();

    private ArrayList<String> celebrityNameList = new ArrayList<>();

    private int taskCounter = 0;

    private Random random;

    private Button button1;

    private Button button2;

    private Button button3;

    private Button button4;

    private int locationOfCorrectAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.celebrity_image_view);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        websiteContent = getWebsiteContent();
        if (websiteContent != null) {
            celebrityPhotoUrlList = getCelebrityPhotoUrlList(websiteContent);
            celebrityNameList = getCelebrityNameList(websiteContent);
            createNewTask(celebrityPhotoUrlList.get(taskCounter), celebrityNameList);
        }
    }

    public static class DownloadCelebrityImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class DownloadWebsiteContentTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            try {
                url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void createNewTask(String photoUrl, ArrayList<String> celebrityNameList) {
        createNewCelebrityPhoto(photoUrl);
        createNewCelebrityNameList(celebrityNameList, taskCounter);
    }

    private String getWebsiteContent() {
        DownloadWebsiteContentTask downloadWebsiteContentTask = new DownloadWebsiteContentTask();
        String result = null;
        try {
            result = downloadWebsiteContentTask.execute("http://www.posh24.se/kandisar").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<String> getCelebrityPhotoUrlList(String websiteContent) {
        pattern = Pattern.compile("<img src=\"(.*?)\"");
        String celebrityPhotoUrl;
        ArrayList<String> celebrityPhotoUrlList = new ArrayList<>();
        matcher = pattern.matcher(websiteContent);
        while (matcher.find()) {
            celebrityPhotoUrl = matcher.group(1);
            if (celebrityPhotoUrl != null && celebrityPhotoUrl.contains(":profile/")) {
                celebrityPhotoUrlList.add(celebrityPhotoUrl);
            }
        }
        return celebrityPhotoUrlList;
    }

    private ArrayList<String> getCelebrityNameList(String websiteContent) {
        pattern = Pattern.compile("alt=\"(.*?)\"");
        String name;
        ArrayList<String> celebrityNameList = new ArrayList<>();
        matcher = pattern.matcher(websiteContent);
        while (matcher.find()) {
            name = matcher.group(1);
            celebrityNameList.add(name);
        }
        return celebrityNameList;
    }

    private void createNewCelebrityPhoto(String url) {
        DownloadCelebrityImageTask downloadCelebrityImageTask = new DownloadCelebrityImageTask();
        Bitmap bitmap;
        try {
            bitmap = downloadCelebrityImageTask.execute(url).get();
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNewCelebrityNameList(ArrayList<String> celebrityNameList, int correctAnswer) {
        String name;
        ArrayList<String> nameList = new ArrayList<>();
        random = new Random();
        int wrongAnswer;
        locationOfCorrectAnswer = random.nextInt(4);
        for (int i = 0; i < 4; i++) {
            if (i == locationOfCorrectAnswer) {
                nameList.add(celebrityNameList.get(correctAnswer));
            } else {
                wrongAnswer = random.nextInt(celebrityNameList.size() - 1);
                while (wrongAnswer == correctAnswer) {
                    wrongAnswer = random.nextInt(celebrityNameList.size() - 1);
                }
                name = celebrityNameList.get(wrongAnswer);
                nameList.add(name);
            }
        }
        button1.setText(nameList.get(0));
        button2.setText(nameList.get(1));
        button3.setText(nameList.get(2));
        button4.setText(nameList.get(3));
    }
}