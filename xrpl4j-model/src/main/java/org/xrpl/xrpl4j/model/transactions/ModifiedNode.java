package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableModifiedNode.class)
@JsonDeserialize(as = ImmutableModifiedNode.class)
@JsonTypeName("ModifiedNode")
public interface ModifiedNode extends AffectedNode {

  @JsonProperty("FinalFields")
  Optional<FinalFields> finalFields();

}
