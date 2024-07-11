package com.example.cse489lab42020160111;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class ClassSummaryListActivity extends AppCompatActivity {
    private ArrayList<ClassSummary> lectures;
    private ListView lvLectureList;
    private ClassSummaryAdapter csAdapter;
    String course ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_summary_list);

        // retrieve course name from intent
        course = getIntent().getStringExtra("course");

        // find and set class name to TextView
        TextView classNameTextView = findViewById(R.id.classNameTextView);
        lvLectureList= findViewById(R.id.lvLecture);

        classNameTextView.setText(course + " : Lectures");

        lectures = new ArrayList<>();
        csAdapter=new ClassSummaryAdapter(this,lectures);
        lvLectureList.setAdapter(csAdapter);
        loadClassSummary();

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClassSummaryListActivity.this, ClassSummaryActivity.class);
                intent.putExtra("course",course);
                startActivity(intent);
            }
        });

        lvLectureList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String lectureId = lectures.get(position).getId();
                Intent intent = new Intent(ClassSummaryListActivity.this, ClassSummaryActivity.class);
                intent.putExtra("lectureId", lectureId);
                startActivity(intent);
            }
        });

        lvLectureList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClassSummaryListActivity.this);
                builder.setMessage("Are you sure you want to delete this lecture?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // delete the lecture from the database
                                ClassSummaryDB db = new ClassSummaryDB(ClassSummaryListActivity.this);
                                String lectureId = lectures.get(position).getId();
                                deleteLectureFromRemoteServer(lectureId);
                                db.deleteLecture(lectures.get(position).getId());
                                // remove the lecture from the list and update the adapter
                                lectures.remove(position);
                                csAdapter.notifyDataSetChanged();
                            }
                        });

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });


                        builder.show();

                return true;
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void loadClassSummary() {
        String q = "SELECT * FROM lectures WHERE course ='" + course +"';";
        ClassSummaryDB db = new ClassSummaryDB(this);
        Cursor cur = db.selectLectures(q);
        lectures.clear();

        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(0);
                String course = cur.getString(1);
                String type = cur.getString(2);
                long date = cur.getLong(3);
                int lecture = cur.getInt(4);
                String topic = cur.getString(5);
                String summary = cur.getString(6);

                ClassSummary cs = new ClassSummary(id, course, type, date, lecture, topic, summary);
                lectures.add(cs);
            }
            csAdapter.notifyDataSetInvalidated();
            csAdapter.notifyDataSetChanged();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadClassSummary();
    }

    private void deleteLectureFromRemoteServer(final String lectureId) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String baseUrl = "https://www.muthosoft.com/univ/cse489/index.php";
                    String studentId = "2020-1-60-111";
                    String semester = "2024-1";

                    String response = RemoteAccess.getInstance().removeLectureSummary(baseUrl, studentId, semester, lectureId);

                    return response;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            protected void onPostExecute(String result) {
                if (result != null) {
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

}