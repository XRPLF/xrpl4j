package com.ripple.xrplj4.client.model.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Market interface for XRP Ledger Objects.
 *
 * TODO: pull common fields up.
 */
@JsonDeserialize(using = LedgerObjectDeserializer.class)
public interface LedgerObject {

}
