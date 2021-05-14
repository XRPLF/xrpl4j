package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * Request parameters for a "deposit_authorized" rippled API method call.
 *
 * @see "https://xrpl.org/deposit_authorized.html"
 */
public interface DepositAuthorizedRequestParams extends XrplRequestParams {

  static ImmutableDepositAuthorizedRequestParams.Builder builder() {
    return ImmutableDepositAuthorizedRequestParams.builder();
  }

  /**
   * Unique {@link Address} of the account that would send funds in a transaction.
   *
   * @return The unique {@link Address} of the source account.
   */
  Address sourceAccount();

  /**
   * Unique {@link Address} of the account that would receive funds in a transaction.
   *
   * @return The unique {@link Address} of the destination account.
   */
  Address destinationAccount();

  /**
   * A 20-byte hex string for the ledger version to use.
   *
   * @return An optionally-present {@link Hash256} containing the ledger hash.
   */
  Optional<Hash256> ledgerHash();

  /**
   * The ledger index of the ledger to use, or a shortcut string to choose a ledger automatically.
   *
   * @return A {@link LedgerIndex} representing the ledger index. Defaults to {@link LedgerIndex#CURRENT}.
   */
  default LedgerIndex ledgerIndex() {
    return LedgerIndex.CURRENT;
  }

  /**
   * To satisfy immutables.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableDepositAuthorizedRequestParams.class)
  @JsonDeserialize(as = ImmutableDepositAuthorizedRequestParams.class)
  abstract class AbstractDepositAuthorizedRequestParams implements DepositAuthorizedRequestParams {

    @JsonProperty("source_account")
    @Override
    public abstract Address sourceAccount();

    @JsonProperty("destination_account")
    @Override
    public abstract Address destinationAccount();

    @JsonProperty("ledger_hash")
    @Override
    public abstract Optional<Hash256> ledgerHash();

    @JsonProperty("ledger_index")
    @Value.Default
    @Override
    public LedgerIndex ledgerIndex() {
      return LedgerIndex.CURRENT;
    }

    @Value.Check
    public void check() {
      // ledger_hash may only exist when ledger_index is the default value.
      ledgerHash().ifPresent($ -> {
        Preconditions.checkArgument(
          ledgerIndex().equals(LedgerIndex.CURRENT),
          "Only specify a ledger_hash or a ledger_index, but not both."
        );
      });
    }
  }
}
