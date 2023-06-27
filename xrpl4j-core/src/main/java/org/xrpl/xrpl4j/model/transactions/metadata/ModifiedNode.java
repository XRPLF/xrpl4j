package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * A {@link ModifiedNode} in transaction metadata indicates that the transaction modified an existing object in the
 * ledger.
 *
 * @param <T> The type of ledger object that was modified, as a {@link MetaLedgerObject}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableModifiedNode.class)
@JsonDeserialize(as = ImmutableModifiedNode.class)
public interface ModifiedNode<T extends MetaLedgerObject> extends AffectedNode {

  /**
   * The {@link T} containing the fields of the ledger object after applying any changes from this transaction. The
   * {@link T} will not contain {@code PreviousTxnID} or {@code PreviousTxnLgrSeq} fields because they are
   * present in this object.
   *
   * @return An optionally-present {@link T}.
   */
  @JsonProperty("FinalFields")
  Optional<T> finalFields();

  /**
   * The {@link T} containing the previous values for all fields of the object that were changed as a result of this
   * transaction. If the transaction only added fields to the object, this field will be empty.
   *
   * @return An optionall-present {@link T}.
   */
  @JsonProperty("PreviousFields")
  Optional<T> previousFields();

  /**
   * The identifying hash of the previous transaction to modify this ledger object. This field will be empty
   * for ledger object types that do not have a {@code PreviousTxnID} field.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();

  /**
   * The Ledger Index of the ledger version containing the previous transaction to modify this ledger object. This
   * field will be empty for ledger object types that do not have a {@code PreviousTxnLgrSeq} field.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<LedgerIndex> previousTransactionsLedgerSequence();

}
