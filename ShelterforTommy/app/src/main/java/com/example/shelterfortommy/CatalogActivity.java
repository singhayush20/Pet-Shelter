package com.example.shelterfortommy;

import androidx.loader.app.LoaderManager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.loader.content.Loader;

import com.example.shelterfortommy.data.PetContract.PetEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Make an Integer Loader Constant for the Laoder
     */
    private static final int PET_LOADER = 0;
    //The value can be anu valid integer value. It will be
    //used to identify the loader
    //Declare an adapter variable for our list view
    //Global because this will be used multiple times
    PetCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        Log.i("###", "entered onCreate()  in CatalogActivity");

        //Set FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("###", "entered onClick() inside Listener for FAB in CatalogActivity");

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
                Log.i("###", "activity editor started exiting onClick Listener for FAB in CatalogActivity");
            }
        });
        mCursorAdapter=new PetCursorAdapter(this,null,0);
        ListView listView=findViewById(R.id.list);
        View emptyView=findViewById(R.id.empty_view);
        //set the empty view of the listview.
        listView.setEmptyView(emptyView);
        listView.setAdapter(mCursorAdapter);
        Log.i("###","Adapter set");
        //Set the Listener for the list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                /*
                 Create a new Intent to go to the {@link EditorActivity class}
                 */
                Intent intent=new Intent(CatalogActivity.this,EditorActivity.class);

                //FOr the content URI that represents the specific pet that was clicked on,
                //by appending the "id" (passed as input to this method) onto the
                //PetEntry.CONTENT_URI
                //For example, the URI would be "content://com.example.pets/pets/2"
                //If the pet with ID 2 was clicked on.
                Uri currentPetUri= ContentUris.withAppendedId(PetEntry.Content_URI,id);

                //Set the URI on the data field of the intent
                intent.setData(currentPetUri);
                //lauch the activity to display the data for the current pet.
                startActivity(intent);
            }
        });
        //We will use the getInstance() method of the androidx.loader.app.LoaderManager class to get
        //an instance of it
        //https://developer.android.com/reference/androidx/loader/app/LoaderManager#getInstance(T)
        LoaderManager loaderManager=LoaderManager.getInstance(this);
        //Start the loader
        loaderManager.initLoader(PET_LOADER,null,this);
        Log.i("###", "loader started and exiting onCreate() inside Catalog activity");
    }

    /**
     * Following method will also be invoked when the method
     * finish() is called in the Editor Activity.
     * Refer the Activity Lifecycle theory.
     */

    @Override
    protected void onStart() {
        Log.i("###", "Entered the onStart() method");
        super.onStart();
        //displayDatabaseInfo();
        Log.i("###", "exiting the onStart() method of Catalog activity");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu options from the res/menu./menu_catalog.xml file.
        //This adds menu items to the app bar.
        Log.i("###", "entered onCreateOptionsMenu  in CatalogActivity");

        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        Log.i("###", "exiting onCreateOptionsMenu  in CatalogActivity" +
                "after inflating menu catalog");

        return true;
    }

    private void insertPet() {
        Log.i("###", "Entered insertPet() method in CatalogActivity class");

        /*Create a ContentValues object where column names are the keys,
        and pet's attributes are the values.
         */
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insert a new row for Toto in the database, returning the ID of that new row.
        // The first argument for db.insert() is the pets table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
        //long newRowId=db.insert(PetEntry.TABLE_NAME,null,values);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(PetEntry.Content_URI, values);
        Log.i("###", "invoked insert() method in insertPet() method in CatalogActivity class, uri obtained: " + newUri.toString());
        Log.i("###", "Returning from insert() in Catalog Activity");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //When user clicks on a menu option in the app bar overflow menu
        Log.i("###", "entered onOptionsItemSelected() in CatalogActivity");
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                //We will take some action when user clicks insert dummy data menu option
                insertPet();
                //displayDatabaseInfo();//for testing purpose
                return true;
            //Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        Log.i("###", "exiting onOptionsItemSelected() in CatalogActivity");

        return super.onOptionsItemSelected(item);
    }
    private void deleteAllPets()
    {
        int rowsDeleted=getContentResolver().delete(PetEntry.Content_URI,null,null);
        Log.i("###",rowsDeleted+"rows deleted from pet database");
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * <p>This will always be called from the process's main thread.
     * This method will execute the ContentProvider's query() method on a \
     * background thread.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.i("###@","Entered the onCreateLoader() method");
        String[] projections = {
                PetEntry._ID, PetEntry.COLUMN_PET_NAME, PetEntry.COLUMN_PET_BREED};
        //Note that we are going to display the name and breed.
        //The ID IS ALWAYS NEEDED BY THE CURSOR THAT WE'RE GOING TO PASS
        //TO ANY CursorAdapter.
        //This loader will execute the ContentProvider's onCreate() method
        //on a background thread.
        Log.i("###@","returning the CursorLoader from onCreateLoader()");
        return new androidx.loader.content.CursorLoader(this, PetEntry.Content_URI, projections
                , null, null, null);
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
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.i("###@","entered the onLoadFinished method ");
        mCursorAdapter.swapCursor(data);//swap the cursor for the adapter
        Log.i("###@","returning from the onLoadFinished method after swapping cursor");
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
        Log.i("###@","entered the onLoaderReset method");
        mCursorAdapter.swapCursor(null);
        Log.i("###@","returning from the onLoaderReset");
        //onLoaderReset is called when the cursor becomes invalid
        //after a data update.
    }
}
