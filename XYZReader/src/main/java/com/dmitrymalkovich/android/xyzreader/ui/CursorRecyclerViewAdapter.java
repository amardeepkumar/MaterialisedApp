/*
 * Copyright 2016.  Dmitry Malkovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dmitrymalkovich.android.xyzreader.ui;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

import com.dmitrymalkovich.android.xyzreader.data.ItemsContract;

/**
 * Created by Amardeep Kumar
 */
public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    private Cursor mCursor;
    private boolean mDataIsValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;

    public CursorRecyclerViewAdapter(Cursor cursor) {
        mCursor = cursor;
        mDataIsValid = cursor != null;
        mRowIdColumn = mDataIsValid ? mCursor.getColumnIndex(ItemsContract.ItemsColumns._ID) : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mDataIsValid) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataIsValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if (!mDataIsValid) {
            throw new IllegalStateException("This should only be called when Cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move Cursor to position: " + position);
        }

        onBindViewHolder(viewHolder, mCursor);
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow(ItemsContract.ItemsColumns._ID);
            mDataIsValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataIsValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataIsValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataIsValid = false;
            notifyDataSetChanged();
        }
    }
}