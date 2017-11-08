package gui.yst.photodiary.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.orangegangsters.lollipin.lib.PinActivity;
import com.github.orangegangsters.lollipin.lib.PinCompatActivity;
import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import gui.yst.photodiary.CustomPinActivity;
import gui.yst.photodiary.SplashScreen;
import gui.yst.photodiary.adapter.DiaryApplicationObj;
import gui.yst.photodiary.R;
import gui.yst.photodiary.activities.ListDiary;
import gui.yst.photodiary.model.User;

//https://firebase.google.com/docs/auth/android/google-signin
//https://www.youtube.com/watch?v=-ywVw2O1pP8
public class GoogleLogin extends PinCompatActivity {

    private Button email_provider;
    private Button google_button;
    private ProgressDialog progressDialog;
    private static final int RC_SIGN_IN = 1;
    public GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "LOGIN";
    private DiaryApplicationObj diaryApplicationObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_google_login);
        email_provider = (Button) findViewById(R.id.email_provider);
        google_button = (Button) findViewById(R.id.google_button);
        mAuth = FirebaseAuth.getInstance();
        diaryApplicationObj = (DiaryApplicationObj) getApplication();
        progressDialog = new ProgressDialog(this);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity(new Intent(GoogleLogin.this, ListDiary.class));
                    diaryApplicationObj.uid = firebaseAuth.getCurrentUser().getUid();
                    diaryApplicationObj.email = firebaseAuth.getCurrentUser().getEmail();
                    diaryApplicationObj.name = firebaseAuth.getCurrentUser().getDisplayName();
                }
            }
        };


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(GoogleLogin.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(GoogleLogin.this, "Error", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        google_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        email_provider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GoogleLogin.this, EmailLogin.class));
            }
        });
    }

    @Override
    protected void onStart() {
        mAuth.addAuthStateListener(mAuthListener);
        super.onStart();
    }


    private void signIn() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    @Override
    public void onBackPressed(){
        this.finishAffinity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                progressDialog.dismiss();
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                if(result.getStatus().getStatusCode() == 7){
                    Toast.makeText(GoogleLogin.this, "Network Error. Please Check Your Network",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(mAuth.getCurrentUser()!=null) {
                            Toast.makeText(GoogleLogin.this, "Welcome, " + mAuth.getCurrentUser().getDisplayName(),
                                    Toast.LENGTH_SHORT).show();

                            diaryApplicationObj.initFirebase(GoogleLogin.this);
                            diaryApplicationObj.mDatabaseReference.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        diaryApplicationObj.uid = mAuth.getCurrentUser().getUid();
                                        diaryApplicationObj.email = mAuth.getCurrentUser().getEmail();
                                        diaryApplicationObj.name = mAuth.getCurrentUser().getDisplayName();
                                    }else{
                                        User user = new User(mAuth.getCurrentUser().getUid(),mAuth.getCurrentUser().getEmail(),mAuth.getCurrentUser().getDisplayName());
                                        diaryApplicationObj.mDatabaseReference.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user,new DatabaseReference.CompletionListener(){
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                diaryApplicationObj.uid = mAuth.getCurrentUser().getUid();
                                                diaryApplicationObj.email = mAuth.getCurrentUser().getEmail();
                                                diaryApplicationObj.name = mAuth.getCurrentUser().getDisplayName();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(GoogleLogin.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

}
