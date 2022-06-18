package com.example.shelterfortommy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PetDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = PetDbHelper.class.getSimpleName();

    /*Name of the datbase file*/
    private static final String DATABASE_NAME = "shelter.db";

    /**
     * Database version. If you change the databse schema,
     * you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Construct a new instance of {@link PetDbHelper}
     *
     * @param context of the app
     */
    static int count1 = 0;
    static int count2 = 0;

    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        count1++;
        //cursor factory- null is passed to use the default.
        Log.i("###", "entered the PetDbHelper constructor" + count1 + " context: " + context.toString());
        Log.i("###", "Exiting the PetDbHelper() constructor" + count1);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //This is where the creation and the initial population
        //of the table will happen
        count2++;
        Log.i("####", "Entered the onCreate() method of the PetDbHelper class " + count2);
        //Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + PetContract.PetEntry.TABLE_NAME + " ("
                + PetContract.PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetContract.PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetContract.PetEntry.COLUMN_PET_BREED + " TEXT, "
                + PetContract.PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                + PetContract.PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
        //Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);
        Log.i("#####", "The SQL string generated on creating the database: " + SQL_CREATE_PETS_TABLE);
        Log.i("###", "Exiting the onCreate() method in PetDbHelper class");

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.i("###", "Entered the onUpgrade() method in PetDbHelper class");
        // The database is still at version 1, so there's nothing to do be done here.
        Log.i("###", "Exiting the onUpgrade() method in PetDbHelper class");

    }
}
