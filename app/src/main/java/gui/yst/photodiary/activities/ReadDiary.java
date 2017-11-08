package gui.yst.photodiary.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import gui.yst.photodiary.adapter.DiaryApplicationObj;
import gui.yst.photodiary.R;
import gui.yst.photodiary.model.Diary;

//List images in folder http://stackoverflow.com/questions/13418807/how-can-i-display-images-from-a-specific-folder-on-android-gallery
public class ReadDiary extends AppCompatActivity {

    private Diary diary;
    private DiaryApplicationObj diaryApplicationObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_diary);
        diaryApplicationObj = (DiaryApplicationObj) getApplication();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView datetimeTextView = (TextView) findViewById(R.id.datetimeTextView);
        TextView contentTextView = (TextView) findViewById(R.id.contentTextView);
        ImageView diaryImageView = (ImageView) findViewById(R.id.diaryImageView);
        TextView dateNumTextView = (TextView) findViewById(R.id.dateNumTextView);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent i = getIntent();
        diary = (Diary) i.getSerializableExtra("Diary");
        String dateFormat = "MMMM yyyy\nEEEE h:mm a";
        String dateFormat2 = "dd";
        SimpleDateFormat sdfdate = new SimpleDateFormat(dateFormat, Locale.UK);
        SimpleDateFormat sdfdate2 = new SimpleDateFormat(dateFormat2, Locale.UK);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(diary.getDatetime());
        String time = sdfdate.format(calendar.getTime());
        String time2 = sdfdate2.format(calendar.getTime());
        File temp = new File(diaryApplicationObj.checkLatestPhoto(diary));
        if(temp.exists()) {
            Uri file = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", temp);
            diaryImageView.setImageURI(file);
        }else{
            diaryImageView.setVisibility(View.GONE);
        }

        setTitle(diary.getTitle());
        datetimeTextView.setText(time);
        dateNumTextView.setText(time2);
        contentTextView.setText(diary.getComment());
    }

    public void clickImage(View view){
        File[] allFiles ;
        File folder = new File(diary.getLocation());
        allFiles = folder.listFiles();
        new SingleMediaScanner(ReadDiary.this, allFiles[allFiles.length-1]);
    }

    public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

        private MediaScannerConnection mMs;
        private File mFile;

        public SingleMediaScanner(Context context, File f) {
            mFile = f;
            mMs = new MediaScannerConnection(context, this);
            mMs.connect();
        }

        public void onMediaScannerConnected() {
            mMs.scanFile(mFile.getAbsolutePath(), null);
        }

        public void onScanCompleted(String path, Uri uri) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
            mMs.disconnect();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read_diary, menu);
        return true;
    }

    public void edit(MenuItem item){
        Intent editDiary = new Intent(ReadDiary.this, EditDiary.class);
        editDiary.putExtra("Diary", diary);
        startActivity(editDiary);
        finish();
    }

    public void delete(MenuItem item){
        AlertDialog deleteDialog =new AlertDialog.Builder(ReadDiary.this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete "+ diary.getTitle() +" ?")
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        diaryApplicationObj.mDatabaseReference.child("users").child(diaryApplicationObj.uid)
                                .child("diarylist").child(diary.getId()).removeValue();
                        dialog.dismiss();
                        onBackPressed();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        deleteDialog.show();
    }

}
