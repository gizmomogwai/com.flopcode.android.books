package com.flopcode.books.android.views;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import butterknife.Bind;
import com.flopcode.books.android.R;

import static butterknife.ButterKnife.bind;
import static com.flopcode.books.android.BooksApplication.LOG_TAG;

public class FirstRunDialog extends Activity {
  @Bind(R.id.startUseButton)
  Button startUseButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(LOG_TAG, "FirstRun.onCreate");

    setContentView(R.layout.first_run_dialog);
    bind(this);

    startUseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
  }

}
