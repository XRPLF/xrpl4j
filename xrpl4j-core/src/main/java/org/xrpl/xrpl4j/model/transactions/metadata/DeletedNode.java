package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * A {@link DeletedNode} in transaction metadata indicates that the transaction removed an object from the ledger.
 *
 * @param <T> The type of ledger object that was deleted, as a {@link MetaLedgerObject}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDeletedNode.class)
@JsonDeserialize(as = ImmutableDeletedNode.class)
public interface DeletedNode<T extends MetaLedgerObject> extends AffectedNode {

  /**
   * The {@link T} containing the fields of the ledger object before it was removed.
   *
   * @return A {@link T}.
   */
  @JsonProperty("FinalFields")
  T finalFields();

  /**
   * The {@link T} containing the fields of the ledger object if it was modified before it was removed in the same
   * transaction. This field will rarely be present, but may be present under certain conditions.
   *
   * @return An {@link Optional} {@link T}.
   */
  @JsonProperty("PreviousFields")
  Optional<T> previousFields();

}
