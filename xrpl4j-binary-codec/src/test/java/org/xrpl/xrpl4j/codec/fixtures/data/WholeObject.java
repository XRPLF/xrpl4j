package org.xrpl.xrpl4j.codec.fixtures.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableWholeObject.class)
@JsonDeserialize(as = ImmutableWholeObject.class)
public interface WholeObject {

  @JsonProperty("tx_json")
  JsonNode txJson();


  @JsonProperty("blob_with_no_signing")
  String expectedHex();

}
