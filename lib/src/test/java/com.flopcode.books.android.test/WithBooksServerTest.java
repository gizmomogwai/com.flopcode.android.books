package com.flopcode.books.android.test;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Scanner;

public abstract class WithBooksServerTest {
  public static final String booksServer = "http://127.0.0.1:3000";

  static class Puller extends Thread {

    private final String name;
    private final InputStream stream;
    private final StringBuilder res;
    private final boolean debug;

    public Puller(String name, InputStream stream, boolean debug) {
      this.name = name;
      this.stream = stream;
      this.res = new StringBuilder();
      this.debug = debug;
    }

    public void run() {
      Scanner s = new Scanner(stream);
      while (s.hasNextLine()) {
        String line = s.nextLine();
        if (debug) {
          System.out.println(name + " > " + line);
        }
        res.append(line);
        res.append("\n");
      }
    }

    public String getOutput() {
      return res.toString();
    }

    public void waitFor(String s) {
      if (s == null) return;

      while (!getOutput().contains(s)) {
        try {
          System.out.println("Puller(" + name + ").waitFor: " + s);
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  static class AutonomeProcess {
    private final Process process;
    private final Puller stdout;
    private final Puller stderr;

    public AutonomeProcess(Process process, boolean debug, String stdout, String stderr) {
      this.process = process;
      this.stdout = new Puller("stdout", this.process.getInputStream(), debug);
      this.stdout.start();
      this.stderr = new Puller("stderr", this.process.getErrorStream(), debug);
      this.stderr.start();
      this.stdout.waitFor(stdout);
      this.stderr.waitFor(stderr);
    }

    private long getPid(Process p) throws Exception {
      Field f = p.getClass().getDeclaredField("pid");
      f.setAccessible(true);
      long res = f.getLong(p);
      f.setAccessible(false);
      return res;
    }

    private long getPid(final String pattern) throws Exception {
      String ps = AutonomeProcess.run(new File("."), false, null, null, "/bin/ps", "ax").waitFor();
      System.out.println("ps = " + ps);
      final ArrayList<String> lines = Lists.newArrayList(ps.split("\n"));
      final String[] rails = Collections2.filter(lines, new com.google.common.base.Predicate<String>() {
        @Override
        public boolean apply(String input) {
          return input.contains(pattern);
        }
      }).iterator().next().split(" ");
      /*
      final String[] rails = lines.stream()
        .filter(new Predicate<String>() {
          @Override
          public boolean test(String s) {
            return s.contains(pattern);
          }
        })
        .findFirst()
        .get()
        .split(" ");
        */
      Collection<String> pids = Collections2.transform(Lists.newArrayList(rails), new com.google.common.base.Function<String, String>() {
        @Override
        public String apply(String input) {
          return input.trim();
        }
      });
      String pid = Collections2.filter(pids, new com.google.common.base.Predicate<String>() {
        @Override
        public boolean apply(String input) {
          return input.length() > 0;
        }
      }).iterator().next();
      /*
      String pid = Lists.newArrayList(rails)
        .stream()
        .map(new Function<String, String>() {
          @Override
          public String apply(String s) {
            return s.trim();
          }
        })
        .filter(new Predicate<String>() {
          @Override
          public boolean test(String line) {
            return line.length() > 0;
          }
        }).findFirst().get();
        */
      System.out.println("pid = " + pid);
      if (pid == null || pid.length() == 0) {
        throw new RuntimeException("could not determine pid of rails app");
      }
      return Long.parseLong(pid);
    }

    public static AutonomeProcess run(File workingDirectory, boolean debug, String stdout, String stderr, String... command) throws Exception {
      if (!workingDirectory.exists()) {

        throw new RuntimeException("working directory does not exist " + new File(".").getAbsolutePath());
      }
      System.out.println("workingDirectory = " + workingDirectory.getAbsoluteFile());
      for (File f: workingDirectory.getAbsoluteFile().listFiles()) {
        System.out.println("f = " + f);
      }
      System.out.println("AutonomeProcess.run");
      for (String s : command) {
        System.out.println("s = " + s);
      }
      return new AutonomeProcess(new ProcessBuilder(command).directory(workingDirectory.getAbsoluteFile()).start(), debug, stdout, stderr);
    }

    public String waitFor() throws Exception {
      process.waitFor();
      stdout.join();
      stderr.join();
      return stdout.getOutput();
    }

    public void destroy() {
      try {
        if (process.isAlive()) {
          AutonomeProcess.run(new File("."), false, null, null, "kill", "-s", "SIGINT", "" + getPid("rails s")).waitFor();
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
    railsServer = AutonomeProcess.run(new File("../../server"), true, null, "WEBrick::HTTPServer#start", System.getProperty("user.home") + "/.rvm/gems/ruby-2.3.1@books/wrappers/bundle", "exec", "rake", "run_testserver");
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
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @After
  public void tearDown() {
    System.out.println("WithBooksServerTest.tearDown");
    railsServer.destroy();
  }


}
