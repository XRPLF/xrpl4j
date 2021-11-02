package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.util.Optional;

/**
* A {@link ModifiedNode} contains the objects in the ledger that a transaction modified in some way.
* 
* @see <a href="https://xrpl.org/transaction-metadata.html#modifiednode-fields">
* https://xrpl.org/transaction-metadata.html#modifiednode-fields</a>
*/
@Value.Immutable
@JsonSerialize(as = ImmutableModifiedNode.class)
@JsonDeserialize(as = ImmutableModifiedNode.class)
@JsonTypeName("ModifiedNode")
public interface ModifiedNode extends AffectedNode {

  /**
   * FinalFields contain the content fields of the ledger object after applying any changes from this transaction. 
   * 
   * @return The content fields of the ledger object after applying any changes from this transaction. 
   */
  @JsonProperty("FinalFields")
  Optional<FinalFields> finalFields();
  
  /**
   * The previous values for all fields of the object that were changed as a result of this transaction.
   * 
   * @return The previous values for all fields of the object that were changed as a result of this transaction.
   */
  @JsonProperty("PreviousFields")
  Optional<PreviousFields> previousFields();
  
  /**
   * The identifying hash of the previous transaction to modify this ledger object.
   * 
   * @return The identifying hash of the previous transaction to modify this ledger object.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();
  
  /**
   * The {@link LedgerIndex} of the ledger version containing the previous 
   * transaction to modify this ledger object.
   * 
   * @return The {@link LedgerIndex} of the ledger version containing the previous 
   *   transaction to modify this ledger object.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<LedgerIndex> previousTransactionLedgerSequence();

}
