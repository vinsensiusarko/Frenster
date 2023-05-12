package informatika.frenster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AvatarActivity extends AppCompatActivity {

    private ImageView myAvatar;
    private EditText myNickName;
    private Button saveButton;
    private String imageUrl;

    private FirebaseFirestore firestore;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);

        myAvatar = findViewById(R.id.imageViewAvatar);
        myNickName = findViewById(R.id.editTextNickName);
        saveButton = findViewById(R.id.buttonSave);

        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        getUserData();

        myAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

    }

    private void getUserData(){
        DocumentReference userRef = firestore.collection("User").document(FirebaseAuth.getInstance().getUid());

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    User user = task.getResult().toObject(User.class);

                    if (user.getNickname() != null){
                        myNickName.setText(user.getNickname());
                    }else{
                        myNickName.setText(user.getEmail());
                    }
                    getAvatar();
                }

            }
        });
    }

    private void getAvatar(){
        storageRef.child(FirebaseAuth.getInstance().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri != null){
                    Picasso.get().load(uri).into(myAvatar);
                }else{
                    myAvatar.setImageResource(R.drawable.avatar);
                }
            }
        });
    }

    private void getImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            Picasso.get().load(filePath).into(myAvatar);
        }else{
            Toast.makeText(this, "Tidak ada gambar yang dipilih", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(){
        if (filePath != null){
            final StorageReference ref = storageRef.child(FirebaseAuth.getInstance().getUid());
            UploadTask uploadTask = ref.putFile(filePath);
            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUri = task.getResult();

                    imageUrl = downloadUri.toString();

                    saveAvatarUrl(imageUrl);

                    saveNickname();
                }
            });
        }else{
            saveNickname();
        }
    }

    private void saveAvatarUrl(String imageUrl){
        DocumentReference documentRef = firestore.collection("User").document(FirebaseAuth.getInstance().getUid());
        documentRef.update("avatar", imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AvatarActivity.this, "Your avatar has been updated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNickname(){
        DocumentReference documentRef = firestore.collection("User").document(FirebaseAuth.getInstance().getUid());
        documentRef.update("nickname", myNickName.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AvatarActivity.this, "Your nickname has been updated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.menu_location:{
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }

            case R.id.menu_logout:{
                logOut();
                return true;
            }

            default:{
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void logOut(){
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
