package gui.yst.photodiary.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import gui.yst.photodiary.adapter.DiaryApplicationObj;
import gui.yst.photodiary.R;
import gui.yst.photodiary.activities.ListDiary;
import gui.yst.photodiary.model.User;

//Firebase Android Tutorial User Login https://www.youtube.com/watch?v=tJVBXCNtUuk
public class EmailLogin extends AppCompatActivity {

    private EditText emailLogin;
    private EditText passwordLogin;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DiaryApplicationObj diaryApplicationObj;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        firebaseAuth = FirebaseAuth.getInstance();
        diaryApplicationObj = (DiaryApplicationObj) getApplication();
        progressDialog = new ProgressDialog(this);
        emailLogin = (EditText) findViewById(R.id.emailLogin);
        passwordLogin = (EditText) findViewById(R.id.passwordLogin);

        if (firebaseAuth.getCurrentUser() != null) {
            diaryApplicationObj.uid = firebaseAuth.getCurrentUser().getUid();
            diaryApplicationObj.email = firebaseAuth.getCurrentUser().getEmail();
            diaryApplicationObj.name = firebaseAuth.getCurrentUser().getDisplayName();
            startActivity(new Intent(EmailLogin.this, ListDiary.class));
            finish();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void userLogin(View view) {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (firebaseAuth.getCurrentUser() != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(firebaseAuth.getCurrentUser().getEmail().split("@")[0])
                                    .build();

                            firebaseAuth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener((new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EmailLogin.this, "Welcome " + firebaseAuth.getCurrentUser().getDisplayName(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }));

                            diaryApplicationObj.initFirebase(EmailLogin.this);
                            User user = new User(firebaseAuth.getCurrentUser().getUid(), firebaseAuth.getCurrentUser().getEmail(), firebaseAuth.getCurrentUser().getDisplayName());
                            diaryApplicationObj.mDatabaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        diaryApplicationObj.uid = firebaseAuth.getCurrentUser().getUid();
                                        diaryApplicationObj.email = firebaseAuth.getCurrentUser().getEmail();
                                        diaryApplicationObj.name = firebaseAuth.getCurrentUser().getDisplayName();
                                    }else{
                                        User user = new User(firebaseAuth.getCurrentUser().getUid(),firebaseAuth.getCurrentUser().getEmail(),firebaseAuth.getCurrentUser().getDisplayName());
                                        diaryApplicationObj.mDatabaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).setValue(user,new DatabaseReference.CompletionListener(){
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                diaryApplicationObj.uid = firebaseAuth.getCurrentUser().getUid();
                                                diaryApplicationObj.email = firebaseAuth.getCurrentUser().getEmail();
                                                diaryApplicationObj.name = firebaseAuth.getCurrentUser().getDisplayName();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        if (task.isSuccessful()) {
                            startActivity(new Intent(EmailLogin.this, ListDiary.class));
                            finish();
                        } else {
                            Toast.makeText(EmailLogin.this, "Invalid Email Or Password. Please Try Again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signUpActivity(View view) {
        startActivity(new Intent(EmailLogin.this, EmailRegister.class));
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("EmailLogin Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
