package com.example.osgi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DefaultGreeterTest {

  Greeter greeter = new DefaultGreeter();

  @Test
  void hello() {
    assertThat(greeter.hello("world"))
        .isEqualTo("Hello world");
  }
}