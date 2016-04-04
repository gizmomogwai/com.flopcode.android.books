package com.flopcode.books.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.View.OnClickListener;

import static android.content.Context.MODE_PRIVATE;

public class BooksApplication {
  public static final String LOG_TAG = "Books";
  private static final String API_KEY = "apiKey";
  private static final String BOOKS_SERVER = "booksServer";

  private static String getPreference(Context c, String s) {
    return getSharedPreferences(c).getString(s, null);
  }

  private static String getPreference(Context c, String s, String defaultValue) {
    return PreferenceManager.getDefaultSharedPreferences(c).getString(s, defaultValue);
  }

  private static SharedPreferences getSharedPreferences(Context c) {
    return c.getSharedPreferences("books", MODE_PRIVATE);
  }

  public static String getApiKey(Context c) {
    return getPreference(c, API_KEY);
  }

  public static String getBooksServer(Context c) {
    return getPreference(c, BOOKS_SERVER, "http://192.168.1.100:3000");
  }

  public static void storeApiKey(Context c, String apiKey) {
    getSharedPreferences(c).edit().putString(API_KEY, apiKey).commit();
  }

  public static void showError(View l, String s, String buttonText, OnClickListener onClickListener) {
    final Snackbar snackbar = Snackbar.make(l, s, Snackbar.LENGTH_LONG);
    if (onClickListener != null) {
      snackbar.setAction(buttonText, onClickListener);
    }
    snackbar.show();
    //Toast.makeText(context, s, Toast.LENGTH_LONG).show();
  }

}
