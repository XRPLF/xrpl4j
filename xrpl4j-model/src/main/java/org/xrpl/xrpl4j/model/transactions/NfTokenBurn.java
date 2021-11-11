package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * The {@link NfTokenBurn} transaction is used to remove an NfToken object from the
 * NfTokenPage in which it is being held, effectively removing the token from
 * the ledger ("burning" it).
 *
 * <p>If this operation succeeds, the corresponding NfToken is removed. If this
 * operation empties the NfTokenPage holding the NfToken or results in the
 * consolidation, thus removing an NfTokenPage, the owner’s reserve requirement
 * is reduced by one.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenBurn.class)
@JsonDeserialize(as = ImmutableNfTokenBurn.class)
public interface NfTokenBurn extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNfTokenBurn.Builder}.
   */
  static ImmutableNfTokenBurn.Builder builder() {
    return ImmutableNfTokenBurn.builder();
  }

  /**
   * Indicates the {@link Address} of the account that submitted this transaction. The account MUST
   * be either the present owner of the token or, if the lsfBurnable flag is set
   * in the NfToken, either the issuer account or an account authorized by the
   * issuer, i.e. {@link Minter} field in {@link AccountSet} set as the address of the issuer.
   *
   * @return An {@link Address} of the account initiating the burning of the NfToken.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * Identifies the NfToken object to be removed by the transaction.
   *
   * @return The {@link TokenID} of the NfToken to be burned.
   */
  @JsonProperty("TokenID")
  String tokenId();
}