package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A {@link CreatedNode} in transaction metadata indicates that the transaction created a new object in the ledger.
 *
 * @param <T> The type of ledger object that was created, as a {@link MetaLedgerObject}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCreatedNode.class)
@JsonDeserialize(as = ImmutableCreatedNode.class)
public interface CreatedNode<T extends MetaLedgerObject> extends AffectedNode {

  /**
   * The {@link T} containing the fields of the ledger object that was created.
   *
   * @return A {@link T}.
   */
  @JsonProperty("NewFields")
  T newFields();

}
