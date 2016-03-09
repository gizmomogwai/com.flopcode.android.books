package com.flopcode.android.books;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.flopcode.android.books.Books.Book;

import static com.flopcode.android.books.Books.LOG_TAG;

public class AddBook extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_book);

    Book book = (Book) getIntent().getExtras().getSerializable("book");
    Log.d(LOG_TAG, "adding book " + book);
    final EditText title = (EditText) findViewById(R.id.title);
    title.setText(book.title);

    final EditText authors = (EditText) findViewById(R.id.authors);
    authors.setText(book.authors);

    Spinner owner = (Spinner) findViewById(R.id.owner);

    final EditText isbn = (EditText) findViewById(R.id.isbn);
    isbn.setText(book.isbn);

    Button ok = (Button)findViewById(R.id.ok_button);
    ok.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        new Book(isbn.getText().toString(), authors.getText().toString(), title.getText().toString()).create();
      }
    });
    Button cancel = (Button)findViewById(R.id.cancel_button);
    cancel.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
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
