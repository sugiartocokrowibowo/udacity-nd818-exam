package com.google.developer.taskmaker;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.developer.taskmaker.data.DatabaseContract;
import com.google.developer.taskmaker.data.Task;
import com.google.developer.taskmaker.data.TaskAdapter;
import com.google.developer.taskmaker.data.TaskUpdateService;
import com.google.developer.taskmaker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, TaskAdapter.OnItemClickListener {

    private static final int LOADER_ID = 0;
    private static final int REQUEST_DETAIL = 1;

    private ActivityMainBinding mBinding;
    private TaskAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mBinding.toolbar);

        mAdapter = new TaskAdapter(null);
        mAdapter.setOnItemClickListener(this);

        mBinding.recyclerView.setHasFixedSize(true);
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBinding.fab.setOnClickListener(v -> {
            final Intent intent = new Intent(this, AddTaskActivity.class);
            startActivity(intent);
        });
        super.getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                final Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, int position) {
        //DONE: Handle list item click event
        final Task task = mAdapter.getItem(position);
        final Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.setData(ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, task.id));
        startActivityForResult(intent, REQUEST_DETAIL);
    }

    /* Click events on RecyclerView item checkboxes */
    @Override
    public void onItemToggled(boolean active, int position) {
        //DONE: Handle task item checkbox event
        final Task task = mAdapter.getItem(position);

        final boolean isComplete = !task.isComplete;
        this.updateTaskComplete(task.id, isComplete);

        int msg = isComplete ? R.string.msg_task_completed : R.string.msg_task_uncompleted;
        final Snackbar snackbar = Snackbar.make(mBinding.getRoot(), msg, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, view -> {
                    this.updateTaskComplete(task.id, !isComplete);
                })
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                    }
                });
        snackbar.show();
    }

    private void updateTaskComplete(long taskId, boolean isComplete) {
        final ContentValues values = new ContentValues(1);
        values.put(DatabaseContract.TaskColumns.IS_COMPLETE, isComplete);

        final Uri uri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, taskId);
        TaskUpdateService.updateTask(this, uri, values);
        getSupportLoaderManager().restartLoader(LOADER_ID, null, MainActivity.this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DatabaseContract.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
