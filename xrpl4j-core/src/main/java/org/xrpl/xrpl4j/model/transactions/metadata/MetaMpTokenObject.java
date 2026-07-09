package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.MpTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.EncryptedAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;

import java.util.Optional;

@Immutable
@JsonSerialize(as = ImmutableMetaMpTokenObject.class)
@JsonDeserialize(as = ImmutableMetaMpTokenObject.class)
public interface MetaMpTokenObject extends MetaLedgerObject {

  @JsonProperty("Flags")
  Optional<MpTokenFlags> flags();

  @JsonProperty("Account")
  Optional<Address> account();

  @JsonProperty("MPTokenIssuanceID")
  Optional<MpTokenIssuanceId> mpTokenIssuanceId();

  @JsonProperty("MPTAmount")
  Optional<MpTokenNumericAmount> mptAmount();

  /**
   * The amount of tokens currently locked up (for example, in escrow).
   *
   * @return An optionally-present {@link MpTokenNumericAmount}.
   */
  @JsonProperty("LockedAmount")
  Optional<MpTokenNumericAmount> lockedAmount();

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

  /**
   * The holder's 33-byte compressed ElGamal encryption key for confidential transfers.
   *
   * @return An optionally-present {@link PublicKey}.
   */
  @JsonProperty("HolderEncryptionKey")
  Optional<PublicKey> holderEncryptionKey();

  /**
   * The holder's confidential spending balance (encrypted).
   *
   * @return An optionally-present {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("ConfidentialBalanceSpending")
  Optional<EncryptedAmount> confidentialBalanceSpending();

  /**
   * The holder's confidential inbox balance (encrypted).
   *
   * @return An optionally-present {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("ConfidentialBalanceInbox")
  Optional<EncryptedAmount> confidentialBalanceInbox();

  /**
   * The issuer's encrypted mirror balance for this holder's tokens.
   *
   * @return An optionally-present {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("IssuerEncryptedBalance")
  Optional<EncryptedAmount> issuerEncryptedBalance();

  /**
   * The auditor's encrypted balance for this holder's tokens.
   *
   * @return An optionally-present {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("AuditorEncryptedBalance")
  Optional<EncryptedAmount> auditorEncryptedBalance();

  /**
   * The version number of the confidential balance.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("ConfidentialBalanceVersion")
  Optional<UnsignedInteger> confidentialBalanceVersion();

}
