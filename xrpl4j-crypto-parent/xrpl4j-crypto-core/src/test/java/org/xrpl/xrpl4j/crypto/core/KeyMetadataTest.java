package org.xrpl.xrpl4j.crypto.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link KeyMetadata}.
 */
public class KeyMetadataTest {

  @Test
  public void testPlatformIdentifierWithNullPlatformIdentifier() {
    Assertions.assertThrows(IllegalStateException.class, () -> KeyMetadata.builder()
      .keyringIdentifier("foo")
      .keyIdentifier("foo")
      .keyPassword("foo")
      .keyVersion("foo")
      .build());
  }

  @Test
  public void testPlatformIdentifierWithNullKeyringIdentifier() {
    Assertions.assertThrows(IllegalStateException.class, () -> KeyMetadata.builder()
      .platformIdentifier("foo")
      // .keyringIdentifier("foo")
      .keyIdentifier("foo")
      .keyPassword("foo")
      .keyVersion("foo")
      .build());
  }

  @Test
  public void testPlatformIdentifierWithNullKeyIdentifier() {
    Assertions.assertThrows(IllegalStateException.class, () -> KeyMetadata.builder()
      .platformIdentifier("foo")
      .keyringIdentifier("foo")
      // .keyIdentifier("foo")
      .keyPassword("foo")
      .keyVersion("foo")
      .build());
  }

  @Test
  public void testPlatformIdentifierWithNullKeyVersion() {
    Assertions.assertThrows(IllegalStateException.class, () -> KeyMetadata.builder()
      .platformIdentifier("foo")
      .keyringIdentifier("foo")
      .keyIdentifier("foo")
      .keyPassword("foo")
      // .keyVersion("foo")
      .build());
  }

  @Test
  public void testPlatformIdentifierBuilder() {
    assertThat(KeyMetadata.builder()
      .platformIdentifier("foo")
      .keyringIdentifier("foo")
      .keyIdentifier("foo")
      .keyVersion("foo")
      .build()).isNotNull();
  }

  @Test
  public void testEmptyKeyMetadata() {
    assertThat(KeyMetadata.EMPTY)
      .isEqualTo(KeyMetadata.builder()
        .platformIdentifier("n/a")
        .keyringIdentifier("n/a")
        .keyIdentifier("n/a")
        .keyVersion("n/a")
        .build());
  }
}
