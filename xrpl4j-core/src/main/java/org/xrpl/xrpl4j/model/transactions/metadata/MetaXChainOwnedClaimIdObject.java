package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.ImmutableXChainOwnedClaimIdObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;
import org.xrpl.xrpl4j.model.transactions.XChainCommit;
import org.xrpl.xrpl4j.model.transactions.XChainCreateClaimId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;
import java.util.Optional;

/**
 * An {@code XChainOwnedClaimID} object represents one cross-chain transfer of value and includes information of the
 * account on the source chain that locks or burns the funds on the source chain.
 *
 * <p>The {@code XChainOwnedClaimID} object must be acquired on the destination chain before submitting a
 * {@link XChainCommit} on the source chain. Its purpose is to prevent transaction replay attacks and is also used as a
 * place to collect attestations from witness servers.</p>
 *
 * <p> An {@link XChainCreateClaimId} transaction is used to create a new {@code XChainOwnedClaimID}. The ledger object
 * is destroyed when the funds are successfully claimed on the destination chain.</p>
 *
 * <p>This interface will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableMetaXChainOwnedClaimIdObject.class)
@JsonDeserialize(as = ImmutableMetaXChainOwnedClaimIdObject.class)
public interface MetaXChainOwnedClaimIdObject extends MetaLedgerObject {

  /**
   * A bit-map of boolean flags. No flags are defined for {@link MetaXChainOwnedClaimIdObject}, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The account that owns this object.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Optional<Address> account();

  /**
   * The door accounts and assets of the bridge this object correlates to.
   *
   * @return An {@link XChainBridge}.
   */
  @JsonProperty("XChainBridge")
  Optional<XChainBridge> xChainBridge();

  /**
   * The unique sequence number for a cross-chain transfer.
   *
   * @return An {@link XChainClaimId}.
   */
  @JsonProperty("XChainClaimID")
  Optional<XChainClaimId> xChainClaimId();

  /**
   * The account that must send the corresponding {@link XChainCommit} on the source chain. The destination may be
   * specified in the {@link XChainCommit} transaction, which means that if the OtherChainSource isn't specified,
   * another account can try to specify a different destination and steal the funds. This also allows tracking only a
   * single set of signatures, since we know which account will send the {@link XChainCommit} transaction.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("OtherChainSource")
  Optional<Address> otherChainSource();

  /**
   * Attestations collected from the witness servers. This includes the parameters needed to recreate the message that
   * was signed, including the amount, which chain (locking or issuing), optional destination, and reward account for
   * that signature.
   *
   * @return A {@link List} of {@link MetaXChainClaimAttestation}s.
   */
  @JsonProperty("XChainClaimAttestations")
  List<MetaXChainClaimAttestation> xChainClaimAttestations();

  /**
   * The total amount to pay the witness servers for their signatures. It must be at least the value of SignatureReward
   * in the Bridge ledger object.
   *
   * @return An {@link XrpCurrencyAmount}.
   */
  @JsonProperty("SignatureReward")
  Optional<XrpCurrencyAmount> signatureReward();

  /**
   * A hint indicating which page of the sender's owner directory links to this object, in case the directory consists
   * of multiple pages.
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.
   *
   * @return A {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

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

}
