package com.google.developer.taskmaker;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;

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
        final long pickerTime = AppUtils.getTaskStandardTimeInMillis(day, month, year);
        AlarmScheduler.scheduleAlarm(this, pickerTime, mTaskUri);
        setResult(MainActivity.RESULT_REMIND_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reminder:
                if (mDatePickerDialog == null) {
                    final Calendar taskDate = Calendar.getInstance();
                    final int day = taskDate.get(Calendar.DAY_OF_MONTH);
                    final int month = taskDate.get(Calendar.MONTH);
                    final int year = taskDate.get(Calendar.YEAR);
                    mDatePickerDialog = new DatePickerDialog(this, this, year, month, day);
                    // https://stackoverflow.com/a/23762355/3072570
                    mDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                }
                if (!mDatePickerDialog.isShowing()) {
                    mDatePickerDialog.show();
                }
                return true;
            case R.id.action_delete:
                new AlertDialog.Builder(this).setTitle(R.string.dialog_confirm_title)
                        .setMessage(R.string.msg_confirm_delete)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            TaskUpdateService.deleteTask(TaskDetailActivity.this, mTaskUri);
                            setResult(MainActivity.RESULT_DELETE_OK);
                            finish();
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
