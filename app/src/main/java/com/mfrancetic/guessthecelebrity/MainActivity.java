package com.mfrancetic.guessthecelebrity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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

    private int currentTask;

    private Random random;

    private Button button0;

    private Button button1;

    private Button button2;

    private Button button3;

    private int locationOfCorrectAnswer;

    private ArrayList<String> answerNameList;

    private static final String answerNameListTag = "answerNameList";

    private static final String currentTaskTag = "currentTask";

    private static final String websiteContentTag = "websiteContent";

    private static final String celebrityPhotoUrlListTag = "celebrityPhotoUrlList";

    private static final String celebrityNameListTag = "celebrityNameList";

    private static final String locationOfCorrectAnswerTag = "locationOfCorrectAnswer";

    private static final String photoUrlPattern = "<img src=\"(.*?)\"";

    private static final String namePattern = "alt=\"(.*?)\"";

    private static final String photoUrlFilter = ":profile/";

    private static final String websiteUrl = "http://www.posh24.se/kandisar";

    private int celebrityListBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.celebrity_image_view);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        if (savedInstanceState != null) {
            currentTask = savedInstanceState.getInt(currentTaskTag);
            websiteContent = savedInstanceState.getString(websiteContent);
            celebrityPhotoUrlList = savedInstanceState.getStringArrayList(celebrityPhotoUrlListTag);
            celebrityNameList = savedInstanceState.getStringArrayList(celebrityNameListTag);
            if (celebrityNameList != null) {
                celebrityListBound = celebrityNameList.size() - 1;
            }
            answerNameList = savedInstanceState.getStringArrayList(answerNameListTag);
            locationOfCorrectAnswer = savedInstanceState.getInt(locationOfCorrectAnswerTag);
            createNewCelebrityPhoto(currentTask);
            populateAnswerList(answerNameList);
        } else {
            websiteContent = getWebsiteContent();
            if (websiteContent != null) {
                celebrityPhotoUrlList = getCelebrityPhotoUrlList(websiteContent);
                celebrityNameList = getCelebrityNameList(websiteContent);
                celebrityListBound = celebrityNameList.size() - 1;
                currentTask = getCurrentTask();
                createNewTask(currentTask);
            }
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

    private void createNewTask(int taskCounter) {
        createNewCelebrityPhoto(taskCounter);
        createNewCelebrityNameList(taskCounter);
    }

    private String getWebsiteContent() {
        DownloadWebsiteContentTask downloadWebsiteContentTask = new DownloadWebsiteContentTask();
        String result = null;
        try {
            result = downloadWebsiteContentTask.execute(websiteUrl).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<String> getCelebrityPhotoUrlList(String websiteContent) {
        pattern = Pattern.compile(photoUrlPattern);
        String celebrityPhotoUrl;
        ArrayList<String> celebrityPhotoUrlList = new ArrayList<>();
        matcher = pattern.matcher(websiteContent);
        while (matcher.find()) {
            celebrityPhotoUrl = matcher.group(1);
            if (celebrityPhotoUrl != null && celebrityPhotoUrl.contains(photoUrlFilter)) {
                celebrityPhotoUrlList.add(celebrityPhotoUrl);
            }
        }
        return celebrityPhotoUrlList;
    }

    private ArrayList<String> getCelebrityNameList(String websiteContent) {
        pattern = Pattern.compile(namePattern);
        String name;
        ArrayList<String> celebrityNameList = new ArrayList<>();
        matcher = pattern.matcher(websiteContent);
        while (matcher.find()) {
            name = matcher.group(1);
            celebrityNameList.add(name);
        }
        return celebrityNameList;
    }

    private void createNewCelebrityPhoto(int taskCounter) {
        String url = celebrityPhotoUrlList.get(taskCounter);
        DownloadCelebrityImageTask downloadCelebrityImageTask = new DownloadCelebrityImageTask();
        Bitmap bitmap;
        try {
            bitmap = downloadCelebrityImageTask.execute(url).get();
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNewCelebrityNameList(int correctAnswer) {
        String name;
        answerNameList = new ArrayList<>();
        random = new Random();
        int wrongAnswer;
        locationOfCorrectAnswer = random.nextInt(4);
        for (int i = 0; i < 4; i++) {
            if (i == locationOfCorrectAnswer) {
                answerNameList.add(celebrityNameList.get(correctAnswer));
            } else {
                wrongAnswer = random.nextInt(celebrityListBound);
                while (wrongAnswer == correctAnswer) {
                    wrongAnswer = random.nextInt(celebrityListBound);
                }
                name = celebrityNameList.get(wrongAnswer);
                answerNameList.add(name);
            }
        }
        populateAnswerList(answerNameList);
    }

    public void chooseAnswer(View view) {
        int tag = Integer.parseInt(view.getTag().toString());
        String toast;
        if (isAnswerCorrect(tag)) {
            toast = getString(R.string.answer_correct);
        } else {
            toast = getString(R.string.answer_wrong) + " " + celebrityNameList.get(currentTask);
        }
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        currentTask = getCurrentTask();
        createNewTask(currentTask);
    }

    private boolean isAnswerCorrect(int tag) {
        return tag == locationOfCorrectAnswer;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(currentTaskTag, currentTask);
        outState.putString(websiteContentTag, websiteContent);
        outState.putStringArrayList(celebrityPhotoUrlListTag, celebrityPhotoUrlList);
        outState.putStringArrayList(celebrityNameListTag, celebrityNameList);
        outState.putStringArrayList(answerNameListTag, answerNameList);
        outState.putInt(locationOfCorrectAnswerTag, locationOfCorrectAnswer);
        super.onSaveInstanceState(outState);
    }

    private void populateAnswerList(ArrayList<String> nameList) {
        button0.setText(nameList.get(0));
        button1.setText(nameList.get(1));
        button2.setText(nameList.get(2));
        button3.setText(nameList.get(3));
    }

    private int getCurrentTask() {
        random = new Random();
        int newTask = random.nextInt(celebrityListBound);
        while (newTask == currentTask) {
            newTask = random.nextInt(celebrityListBound);
        }
        return newTask;
    }
}