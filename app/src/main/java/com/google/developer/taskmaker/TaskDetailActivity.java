package com.google.developer.taskmaker;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.widget.DatePicker;

import com.google.developer.taskmaker.data.Task;
import com.google.developer.taskmaker.databinding.ActivityTaskDetailBinding;

public class TaskDetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityTaskDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_task_detail);

        //Task must be passed to this activity as a valid provider Uri
        final Uri taskUri = getIntent().getData();

        //DONE: Display attributes of the provided task in the UI
        final Cursor cursor = super.getContentResolver().query(taskUri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            final Task task = new Task(cursor);
            binding.textDescription.setText(task.description);
            if (task.hasDueDate()) {
                binding.textDate.setText(DateUtils.getRelativeTimeSpanString(task.dueDateMillis));
            } else {
                binding.textDate.setText(R.string.date_empty);
            }
            binding.priority.setImageResource(task.isPriority ? R.drawable.ic_priority : R.drawable.ic_not_priority);
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //TODO: Handle date selection from a DatePickerFragment
    }
}
