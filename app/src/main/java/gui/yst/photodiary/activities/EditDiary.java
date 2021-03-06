package gui.yst.photodiary.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import gui.yst.photodiary.adapter.DiaryApplicationObj;
import gui.yst.photodiary.R;
import gui.yst.photodiary.model.Diary;

//*Firebase Database https://firebase.google.com/docs/database/android/read-and-write
public class EditDiary extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private DiaryApplicationObj diaryApplicationObj;
    private Diary diary;
    private Diary newDiary;
    private ImageView photo;
    private EditText datepickerText;
    private EditText timepickerText;
    private EditText diaryTitle;
    private EditText content;
    private Uri orifile;
    private Uri file;
    private File diaryDir;
    private File tempFile = null;
    private String path;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_diary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        checkPermission();
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent i = getIntent();
        diary = (Diary) i.getSerializableExtra("Diary");
        diaryApplicationObj = (DiaryApplicationObj) getApplication();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        diaryTitle = (EditText) findViewById(R.id.diaryTitle);
        photo = (ImageView) findViewById(R.id.photo);
        content = (EditText) findViewById(R.id.content) ;
        datepickerText = (EditText) findViewById(R.id.datepickerText);
        datepickerText.setText(sdfdate.format(calendar.getTime()));
        timepickerText = (EditText) findViewById(R.id.timepickerText);
        timepickerText.setText(sdftime.format(calendar.getTime()));
        diaryTitle.setText(diary.getTitle());
        content.setText(diary.getComment());
        calendar.setTimeInMillis(diary.getDatetime());
        datepickerText.setText(sdfdate.format(calendar.getTime()));
        timepickerText.setText(sdftime.format(calendar.getTime()));
        path = diary.getLocation();
        diaryDir = new File(path);
        orifile = FileProvider.getUriForFile(this,this.getApplicationContext().getPackageName() + ".provider",new File(diaryApplicationObj.checkLatestPhoto(diary)));
        photo.setImageURI(orifile);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkPermission(){
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    100);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = FileProvider.getUriForFile(this,this.getApplicationContext().getPackageName() + ".provider", getOutputMediaFile());
        if(tempFile != null) {
            diaryApplicationObj.DeleteRecursive(tempFile);
        }
        tempFile = getOutputMediaFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                photo.setImageURI(file);
            }
        }
    }

    private File getOutputMediaFile(){
        if (!diaryDir.exists()){
            if (!diaryDir.mkdirs()){
                return null;
            }
        }

        int i = 0;
        String num = Integer.toString(i);
        File f = new File(diaryDir.getPath() + File.separator + "IMG_" + num + ".jpg");
        while (f.exists()) {
            i++;
            num = Integer.toString(i);
            f = new File(diaryDir.getPath() + File.separator + "IMG_"+ num +".jpg");
        }
        return f;
    }

    public void saveButtonPressed (View view){
        String title = diaryTitle.getText().toString().trim();
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this,"Please Enter Title",Toast.LENGTH_SHORT).show();
            return;
        }
        title = diaryTitle.getText().toString();
        long datetime = calendar.getTimeInMillis();
        String location = path;
        String comment = content.getText().toString();
        diaryApplicationObj.initFirebase(this);
        String diaryId = diary.getId();
        newDiary = new Diary(diaryId,title,datetime,location,comment);
        diaryApplicationObj.mDatabaseReference.child("users").child(diaryApplicationObj.uid).child("diarylist").child(diaryId).setValue(newDiary, new DatabaseReference.CompletionListener(){
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(EditDiary.this, "Edited Successfully", Toast.LENGTH_SHORT).show();
                    Intent readDiary = new Intent(EditDiary.this, ReadDiary.class);
                    readDiary.putExtra("Diary", newDiary);
                    startActivity(readDiary);
                    finish();
                }else{
                    Toast.makeText(EditDiary.this, "Error. Please Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void clearButtonPressed (View view){
        clear();
    }

    public void clear(){
        diaryTitle.setText(diary.getTitle());
        content.setText(diary.getComment());
        calendar.setTimeInMillis(diary.getDatetime());
        datepickerText.setText(sdfdate.format(calendar.getTime()));
        timepickerText.setText(sdftime.format(calendar.getTime()));
        if(tempFile != null) {
            diaryApplicationObj.DeleteRecursive(tempFile);
        }
        photo.setImageURI(orifile);
    }

    public void datepickerTextPressed (View view){
        new DatePickerDialog(EditDiary.this, date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void timepickerTextPressed (View view){
        new TimePickerDialog(EditDiary.this, time, calendar
                .get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),false).show();
    }

    Calendar calendar = Calendar.getInstance();
    String dateFormat = "EEEE, yyyy/MM/dd"; //In which you need put here
    SimpleDateFormat sdfdate = new SimpleDateFormat(dateFormat, Locale.UK);
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            datepickerText.setText(sdfdate.format(calendar.getTime()));
        }
    };

    String timeFormat = "h:mm a"; //In which you need put here
    SimpleDateFormat sdftime = new SimpleDateFormat(timeFormat, Locale.UK);
    TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            timepickerText.setText(sdftime.format(calendar.getTime()));
        }
    };

    @Override
    public void onBackPressed(){
        clear();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
