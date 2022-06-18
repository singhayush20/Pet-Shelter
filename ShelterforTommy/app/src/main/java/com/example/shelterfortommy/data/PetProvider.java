package com.example.shelterfortommy.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();


    private PetDbHelper mDbHelper;
    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int PETS = 100;
    /**
     * URI matcher code for the content URI for a single
     * pet in the pets table
     */
    private static final int PETS_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PETS_ID);
    }

    @Override
    public boolean onCreate() {
        //Create and initialize a PetDbHelper object to gain access to the pets database.
        //Make sure the variable is a global variable, so it can be referenced from other
        //ContentProvider methods.
        Log.i("###", "entered the onCreate() method of PetProvider class");
        mDbHelper = new PetDbHelper(getContext());

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.i("###", "entered the query method of the PetProvider class uri: " + uri.toString());
        //Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        //This cursor will hold the result of the query and will be returned.
        Cursor cursor = null;//Otherwise, cursor not initialised error in return
        //Figure out the URI match.
        //Figure out if the URI Matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        Log.i("###", "URI is: " + uri.toString() + " match: " + match);
        switch (match) {
            case PETS:
                Log.i("###", "entered the case PETS: " + PETS + " in query() of PetProvider");
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                // Perform database query on pets table
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                Log.i("###", "Database queried in PETS case in query() of PetProvider");
                break;
            case PETS_ID:
                Log.i("###", "entered in case PETS_ID: " + PETS_ID);
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                Log.i("###", "selection and selectionArgs set");
                //This will perform a query on the pets table where the _id equals 3 to return
                //cursor containing that row of the table.
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null,
                        null, sortOrder);
                Log.i("###", "database queried in PETS_ID case");
                break;
            default:
                Log.i("###", "Error occurred in query() method of content provider query()");
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //SET THE NOTIFICATION URI ON THE CURSOR, SO WE KNOW WHAT
        //CONTENT URI THE CURSOR WAS CREATED FOR.
        //IF THE DATA AT THIS URI CHANGES, THEN WE
        //KNOW THAT WE NEED TO UPDATE THE CURSOR.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.i("###", "Returning from query()  of content provider PetProvider after invoking setNotification() uri");
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Log.i("###", "entered the getType() method of the PetProvider class");
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                Log.i("###", "returning from the getType() method of PetProvider after returning MIME for dir " + uri);

                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                Log.i("###", "returning from the getType() method of PetProvider after returning MIME for item " + uri);

                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
        }
        return null;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        Log.i("###", "Entered the insert() method of PetProvider class");
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                Log.i("###", "PETS: " + PETS + " case matched, insert and " +
                        "return the URI with ID appended to it");
                return insertPet(uri, contentValues);
            //insertPet is a helper method
                /*
                Case PETS_ID is not possible because while
                inserting a new pet, the _id will not be the part of
                the URI.
                 */
            default:
                Log.i("###", "throw error: no case matched");
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values.
     * Return the new content URI for that specific row in the
     * database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        Log.i("###", "entered the insertPet() method in PetProvider class");

        if (values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME).equals("")) {
//            throw new IllegalArgumentException("Pet requires a name");
            Toast.makeText(getContext(),"Pet requires name!",Toast.LENGTH_SHORT).show();
            return null;
        }


        Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetEntry.isValidGender(gender))
            throw new IllegalArgumentException("Pet requires a gender");


        Integer weight = values.getAsInteger((PetContract.PetEntry.COLUMN_PET_WEIGHT));
        if (weight == null)
            throw new IllegalArgumentException("Pet requires a weight");
        //Note that the weight will never be null

        //If weight is null default 0 will be used.
        //No need to apply check on breed, everything is valid including null.


        //To insert the Pet details, we need a database object first.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        Log.i("###", "mDbHelper object of the database obtained in write mode");

        //Now we need to do the database insertion.
        //Call the database insert() method for inserting the details. Pass in the
        //pet table name and the ContentValues object
        //The return value will be the ID of the new row that was just created, in long type
        long id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);
        Log.i("###", "New data inserted id: " + id + "now return");
        if (id == -1) {
            Log.e(LOG_TAG, " Failed to insert for " + uri);
            return null;
        }
        //Notify all the listeners that the data has changed for the pet content URI.
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.i("###", "entered the delete method of PetProvider");
        //Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (match) {
            case PETS:
                //Delete all rows that match the selection and selection args
                Log.i("###", "returning from the delete method of PetProvider after deleting: " + PETS);

                rowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PETS_ID:
                Log.i("###", "returning from the delete method of PetProvider after deleting: " + PETS_ID);

                //Delete a single row given by the ID in the URI
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        //If 1 or more rows were deleted, then notify all listeners
        //that the data at this URI has changed.
        Log.i("####", "check if notifyChange() needs to be called: rowsDeleted: " + rowsDeleted);
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        Log.i("####", " after notifyChange() needs to be called, return the no. of rowsDeleted from delete() in PetProvider");
        return rowsDeleted;

    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     * Update can be be for the whole table or for one/more than one
     * entries.
     * Therefore a switch case will be required
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.i("####", "entered the update method of PetProvider() class");
        //if there are no values to update, return 0.
        if (contentValues == null || contentValues.size() == 0)
            return 0;//nothing to change
        //In the course the size check is done after sanity check, before
        //obtaining the Database object
        //get the match integer
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                Log.i("####", "calling updatePet() and then will return, in petprovider " + PETS);

                return updatePet(uri, contentValues, selection, selectionArgs);


            case PETS_ID:
                Log.i("####", "calling updatePet() and then will return, in petprovider " + PETS_ID);

                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //Update the selected pets in
        // the pets database table with
        // the given ContentValues
        //Sanity Check- All the fields need not be checked like
        //insert(), use the containsKey() method to find out if input is
        //given for that or not.
        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)) {
            String name = contentValues.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name.equals("")) {
//                throw new IllegalArgumentException("Requires a valid name");
                Toast.makeText(getContext(),"Pet requires name!",Toast.LENGTH_SHORT).show();
                return 0;
            }
        }
        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetContract.PetEntry.isValidGender(gender))
                throw new IllegalArgumentException("Requires a valid gender");
        }
        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0)
                throw new IllegalArgumentException("Requires a valid weight");
        }
        //Breed check not required

        //Obtain the database object
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //Returns the number of rows affected by the update statement
        int rowsUpdated = database.update(PetContract.PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        //If the number of updated rows is greater than zero, notify the changes
        //If 1 or more rows were updated, notify all the listeners that the given Uri has
        //changed
        Log.i("####", "updated the database, now check to notify in ContentProvider updatePet()");
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        //Now return the value
        Log.i("####", "checked if rowsUpdated!=0 and notifed the listeners, now return from ContentProvider updatePet()");
        return rowsUpdated;
    }
}
