package com.flopcode.android.books;

import android.app.Activity;
import android.content.Intent;
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
import com.flopcode.android.books.BooksApi.BooksService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static butterknife.ButterKnife.bind;
import static com.flopcode.android.books.Books.LOG_TAG;

public class AddBook extends Activity {

  private BooksService booksService;
  @Bind(R.id.book_isbn)
  public EditText isbn;

  @Bind(R.id.book_title)
  public EditText title;

  @Bind(R.id.book_authors)
  public EditText authors;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_book);
    booksService = BooksApi.createBooksService();
    bind(this);
    fillForm((Book) getIntent().getExtras().getSerializable("book"));
  }

  @OnClick(R.id.cancel_button)
  public void onCancelButton(View button) {
    finish();
  }

  @OnClick(R.id.ok_button)
  public void onAddButton(View v) {
    Book b = new Book(null, isbn.getText().toString(), title.getText().toString(), authors.getText().toString());
    booksService.createBook(b.isbn, b.title, b.authors).enqueue(new Callback<Book>() {
      @Override
      public void onResponse(Call<Book> call, Response<Book> response) {
        Log.d(LOG_TAG, "book created on server");
        Log.d(LOG_TAG, response.message());
        Log.d(LOG_TAG, response.body().toString());
        Book createdBook = response.body();
        final Intent intent = new Intent(AddBook.this, ShowBook.class);
        intent.putExtra("book", createdBook);
        startActivity(intent);
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
    getMenuInflater().inflate(R.menu.menu_add_book, menu);
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
}
