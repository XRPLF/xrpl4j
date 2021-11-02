package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * An {@link AffectedNode} contains the objects in the ledger that a transaction modified in some way.
 * 
 * @see <a href="https://xrpl.org/transaction-metadata.html#affectednodesl">
 * https://xrpl.org/transaction-metadata.html#affectednodesl</a>
 */
@JsonTypeInfo(use = Id.NAME,
              include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @Type(value = CreatedNode.class, name = "CreatedNode"),
    @Type(value = ModifiedNode.class, name = "ModifiedNode"),
    @Type(value = DeletedNode.class, name = "DeletedNode")}
)
public interface AffectedNode {
  
  /**
   * The type of ledger object that was created/modified/deleted.
   * 
   * @return the type of ledger object that was created/modified/deleted.
   */ 
  @JsonProperty("LedgerEntryType")
  String ledgerEntryType();
  
  /**
   * The ID of the ledger object in the ledger's state tree.
   * This is not the same as a ledger index, even though the field name is very similar.
   * 
   * @return the {@link Hash256} of the ledger object in the ledger's state tree. 
   */
  @JsonProperty("LedgerIndex")
  Hash256 ledgerIndex();
    
}
