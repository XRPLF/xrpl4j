package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

@Value.Immutable
@JsonSerialize(as = ImmutableModifiedNode.class)
@JsonDeserialize(as = ImmutableModifiedNode.class)
public interface ModifiedNode<T extends MetaLedgerObject> extends AffectedNode {

  /**
   * Construct a {@code ModifiedNode} builder.
   *
   * @return An {@link ImmutableModifiedNode.Builder}.
   */
  static ImmutableModifiedNode.Builder builder() {
    return ImmutableModifiedNode.builder();
  }

  @JsonProperty("FinalFields")
  T finalFields();

  @JsonProperty("PreviousFields")
  T previousFields();

  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  @JsonProperty("PreviousTxnLgrSeq")
  LedgerIndex previousTransactionsLedgerSequence();

}
