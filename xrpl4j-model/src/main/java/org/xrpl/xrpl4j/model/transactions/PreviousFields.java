package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutablePreviousFields.class)
@JsonDeserialize(as = ImmutablePreviousFields.class)
public interface PreviousFields {
    
   
}
