package com.flopcode.android.books;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.flopcode.android.books.Books.Book;
import com.flopcode.android.books.BooksApi.BooksService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.flopcode.android.books.Books.LOG_TAG;

public class AddBook extends Activity {

  private BooksService booksService;
  private EditText title;
  private EditText authors;
  private EditText isbn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_book);

    booksService = BooksApi.createBooksService();
    isbn = (EditText) findViewById(R.id.book_isbn);
    title = (EditText) findViewById(R.id.book_title);
    authors = (EditText) findViewById(R.id.book_authors);

    Button ok = (Button) findViewById(R.id.ok_button);
    ok.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Book b = new Book(isbn.getText().toString(), title.getText().toString(), authors.getText().toString());
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
    });
    Button cancel = (Button) findViewById(R.id.cancel_button);
    cancel.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    fillForm((Book) getIntent().getExtras().getSerializable("book"));
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
