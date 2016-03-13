package com.flopcode.books.android.views;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.flopcode.books.android.BooksApplication;

public class ImportApiKey extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    Uri uri = intent.getData();
    final String apiKey = uri.getPath();
    Log.d(BooksApplication.LOG_TAG, "API_KEY: "+ apiKey);
    BooksApplication.storeApiKey(this, apiKey);
    startActivity(new Intent(this, Index.class));
  }
}
