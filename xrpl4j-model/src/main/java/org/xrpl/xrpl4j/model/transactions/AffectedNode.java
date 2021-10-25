package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.Optional;

@JsonTypeInfo(use = Id.NAME,
              include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @Type(value = CreatedNode.class, name = "CreatedNode"),
    @Type(value = ModifiedNode.class, name = "ModifiedNode"),
    @Type(value = DeletedNode.class, name = "DeletedNode")}
)
public interface AffectedNode {
    
  @JsonProperty("LedgerEntryType")
  Optional<String> ledgerEntryType();
    
  @JsonProperty("LedgerIndex")
  Optional<String> ledgerIndex();
    
}
