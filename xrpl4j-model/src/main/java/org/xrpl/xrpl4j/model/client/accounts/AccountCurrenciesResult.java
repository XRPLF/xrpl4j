package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;

/**
 * The result of an account_currencies rippled call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountCurrenciesResult.class)
@JsonDeserialize(as = ImmutableAccountCurrenciesResult.class)
public interface AccountCurrenciesResult extends XrplResult {

    static ImmutableAccountCurrenciesResult.Builder builder() {
        return ImmutableAccountCurrenciesResult.builder();
    }

    /**
     * The identifying Hash of the ledger version used to generate this response.
     *
     * @return A {@link Hash256} containing the ledger hash.
     */
    @JsonProperty("ledger_hash")
    Hash256 ledgerHash();

    /**
     * The Ledger Index of the ledger version used to generate this response.
     *
     * @return A {@link LedgerIndex}.
     */
    @JsonProperty("ledger_index")
    LedgerIndex ledgerIndex();

    /**
     * If true, the information in this response comes from a validated ledger version.
     * Otherwise, the information is subject to change.
     *
     * @return {@code true} if the information in this response comes from a validated ledger version, {@code false}
     *     if not.
     */
    boolean validated();

    /**
     * Array of currency codes for currencies that this account can receive.
     *
     * @return Array of currencies that this account can receive.
     */
    @JsonProperty("receive_currencies")
    List<String> receiveCurrencies();

    /**
     * Array of currency codes for currencies that this account can send
     *
     * @return Array of currencies that this account can send.
     */
    @JsonProperty("send_currencies")
    List<String> sendCurrencies();
}
