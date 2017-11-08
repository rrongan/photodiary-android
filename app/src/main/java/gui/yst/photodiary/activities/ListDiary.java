package gui.yst.photodiary.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.orangegangsters.lollipin.lib.PinCompatActivity;
import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import gui.yst.photodiary.CustomPinActivity;
import gui.yst.photodiary.Preferences;
import gui.yst.photodiary.adapter.DiaryApplicationObj;
import gui.yst.photodiary.adapter.DiaryListAdapter;
import gui.yst.photodiary.R;
import gui.yst.photodiary.login.GoogleLogin;
import gui.yst.photodiary.model.Diary;

//Delete Multiple Select Tutorial http://www.androidbegin.com/tutorial/android-delete-multiple-selected-items-listview-tutorial/
//*Sorting http://stackoverflow.com/questions/33213949/how-to-sort-listview-items-by-date
public class ListDiary extends PinCompatActivity {

    ListView diaryListView;
    private DiaryApplicationObj diaryApplicationObj;
    private static final String TAG = "MainActivity";
    private DiaryListAdapter adapter;
    private List<Diary> diaries = new ArrayList<>();
    private SparseBooleanArray selected;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_diary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        diaryApplicationObj = (DiaryApplicationObj) getApplication();
        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(ListDiary.this, CustomPinActivity.class);
        lockManager.getAppLock().setOnlyBackgroundTimeout(true);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(ListDiary.this, GoogleLogin.class));
                }
            }
        };

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListDiary.this, CreateDiary.class));
            }
        });
        diaryListView = (ListView) findViewById(R.id.diaryListView);
        diaryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        diaryApplicationObj.initFirebase(ListDiary.this);
        diaryApplicationObj.mDatabaseReference.child("users").child(diaryApplicationObj.uid).child("diarylist").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        diaries.clear();
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Diary diary = postSnapshot.getValue(Diary.class);
                            diaries.add(diary);
                        }
                        Collections.sort(diaries, new CustomComparator());
                        adapter = new DiaryListAdapter(ListDiary.this, diaries);
                        adapter.notifyDataSetChanged();
                        diaryListView.setAdapter(adapter);
                        diaryListView.setLongClickable(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "Failed to read value.", databaseError.toException());
                    }
                }

        );

        diaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Diary selecteditem = adapter.getItem(position);
                Intent readDiary = new Intent(ListDiary.this, ReadDiary.class);
                readDiary.putExtra("Diary", selecteditem);
                startActivity(readDiary);
            }
        });

        diaryListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                final int checkedCount = diaryListView.getCheckedItemCount();
                mode.setTitle(checkedCount + " Selected");
                adapter.toggleSelection(position);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        selected = adapter.getSelectedIds();
                        AlertDialog deleteDialog =new AlertDialog.Builder(ListDiary.this)
                                .setTitle("Delete")
                                .setMessage("Are you sure you want to delete selected item(s)?")
                                .setIcon(android.R.drawable.ic_delete)
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        for (int i = (selected.size() - 1); i >= 0; i--) {
                                            if (selected.valueAt(i)) {
                                                Diary selecteditem = adapter.getItem(selected.keyAt(i));
                                                diaryApplicationObj.mDatabaseReference.child("users").child(diaryApplicationObj.uid)
                                                        .child("diarylist").child(selecteditem.getId()).removeValue();
                                                diaryApplicationObj.DeleteRecursive(new File(selecteditem.getLocation()));
                                                adapter.remove(selecteditem);
                                            }
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        deleteDialog.show();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.action_list_diary, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                adapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_diary, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true; // handled
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);

                return true;
            }
        });
        return true;
    }

    public void logout(MenuItem item) {
        mAuth.signOut();
    }

    public void setting(MenuItem item) {
        Intent i = new Intent(this, Preferences.class);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class CustomComparator implements Comparator<Diary> {// may be it would be Model
        @Override
        public int compare(Diary obj1, Diary obj2) {
            Calendar calendar = Calendar.getInstance();
            String dateFormat = "dd MMMM yyyy h mm a";
            SimpleDateFormat sdfdate = new SimpleDateFormat(dateFormat, Locale.UK);
            calendar.setTimeInMillis(obj1.getDatetime());
            String obj1time = sdfdate.format(calendar.getTime());
            calendar.setTimeInMillis(obj2.getDatetime());
            String obj2time = sdfdate.format(calendar.getTime());
            return obj1time.compareTo(obj2time);// compare two objects
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }

}
