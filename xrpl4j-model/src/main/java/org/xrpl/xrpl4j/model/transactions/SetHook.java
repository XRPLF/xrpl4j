package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.Flags;

/**
 * An SetHook transaction creates a Hook.
 */
@Immutable
@JsonSerialize(as = ImmutableSetHook.class)
@JsonDeserialize(as = ImmutableSetHook.class)
public interface SetHook extends Transaction {

  static ImmutableSetHook.Builder builder() {
    return ImmutableSetHook.builder();
  }

  @JsonProperty("Flags")
  @Value.Default
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }

  @JsonProperty("HookOn")
  @Value.Default
  default UnsignedLong hookOn() {
    return UnsignedLong.valueOf(0);
  }

  /**
   * Hex-encoded binary of hook code.
   * @return Hex-encoded hook code.
   */
  @JsonProperty("CreateCode")
  String createCode();

}
