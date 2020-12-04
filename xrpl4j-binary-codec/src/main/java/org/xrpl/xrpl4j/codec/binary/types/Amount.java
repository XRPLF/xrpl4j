package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

/**
 * Model for XRPL issued currency JSON.
 */
@Immutable
@JsonSerialize(as = ImmutableAmount.class)
@JsonDeserialize(as = ImmutableAmount.class)
interface Amount {

  static ImmutableAmount.Builder builder() {
    return ImmutableAmount.builder();
  }

  String currency();

  String value();

  String issuer();

}
