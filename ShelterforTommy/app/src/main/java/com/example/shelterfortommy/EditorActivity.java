package com.example.shelterfortommy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.example.shelterfortommy.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;
    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;// to store the gender
    /**
     * Identifier for the currentPetURI
     */
    private static Uri mCurrentPetUri;
    /**
     * Identifier for the Pet Data Loader
     */
    private static final int EXISTING_PET_LOADER = 0;

    /**
     * A boolean variable to check whether changes were made
     * This will be used for warning the user.
     */
    private boolean mPetHasChanged = false;
    /**
     * Setup the Touch Listener for the editorActivity.
     * If the user touches any field, we'll know that the user is
     * adding something.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    /**
     * Method to create the dialog box.
     *
     * @param discardButtonClickListener :OnClickListener object
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_message);
        builder.setPositiveButton(R.string.discard_changes, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        //Create and show AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        //This code makes a AlertDialog using the
        // AlertDialogBuilder. The method accepts a
        // OnClickListener for the discard button.
        // We do this because the behavior for
        // clicking back or up is a little bit
        // different.
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    //Hook up the back button
    @Override
    public void onBackPressed() {
        //If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "Discard" button, close the current activity.
                finish();
            }
        };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Log.i("###", "entered onCreate() inside EditorActivity.java");

        //Examine the intent that was used to launch this activity.
        //In order to figure out if we're creating a new pet or editing an existing on.
        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();
        Log.i("###?", "Intent and URI obtained in EditorActivity onCreate(): " + mCurrentPetUri);
        //If the intent DOES NOT contain a pet content URI, then we know that
        //we are creating a new pet.
        if (mCurrentPetUri == null) {
            //This is a new pet, so set the app bar title as "Add a Pet".
            setTitle(getString(R.string.add_a_pet));//Add Pet
            //Also hide the delete option
            invalidateOptionsMenu();
        } else {
            //Otherwise this is an existing pet, so change the bar to say "Edit Pet"
            setTitle(getString(R.string.edit_the_clicked_pet));//Edit Pet
            //Initialise the loader, using
            LoaderManager loaderManager = LoaderManager.getInstance(this);
            loaderManager.initLoader(EXISTING_PET_LOADER, null, this);
            //If the loader is not started inside else, a NullPointerException will be thrown inside
            //the doInBackground() method (some superclass method)
            Log.i("###?", "loader started in EditorActivity.java");
        }
        //Find all relevant views that will need input from user
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        Log.i("###", "all the views obtained inside onCreate() inside EditorActivity.java");
        Log.i("###", "calling setUpSpinner() inside onCreate() in EditorActivity.java");

        setUpSpinner();

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        Log.i("###?", "returning from the onCreate() method of EditorActivity");

    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setUpSpinner() {
        Log.i("###", "entered setUpSpinner() in EditorActivity.java");

        //Create adapter for spinner. The list options are from the String array it will use
        //the spinner wll use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_gender_options, android.R.layout.simple_dropdown_item_1line);
        //Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);
        //Set the integer mSelected to the constant values
        Log.i("###", "adapter for the Spinner set setUpSpinner() in EditorActivity.java");

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Log.i("###", "entered onItemSelected() in setUpSpinner() in EditorActivity.java");

                String selection = (String) adapterView.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male)))
                        mGender = PetEntry.GENDER_MALE;//Form male
                    else if (selection.equals(getString(R.string.gender_female)))
                        mGender = PetEntry.GENDER_FEMALE;//Form female
                    else
                        mGender = PetEntry.GENDER_UNKNOWN;//Unknown
                }//PetContract.PetEntry not required because the class is imported
                Log.i("###", "exiting onItemSelected() in EditorActivity.java");
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i("###", "entered onNothingSelected() in EditorActivity.java");

                mGender = 0;//Unknown
                Log.i("###", "exiting onNothinSelected()");
            }
        });
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        //If this is a new pet, hide the "Delete" menu item.
        if(mCurrentPetUri==null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("###", "entered onCreateOptionMenu() in EditorActivity.java");
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        Log.i("###", "Inflated the menu editor in " +
                "onCreateOptionsMenu in EditorActivity.java");
        return true;
    }

    //refactor insertPet() to savePet() for more generic meaning.
    private void savePet() {
        Log.i("###", "entered the insertPet() method in EditorActivity()");
        //Read the inputs from the input fields
        //Note that the Gender value was stored in mGender variable
        String nameString = mNameEditText.getText().toString().trim();//checked
        String breedString = mBreedEditText.getText().toString().trim();//check not required
        String weightString = mWeightEditText.getText().toString().trim();//checked
        /*
        Problem with EditText
        Although we have set the default values for the entries and according to our expectation,
        the name should be null if nothing is entered.
        But, since we are using EditText, it automatically sets null inputs as "Empty Strings s="" "
        That's why we need to handle the cases accordingly.
         */
        //If all the fields are empty and gender is unknown, simply return and do not
        //save anything in the database
        if (mCurrentPetUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) && TextUtils.isEmpty(weightString)
                && mGender == PetEntry.GENDER_UNKNOWN) {
            Toast.makeText(this, "Nothing Saved!", Toast.LENGTH_LONG).show();
            return;
        }
        //If weight is not set then place default 0.
        int weight;
        if (TextUtils.isEmpty(weightString))
            weight = 0;
        else
            weight = Integer.parseInt(weightString);
        //Create a database helper object.


        //Create the ContentValues object where column names are the key,
        //and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

//        //long newRowID=db.insert(PetEntry.TABLE_NAME,null,values);
//        //return value of db.insert is int therefore check if -1 or not
//        Uri newUri = getContentResolver().insert(PetEntry.Content_URI, values);
//        //Return value is uri therefore check if null or not
//        if (newUri == null)/*newRowID==-1*/ {
//            //If the row ID is -1, then there was an error with insertion.
//            Toast.makeText(this, "Error while saving the pet data!", Toast.LENGTH_LONG).show();
//            return false;
//        } else {
//            //Toast.makeText(this,"Pet saved with row ID: "+newRowID,Toast.LENGTH_LONG).show();
//            Toast.makeText(this, "Details saved", Toast.LENGTH_LONG).show();
//
//        }
        //Determine if the currentUri is null or not
        //If its null, insert a new pet else update existing
        if (mCurrentPetUri == null) {
            Log.i("###?", "Adding new pet in savePet() " + mCurrentPetUri);
            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(PetEntry.Content_URI, values);
            //Show a text message if the insertion was successful
            if (newUri == null) {
                Toast.makeText(this, "Failed!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_LONG).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentPetUri, values, null, null);
            //SHow a toast message depending on whether the update was successful or not.
            if (rowsAffected == 0) {
                Toast.makeText(this, "Update failed! Try Again!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Updated Successfully", Toast.LENGTH_LONG).show();

            }
        }
        Log.i("###", "Exiting the insertPet() method of EditorActivity class");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //User clicked on menu option in the app bar overflow menu
        Log.i("###", "entered onOptionsItemSelected()" +
                " in EditorActivity.java");
        switch (item.getItemId()) {

            //Respond to a click on the "Save" menu option.
            case R.id.action_save:
                //When Save icon (Tick) is clicked, display a toast message
                //and return back to the main catalog screen.
                savePet();
                //to go back to the catalog screen, call the finish() method.
                finish();
                Log.i("###", "called the finish() method in EditorActivity()");

                //Display a toast message whether the operating was successful or not.
                return true;
            //Respond to a click on the "Delete" menu option.
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                //Hook up the Up button with the warning dialog
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        Log.i("###", "Returning from onOptionsItemSelected() in EditorActivity.java");
        return super.onOptionsItemSelected(item);
    }
    private void deletePet()
    {
        int rowsDeleted=-1;
        //Perform the delete operation only when mCurrentPet!=null
        if(mCurrentPetUri!=null)
        // Call the ContentResolver to delete the pet at the given content URI.
        // Pass in null for the selection and selection args because the mCurrentPetUri
        // content URI already identifies the pet that we want.
            rowsDeleted=getContentResolver().delete(mCurrentPetUri,null,null);
        //Show toast message
        if(rowsDeleted==0)
        {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.deletion_operation_failed), Toast.LENGTH_LONG).show();
        }
        else
        {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_pet_successful), Toast.LENGTH_SHORT).show();
        }
        //Once operation is done, activity can be closed using finish() method
        finish();

    }
    private void showDeleteConfirmationDialog()
    {
        //Create an AlertDialog.Builder and set the message, and click
        //listeners for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_dialog_message));
        AlertDialog.Builder del = builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //user clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if(dialog!=null)
                    dialog.dismiss();
            }
        });
        //Create and show te AlertDialog
        AlertDialog alertDialog= builder.create();
        alertDialog.show();
    }
    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * <p>This will always be called from the process's main thread.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.i("###?", "entered the onCreateLoader in EditorActivity id: " + id);
        //SInce the editor shows all the pet attributes, define a projection
        //that contains all the columns from the pet table
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT};
        //This loader will execute the ContentProvider's query method on a background thread.
        Log.i("###?", "returning a cursor loader from onCreateLoader() in EditorActivity");
        return new androidx.loader.content.CursorLoader(this,//Parent activity context
                mCurrentPetUri, //Query the content URI for the current Pet
                projection,     //Columns to include in the resulting Cursor
                null,   //No selection clause
                null,//no selection arguments
                null);//default sort order
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     *
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     *
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * <p>This will always be called from the process's main thread.
     *  @param loader The Loader that has finished.
     *
     * @param cursor The data generated by the Loader.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.i("###?", "Entered the onLoadFinished() method in EditorActivity");
        //bail early if the cursor is null or
        //there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1)
            return;
        //Proceed with moving to the first row of the cursor and reading data
        //from it (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            Log.i("###?", "Cursor.moveToFirst() is found true");
            //Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
            //Extract out the value from the cursor for the given column
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            //int weight = 0;

            int weight = cursor.getInt(weightColumnIndex);


            Log.e("###?", "Weight obtained in onLoadFinished(): in EditorActivity " + weight + " column index: " + weightColumnIndex);

            //Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (gender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }

        }
        Log.i("###?", "returning from the onLoadFinished() method in EditorActivity");

    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * <p>This will always be called from the process's main thread.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.i("###?", "entered onLoadReset() method in EditorActivity");
        //If the loader is invalidated, clear out all the data from
        //the input fields.
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);//select unknown gender
        Log.i("###?", "returning from onLoadReset() method in EditorActivity");

    }
}
