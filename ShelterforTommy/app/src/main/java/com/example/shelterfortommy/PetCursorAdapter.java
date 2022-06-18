package com.example.shelterfortommy;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.shelterfortommy.data.PetContract;

/**
 * {@link PetCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class PetCursorAdapter extends CursorAdapter {
    /**
     *
     * @param context: The Context
     * @param c : The cursor from which to get the data
     * @param flags: Pass 0
     */
    public PetCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        Log.i("#####","Entered the constructor of PetCursorAdapter");

    }
/**
 * Makes a new blank list item view. No data is set (or bound) to the views yet.
 *
 * @param context app context
 * @param cursor  The cursor from which to get the data. The cursor is already
 *                moved to the correct position.
 * @param parent  The parent to which the new view is attached to
 * @return the newly created list item view.
 */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.i("#####","Entered the newView() method of the PetCursorAdapter, inflate and return");
        //Inflate a new view and return it.
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }
    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView=(TextView) view.findViewById(R.id.name);
        TextView summaryView=(TextView) view.findViewById(R.id.summary);
        //Find the columns of pet database we want to modify in the list item layout
        int nameColumnIndex=cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex=cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);
        //Read the attributes from the cursor.
        String name=cursor.getString(nameColumnIndex);
        String summaryBreed=cursor.getString(breedColumnIndex);
        if(TextUtils.isEmpty(summaryBreed))
            summaryBreed=context.getString(R.string.unknown_breed);
        //Note- If the breed is no known, we leave it blank in the database
        //Only show it to user
        //Update the textview with the new data just extracted from cursor
        nameTextView.setText(name);
        summaryView.setText(summaryBreed);
    }
}
