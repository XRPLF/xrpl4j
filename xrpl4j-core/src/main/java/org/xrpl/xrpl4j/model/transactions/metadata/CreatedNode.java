package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCreatedNode.class)
@JsonDeserialize(as = ImmutableCreatedNode.class)
public interface CreatedNode<T extends MetaLedgerObject> extends AffectedNode {

  /**
   * Construct a {@code CreatedNode} builder.
   *
   * @return An {@link ImmutableCreatedNode.Builder}.
   */
  static ImmutableCreatedNode.Builder builder() {
    return ImmutableCreatedNode.builder();
  }

  @JsonProperty("NewFields")
  T newFields();

}
