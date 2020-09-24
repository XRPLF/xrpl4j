package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
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

  /**
   * @see "https://xrpl.org/paths.html#path-specifications"
   */
  @Value.Check
  default void validateFields() {
    if (account().isPresent()) {
      Preconditions.checkArgument(!currency().isPresent(), "Path currency cannot be specified if the Path account is specified.");
      Preconditions.checkArgument(!issuer().isPresent(), "Path issuer cannot be specified if the Path account is specified.");
    }

    if (currency().isPresent()) {
      Preconditions.checkArgument(!account().isPresent(), "Path account cannot be specified if the Path currency is specified.");
      if (issuer().isPresent()) {
        Preconditions.checkArgument(!currency().get().equals("XRP"), "Path currency and issuer can only be specified if currency is NOT XRP");
      }
    }

    if (issuer().isPresent()) {
      Preconditions.checkArgument(!currency().isPresent(), "Path currency cannot be specified if the Path issuer is specified.");
      if (currency().isPresent()) {
        Preconditions.checkArgument(!currency().get().equals("XRP"), "Path currency and issuer can only be specified if currency is NOT XRP");
      }
    }
  }



}
