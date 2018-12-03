package ie.ul.daveberry.photobucket;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.koushikdutta.ion.Ion;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PhotoDetailActivity extends AppCompatActivity {

  private DocumentReference mDocRef;
  private DocumentSnapshot mPhotoSnapshot;
  private TextView mCaptionTextView;
  private ImageView mImageView;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_photo_detail);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mCaptionTextView = findViewById(R.id.detail_caption);
    mImageView = findViewById(R.id.detail_image_view);

    String docId = getIntent().getStringExtra(Constants.EXTRA_DOC_ID);
    mCaptionTextView.setText(docId);

    mDocRef = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_PATH).document(docId);

    mDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
      @Override
      public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
          Log.w(Constants.TAG, "Listener failed", e);
          return;
        }
        if (documentSnapshot.exists()) {
          mPhotoSnapshot = documentSnapshot;
          mCaptionTextView.setText((String)documentSnapshot.get(Constants.KEY_CAPTION));

          Ion.with(mImageView).load((String)documentSnapshot.get(Constants.KEY_URL));
        }
      }
    });

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showEditDialog();
      }
    });
  }

  private void showEditDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Edit Photograph");
    View view = getLayoutInflater().inflate(R.layout.photo_dialog, null, false);
    builder.setView(view);
    final EditText captionEditText = view.findViewById(R.id.dialog_caption_edittext);
    final EditText imageUrlEditText = view.findViewById(R.id.dialog_image_url_edittext);
    captionEditText.setText((String)mPhotoSnapshot.get(Constants.KEY_CAPTION));
    imageUrlEditText.setText((String)mPhotoSnapshot.get(Constants.KEY_URL));

    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Map<String, Object> photo = new HashMap<>();
        photo.put(Constants.KEY_CAPTION, captionEditText.getText().toString());
        photo.put(Constants.KEY_URL, imageUrlEditText.getText().toString());
        photo.put(Constants.KEY_CREATED, new Date());
        mDocRef.update(photo);
      }
    });
    builder.setNegativeButton(android.R.string.cancel, null);
    builder.create().show();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch ( item.getItemId()) {
      case R.id.action_remove:
        mDocRef.delete();
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
