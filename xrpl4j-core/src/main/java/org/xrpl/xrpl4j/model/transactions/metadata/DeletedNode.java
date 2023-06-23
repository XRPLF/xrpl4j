package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDeletedNode.class)
@JsonDeserialize(as = ImmutableDeletedNode.class)
public interface DeletedNode<T extends MetaLedgerObject> extends AffectedNode {

  /**
   * Construct a {@code DeletedNode} builder.
   *
   * @return An {@link ImmutableDeletedNode.Builder}.
   */
  static ImmutableDeletedNode.Builder builder() {
    return ImmutableDeletedNode.builder();
  }

  @JsonProperty("FinalFields")
  T finalFields();

}
