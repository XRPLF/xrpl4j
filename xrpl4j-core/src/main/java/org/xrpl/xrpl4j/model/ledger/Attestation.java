package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.Beta;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;

/**
 * Represents a generic cross-chain attestation.
 *
 * <p>This class will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
public interface Attestation {

  /**
   * The bridge spec.
   *
   * @return An {@link XChainBridge}.
   */
  @JsonProperty("XChainBridge")
  @SuppressWarnings("MethodName")
  XChainBridge xChainBridge();

  /**
   * The account on the source chain that submitted the
   * {@link org.xrpl.xrpl4j.model.transactions.XChainAccountCreateCommit} transaction or
   * {@link org.xrpl.xrpl4j.model.transactions.XChainCommit} transaction that triggered the event associated with the
   * attestation.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("OtherChainSource")
  Address otherChainSource();

  /**
   * The account that should receive this signer's share of the {@code SignatureReward}.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("AttestationRewardAccount")
  Address attestationRewardAccount();

  /**
   * The amount committed by the {@link org.xrpl.xrpl4j.model.transactions.XChainAccountCreateCommit} or
   * {@link org.xrpl.xrpl4j.model.transactions.XChainCommit} transaction on the source chain.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

  /**
   * A boolean representing the chain where the event occurred.
   *
   * @return {@code true} if the event occurred on the locking chain, otherwise {@code false}.
   */
  @JsonProperty("WasLockingChainSend")
  @JsonFormat(shape = Shape.NUMBER)
  boolean wasLockingChainSend();
}
