package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * A {@link DeletedNode} contains the objects in the ledger that a transaction deleted.
 * 
 * @see <a href="https://xrpl.org/transaction-metadata.html#deletednode-fields">
 * https://xrpl.org/transaction-metadata.html#deletednode-fields</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDeletedNode.class)
@JsonDeserialize(as = ImmutableDeletedNode.class)
@JsonTypeName("DeletedNode")
public interface DeletedNode extends AffectedNode {
    
  /**
   * {@link FinalField}s contain the fields of the objects affected by the {@link DeletedNode}.
   * 
   * @return The content fields of the ledger object immediately before it was deleted. 
   */
  @JsonProperty("FinalFields")
  Optional<FinalFields> finalFields();

}
