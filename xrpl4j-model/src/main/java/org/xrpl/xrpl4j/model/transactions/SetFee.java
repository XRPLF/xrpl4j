package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.util.Optional;

/**
 * A {@link SetFee} pseudo-transaction marks a change in transaction cost or reserve
 * requirements as a result of Fee Voting.
 *
 * @see "https://xrpl.org/setfee.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSetFee.class)
@JsonDeserialize(as = ImmutableSetFee.class)
public interface SetFee extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSetFee.Builder}.
   */
  static ImmutableSetFee.Builder builder() {
    return ImmutableSetFee.builder();
  }

  /**
   * The charge, in drops of XRP, for the reference transaction, as hex.
   * (This is the transaction cost before scaling for load.)
   *
   * @return A hex {@link String} basefee value.
   */
  @JsonProperty("BaseFee")
  String baseFee();

  /**
   * The cost, in fee units, of the reference transaction.
   *
   * @return An {@link UnsignedInteger} cost of ref transaction.
   */
  @JsonProperty("ReferenceFeeUnits")
  UnsignedInteger referenceFeeUnits();

  /**
   * The base reserve, in drops.
   *
   * @return An {@link UnsignedInteger} base reverse value in {@link org.xrpl.xrpl4j.model.client.fees.FeeDrops}.
   */
  @JsonProperty("ReserveBase")
  UnsignedInteger reserveBase();

  /**
   * The incremental reserve, in drops.
   *
   * @return An {@link UnsignedInteger} incremental reserve in {@link org.xrpl.xrpl4j.model.client.fees.FeeDrops}.
   */
  @JsonProperty("ReserveIncrement")
  UnsignedInteger reserveIncrement();

  /**
   * The index of the ledger version where this pseudo-transaction appears. This distinguishes the
   * pseudo-transaction from other occurrences of the same change.
   * Omitted for some historical SetFee pseudo-transactions hence making it optional.
   *
   * @return A {@link LedgerIndex} to indicates where the tx appears.
   */
  @JsonProperty("LedgerSequence")
  Optional<LedgerIndex> ledgerSequence();
}
