package com.flopcode.books.android.test;

import com.flopcode.books.BooksApi;
import com.flopcode.books.models.Book;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

public class WithBooksServerTest {
  static final String booksServer = "http://127.0.0.1:3000";

  static class Puller extends Thread {

    private final InputStream stream;
    private final StringBuilder res;
    private final boolean debug;

    public Puller(InputStream stream, StringBuilder res, boolean debug) {
      this.stream = stream;
      this.res = res;
      this.debug = debug;
    }

    public void run() {
      Scanner s = new Scanner(stream);
      while (s.hasNextLine()) {
        String line = s.nextLine();
        if (debug) {
          System.out.println(" > " + line);
          res.append(line);
        }
      }
    }
  }

  static class AutonomeProcess {
    private final Process process;
    private final StringBuilder stringBuilder;
    private final Puller stdout;
    private final Puller stderr;

    public AutonomeProcess(Process process, boolean debug) {
      this.process = process;
      this.stringBuilder = new StringBuilder();
      this.stdout = new Puller(this.process.getInputStream(), stringBuilder, debug);
      this.stdout.start();
      this.stderr = new Puller(this.process.getErrorStream(), stringBuilder, debug);
      this.stderr.start();
    }

    private long getPid(Process p) throws Exception {
      Field f = p.getClass().getDeclaredField("pid");
      f.setAccessible(true);
      long res = f.getLong(p);
      f.setAccessible(false);
      return res;
    }

    private long getPid(Process p, String pattern) throws Exception {
      String ps = AutonomeProcess.run(new File("."), false, "/bin/ps", "ax").waitFor();
      final ArrayList<String> lines = Lists.newArrayList(ps.split("\n"));
      final String[] rails = lines.stream()
        .filter(line -> line.contains(pattern))
        .findFirst()
        .get()
        .split(" ");
      String pid = Lists.newArrayList(rails)
        .stream()
        .map(line -> line.trim())
        .filter(line -> line.trim().length() > 0).findFirst().get();
      System.out.println("pid = " + pid);
      if (pid == null || pid.length() == 0) {
        throw new RuntimeException("could not determine pid of rails app");
      }
      return Long.parseLong(pid);
    }

    public static AutonomeProcess run(File workingDirectory, boolean debug, String... command) throws Exception {
      if (!workingDirectory.exists()) {
        throw new RuntimeException("working directory does not exist");
      }
      System.out.println("AutonomeProcess.run");
      for (String s : command) {
        System.out.println("s = " + s);
      }
      return new AutonomeProcess(new ProcessBuilder(command).directory(workingDirectory.getAbsoluteFile()).start(), debug);
    }

    public String getStdout() {
      return stdout.res.toString();
    }

    public String waitFor() throws InterruptedException {
      process.waitFor();
      stdout.join();
      stderr.join();
      return getStdout();
    }

    public void destroy() {
      try {
        if (process.isAlive()) {
          AutonomeProcess.run(new File("."), false, "kill", "-s", "SIGINT", "" + getPid(process, "rails s")).waitFor();
        }
        process.waitFor();
        System.out.println("AutonomeProcess.destroy - finished");
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.flush();
      System.err.flush();
    }
  }

  private AutonomeProcess railsServer;

  @Before
  public void setUp() throws Exception {
    System.out.println("WithBooksServerTest.setUp");
    railsServer = AutonomeProcess.run(new File("../../server"), true, "/Users/gizmo/.rvm/gems/ruby-2.3.0@books/wrappers/rake", "run_testserver");
    waitForRailsServer();
  }

  // try for some seconds to connect to the rails server
  private void waitForRailsServer() {
    System.out.println("WithBooksServerTest.waitForRailsServer");
    DateTime startTime = DateTime.now();
    int retry = 0;
    while (true) {
      retry++;
      try {
        Thread.sleep(1000);
        URL u = new URL("http://127.0.0.1:3000/books");

        URLConnection connection = u.openConnection();

        connection.setConnectTimeout(2000);
        connection.connect();
        /*
        Call<List<Book>> call = BooksApi.createBooksService(u, "key1").index();
        Response<List<Book>> response = call.execute();
        if (!response.isSuccess()) {
          throw new RuntimeException("could not get index from books service");
        }
        */
        return;
      } catch (Exception e) {
        System.out.println("WithBooksServerTest.waitForRailsServer retry: " + retry);
        retry++;
      }
      Seconds i = Seconds.secondsBetween(startTime, DateTime.now());
      if (i.isGreaterThan(Seconds.seconds(15))) {
        return;
      }
    }
  }

  @After
  public void tearDown() {
    System.out.println("WithBooksServerTest.tearDown");
    railsServer.destroy();
  }

  @Test
  public void testBooksIndex() throws Exception {
    Call<List<Book>> call = BooksApi.createBooksService(new URL("http://127.0.0.1:3000"), "key2").index();
    Response<List<Book>> res = call.execute();
    assertThat(res.body().size()).isEqualTo(2);
  }

  @Test
  public void testBooksShow() throws Exception {
    Call<List<Book>> call1 = BooksApi.createBooksService(new URL(booksServer), "key2").index();
    String id = call1.execute().body().get(0).id;

    Call<Book> call = BooksApi.createBooksService(new URL(booksServer), "key2").show(id);
    Response<Book> res = call.execute();
    Book book = res.body();
    assertThat(book.id).isEqualTo("1");
    assertThat(book.isbn).isEqualTo("isbn1");
    assertThat(book.title).isEqualTo("title1");
    assertThat(book.authors).isEqualTo("authors1");
    assertThat(book.userId).isEqualTo(1);
    assertThat(book.locationId).isEqualTo(1);
  }


  @Test
  public void testBooksCreate() throws Exception {
    final String isbn = "isbn";
    final String title = "title";
    final String authors = "authors";
    final int userId = 1;
    final int locationId = 1;


    Call<Book> call = BooksApi.createBooksService(new URL(booksServer), "key1").create(isbn, title, authors, userId, locationId);
    Response<Book> response = call.execute();
    Book result = response.body();
    assertThat(result.isbn).isEqualTo(isbn);
    assertThat(result.title).isEqualTo(title);
    assertThat(result.authors).isEqualTo(authors);
    assertThat(result.userId).isEqualTo(userId);
    assertThat(result.locationId).isEqualTo(locationId);
  }

}
