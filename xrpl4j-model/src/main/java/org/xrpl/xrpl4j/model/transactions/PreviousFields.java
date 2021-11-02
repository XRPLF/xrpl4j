package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * The previous values for all fields of the object that were changed as a result of this transaction. 
 * If the transaction only added fields to the object, this field is an empty object.
 * 
 * @see <a href="https://xrpl.org/transaction-metadata.html#modifiednode-fields">
 * https://xrpl.org/transaction-metadata.html#modifiednode-fields</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePreviousFields.class)
@JsonDeserialize(as = ImmutablePreviousFields.class)
public interface PreviousFields {
    
   
}
