package gui.yst.photodiary.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import gui.yst.photodiary.model.Diary;

/****************** No longer using for this version ***********************/
//Database Tutorial http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "diaryManager";
    private static final String TABLE_DIARY = "diary";

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DATETIME =  "datetime";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_COMMENT = "comment";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DIARY_TABLE = "CREATE TABLE " + TABLE_DIARY + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT,"
                + KEY_DATETIME + " INTEGER," + KEY_LOCATION + " TEXT," + KEY_COMMENT + " TEXT" + ")";
        db.execSQL(CREATE_DIARY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY);

        // Create tables again
        onCreate(db);
    }

    /*All CRUD*/
    // Adding new Diary
    public long addDiary(Diary diary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TITLE, diary.getTitle());
        values.put(KEY_DATETIME, diary.getDatetime());
        values.put(KEY_LOCATION, diary.getLocation());
        values.put(KEY_COMMENT, diary.getComment());

        long r = db.insert(TABLE_DIARY, null, values);
        db.close();
        return r;
    }

    // Getting single Diary
    public Diary getDiary(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DIARY, new String[] { KEY_ID,KEY_TITLE,
                        KEY_DATETIME, KEY_LOCATION,KEY_COMMENT }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Diary diary = new Diary(cursor.getString(0),
                cursor.getString(1), Integer.parseInt(cursor.getString(2)),
                cursor.getString(3), cursor.getString(4));
        return diary;
    }

    // Getting All Diarys
    public List<Diary> getAllDiaries() {
        List<Diary> diaryList = new ArrayList<Diary>();
        String selectQuery = "SELECT  * FROM " + TABLE_DIARY;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Diary diary = new Diary();
                diary.setId(cursor.getString(0));
                diary.setTitle(cursor.getString(1));
                diary.setDatetime(Long.parseLong(cursor.getString(2)));
                diary.setLocation(cursor.getString(3));
                diary.setComment(cursor.getString(4));
                // Adding contact to list
                diaryList.add(diary);
            } while (cursor.moveToNext());
        }
        return diaryList;
    }

    // Getting Diaries Count
    public int getDiariesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_DIARY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }

    // Updating single Diary
    public int updateDiary(Diary diary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TITLE, diary.getTitle());
        values.put(KEY_DATETIME, diary.getDatetime());
        values.put(KEY_LOCATION, diary.getLocation());
        values.put(KEY_COMMENT, diary.getComment());

        return db.update(TABLE_DIARY, values, KEY_ID + " = ?",
                new String[] { String.valueOf(diary.getId()) });
    }

    // Deleting single Diary
    public void deleteDiary(Diary diary) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DIARY, KEY_ID + " = ?",
                new String[] { String.valueOf(diary.getId()) });
        db.close();
    }
}
