package com.ripple.xrpl4j.codec.fixtures.codec;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.codec.fixtures.ImmutableFixtures;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableCodecFixtures.class)
@JsonDeserialize(as = ImmutableCodecFixtures.class)
public interface CodecFixtures {

  List<CodecFixture> accountState();

  List<CodecFixture> transactions();

  List<CodecFixture> ledgerData();

}
