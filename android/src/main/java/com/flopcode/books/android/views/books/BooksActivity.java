package com.flopcode.books.android.views.books;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import com.flopcode.books.android.BooksApplication;
import com.flopcode.books.android.views.PreferencesActivity;

public abstract class BooksActivity extends AppCompatActivity implements BooksDataListener {

  public void showPreferences() {
    startActivity(new Intent(this, PreferencesActivity.class));
  }

  public BooksApplication getBooksApplication() {
    return (BooksApplication) getApplication();
  }

}
