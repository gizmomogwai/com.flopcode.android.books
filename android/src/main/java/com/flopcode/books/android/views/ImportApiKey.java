package com.flopcode.books.android.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.flopcode.books.android.BooksApplication;
import com.flopcode.books.android.views.books.BooksActivity;
import com.flopcode.books.android.views.books.Index;

import static com.flopcode.books.android.BooksApplication.LOG_TAG;

public class ImportApiKey extends BooksActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    Uri uri = intent.getData();
    final String apiKey = uri.getPath().substring(1);
    Log.e(LOG_TAG, "API_KEY: " + apiKey);
    getBooksApplication().storeApiKey(apiKey);
    startActivity(new Intent(this, Index.class));
  }

  @Override
  public void dataChanged() {

  }
}
