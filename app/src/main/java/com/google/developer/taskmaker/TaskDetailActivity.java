package com.google.developer.taskmaker;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.developer.taskmaker.data.Task;
import com.google.developer.taskmaker.data.TaskUpdateService;
import com.google.developer.taskmaker.databinding.ActivityTaskDetailBinding;
import com.google.developer.taskmaker.reminders.AlarmScheduler;
import com.google.developer.taskmaker.util.AppUtils;

import java.util.Calendar;

public class TaskDetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener {

    private static final int REMINDER_RC = 0;

    private Uri mTaskUri;
    private DatePickerDialog mDatePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityTaskDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_task_detail);

        //Task must be passed to this activity as a valid provider Uri
        mTaskUri = getIntent().getData();

        //DONE: Display attributes of the provided task in the UI
        final Cursor cursor = super.getContentResolver().query(mTaskUri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            Task task = new Task(cursor);
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
        //DONE: Handle date selection from a DatePickerFragment
        final Calendar calendar = Calendar.getInstance(AppUtils.LOCALE);
        final long nowTime = calendar.getTimeInMillis();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final long pickerTime = calendar.getTimeInMillis();
        long alarmTime = pickerTime - nowTime;
        if (alarmTime < 0) {
            // If hour of the day is after 12:00:00, execute alarm immediately.
            alarmTime = 0;
        }
        AlarmScheduler.scheduleAlarm(this, alarmTime, mTaskUri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reminder:
                if (mDatePickerDialog == null) {
                    final Calendar taskDate = Calendar.getInstance(AppUtils.LOCALE);
                    final int year = taskDate.get(Calendar.YEAR);
                    final int month = taskDate.get(Calendar.MONTH);
                    final int day = taskDate.get(Calendar.DAY_OF_MONTH);
                    mDatePickerDialog = new DatePickerDialog(this, this, year, month, day);
                    // https://stackoverflow.com/a/23762355/3072570
                    mDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                }
                if (!mDatePickerDialog.isShowing()) {
                    mDatePickerDialog.show();
                }
                return true;
            case R.id.action_delete:
                TaskUpdateService.deleteTask(this, mTaskUri);
                super.onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
