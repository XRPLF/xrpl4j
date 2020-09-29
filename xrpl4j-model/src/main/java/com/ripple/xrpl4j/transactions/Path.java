package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePath.class)
@JsonDeserialize(as = ImmutablePath.class)
public interface Path {

  static ImmutablePath.Builder builder() {
    return ImmutablePath.builder();
  }

  Optional<Address> account();

  Optional<String> currency();

  Optional<String> issuer();

}
