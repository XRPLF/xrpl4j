package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.util.Optional;

@Immutable
@JsonSerialize(as = ImmutableMetaMpTokenIssuanceObject.class)
@JsonDeserialize(as = ImmutableMetaMpTokenIssuanceObject.class)
public interface MetaMpTokenIssuanceObject extends MetaLedgerObject {

  @JsonProperty("Flags")
  Optional<MpTokenIssuanceFlags> flags();

  @JsonProperty("Issuer")
  Optional<Address> issuer();

  @JsonProperty("AssetScale")
  Optional<AssetScale> assetScale();

  @JsonProperty("MaximumAmount")
  Optional<MpTokenNumericAmount> maximumAmount();

  @JsonProperty("OutstandingAmount")
  Optional<MpTokenNumericAmount> outstandingAmount();

  @JsonProperty("TransferFee")
  Optional<TransferFee> transferFee();

  @JsonProperty("MPTokenMetadata")
  Optional<String> mpTokenMetadata();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<UnsignedInteger> previousTransactionLedgerSequence();

  /**
   * A 32-bit unsigned integer that is used to ensure issuances from a given sender may only ever exist once, even if an
   * issuance is later deleted. Whenever a new issuance is created, this value must match the account's current Sequence
   * number.
   *
   * @return An {@link UnsignedInteger} representing the account sequence number.
   */
  @JsonProperty("Sequence")
  Optional<UnsignedInteger> sequence();

  /**
   * A hint indicating which page of the owner directory links to this object, in case the directory consists of
   * multiple pages.
   *
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.</p>
   *
   * @return An {@link Optional} of type {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

}
