package org.xrpl.xrpl4j.model.client.mpt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenObjectAmount;

import java.util.Optional;

@Immutable
@JsonSerialize(as = ImmutableMptHoldersMpToken.class)
@JsonDeserialize(as = ImmutableMptHoldersMpToken.class)
public interface MptHoldersMpToken {

  /**
   * Construct a {@code MptHoldersMpToken} builder.
   *
   * @return An {@link ImmutableMptHoldersMpToken.Builder}.
   */
  static ImmutableMptHoldersMpToken.Builder builder() {
    return ImmutableMptHoldersMpToken.builder();
  }

  Address account();

  MpTokenFlags flags();

  @JsonProperty("mpt_amount")
  MpTokenObjectAmount mptAmount();

  @JsonProperty("locked_amount")
  Optional<MpTokenAmount> lockedAmount();

  @JsonProperty("mptoken_index")
  Hash256 mpTokenIndex();

}
