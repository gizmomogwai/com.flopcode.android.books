package com.flopcode.android.books;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.flopcode.android.books.BooksApi.BooksService;
import com.flopcode.android.books.BooksApi.IsbnLookupService;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.Serializable;
import java.util.List;

public class Books extends Activity {

  public static final String LOG_TAG = "Books";
  private BooksService booksService;
  private IsbnLookupService isbnLookupService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    booksService = BooksApi.createBooksService();
    isbnLookupService = BooksApi.createIsbnLookupService();
    setContentView(R.layout.books);
    Log.d(LOG_TAG, "onCreate");
    //new GetBookAsyncTask().execute("9780321635365");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.books, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    if (id == R.id.action_add_book) {
      new IntentIntegrator(this).initiateScan();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public static class Book implements Serializable {
    public final String isbn;
    public final String title;
    public final String authors;

    public Book(String isbn, String authors, String title) {
      this.isbn = isbn;
      this.authors = authors;
      this.title = title;
    }

    @Override
    public String toString() {
      return "Book { isbn = " + isbn + ", title = " + title + ", authors = " + authors + " }";
    }
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
      Log.d(LOG_TAG, "scanResult.getFormatName(): " + scanResult.getFormatName());
      Log.d(LOG_TAG, "scanResult.getContents(): " + scanResult.getContents());
      new GetBookAsyncTask().execute(scanResult.getContents());
    }
  }

  private class GetBookAsyncTask extends AsyncTask<String, Object, Book> {

    @Override
    protected Book doInBackground(String... params) {
      try {
        return findBookForIsbn(params[0]);
      } catch (Exception e) {
        e.printStackTrace();
        Log.e(LOG_TAG, "could not process " + params[0], e);
      }
      return null;
    }

    @Override
    protected void onPostExecute(Book book) {
      Log.d(LOG_TAG, "adding book: " + book);
      Intent i = new Intent(Books.this, AddBook.class);
      i.putExtra("book", book);
      startActivity(i);
    }
  }

  private Book findBookForIsbn(String isbn) throws Exception {
    return isbnLookupService.find("ISBN:" + isbn).execute().body();
  }

  public static class User {

    public long id;
    public String name;
    public String account;

    @Override
    public String toString() {
      return "User { id=" + id + ", name=" + name + ", account=" + account + " }";
    }

  }
}
