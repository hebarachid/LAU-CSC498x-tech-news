package com.example.lau_csc498x_tech_news;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<Integer> articles = new ArrayList<Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String listSiteURL = "https://hacker-news.firebaseio.com/v0/topstories.json";
        DownloadListTask listDownloader = new DownloadListTask();
        listDownloader.execute(listSiteURL);
    }

    public class DownloadListTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {

            if (Debug.isDebuggerConnected())
                Debug.waitForDebugger();
            String res = "";

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();

                while (data != -1) {
                    char current = (char) data;
                    res += current;

                    data = inputStreamReader.read();
                }
                JSONArray jsonArray = new JSONArray(res);



                for (int i=0;i < 20; i++) {
                    String articleId = jsonArray.getString(i);
                    Log.i("string",articleId);
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();

                    inputStream = urlConnection.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);

                    data = inputStreamReader.read();

                    String articleInfo = "";

                    while (data != -1) {
                        char current = (char) data;
                        articleInfo += current;
                        data = inputStreamReader.read();
                    }
                    JSONObject jsonArticleArray = new JSONObject(articleInfo);
                    Log.i("af",jsonArticleArray.toString());
                    for(int j=0;j<jsonArticleArray.length();j++){
                        String id= jsonArticleArray.getString("id");
                        String name= jsonArticleArray.getString("title");
                        String articleUrl= jsonArticleArray.getString("url");
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
    }
}