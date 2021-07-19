package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * Response object for a "deposit_authorized" rippled API method call.
 *
 * @see "https://xrpl.org/deposit_authorized.html"
 */
@JsonSerialize(as = ImmutableDepositAuthorizedResult.class)
@JsonDeserialize(as = ImmutableDepositAuthorizedResult.class)
public interface DepositAuthorizedResult extends XrplResult {

  static ImmutableDepositAuthorizedResult.Builder builder() {
    return ImmutableDepositAuthorizedResult.builder();
  }

  /**
   * Unique {@link Address} of the account that would send funds in a transaction.
   *
   * @return The unique {@link Address} of the source account.
   */
  Address sourceAccount();

  /**
   * The destination account specified in the request.
   *
   * @return An {@link Address}.
   */
  Address destinationAccount();

  /**
   * <p>Whether the specified source account is authorized to send payments directly to the destination account. If
   * true, either the destination account does not require Deposit Authorization or the source account is
   * preauthorized.</p>
   *
   * <p>NOTE: A deposit_authorized status of true does not guarantee that a payment can be sent from the specified
   * source to the specified destination. For example, the destination account may not have a trust line for the
   * specified currency, or there may not be sufficient liquidity to deliver a payment.</p>
   *
   * @return {@code true} if the deposit is authorized; {@code false} otherwise.
   */
  boolean depositAuthorized();

  /**
   * A 20-byte hex string for the ledger version to use.
   *
   * @return An optionally-present {@link Hash256} containing the ledger hash.
   */
  Optional<Hash256> ledgerHash();

  /**
   * (Omitted if ledger_current_index is provided instead) The ledger index of the ledger version used when retrieving
   * this information. The information does not contain any changes from ledger versions newer than this one.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  Optional<LedgerIndex> ledgerIndex();

  /**
   * (Omitted if ledger_index is provided instead) The ledger index of the current in-progress ledger, which was used
   * when retrieving this information.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * True if this data is from a validated ledger version; if false, this data is not final.
   *
   * @return {@code true} if this data is from a validated ledger version, otherwise {@code false}.
   */
  default boolean validated() {
    return false;
  }

  /**
   * To satisfy immutables.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableDepositAuthorizedResult.class)
  @JsonDeserialize(as = ImmutableDepositAuthorizedResult.class)
  abstract class AbstractDepositAuthorizedResult implements DepositAuthorizedResult {

    @JsonProperty("source_account")
    @Override
    public abstract Address sourceAccount();

    @JsonProperty("destination_account")
    @Override
    public abstract Address destinationAccount();

    @JsonProperty("deposit_authorized")
    @Override
    public abstract boolean depositAuthorized();

    @JsonProperty("ledger_hash")
    @Override
    public abstract Optional<Hash256> ledgerHash();

    @JsonProperty("ledger_index")
    @Override
    public abstract Optional<LedgerIndex> ledgerIndex();

    @JsonProperty("ledger_current_index")
    @Override
    public abstract Optional<LedgerIndex> ledgerCurrentIndex();

    @Value.Default
    @Override
    public boolean validated() {
      return false;
    }

    @Value.Check
    public void check() {
      // ledger_hash may only exist when ledger_index is the default value.
      ledgerHash().ifPresent(ledgerHash -> {
        ledgerIndex().ifPresent(ledgerIndex -> {
          Preconditions.checkArgument(
            ledgerIndex.equals(LedgerIndex.CURRENT),
            "Only specify a ledger_hash or a ledger_index, but not both."
          );
        });
      });
    }
  }
}
