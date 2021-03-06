package com.ieeemalabar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ieeemalabar.models.Post;
import com.ieeemalabar.models.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends BaseActivity {

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";
    ProgressDialog pd;

    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    StorageReference imageRef;

    private EditText mTitleField;
    private EditText mBodyField;
    private ImageView post_img;
    Activity context;
    Boolean image_selected = false;

    private static final int RESULT_LOAD_IMAGE = 1;
    final int PIC_CROP = 2;
    Uri selectedImage;
    private String condition = "", mPostKey = "";
    private String SBorH, editCollege;
    private DatabaseReference mPostReference;
    private ValueEventListener mPostListener;
    Post post;
    Bitmap bmp;
    Boolean click = false;
    String college, title, body, cancelText = "Cancel Report";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        context = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            condition = extras.getString("condition");
            mPostKey = extras.getString("post_key");
            SBorH = extras.getString("SBorH");
            editCollege = extras.getString("college");
        }

        if (condition.equals("edit")) {
            cancelText = "Cancel Changes";
            //LinearLayout del_lin = (LinearLayout) findViewById(R.id.del_lin);
            //del_lin.setVisibility(View.VISIBLE);

            setTitle("Edit Report");
            TextView pic_name = (TextView) findViewById(R.id.pic_name);
            pic_name.setText("Replace old image (Optional)");

            mPostReference = FirebaseDatabase.getInstance().getReference()
                    .child("posts").child(mPostKey);

            mStorage = FirebaseStorage.getInstance();
            mStorageRef = mStorage.getReferenceFromUrl("gs://project-3576505284407387518.appspot.com/");

            imageRef = mStorageRef.child("featured/" + mPostKey + ".jpg");

            mTitleField = (EditText) findViewById(R.id.field_title);
            mBodyField = (EditText) findViewById(R.id.field_body);
            post_img = (ImageView) findViewById(R.id.post_img);

            mPostListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    post = dataSnapshot.getValue(Post.class);
                    // [START_EXCLUDE]
                    //mAuthorView.setText(post.author);
                    if (!click) {
                        mTitleField.setText(post.title);
                        mBodyField.setText(post.body);

                        SharedPreferences settings = getSharedPreferences("com.ieeemalabar", MODE_PRIVATE);
                        college = settings.getString("college", "");

                        /*Button delete = (Button) findViewById(R.id.delete_b);
                        assert delete != null;
                        delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(context)
                                        .setTitle("Delete entry")
                                        .setMessage("Are you sure you want to delete this entry?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete

                                                PostDetailActivity.PostDA.finish();

                                                pd.setMessage("Deleting...");
                                                pd.show();

                                                mDatabase = FirebaseDatabase.getInstance().getReference();
                                                mStorage = FirebaseStorage.getInstance();
                                                mStorageRef = mStorage.getReferenceFromUrl("gs://project-3576505284407387518.appspot.com/");

                                                imageRef = mStorageRef.child("featured/"+mPostKey+".jpg");

                                                imageRef.delete().addOnSuccessListener(new OnSuccessListener() {
                                                    @Override
                                                    public void onSuccess(Object o) {

                                                        //ActivityMain.AM.finish();

                                                        DatabaseReference delRef1 = FirebaseDatabase.getInstance().getReference()
                                                                .child("posts").child(mPostKey);
                                                        DatabaseReference delRef2 = FirebaseDatabase.getInstance().getReference()
                                                                .child("user-posts").child(college).child(mPostKey);
                                                        delRef1.setValue(null);
                                                        delRef2.setValue(null);

                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).child("author").removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).child("title").removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).child("date").removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).child("college").removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).child("starCount").removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).child("stars").removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).child("uid").removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).child("body").removeValue();

                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey).removeValue();

                                                        //Intent intent = new Intent(NewPostActivity.this, ActivityMain.class);
                                                        //intent.putExtra("delete", "true");
                                                        //intent.putExtra("post_key", mPostKey);
                                                        //startActivity(intent);
                                                        finish();
                                                    }

                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        Toast.makeText(NewPostActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                                PostDetailActivity.PostDA.finish();
                                                ActivityMain.AM.finish();

                                                pd.setMessage("Deleting...");
                                                pd.show();

                                                DatabaseReference delRef1 = FirebaseDatabase.getInstance().getReference()
                                                        .child("posts").child(mPostKey);
                                                DatabaseReference delRef2 = FirebaseDatabase.getInstance().getReference()
                                                        .child("user-posts").child(college).child(mPostKey);
                                                delRef1.removeValue();
                                                delRef2.removeValue();

                                                pd.hide();
                                                startActivity(new Intent(NewPostActivity.this, ActivityMain.class));
                                                finish();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        });*/

                        // [END_EXCLUDE]
                        /*final long ONE_MEGABYTE = 1024 * 1024;
                        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                post_img.setImageBitmap(bmp);
                                post_img.setVisibility(View.VISIBLE);
                                LinearLayout button_linear = (LinearLayout) findViewById(R.id.button_linear);
                                button_linear.setVisibility(View.VISIBLE);
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });*/
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    Toast.makeText(NewPostActivity.this, "Failed to load report.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            mPostReference.addValueEventListener(mPostListener);
        } else {
            if(SBorH.equals("sbevent"))
                setTitle("SB Event Report");
            else if(SBorH.equals("hubevent"))
                setTitle("Hub Event Report");
        }

        pd = new ProgressDialog(this);
        pd.setCancelable(false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReferenceFromUrl("gs://project-3576505284407387518.appspot.com/");

        imageRef = mStorageRef.child("featured/default.jpg");

        mTitleField = (EditText) findViewById(R.id.field_title);
        mBodyField = (EditText) findViewById(R.id.field_body);
        post_img = (ImageView) findViewById(R.id.post_img);
        Button select = (Button) findViewById(R.id.img_buttom);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                //Toast.makeText(NewPostActivity.this, images.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.fab_submit_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(NewPostActivity.this, condition, Toast.LENGTH_SHORT).show();
                if (condition.equals("edit"))
                    editPost();
                else
                    submitPost();
            }
        });
    }

    private void editPost() {
        //Toast.makeText(NewPostActivity.this, "Editing!", Toast.LENGTH_SHORT).show();

        mTitleField = (EditText) findViewById(R.id.field_title);
        mBodyField = (EditText) findViewById(R.id.field_body);
        title = mTitleField.getText().toString().trim();
        body = mBodyField.getText().toString().trim();

        //hide keyboard
        hideSoftKeyboard(this);
        click = true;
        pd.setMessage("Editing Report...");
        pd.show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            // Write new post
                            Calendar c = Calendar.getInstance();
                            System.out.println("Current time => " + c.getTime());

                            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                            String date = df.format(c.getTime());

                            editThisPost(title, body, editCollege);

                            //Finish this Activity, back to the stream
                            //startActivity(new Intent(getApplicationContext(), MainContainer.class));
                            //finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]
    }

    private void submitPost() {
        title = mTitleField.getText().toString().trim();
        body = mBodyField.getText().toString().trim();
        TextView pic_name = (TextView) findViewById(R.id.pic_name);

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        //Image is required
        if (!image_selected) {
            pic_name.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(REQUIRED);
            return;
        }

        //hide keyboard
        hideSoftKeyboard(this);

        pd.setMessage("Saving Report...");
        pd.show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            // Write new post
                            Calendar c = Calendar.getInstance();
                            System.out.println("Current time => " + c.getTime());

                            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                            String date = df.format(c.getTime());
                            if (SBorH.equals("sbevent"))
                                writeNewPost(userId, user.name, title, body, date, user.college);
                            else if (SBorH.equals("hubevent"))
                                writeNewPost(userId, user.name, title, body, date, "HUB EVENT");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String title, String body, String date, String college) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously

        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, title, body, date, college);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        if(SBorH.equals("sbevent"))
            childUpdates.put("/user-posts/" + college + "/" + key, postValues);
        else if(SBorH.equals("hubevent"))
            childUpdates.put("/hub-posts/" + key, postValues);
        childUpdates.remove("");

        mDatabase.updateChildren(childUpdates);

        imageRef = mStorageRef.child("featured/" + key + ".jpg");
        post_img.setDrawingCacheEnabled(true);
        post_img.buildDrawingCache();
        Bitmap bitmap = post_img.getDrawingCache();
        bitmap = getResizedBitmap(bitmap, 750);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                pd.hide();
                finish();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                pd.hide();
                finish();
            }
        });
    }

    private void editThisPost(String title, String body, String college) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously

        mDatabase.child("posts").child(mPostKey).child("title").setValue(title);
        mDatabase.child("posts").child(mPostKey).child("body").setValue(body);

        if(college.equals("HUB EVENT")) {
            mDatabase.child("hub-posts").child(mPostKey).child("title").setValue(title);
            mDatabase.child("hub-posts").child(mPostKey).child("body").setValue(body);
        } else {
            mDatabase.child("user-posts").child(college).child(mPostKey).child("title").setValue(title);
            mDatabase.child("user-posts").child(college).child(mPostKey).child("body").setValue(body);
        }


        if (image_selected) {
            imageRef = mStorageRef.child("featured/" + mPostKey + ".jpg");
            post_img.setDrawingCacheEnabled(true);
            post_img.buildDrawingCache();
            Bitmap bitmap = post_img.getDrawingCache();
            bitmap = getResizedBitmap(bitmap, 750);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    pd.hide();
                    finish();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    pd.hide();
                    finish();
                }
            });
        } else {
            pd.hide();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            image_selected = true;
            selectedImage = data.getData();
            post_img.setImageURI(selectedImage);
            post_img.setVisibility(View.VISIBLE);
            TextView pic_name = (TextView) findViewById(R.id.pic_name);
            pic_name.setError(null);
            String fileName;

            String scheme = selectedImage.getScheme();
            if (scheme.equals("file")) {
                fileName = selectedImage.getLastPathSegment();
                pic_name.setText(fileName);
            } else if (scheme.equals("content")) {
                String[] proj = {MediaStore.Images.Media.TITLE};
                Cursor cursor = context.getContentResolver().query(selectedImage, proj, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                    pic_name.setText(fileName);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
            //performCrop(selectedImage);
        }
        /*if (requestCode == PIC_CROP && data != null) {
            // get the returned data
            Bundle extras = data.getExtras();
            // get the cropped bitmap
            Bitmap selectedBitmap = extras.getParcelable("data");

            ImageView post_img = (ImageView) findViewById(R.id.post_img);
            post_img.setImageBitmap(selectedBitmap);
            post_img.setVisibility(View.VISIBLE);

            TextView pic_name = (TextView) findViewById(R.id.pic_name);
            pic_name.setError(null);
            String fileName;

            String scheme = selectedImage.getScheme();
            if (scheme.equals("file")) {
                fileName = selectedImage.getLastPathSegment();
                pic_name.setText(fileName);
            }
            else if (scheme.equals("content")) {
                String[] proj = { MediaStore.Images.Media.TITLE };
                Cursor cursor = context.getContentResolver().query(selectedImage, proj, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                    pic_name.setText(fileName);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }*/
    }

    /*private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 16);
            cropIntent.putExtra("aspectY", 9);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 1280);
            cropIntent.putExtra("outputY", 720);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }*/

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        title = mTitleField.getText().toString().trim();
        body = mBodyField.getText().toString().trim();

        if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(body)) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            finish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to cancel?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .setTitle(cancelText)
                    .show();
        } else {
            finish();
        }
    }
}
