package com.google.developer.taskmaker.data;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.developer.taskmaker.R;
import com.google.developer.taskmaker.databinding.ListItemTaskBinding;

import java.util.Date;

import static com.google.developer.taskmaker.util.AppUtils.DATE_NOW;
import static com.google.developer.taskmaker.views.TaskTitleView.DONE;
import static com.google.developer.taskmaker.views.TaskTitleView.NORMAL;
import static com.google.developer.taskmaker.views.TaskTitleView.OVERDUE;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    /* Callback for list item click events */
    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemToggled(boolean active, int position);
    }

    /* ViewHolder for each task item */
    class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ListItemTaskBinding mBinding;

        TaskHolder(ListItemTaskBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            binding.getRoot().setOnClickListener(this);
            binding.checkbox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == mBinding.checkbox) {
                completionToggled(this);
            } else {
                postItemClick(this);
            }
        }
    }

    private Cursor mCursor;
    private OnItemClickListener mOnItemClickListener;

    public TaskAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void completionToggled(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemToggled(holder.mBinding.checkbox.isChecked(), holder.getAdapterPosition());
        }
    }

    private void postItemClick(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
        }
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final ListItemTaskBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item_task, parent, false);
        return new TaskHolder(binding);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {
        //DONE: Bind the task data to the views
        final Task task = getItem(position);
        if (task.hasDueDate()) {
            holder.mBinding.textDate.setVisibility(View.VISIBLE);
            holder.mBinding.textDate.setText(DateUtils.getRelativeTimeSpanString(task.dueDateMillis));
            final Date dueDate = new Date(task.dueDateMillis);
            final int state = task.isComplete ? DONE : dueDate.before(DATE_NOW) ? OVERDUE : NORMAL;
            holder.mBinding.textDescription.setState(state);
        } else {
            holder.mBinding.textDate.setVisibility(View.GONE);
            holder.mBinding.textDate.setText(R.string.date_empty);
            holder.mBinding.textDescription.setState(NORMAL);
        }
        holder.mBinding.textDescription.setText(task.description);
        holder.mBinding.priority.setImageResource(task.isPriority ? R.drawable.ic_priority : R.drawable.ic_not_priority);
        holder.mBinding.checkbox.setChecked(task.isComplete);
    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    /**
     * Retrieve a {@link Task} for the data at the given position.
     *
     * @param position Adapter item position.
     * @return A new {@link Task} filled with the position's attributes.
     */
    public Task getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }
        return new Task(mCursor);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
