package com.flopcode.books.android.test;

import com.flopcode.books.BooksApi;
import com.flopcode.books.models.Book;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WithBooksServerTest {

  static class Puller extends Thread {

    private final InputStream stream;
    private final StringBuilder res;

    public Puller(InputStream stream, StringBuilder res) {
      this.stream = stream;
      this.res = res;
    }

    public void run() {
      try {
        int r = stream.read();
        while (r != -1) {
          final char r1 = (char) r;
          // System.out.print(r1);
          res.append(r1);
          r = stream.read();
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
        System.out.println("Puller.run - caught exception: " + ioe);
      }
    }
  }

  static class AutonomeProcess {
    private final Process process;
    private final StringBuilder stringBuilder;
    private final Puller puller;

    public AutonomeProcess(Process process) {
      this.process = process;
      this.stringBuilder = new StringBuilder();
      this.puller = new Puller(this.process.getInputStream(), stringBuilder);
      this.puller.start();
    }

    public static AutonomeProcess run(File workingDirectory, String... command) throws Exception {
      if (!workingDirectory.exists()) {
        throw new RuntimeException("working directory does not exist");
      }
      return new AutonomeProcess(new ProcessBuilder(command).directory(workingDirectory.getAbsoluteFile()).redirectErrorStream(true).start());
    }

    public void destroy() {
      this.process.destroy();
    }
  }

  private AutonomeProcess railsServer;

  @Before
  public void setUp() throws Exception {
    railsServer = AutonomeProcess.run(new File("../../server"), "/Users/gizmo/.rvm/gems/ruby-2.3.0@books/wrappers/rake", "run_testserver");
    waitForRailsServer();
  }

  // try for some seconds to connect to the rails server
  private void waitForRailsServer() {
    DateTime startTime = DateTime.now();
    int retry = 0;
    while (true) {
      retry++;
      try {
        Thread.sleep(5000);
        URL u = new URL("http://127.0.0.1:3000/books");
        URLConnection connection = u.openConnection();
        connection.setConnectTimeout(1000);
        connection.connect();
        return;
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("WithBooksServerTest.waitForRailsServer retry: " + retry);
        retry++;
      }
      Seconds i = Seconds.secondsBetween(startTime, DateTime.now());
      if (i.isGreaterThan(Seconds.seconds(5))) {
        return;
      }
    }
  }

  @After
  public void tearDown() {
    railsServer.destroy();
  }

  @Test
  public void testBookIndex() throws Exception {
    Call<List<Book>> call = BooksApi.createBooksService(new URL("http://127.0.0.1:3000"), "key1").index();
    Response<List<Book>> res = call.execute();
    assertThat(res.body().size()).isEqualTo(2);
  }

  @Test
  public void testBookShow() throws Exception {
    Call<List<Book>> call1 = BooksApi.createBooksService(new URL("http://127.0.0.1:3000"), "key1").index();
    String id = call1.execute().body().get(0).id;

    Call<Book> call = BooksApi.createBooksService(new URL("http://127.0.0.1:3000"), "key1").show(id);
    Response<Book> res = call.execute();
    Book book = res.body();
    assertThat(book.id).isEqualTo("1");
    assertThat(book.isbn).isEqualTo("isbn1");
    assertThat(book.title).isEqualTo("title1");
    assertThat(book.authors).isEqualTo("authors1");
    assertThat(book.userId).isEqualTo("1");
    assertThat(book.locationId).isEqualTo("1");
  }

}
