package com.google.developer.taskmaker.views;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.google.developer.taskmaker.R;

/**
 * Custom view to display the state of a task as well as
 * its description text.
 */
public class TaskTitleView extends AppCompatTextView {
    public static final int NORMAL = 0;
    public static final int DONE = 1;
    public static final int OVERDUE = 2;
    private int mState;

    public TaskTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TaskTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TaskTitleView(Context context) {
        super(context);
    }

    /**
     * Return the current display state of this view.
     *
     * @return One of {@link #NORMAL}, {@link #DONE}, or {@link #OVERDUE}.
     */
    public int getState() {
        return mState;
    }

    /**
     * Update the text display state of this view.
     * Normal status shows black text. Overdue displays in red.
     * Completed draws a strikethrough line on the text.
     *
     * @param state New state. One of {@link #NORMAL}, {@link #DONE}, or {@link #OVERDUE}.
     */
    public void setState(int state) {
        // I am not seeing the task description text style change to strikethrough in the list when a task item is completed. (FIXED)
        if ((getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) == Paint.STRIKE_THRU_TEXT_FLAG) {
            setPaintFlags(getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
        }
        final Context context = getContext();
        switch (state) {
            case DONE:
                setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                setTextColor(ContextCompat.getColor(context, R.color.black));
                break;
            case NORMAL:
                setTextColor(ContextCompat.getColor(context, R.color.black));
                break;
            case OVERDUE:
                setTextColor(ContextCompat.getColor(context, R.color.red));
                break;
            default:
                return;
        }
        invalidate();
        mState = state;
    }
}
