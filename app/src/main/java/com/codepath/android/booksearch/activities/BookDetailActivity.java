package com.codepath.android.booksearch.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.android.booksearch.R;
import com.codepath.android.booksearch.models.Book;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

@RuntimePermissions
public class BookDetailActivity extends AppCompatActivity {
    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvPublisherYear;
    private TextView tvPublisher;

    private Intent shareIntent;
    private ShareActionProvider miShareAction;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
       // BookDetailActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        // Fetch views
        ivBookCover = (ImageView) findViewById(R.id.ivBookCover);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvAuthor = (TextView) findViewById(R.id.tvAuthor);
        tvPublisherYear = (TextView) findViewById(R.id.tvPublisherYear);
        tvPublisher = (TextView) findViewById(R.id.tvPublisher);

        // Extract book object from intent extras
        Book selectedBook = (Book) getIntent().getParcelableExtra("book");

        setSupportActionBar((Toolbar) findViewById(R.id.tbBookDetail));
        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.setTitle(selectedBook.getTitle()); // set the top title

        //actionBar.hide(); // or even hide the actionbar

        // Use book object to populate data into views
        tvTitle.setText(selectedBook.getTitle());
        tvAuthor.setText(selectedBook.getAuthor());
        tvPublisherYear.setText(selectedBook.getPublisherYear());
        tvPublisher.setText(selectedBook.getPublisher());
        Picasso.with(getApplicationContext()).load(selectedBook.getCoverUrl()).into(ivBookCover, new Callback() {
            @Override
            public void onSuccess() {
                // Setup share intent now that image has loaded
                prepareShareIntent();
                attachShareIntentAction();
            }

            @Override
            public void onError() {
                // ...
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        MenuItem item = menu.findItem(R.id.miShare);
        miShareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        attachShareIntentAction();
        //miShareAction.setShareIntent(shareIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.miShare:
                Log.d(LOG_TAG, "Share...");
                return true;
            case R.id.action_settings:
                Log.d(LOG_TAG, "settings...");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    // Gets the image URI and setup the associated share intent to hook into the provider
    public void prepareShareIntent() {
        // Fetch Bitmap Uri locally
        ImageView ivImage = (ImageView) findViewById(R.id.ivBookCover);
        Uri bmpUri = getLocalBitmapUri(ivImage); // see previous remote images section
        // Construct share intent as described above based on bitmap
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.setType("image/*");
    }

    // Attaches the share intent to the share menu item provider
    public void attachShareIntentAction() {
        if (miShareAction != null && shareIntent != null)
            miShareAction.setShareIntent(shareIntent);
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        //try {
        // TODO higher version use some permissions
        // getExternalFilesDir() + "/Pictures" should match the declaration in fileprovider.xml paths
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
        // wrap File object into a content provider
        bmpUri = FileProvider.getUriForFile(BookDetailActivity.this, "com.codepath.fileprovider", file);

        // Use methods on Context to access package-specific directories on external storage.
        // This way, you don't need to request external read/write permission.
        // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            /*File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
            bmpUri = Uri.fromFile(file);*/
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/
        return bmpUri;
    }

}
