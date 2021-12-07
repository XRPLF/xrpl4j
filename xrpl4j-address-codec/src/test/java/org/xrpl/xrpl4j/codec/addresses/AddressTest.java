package org.xrpl.xrpl4j.codec.addresses;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class AddressTest {

  private final AddressCodec addressCodec = new AddressCodec();


  @Test
  public void addressWithBadPrefix() {

    assertThrows(
      IllegalArgumentException.class,
      () -> {
        Address address = Address.of("c9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
      },
      "Invalid Address: Bad Prefix"
    );
  }

  @Test
  public void addressOfIncorrectLength() {

    assertThrows(
      IllegalArgumentException.class,
      () -> {
        Address address = Address.of("r9cZA1mLK5R");
      },
      "Classic Addresses must be (25,35) characters long inclusive."
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        Address address = Address.of("rAJYB9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
      },
      "Classic Addresses must be (25,35) characters long inclusive."
    );
  }

  @Test
  public void useValidateAddress() {
    Address address1 = Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
    assertDoesNotThrow(() -> {
      address1.validateAddress();
    });
  }
}
