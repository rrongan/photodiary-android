package gui.yst.photodiary.adapter;

import android.app.Application;
import android.content.Context;
import android.widget.ProgressBar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gui.yst.photodiary.model.Diary;
import gui.yst.photodiary.model.User;

//*Firebase Database https://www.youtube.com/watch?v=tAV_ehyZmTE
//*Firebase Database https://www.simplifiedcoding.net/firebase-realtime-database-crud/
public class DiaryApplicationObj extends Application{

    public DatabaseReference mDatabaseReference;
    public FirebaseDatabase mFirebaseDatabase;
    public String uid;
    public String email;
    public String name;
    private List<Diary> diaryList = new ArrayList();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void initFirebase(Context context){
        FirebaseApp.initializeApp(context);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("diaries");
    }

    public String checkLatestPhoto(Diary diary){
        int j = 0;
        String num = Integer.toString(j);
        String tempPath = diary.getLocation()+ File.separator + "IMG_" + num + ".jpg";
        File tempFile = new File(tempPath);
        while(tempFile.exists()){
            j++;
            num = Integer.toString(j);
            tempPath = diary.getLocation()+ File.separator + "IMG_" + num + ".jpg";
            tempFile = new File(tempPath);
        }
        num = Integer.toString(j-1);
        tempPath = diary.getLocation()+ File.separator + "IMG_" + num + ".jpg";
        return tempPath;
    }

    public boolean DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) for (File child : fileOrDirectory.listFiles())
            DeleteRecursive(child);
        return fileOrDirectory.delete();
    };
}
