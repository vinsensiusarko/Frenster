package informatika.frenster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class SignupActivity extends AppCompatActivity {

    private EditText txtEmail, txtPassword, txtConfirm;
    private Button tombolRegister;
    private ProgressBar progressBar;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        txtEmail = findViewById(R.id.editTextEmail);
        txtPassword = findViewById(R.id.editTextPassword);
        txtConfirm = findViewById(R.id.editTextConfirm);
        tombolRegister = findViewById(R.id.buttonRegister);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        firestore = FirebaseFirestore.getInstance();

        tombolRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cek apakah tidak kosong
                if (!datakosong(txtEmail.getText().toString())
                    &&!datakosong(txtPassword.getText().toString())
                    &&!datakosong(txtConfirm.getText().toString()))
                {

                    //cek apakah password sama dengan confirm password
                if (isMatch(txtPassword.getText().toString(),txtConfirm.getText().toString())){
                    //melakukan proses registrasi
                    registerNewEmail(txtEmail.getText().toString(), txtPassword.getText().toString());
                }
                }
            }
        });
    }

    private void registerNewEmail(final String email, String password){

        progressBar.setVisibility(View.VISIBLE);

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    User user = new User();
                    user.setEmail(email);
                    user.setAvatar("https://firebasestorage.googleapis.com/v0/b/frenster-informatika.appspot.com/o/user.png?alt=media&token=c0e4e369-3dc9-475f-8432-5467772ef532");

                    user.setUser_id(FirebaseAuth.getInstance().getUid());

                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();

                    firestore.setFirestoreSettings(settings);

                    DocumentReference newUserRef = firestore.collection("User").document(FirebaseAuth.getInstance().getUid());

                    newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.VISIBLE);
                            if(task.isSuccessful()){
                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                finish();
                            }else{
                                View parentLayout = findViewById(R.id.content);
                                Snackbar.make(parentLayout, "Something Wrong", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    View parentLayout = findViewById(R.id.content);
                    Snackbar.make(parentLayout, "Something Wrong", Snackbar.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private boolean datakosong(String text){
        return text.equals("");
    }

    private boolean isMatch(String text1, String text2){
        return  text1.equals(text2);

    }
}
