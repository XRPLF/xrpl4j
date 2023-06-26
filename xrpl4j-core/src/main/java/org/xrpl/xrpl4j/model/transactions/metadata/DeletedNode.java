package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

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
   * Construct a {@code DeletedNode} builder.
   *
   * @return An {@link ImmutableDeletedNode.Builder}.
   */
  static <T extends MetaLedgerObject> ImmutableDeletedNode.Builder<T> builder() {
    return ImmutableDeletedNode.builder();
  }

  /**
   * The {@link T} containing the fields of the ledger object before it was removed.
   *
   * @return A {@link T}.
   */
  @JsonProperty("FinalFields")
  T finalFields();

}
