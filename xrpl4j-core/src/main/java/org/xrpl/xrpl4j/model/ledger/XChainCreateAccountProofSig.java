package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XChainAccountCreateCommit;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Represents an attestation for an {@link XChainOwnedCreateAccountClaimIdObject}.
 *
 * <p>This interface will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableXChainCreateAccountProofSig.class)
@JsonDeserialize(as = ImmutableXChainCreateAccountProofSig.class)
public interface XChainCreateAccountProofSig {

  /**
   * Construct a {@code XChainCreateAccountProofSig} builder.
   *
   * @return An {@link ImmutableXChainCreateAccountProofSig.Builder}.
   */
  static ImmutableXChainCreateAccountProofSig.Builder builder() {
    return ImmutableXChainCreateAccountProofSig.builder();
  }

  /**
   * The amount committed by the {@link XChainAccountCreateCommit} transaction on the source chain.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

  /**
   * The total amount, in XRP, to be rewarded for providing a signature for cross-chain transfer or for signing for the
   * cross-chain reward. This amount will be split among the signers.
   *
   * @return An {@link XrpCurrencyAmount}.
   */
  @JsonProperty("SignatureReward")
  XrpCurrencyAmount signatureReward();

  /**
   * The account that should receive this signer's share of the {@link #signatureReward()}.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("AttestationRewardAccount")
  Address attestationRewardAccount();

  /**
   * The account on the door account's signer list that is signing the transaction.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("AttestationSignerAccount")
  Address attestationSignerAccount();

  /**
   * The destination account for the funds on the destination chain.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * The public key used to verify the signature.
   *
   * @return A {@link PublicKey}.
   */
  @JsonProperty("PublicKey")
  PublicKey publicKey();

  /**
   * A boolean representing the chain where the event occurred.
   *
   * @return {@code true} if the event occurred on the locking chain, otherwise {@code false}.
   */
  @JsonProperty("WasLockingChainSend")
  @JsonFormat(shape = Shape.NUMBER)
  boolean wasLockingChainSend();

}
