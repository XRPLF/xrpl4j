package com.ripple.xrpl4j.codec.fixtures.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCodecFixture.class)
@JsonDeserialize(as = ImmutableCodecFixture.class)
public interface CodecFixture {

  String binary();

  JsonNode json();

}


