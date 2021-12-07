package org.xrpl.xrpl4j.model.transactions;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

  @Test
  public void addressWithBadPrefix() {

    assertThrows(
      IllegalArgumentException.class,
      () -> Address.of("c9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
      "Invalid Address: Bad Prefix"
    );
  }

  @Test
  public void addressOfIncorrectLength() {

    assertThrows(
      IllegalArgumentException.class,
      () -> Address.of("r9cZA1mLK5R"),
      "Classic Addresses must be (25,35) characters long inclusive."
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> Address.of("rAJYB9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
      "Classic Addresses must be (25,35) characters long inclusive."
    );
  }
}
