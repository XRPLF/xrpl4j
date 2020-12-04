package org.xrpl.xrpl4j.codec.fixtures.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableFieldTest.class)
@JsonDeserialize(as = ImmutableFieldTest.class)
public interface FieldTest {

  @JsonProperty("type_name")
  String typeName();

  String name();

  @JsonProperty("nth_of_type")
  int nthOfType();

  int type();

  @JsonProperty("expected_hex")
  String expectedHex();

}
