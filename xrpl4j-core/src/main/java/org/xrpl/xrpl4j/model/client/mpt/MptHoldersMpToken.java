package org.xrpl.xrpl4j.model.client.mpt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;

import java.util.Optional;

/**
 * Representation of an MPToken found in {@link MptHoldersResponse}s.
 */
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

  /**
   * The account that owns the MPToken.
   *
   * @return An {@link Address}.
   */
  Address account();

  /**
   * The {@link MpTokenFlags} for this MPToken.
   *
   * @return An {@link MpTokenFlags}.
   */
  MpTokenFlags flags();

  /**
   * The balance of this MPToken.
   *
   * @return An {@link MpTokenNumericAmount}.
   */
  @JsonProperty("mpt_amount")
  MpTokenNumericAmount mptAmount();

  /**
   * The amount of MPToken that is locked.
   *
   * @return An optionally present {@link MpTokenNumericAmount}.
   */
  @JsonProperty("locked_amount")
  Optional<MpTokenNumericAmount> lockedAmount();

  /**
   * The index of this MPToken.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("mptoken_index")
  Hash256 mpTokenIndex();

}
