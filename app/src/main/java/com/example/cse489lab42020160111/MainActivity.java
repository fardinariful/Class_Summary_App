package com.example.cse489lab42020160111;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GridLayout gridLayout;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("user_info", MODE_PRIVATE);
        boolean remPass = sp.getBoolean("REM_PASS", false);
        gridLayout = findViewById(R.id.gridLayout);

        CardView cse489Card = findViewById(R.id.cse489Card);
        CardView cse103Card = findViewById(R.id.cse103Card);
        CardView cse345Card = findViewById(R.id.cse345Card);
        CardView cse477Card = findViewById(R.id.cse477Card);

        findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decideNavigation(remPass);
            }
        });

        findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCourseDialog();
            }
        });

        // add OnClickListener to each card view and open clicked class lecture summary
        cse489Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openClassLectures("CSE489");
            }
        });

        cse103Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openClassLectures("CSE103");
            }
        });

        cse345Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openClassLectures("CSE345");
            }
        });

        cse477Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openClassLectures("CSE477");
            }
        });
    }

    public void onStart() {
        super.onStart();
        // write code here to send request to remote server for loading data
        String keys[] = {"action", "sid", "semester"};
        String values[] = {"restore", "2020-1-60-111", "2024-1"};
        httpRequest(keys, values);
    }

    private void httpRequest(final String keys[], final String values[]) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                for (int i = 0; i < keys.length; i++) {
                    params.add(new BasicNameValuePair(keys[i], values[i]));
                }
                String url = "https://www.muthosoft.com/univ/cse489/index.php";
                try {
                    String data = RemoteAccess.getInstance().makeHttpRequest(url, "POST", params);
                    return data;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String data) {
                if (data != null) {
                    updateLocalDBByServerData(data);
                }
            }
        }.execute();
    }

    private void updateLocalDBByServerData(String data) {
        System.out.println("found");
        try {
            JSONObject jo = new JSONObject(data);
            ClassSummaryDB summaryDB = new ClassSummaryDB(MainActivity.this);
            if (jo.has("classes")) {
                JSONArray ja = jo.getJSONArray("classes");
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject summary = ja.getJSONObject(i);
                    String id = summary.getString("id");
                    String course = summary.getString("course");
                    String topic = summary.getString("topic");
                    String type = summary.getString("type");
                    long date = summary.getLong("date");
                    int lecture = summary.getInt("lecture");
                    String sum = summary.getString("summary");
                    // Write code here to insert lecture information in SQL Database
                    try {
                        summaryDB.insertLecture(id, course, type, date, lecture, topic, sum);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }
    }


    private void showAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.add_course, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etCode = dialogView.findViewById(R.id.etCode);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = etTitle.getText().toString();
                String code = etCode.getText().toString();

                if (!title.isEmpty() && !code.isEmpty()) {

                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please fill up all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void openClassLectures(String course) {
        Intent intent = new Intent(MainActivity.this, ClassSummaryListActivity.class);
        intent.putExtra("course", course);
        startActivity(intent);
    }
    private void decideNavigation(boolean remPass){
        if(!remPass){
            SharedPreferences.Editor e= sp.edit();
            e.remove("LOGGED_IN");
            e.commit();
            finish();
        }
        else{
            finish();
        }
    }
}

