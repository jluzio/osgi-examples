package com.example.osgi;

public class DefaultGreeter implements Greeter {

  @Override
  public String hello(String name) {
    return "Hello %s".formatted(name);
  }
}
