package com.flopcode.books.android.views.books;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.OnClick;
import com.flopcode.books.BooksApi;
import com.flopcode.books.BooksApi.BooksService;
import com.flopcode.books.BooksApi.IsbnLookupService;
import com.flopcode.books.android.BooksApplication;
import com.flopcode.books.android.R;
import com.flopcode.books.models.Book;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static butterknife.ButterKnife.bind;
import static com.flopcode.books.android.BooksApplication.LOG_TAG;

public class Add extends Activity {

  private BooksService booksService;
  @Bind(R.id.book_isbn)
  public EditText isbn;

  @Bind(R.id.book_title)
  public EditText title;

  @Bind(R.id.book_authors)
  public EditText authors;

  private IsbnLookupService isbnLookupService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.books_add);
    new IntentIntegrator(this).initiateScan();
    booksService = BooksApi.createBooksService("http://localhost:3000", BooksApplication.getApiKey(this));
    isbnLookupService = BooksApi.createIsbnLookupService();
    bind(this);
  }

  @OnClick(R.id.cancel_button)
  public void onCancelButton(View button) {
    finish();
  }

  @OnClick(R.id.ok_button)
  public void onAddButton(View v) {
    Book b = new Book(null, isbn.getText().toString(), title.getText().toString(), authors.getText().toString());
    booksService.create(b.isbn, b.title, b.authors).enqueue(new Callback<Book>() {
      @Override
      public void onResponse(Call<Book> call, Response<Book> response) {
        if (response.isSuccess()) {
          Log.d(LOG_TAG, "book created on server");
          Log.d(LOG_TAG, response.message());
          Log.d(LOG_TAG, response.body().toString());
          Book createdBook = response.body();
          final Intent intent = new Intent(Add.this, Show.class);
          intent.putExtra("book", createdBook);
          startActivity(intent);
        } else {
          Log.d(LOG_TAG, "book not created on server");
        }
      }

      @Override
      public void onFailure(Call<Book> call, Throwable throwable) {
        Log.d(LOG_TAG, "problem with creation of book on server", throwable);
      }
    });
  }

  private void fillForm(Book book) {
    if (book == null) {
      Toast.makeText(this, "could not fetch book data", Toast.LENGTH_LONG).show();
      return;
    }
    Log.d(LOG_TAG, "adding book " + book);
    isbn.setText(book.isbn);
    title.setText(book.title);
    authors.setText(book.authors);
    Spinner owner = (Spinner) findViewById(R.id.book_owner);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.add, menu);
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

    return super.onOptionsItemSelected(item);
  }


  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    Log.d(LOG_TAG, "Add.onActivityResult " + requestCode + ", " + resultCode + ", " + intent);
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
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

    private IsbnLookupService isbnLookupService;

    private Book findBookForIsbn(String isbn) throws Exception {
      return isbnLookupService.find("ISBN:" + isbn).execute().body();
    }

    @Override
    protected void onPostExecute(Book book) {
      fillForm(book);
    }
  }

}
