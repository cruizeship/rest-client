package com.example.restservice;
import java.util.*;

public class UserList {
  ArrayList<User> userList;

  public UserList() {
    userList = new ArrayList<>();
  }
  public void addUser(User a) {
    userList.add(a);
  }
  public User getUser(String username) {
    for (User a : userList) {
      if (a.username() == username) {
        return a;
      }
    }
    return null;
  }
  public String toString() {
    String ans = "";
    for (User a: userList) {
      ans += a.toString();
    }
    return ans;
  }
}