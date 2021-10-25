package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCreatedNode.class)
@JsonDeserialize(as = ImmutableCreatedNode.class)
@JsonTypeName("CreatedNode")
public interface CreatedNode extends AffectedNode {

  @JsonProperty("NewFields") 
  NewFields newFields();

}
