package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDeletedNode.class)
@JsonDeserialize(as = ImmutableDeletedNode.class)
@JsonTypeName("DeletedNode")
public interface DeletedNode extends AffectedNode {
    
  @JsonProperty("FinalFields")
  FinalFields finalFields();

}
