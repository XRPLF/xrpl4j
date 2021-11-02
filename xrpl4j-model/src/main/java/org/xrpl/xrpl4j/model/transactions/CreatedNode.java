package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A {@link CreatedNode} contains the objects in the ledger that a transaction created.
 * 
 * @see <a href="https://xrpl.org/transaction-metadata.html#creatednode-fields">
 * https://xrpl.org/transaction-metadata.html#creatednode-fields</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCreatedNode.class)
@JsonDeserialize(as = ImmutableCreatedNode.class)
@JsonTypeName("CreatedNode")
public interface CreatedNode extends AffectedNode {

  /**
   * The content fields of the newly-created ledger object. 
   * 
   * @return The content fields of the newly-created ledger object. 
   */
  @JsonProperty("NewFields") 
  NewFields newFields();

}
