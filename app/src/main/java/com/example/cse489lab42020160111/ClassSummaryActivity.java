package com.example.cse489lab42020160111;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;

import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClassSummaryActivity extends AppCompatActivity {
    EditText etDate, etLecture, etTopic, etSummary,etCourse;
    RadioButton rb, rbTheory, rbLab;
    RadioGroup rgType;
    DatePickerDialog datePickerDialog;
    Calendar selectedDate;
    private String id = "";
    private ClassSummaryDB summaryDB;

    String course ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_summary);




        if (getIntent().hasExtra("course")) {
            course = getIntent().getStringExtra("course");
        }
        if (getIntent().hasExtra("lectureId")) {
            id = getIntent().getStringExtra("lectureId");
        }

        summaryDB = new ClassSummaryDB(this);

        rgType = findViewById(R.id.rgType);


        rbTheory = findViewById(R.id.rbTheory);
        rbLab = findViewById(R.id.rbLab);

        etDate = findViewById(R.id.etDate);
        etLecture = findViewById(R.id.etLecture);
        etTopic = findViewById(R.id.etTopic);
        etSummary = findViewById(R.id.etSummary);
        etCourse=findViewById(R.id.etCourse);

        etCourse.setText(course);

        // handle EditText click function by opening DatePickerDialog with current date and limiting selection to today's date
        handleDateSelection();

        if (!id.isEmpty()) {
            populateFieldsFromDatabase();
        }

        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveClassSummary();
            }
        });
    }



    private void handleDateSelection() {
        // set the hint to current date
        String currentDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        etDate.setHint(currentDate);

        // get current date
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // initialize selected date with current date
        selectedDate = calendar;

        // handle click event to select date
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a DatePickerDialog with current date
                datePickerDialog = new DatePickerDialog(ClassSummaryActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        // set selected date in the format
                        selectedDate.set(year, month, day);
                        long date = selectedDate.getTimeInMillis(); // get selected date in milliseconds

                        String dateText = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selectedDate.getTime());
                        etDate.setText(dateText);
                    }
                }, year, month, day);

                // limit selection to today's date
                datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());

                // set previously selected date if available
                if (selectedDate != null) {
                    datePickerDialog.getDatePicker().updateDate(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
                }

                // show the DatePickerDialog on click
                datePickerDialog.show();
            }
        });
    }


    private void saveClassSummary() {
        String errMsg = "";
        String printMsg = "";
        String invalidMsg = "";

        course = etCourse.getText().toString().trim();
        String type = "";
        long date = selectedDate.getTimeInMillis();
        String lectureNum = etLecture.getText().toString().trim();
        String topic = etTopic.getText().toString().trim();
        String summary = etSummary.getText().toString().trim();


        // get the text of selected class type
        if (rbTheory.isChecked() || rbLab.isChecked()) {
            int selectedId = rgType.getCheckedRadioButtonId();
            rb = findViewById(selectedId);
            type = rb.getText().toString();
        }

        // check for empty fields and display error message if found
        if (course.isEmpty() || type.isEmpty() || date == 0 || lectureNum.isEmpty() || topic.isEmpty() || summary.isEmpty()) {
            errMsg = "Please provide information for all fields.";
            showMsgDialog(errMsg, false);
            return;
        }

        // lecture number validation
        int lecture = Integer.parseInt(lectureNum);
        if (lecture > 50 || lecture < 1) {
            errMsg += "Lecture number must be between 1 and 50, ";
            invalidMsg += "Lecture: " + lecture + ", ";
        }

        // topic title length validation
        if (topic.length() < 5 || topic.length() > 50 || !topic.matches("^[a-zA-Z0-9 ]+$")) {
            errMsg += "Topic title should be 5 to 50 characters long and only alpha-numeric, ";
            invalidMsg += "Topic: " + topic + ", ";
        }

        // summary length validation
        if (summary.length() < 5 || summary.length() > 1000 || !summary.matches("^[a-zA-Z0-9, ]+$")) {
            errMsg += "Summary should be 5 to 1000 characters long and only alpha-numeric";
            invalidMsg += "Summary: " + summary;
        }

        if (errMsg.length() > 0) {
            // remove the trailing comma if it exists
            if (errMsg.endsWith(", ")) {
                errMsg = errMsg.substring(0, errMsg.length() - 2);
            }
            if (invalidMsg.endsWith(", ")) {
                invalidMsg = invalidMsg.substring(0, invalidMsg.length() - 2);
            }

            Toast.makeText(this, invalidMsg, Toast.LENGTH_LONG).show();
            showMsgDialog(errMsg, false);
            return;
        }

        if (id.isEmpty()) {
            id = topic + System.currentTimeMillis();
            summaryDB.insertLecture(id, course, type, date, lecture, topic, summary);
            finish();
        } else {
            summaryDB.updateLecture(id, course, type, date, lecture, topic, summary);
        }

        printMsg = "Id: " + id + "\nCourse: " + course + "\nType: " + type + "\nDate: " + date + "\nLecture: " + lectureNum + "\nTopic: " + topic + "\nSummary: " + summary;
        Toast.makeText(ClassSummaryActivity.this, "Lecture info is inserted", Toast.LENGTH_LONG).show();
//        showMsgDialog(printMsg, true);

        String keys[] = {"action", "sid", "semester", "id", "course", "type", "topic", "date", "lecture", "summary"};
        String values[] = {"backup", "2020-1-60-111", "2024-1", id, course, type, topic, String.valueOf(date), String.valueOf(lecture), summary};


        httpRequest(keys,values);

        finish();
    }

    private void httpRequest(final String keys[],final String values[]){
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... voids) {
                List<NameValuePair> params=new ArrayList<NameValuePair>();
                for (int i=0; i<keys.length; i++){
                    params.add(new BasicNameValuePair(keys[i],values[i]));
                }
                String url= "https://www.muthosoft.com/univ/cse489/index.php";
                try {
                    String data= RemoteAccess.getInstance().makeHttpRequest(url,"POST",params);
                    return data;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            protected void onPostExecute(String data){
                if(data!=null){
                    Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void populateFieldsFromDatabase() {
        ClassSummary existingLecture = summaryDB.getLectureById(id);
        if (existingLecture != null) {
            etCourse.setText(existingLecture.getCourse());
            etLecture.setText(String.valueOf(existingLecture.getLecture()));
            etTopic.setText(existingLecture.getTopic());
            etSummary.setText(existingLecture.getSummary());

            // Set radio button based on type
            if (existingLecture.getType().equals("Theory")) {
                rbTheory.setChecked(true);
            } else {
                rbLab.setChecked(true);
            }

            // Set date
            selectedDate.setTimeInMillis(existingLecture.getDate());
            String dateText = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selectedDate.getTime());
            etDate.setText(dateText);
        }
    }



    private void showMsgDialog(String message, boolean isValid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);

        if (isValid) {
            builder.setTitle("Class Summary");
        }
        else {
            builder.setTitle("Error");
        }

        builder.setCancelable(true);

        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


}