package com.flopcode.books.android.views.books;

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
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.flopcode.books.BooksApi;
import com.flopcode.books.BooksApi.ActiveCheckoutsService;
import com.flopcode.books.android.R;
import com.flopcode.books.models.ActiveCheckout;
import com.flopcode.books.models.Book;
import com.flopcode.books.models.Location;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.flopcode.books.android.BooksApplication.LOG_TAG;
import static com.flopcode.books.android.BooksApplication.showError;

/**
 * shows a book
 * needs the book information, the locations and users in the database
 */
public class Show extends BooksActivity {

  private static final int TAG_DETECTED = 17;
  private NfcAdapter nfcAdapter;
  private PendingIntent pendingIntent;
  private IntentFilter discovery;

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

  @Bind(R.id.location)
  TextView location;

  @Bind(R.id.checkout_checkin_button)
  Button checkoutCheckinButton;

  private Book book;

  private ActiveCheckoutsService checkoutsService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(LOG_TAG, "ShowBook.onCreate");
    checkoutsService = BooksApi.createActiveCheckoutsService(getBooksApplication().getBooksServer(this), getBooksApplication().getApiKey(this));
    setContentView(R.layout.books_show);
    ButterKnife.bind(this);

    final Intent intent = getIntent();

    Log.d(LOG_TAG, intent.toString());
    handleNfcIntent(intent);
    handleBookIntent(intent);

    nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    pendingIntent = PendingIntent
      .getActivity(this, TAG_DETECTED,
        new Intent(this, this.getClass())
          .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
    discovery = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

    getBooksApplication().fetchData(this);
  }

  private void handleBookIntent(Intent intent) {
    if (intent.hasExtra("book")) {
      book = (Book) intent.getSerializableExtra("book");
      dataChanged();
    }
  }

  private void handleNfcIntent(Intent intent) {
    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
      if (intent.getData() != null) {
        Uri uri = intent.getData();
        Log.d(LOG_TAG, "incoming intent: " + uri);
        final String bookId = uri.getPathSegments().get(0);
        Log.d(LOG_TAG, "incoming intent bookId: " + bookId);
        BooksApi.createBooksService(getBooksApplication().getBooksServer(this), getBooksApplication().getApiKey(this)).show(bookId).enqueue(new Callback<Book>() {
          @Override
          public void onResponse(Call<Book> call, Response<Book> response) {
            book = response.body();
            mount(book);
          }

          @Override
          public void onFailure(Call<Book> call, Throwable throwable) {
            showError(Show.this, "could not fetch book data for bookId " + bookId);
          }
        });
      }
    }
  }

  @OnClick(R.id.checkout_checkin_button)
  public void onCheckoutCheckinButton(View v) {
    System.out.println("Show.onCheckoutCheckinButton");
    System.out.println("book.activeCheckout = " + book.activeCheckout);
    if (book.activeCheckout == 0) {
      checkoutsService.create(book.id).enqueue(new Callback<ActiveCheckout>() {
        @Override
        public void onResponse(Call<ActiveCheckout> call, Response<ActiveCheckout> response) {
          checkoutCreated(response.body());
        }

        @Override
        public void onFailure(Call<ActiveCheckout> call, Throwable t) {
          showError(Show.this, "could not checkout book");
        }
      });
    } else {
      if (ownUserId(book.userId)) {
        checkoutsService.destroy("" + book.activeCheckout).enqueue(new Callback<Void>() {
          @Override
          public void onResponse(Call<Void> call, Response<Void> response) {
            activeCheckoutDestroyed();
          }

          @Override
          public void onFailure(Call<Void> call, Throwable t) {
            showError(Show.this, "could not checkin book");
          }
        });
      }
    }
  }

  private void activeCheckoutDestroyed() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        book.activeCheckout = 0;
        dataChanged();
      }
    });
  }

  private void checkoutCreated(final ActiveCheckout body) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        book.activeCheckout = body.id;
        dataChanged();
      }
    });
  }

  private boolean ownUserId(long userId) {
    return true;
  }

  private void mount(final Book book) {
    this.book = book;
    id.setText("nyi");//book.id);
    isbn.setText(book.isbn);
    title.setText(book.title);
    authors.setText(book.authors);
    owner.setText("nyi");
    Location l = Iterables.find(getBooksApplication().getLocations(), new Predicate<Location>() {
      @Override
      public boolean apply(Location input) {
        return input.id == book.locationId;
      }
    });
    location.setText(l.name);
    checkoutCheckinButton.setText(book.activeCheckout == 0 ? "Checkout" : "Checkin");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(LOG_TAG, "ShowBook.onResume");
    if (!nfcAdapter.isEnabled()) {
      showError(this, "please enable nfc");
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
          showError(this, "not writeable");
          return;
        }
        NdefMessage message = new NdefMessage(new NdefRecord[]{NdefRecord.createUri("books:///1")});
        ndef.writeNdefMessage(message);
      } catch (Exception e) {
        showError(this, "problems " + e.getMessage());
        Log.e(LOG_TAG, "problems", e);
      }
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    nfcAdapter.disableForegroundDispatch(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.show, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
      case R.id.action_write_tag_to_book:
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{discovery}, null);
        showError(this, "approach tag now");
        break;
      case R.id.action_checkout_book:
        showError(this, "trying to get the book");
        break;
    }
    return true;
  }

  @Override
  public void dataChanged() {
    if (dataComplete()) {
      mount(book);
    }
  }

  // needs locations and users to display a book properly
  private boolean dataComplete() {
    return book != null && getBooksApplication().getLocations() != null && getBooksApplication().getUsers() != null;
  }
}
