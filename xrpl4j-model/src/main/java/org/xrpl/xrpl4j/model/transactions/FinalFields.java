package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * The content fields of the ledger object immediately before it was deleted. 
 * Which fields are present depends on what type of ledger object was created.
 * 
 * @see <a href="https://xrpl.org/transaction-metadata.html#deletednode-fields">
 * https://xrpl.org/transaction-metadata.html#deletednode-fields</a>
 * @see <a href="https://xrpl.org/transaction-metadata.html#modifiednode-fields">
 * https://xrpl.org/transaction-metadata.html#modifiednode-fields</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableFinalFields.class)
@JsonDeserialize(as = ImmutableFinalFields.class)
public interface FinalFields {
  
  /**
   * Optional {@link Address} that was the subject of a {@link DeletedNode} 
   * or {@link ModifiedNode} of a transaction.
   * 
   * @return if an account was involved in this transaction, this is that account's {@link Address}.
   */
  @JsonProperty("Account") 
  Optional<Address> account();
    
}
