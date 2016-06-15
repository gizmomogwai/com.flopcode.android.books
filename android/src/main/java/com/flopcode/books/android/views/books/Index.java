package com.flopcode.books.android.views.books;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.flopcode.books.android.R;
import com.flopcode.books.models.Book;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

import static butterknife.ButterKnife.bind;
import static com.flopcode.books.android.BooksApplication.LOG_TAG;

public class Index extends BooksActivity {

  @Bind(R.id.recycler_view)
  RecyclerView booksList;
  private CardAdapter cardAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(LOG_TAG, "Books.onCreate");

    setContentView(R.layout.books_index);
    bind(this);

    //booksList.setVisibility(GONE);
    booksList.setHasFixedSize(true);
    booksList.setLayoutManager(new LinearLayoutManager(this));
    cardAdapter = new CardAdapter();
    booksList.setAdapter(cardAdapter);

    getBooksApplication().fetchData(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(LOG_TAG, "Books.onResume");
    //progressDescription.setText("fetching data from server");

    getBooksApplication().fetchBooks(this);
    getBooksApplication().fetchUsers(this);
    getBooksApplication().fetchLocations(this);
  }

  private void updateUi() {
    if (fetchDataComplete()) {
      switchToRealUI();
    }
  }

  private void switchToRealUI() {
    //progress.setVisibility(GONE);
    //booksList.setVisibility(View.VISIBLE);
    cardAdapter.setData(getBooksApplication().getBooks());
  }

  private boolean fetchDataComplete() {
    return getBooksApplication().getBooks() != null;
  }

  public static void showBook(Context c, Book book) {
    c.startActivity(new Intent(c, Show.class).putExtra("book", book));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.index, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        showPreferences();
        return true;
      case R.id.action_add_book:
        startActivity(new Intent(this, Add.class));
        return true;
      case R.id.action_set_api_key:
        new IntentIntegrator(this).initiateScan();
        return true;
      case R.id.action_refresh:
        getBooksApplication().refetchData(this);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    Log.d(LOG_TAG, "Index.onActivityResult " + requestCode + ", " + resultCode + ", " + intent);
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
      Uri uri = Uri.parse(scanResult.getContents());
      if (uri.getScheme().equals("books-api-key")) {
        getBooksApplication().storeUserIdAndApiKeyFromUri(uri);
      }
    }
  }

  @Override
  public void dataChanged() {
    updateUi();
  }

  private class CardAdapter extends RecyclerView.Adapter<BookHolder> {

    private List<Book> books;

    public void setData(List<Book> books) {
      this.books = books;
      notifyDataSetChanged();
    }

    @Override
    public BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.books_index_card, parent, false);
      BookHolder viewHolder = new BookHolder(v);
      return viewHolder;
    }

    @Override
    public void onBindViewHolder(BookHolder holder, int i) {
      Book book = books.get(i);
      holder.setBook(book, Index.this);
    }

    @Override
    public int getItemCount() {
      if (books == null) return 0;

      return books.size();
    }
  }

  static class BookHolder extends RecyclerView.ViewHolder {
    private Book book;
    public TextView title;
    public TextView authors;
    public TextView isbn;
    public TextView status;

    public BookHolder(View itemView) {
      super(itemView);
      itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          showBook(v.getContext(), book);
        }
      });
      title = ButterKnife.findById(itemView, R.id.title);
      authors = ButterKnife.findById(itemView, R.id.authors);
      isbn = ButterKnife.findById(itemView, R.id.isbn);
      status = ButterKnife.findById(itemView, R.id.status);
    }

    public void setBook(Book book, BooksActivity a) {
      this.book = book;

      title.setText(book.title);
      authors.setText(book.authors);
      isbn.setText(book.isbn);
      status.setText("status: " + book.status(a.getBooksApplication().users, a.getBooksApplication().getUserId(a)));
    }
  }
}
