package org.xrpl.xrpl4j.crypto.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

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

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    KeyMetadata keyMetadata = KeyMetadata.builder()
      .platformIdentifier("foo1")
      .keyringIdentifier("foo2")
      .keyIdentifier("foo3")
      .keyPassword("foo4")
      .keyVersion("foo5")
      .build();

    String json = ObjectMapperFactory.create().writeValueAsString(keyMetadata);
    JsonAssert.with(json).assertEquals("$.platformIdentifier", "foo1");
    JsonAssert.with(json).assertEquals("$.keyringIdentifier", "foo2");
    JsonAssert.with(json).assertEquals("$.keyIdentifier", "foo3");
    JsonAssert.with(json).assertEquals("$.keyPassword", "foo4");
    JsonAssert.with(json).assertEquals("$.keyVersion", "foo5");

    KeyMetadata actual = ObjectMapperFactory.create().readValue(json, KeyMetadata.class);
    assertThat(actual).isEqualTo(keyMetadata);
  }
}
