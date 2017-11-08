package gui.yst.photodiary.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import gui.yst.photodiary.R;

//Firebase Android Tutorial User Registration https://www.youtube.com/watch?v=0NFwF7L-YA8&t=528s
public class EmailRegister extends AppCompatActivity {
    
    private EditText emailRegister;
    private EditText passwordRegister;
    private EditText reEnterPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_register);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        emailRegister = (EditText) findViewById(R.id.emailRegister);
        passwordRegister = (EditText) findViewById(R.id.passwordRegister);
        reEnterPassword = (EditText) findViewById(R.id.reEnterPassword);
    }

    public void registerUser(View view){
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();
        String reenter = reEnterPassword.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please Enter Email",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please Enter Password",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!TextUtils.equals(password,reenter)){
            Toast.makeText(this,"Passwords Is Not Equal. Please Try Again",Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Registering User...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(EmailRegister.this, "Registered Successfully",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(EmailRegister.this, "Registered Failed. Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    public void signInActivity(View view){
        super.onBackPressed();
    }
}
