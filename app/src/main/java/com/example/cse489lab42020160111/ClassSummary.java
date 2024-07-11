package com.example.cse489lab42020160111;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ClassSummary {
    String id = "";
    String course = "";
    String type = "";
    long date = 0;
    int lecture = 0;
    String topic = "";
    String summary = "";


    public ClassSummary(String id, String course, String type, long date, int lecture, String topic, String summary){
        this.id = id;
        this.course = course;
        this.topic = topic;
        this.type = type;
        this.date = date;
        this.lecture = lecture;
        this.summary = summary;
    }

    public String getId() {
        return id;
    }

    public String getCourse() {
        return course;
    }

    public String getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public int getLecture() {
        return lecture;
    }

    public String getTopic() {
        return topic;
    }

    public String getSummary() {
        return summary;
    }
}