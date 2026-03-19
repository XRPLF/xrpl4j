package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerData;

import java.util.Optional;

/**
 * Represents a LoanBroker ledger object, which describes a loan broker instance.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanBrokerObject.class)
@JsonDeserialize(as = ImmutableLoanBrokerObject.class)
@Beta
public interface LoanBrokerObject extends LedgerObject {

  /**
   * Construct a {@code LoanBrokerObject} builder.
   *
   * @return An {@link ImmutableLoanBrokerObject.Builder}.
   */
  static ImmutableLoanBrokerObject.Builder builder() {
    return ImmutableLoanBrokerObject.builder();
  }

  /**
   * The type of ledger object, which will always be "LoanBroker" in this case.
   *
   * @return Always returns {@link LedgerEntryType#LOAN_BROKER}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.LOAN_BROKER;
  }

  /**
   * No flags are defined for {@link LoanBrokerObject}, so this value is always 0.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The ID of the transaction that last modified this object.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The sequence of the ledger containing the transaction that last modified this object.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The transaction sequence number that created the {@code LoanBroker}.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * A sequential identifier for Loan objects, incremented each time a new Loan is created by this LoanBroker
   * instance.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("LoanSequence")
  UnsignedInteger loanSequence();

  /**
   * Identifies the page where this item is referenced in the owner's directory.
   *
   * @return A {@link String}.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * The number of objects this account owns in the ledger, which contributes to its owner reserve.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the number of objects.
   */
  @JsonProperty("OwnerCount")
  Optional<UnsignedInteger> ownerCount();

  /**
   * Identifies the page where this item is referenced in the Vault's pseudo-account owner's directory.
   *
   * @return A {@link String}.
   */
  @JsonProperty("VaultNode")
  String vaultNode();

  /**
   * The ID of the {@code Vault} object associated with this Lending Protocol Instance.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("VaultID")
  Hash256 vaultId();

  /**
   * The address of the {@code LoanBroker} pseudo-account.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The address of the Loan Broker account.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Owner")
  Address owner();

  /**
   * Arbitrary metadata about the {@code LoanBroker}. Limited to 256 bytes.
   *
   * @return An optionally-present {@link LoanBrokerData}.
   */
  @JsonProperty("Data")
  Optional<LoanBrokerData> data();

  /**
   * The 1/10th basis point fee charged by the Lending Protocol. Valid values are between 0 and 10000
   * inclusive. A value of 1 is equivalent to 1/10 bps or 0.001%.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("ManagementFeeRate")
  Optional<UnsignedInteger> managementFeeRate();

  /**
   * The total asset amount the protocol owes the Vault, including interest.
   *
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("DebtTotal")
  Optional<AssetAmount> debtTotal();

  /**
   * The maximum amount the protocol can owe the Vault. The default value of 0 means there is no limit to the
   * debt.
   *
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("DebtMaximum")
  Optional<AssetAmount> debtMaximum();

  /**
   * The total amount of first-loss capital deposited into the Lending Protocol.
   *
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("CoverAvailable")
  Optional<AssetAmount> coverAvailable();

  /**
   * The 1/10th basis point of the {@code DebtTotal} that the first-loss capital must cover. Valid values are
   * between 0 and 100000 inclusive. A value of 1 is equivalent to 1/10 bps or 0.001%.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("CoverRateMinimum")
  Optional<UnsignedInteger> coverRateMinimum();

  /**
   * The 1/10th basis point of minimum required first-loss capital that is liquidated to cover a Loan default.
   * Valid values are between 0 and 100000 inclusive. A value of 1 is equivalent to 1/10 bps or 0.001%.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("CoverRateLiquidation")
  Optional<UnsignedInteger> coverRateLiquidation();

  /**
   * The unique ID of this {@link LoanBrokerObject}.
   *
   * @return A {@link Hash256}.
   */
  Hash256 index();

}
