package com.google.developer.taskmaker.data;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

public class TaskProvider extends ContentProvider {
    private static final String TAG = TaskProvider.class.getSimpleName();

    private static final int CLEANUP_JOB_ID = 43;

    private static final int TASKS = 100;
    private static final int TASKS_WITH_ID = 101;

    private TaskDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // content://com.google.developer.taskmaker/tasks
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_TASKS,
                TASKS);

        // content://com.google.developer.taskmaker/tasks/id
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_TASKS + "/#",
                TASKS_WITH_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new TaskDbHelper(getContext());
        manageCleanupJob();
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null; /* Not used */
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //DONE: Implement task query
        //DONE: Expected "query all" Uri: content://com.google.developer.taskmaker/tasks
        //DONE: Expected "query one" Uri: content://com.google.developer.taskmaker/tasks/{id}
        Cursor returnCursor;
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        final int endpoint = sUriMatcher.match(uri);
        switch (endpoint) {
            case TASKS:
            case TASKS_WITH_ID:
                if (endpoint == TASKS_WITH_ID) {
                    final long id = ContentUris.parseId(uri);
                    selection = String.format("%s = ?", DatabaseContract.TaskColumns._ID);
                    selectionArgs = new String[]{String.valueOf(id)};
                }
                returnCursor = db.query(
                        DatabaseContract.TABLE_TASKS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        final Context context = getContext();
        if (context != null) {
            returnCursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return returnCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //DONE: Implement new task insert
        //DONE: Expected Uri: content://com.google.developer.taskmaker/tasks
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                db.insert(DatabaseContract.TABLE_TASKS, null, values);
                returnUri = DatabaseContract.CONTENT_URI;
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        final Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }

        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //DONE: Implement existing task update
        //DONE: Expected Uri: content://com.google.developer.taskmaker/tasks/{id}
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int affectedRows = 0;

        switch (sUriMatcher.match(uri)) {
            case TASKS_WITH_ID:
                final long id = ContentUris.parseId(uri);
                selection = String.format("%s = ?", DatabaseContract.TaskColumns._ID);
                selectionArgs = new String[]{String.valueOf(id)};
                affectedRows = db.update(DatabaseContract.TABLE_TASKS, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        final Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return affectedRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                //Rows aren't counted with null selection
                selection = (selection == null) ? "1" : selection;
                break;
            case TASKS_WITH_ID:
                long id = ContentUris.parseId(uri);
                selection = String.format("%s = ?", DatabaseContract.TaskColumns._ID);
                selectionArgs = new String[]{String.valueOf(id)};
                break;
            default:
                throw new IllegalArgumentException("Illegal delete URI");
        }

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.delete(DatabaseContract.TABLE_TASKS, selection, selectionArgs);

        final Context context = getContext();
        if (count > 0 && context != null) {
            //Notify observers of the change
            context.getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    /* Initiate a periodic job to clear out completed items */
    private void manageCleanupJob() {
        Log.d(TAG, "Scheduling cleanup job");
        JobScheduler jobScheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);

        //Run the job approximately every hour
        //Completed tasks should be automatically deleted once every hour. I’m seeing this happen every 15 minutes. (FIXED)
        long jobInterval = 3600000L;

        ComponentName jobService = new ComponentName(getContext(), CleanupJobService.class);
        JobInfo task = new JobInfo.Builder(CLEANUP_JOB_ID, jobService)
                .setPeriodic(jobInterval)
                .setPersisted(true)
                .build();

        if (jobScheduler.schedule(task) != JobScheduler.RESULT_SUCCESS) {
            Log.w(TAG, "Unable to schedule cleanup job");
        }
    }
}
