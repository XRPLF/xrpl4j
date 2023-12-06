package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.ledger.ImmutableXChainClaimProofSig;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XChainCommit;

import java.util.Optional;

/**
 * Represents an attestation for an {@link MetaXChainOwnedClaimIdObject}.
 *
 * <p>This class will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Immutable
@JsonSerialize(as = ImmutableMetaXChainClaimProofSig.class)
@JsonDeserialize(as = ImmutableMetaXChainClaimProofSig.class)
public interface MetaXChainClaimProofSig {

  /**
   * The account on the door account's signer list that is signing the transaction.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("AttestationSignerAccount")
  Optional<Address> attestationSignerAccount();

  /**
   * The public key used to verify the signature.
   *
   * @return A {@link PublicKey}.
   */
  @JsonProperty("PublicKey")
  Optional<PublicKey> publicKey();

  /**
   * The amount to claim in the {@link org.xrpl.xrpl4j.model.transactions.XChainCommit} transaction on the destination
   * chain.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  Optional<CurrencyAmount> amount();

  /**
   * The account that should receive this signer's share of the SignatureReward.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("AttestationRewardAccount")
  Optional<Address> attestationRewardAccount();

  /**
   * A boolean representing the chain where the event occurred.
   *
   * @return {@code true} if the event occurred on the locking chain, otherwise {@code false}.
   */
  @JsonProperty("WasLockingChainSend")
  @JsonFormat(shape = Shape.NUMBER)
  Optional<Boolean> wasLockingChainSend();

  /**
   * The destination account for the funds on the destination chain.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Destination")
  Optional<Address> destination();

}
