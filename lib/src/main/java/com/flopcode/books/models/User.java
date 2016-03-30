package com.flopcode.books.models;

/**
 * Created by gizmo on 30/03/16.
 */
public class User {
  public final String name;
  public final String account;
  public final boolean admin;

  public User(String name, String account, boolean admin) {
    this.name = name;
    this.account = account;
    this.admin = admin;
  }

  @Override
  public String toString() {
    return "User { name: " + name + ", account: " + account + ", admin: " + admin  + " }";
  }
}
