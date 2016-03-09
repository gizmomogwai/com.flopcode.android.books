package com.flopcode.android.books;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.flopcode.android.books.Books.Book;
import com.google.common.collect.Lists;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.flopcode.android.books.Books.LOG_TAG;

public class ShowBook extends Activity {

  private static final int TAG_DETECTED = 17;
  private NfcAdapter nfcAdapter;
  private PendingIntent pendingIntent;
  private IntentFilter discovery;
  private IntentFilter ndefDetected;

  @Bind(R.id.book_id)
  TextView id;
  @Bind(R.id.book_isbn)
  TextView isbn;
  @Bind(R.id.book_title)
  TextView title;
  @Bind(R.id.book_authors)
  TextView authors;
  @Bind(R.id.book_owner)
  TextView owner;
  private Book book;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "ShowBook.onCreate");
    setContentView(R.layout.activity_show_book);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    Log.d(LOG_TAG, intent.toString());
    if (intent.getAction() == NfcAdapter.ACTION_NDEF_DISCOVERED) {
      if (intent.getData() != null) {
        Uri uri = intent.getData();
        final String bookId = uri.getHost();
        BooksApi.createBooksService().book(bookId).enqueue(new Callback<Book>() {
          @Override
          public void onResponse(Call<Book> call, Response<Book> response) {
            book = response.body();
            mount(book);
          }

          @Override
          public void onFailure(Call<Book> call, Throwable throwable) {
            toast("could not fetch book data for bookId " + bookId);
          }
        });
      }
    }

    nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    pendingIntent = PendingIntent
      .getActivity(this, TAG_DETECTED,
        new Intent(this, this.getClass())
          .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
    discovery = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
    ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
  }

  private void mount(Book book) {
    id.setText("nyi");//book.id);
    isbn.setText(book.isbn);
    title.setText(book.title);
    authors.setText(book.authors);
    owner.setText("nyi");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(LOG_TAG, "ShowBook.onResume");
    if (!nfcAdapter.isEnabled()) {
      toast("please enable nfc");
      return;
    }

  }

  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Log.d(LOG_TAG, "ShowBook.onNewIntent");
    if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
      try {
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d(LOG_TAG, "tech list" + Lists.newArrayList(detectedTag.getTechList()));
        Ndef ndef = Ndef.get(detectedTag);
        ndef.connect();
        if (!ndef.isWritable()) {
          toast("not writeable");
          return;
        }
        NdefMessage message = new NdefMessage(new NdefRecord[]{NdefRecord.createUri("books://1")});
        ndef.writeNdefMessage(message);
      } catch (Exception e) {
        toast("problems " + e.getMessage());
        Log.e(LOG_TAG, "problems", e);
      }
    }
  }


  private void toast(String s) {
    Toast.makeText(this, s, Toast.LENGTH_LONG).show();
  }

  @Override
  protected void onPause() {
    super.onPause();
    nfcAdapter.disableForegroundDispatch(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_show_book, menu);
    return true;
  }


  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_write_tag_to_book:
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{discovery}, null);
        toast("approach tag now");
        break;
    }
    return true;
  }
}
