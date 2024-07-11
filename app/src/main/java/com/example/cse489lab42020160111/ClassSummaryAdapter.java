package com.example.cse489lab42020160111;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ClassSummaryAdapter extends ArrayAdapter<ClassSummary> {

    private final Context context;
    private final ArrayList<ClassSummary> values;
    LayoutInflater inflater;
    public ClassSummaryAdapter(@NonNull Context context, @NonNull ArrayList<ClassSummary> items) {
        super(context, -1, items);
        this.context = context;
        this.values = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.layout_class_summary_row, parent, false);

        TextView topLeftTitle = rowView.findViewById(R.id.tvLectureTitle);
        TextView dateTime = rowView.findViewById(R.id.tvDate);
        TextView topic = rowView.findViewById(R.id.tvSummary);
        //TextView eventType = rowView.findViewById(R.id.tvEventType);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        ClassSummary e = values.get(position);
        topLeftTitle.setText(e.topic);
        dateTime.setText(sdf.format(e.date));
        topic.setText(e.summary);

        //eventType.setText(e.eventType);
        return rowView;

    }
}