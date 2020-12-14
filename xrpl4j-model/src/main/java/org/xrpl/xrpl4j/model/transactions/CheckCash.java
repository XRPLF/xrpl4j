package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.flags.Flags.TransactionFlags;

import java.util.Optional;

/**
 * The {@link CheckCash} transaction attempts to redeem a Check object in the ledger to receive up to the amount
 * authorized by the corresponding {@link CheckCreate} transaction. Only the Destination address of a Check can cash
 * it with a CheckCash transaction. Cashing a check this way is similar to executing a {@link Payment} initiated by
 * the destination.
 *
 * <p>Since the funds for a check are not guaranteed, redeeming a Check can fail because the sender does not have a
 * high enough balance or because there is not enough liquidity to deliver the funds. If this happens, the Check
 * remains in the ledger and the destination can try to cash it again later, or for a different amount.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCheckCash.class)
@JsonDeserialize(as = ImmutableCheckCash.class)
public interface CheckCash extends Transaction {

  static ImmutableCheckCash.Builder builder() {
    return ImmutableCheckCash.builder();
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link AccountDelete}, which only allows tfFullyCanonicalSig flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default TransactionFlags flags() {
    return new TransactionFlags.Builder().fullyCanonicalSig(true).build();
  }

  /**
   * The ID of the Check ledger object to cash, as a 64-character hexadecimal string.
   */
  @JsonProperty("CheckID")
  Hash256 checkId();

  /**
   * Redeem the Check for exactly this amount, if possible.
   * The currency must match that of the {@link CheckCreate#sendMax()}SendMax of the corresponding {@link CheckCreate}
   * transaction. You must provide either this field or {@link CheckCash#deliverMin()}.
   */
  @JsonProperty("Amount")
  Optional<CurrencyAmount> amount();

  /**
   * Redeem the Check for at least this amount and for as much as possible.
   * The currency must match that of the {@link CheckCreate#sendMax()}SendMax of the corresponding {@link CheckCreate}
   * transaction. You must provide either this field or {@link CheckCash#amount()}.
   *
   * @return
   */
  @JsonProperty("DeliverMin")
  Optional<CurrencyAmount> deliverMin();

  /**
   * Ensure that either {@link CheckCash#amount()} or {@link CheckCash#deliverMin()} is present, but not both.
   */
  @Value.Check
  default void validateOnlyOneAmountSet() {
    Preconditions.checkArgument((amount().isPresent() || deliverMin().isPresent()) &&
            !(amount().isPresent() && deliverMin().isPresent()),
        "The CheckCash transaction must include either amount or deliverMin, but not both.");
  }
}
