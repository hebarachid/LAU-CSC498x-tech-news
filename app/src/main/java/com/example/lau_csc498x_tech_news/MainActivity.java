package com.example.lau_csc498x_tech_news;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    SQLiteDatabase articlesDB;
    ArrayAdapter arrayAdapter;
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String listSiteURL = "https://hacker-news.firebaseio.com/v0/topstories.json";
        DownloadListTask listDownloader = new DownloadListTask();
        listDownloader.execute(listSiteURL);
         articlesDB = this.openOrCreateDatabase("articlesDB", MODE_PRIVATE, null);
    //  articlesDB.delete("articles",null,null);
       articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId, INTEGER, title VARCHAR, content VARCHAR)");
        ListView listView = findViewById(R.id.listview);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), Article.class);
                intent.putExtra("content", content.get(i));

                startActivity(intent);
            }
        });

        Cursor c = articlesDB.rawQuery("SELECT * FROM articles", null);

        int contentIndex = c.getColumnIndex("content");
        int titleIndex = c.getColumnIndex("title");

        if (c.moveToFirst()) {
            titles.clear();
            content.clear();

            do {

                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));

            } while (c.moveToNext());

            arrayAdapter.notifyDataSetChanged();
        }
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
                    if ( !jsonArticleArray.isNull("url")) {
                        String id= jsonArticleArray.getString("id");
                       String name= jsonArticleArray.getString("title");
                        String articleUrl= jsonArticleArray.getString("url");
                        String sql = "INSERT INTO articles (articleId, title, content) VALUES (?, ?, ?)";
                        SQLiteStatement statement = articlesDB.compileStatement(sql);
                        statement.bindString(1,id);
                        statement.bindString(2,name);
                        statement.bindString(3,articleUrl);
                        statement.execute();
                    Log.i("i",i+"");
                }}


            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
    }
}