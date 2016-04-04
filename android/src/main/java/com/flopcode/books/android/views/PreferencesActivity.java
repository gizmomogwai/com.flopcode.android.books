package com.flopcode.books.android.views;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.flopcode.books.android.R;

public class PreferencesActivity extends Activity {
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, new PreferencesFragment())
      .commit();
  }

  public static class PreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
    }
  }
}
