package com.flopcode.books.models;

public class User {
  public final long id;
  public final String name;
  public final String account;
  public final boolean admin;

  public User(long id, String name, String account, boolean admin) {
    this.id = id;
    this.name = name;
    this.account = account;
    this.admin = admin;
  }

  @Override
  public String toString() {
    return "User { id: " + id + ", name: " + name + ", account: " + account + ", admin: " + admin  + " }";
  }
}
