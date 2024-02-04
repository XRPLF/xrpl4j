package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.TimeUtils;
import org.xrpl.xrpl4j.model.flags.Flags;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UnknownTransaction implements Transaction {
  private Address account;
  private String transactionTypeText;
  private XrpCurrencyAmount fee;
  private UnsignedInteger sequence;
  private Optional<UnsignedInteger> ticketSequence;
  private Optional<Hash256> accountTransactionId;
  private Flags flags;
  private Optional<UnsignedInteger> lastLedgerSequence;
  private List<MemoWrapper> memos;
  private List<SignerWrapper> signers;
  private Optional<UnsignedInteger> sourceTag;
  private Optional<Signature> transactionSignature;
  private Optional<NetworkId> networkId;
  private PublicKey signingPublicKey;
  private Hash256 hash;
  private LedgerIndex ledgerIndex;
  private Optional<UnsignedLong> closeDate;

  private final Map<String, Object> unknowns = new LinkedHashMap<>();

  /**
   * The unique {@link Address} of the account that initiated this transaction.
   *
   * @return The {@link Address} of the account submitting this transaction.
   */
  @Override
  public Address account() {
    return account;
  }

  /**
   * The type as text of transaction.
   */
  @JsonProperty("TransactionType")
  public String transactionTypeText() {
    return transactionTypeText;
  }

  /**
   * The {@link String} representation of an integer amount of XRP, in drops, to be destroyed as a cost for distributing
   * this Payment transaction to the network.
   *
   * @return An {@link XrpCurrencyAmount} representing the transaction cost.
   */
  @Override
  public XrpCurrencyAmount fee() {
    return fee;
  }

  /**
   * The sequence number of the account submitting the {@link Transaction}. A {@link Transaction} is only valid if the
   * Sequence number is exactly 1 greater than the previous transaction from the same account.
   *
   * @return An {@link UnsignedInteger} representing the sequence of the transaction.
   */
  @JsonProperty("Sequence")
  public UnsignedInteger sequence() {
    return sequence;
  }

  /**
   * The sequence number of the {@link org.xrpl.xrpl4j.model.ledger.TicketObject} to use in place of a
   * {@link #sequence()} number. If this is provided, {@link #sequence()} must be 0. Cannot be used with
   * {@link #accountTransactionId()}.
   *
   * @return An {@link UnsignedInteger} representing the ticket sequence of the transaction.
   */
  @Override
  public Optional<UnsignedInteger> ticketSequence() {
    return ticketSequence;
  }

  /**
   * Hash value identifying another transaction. If provided, this {@link Transaction} is only valid if the sending
   * account's previously-sent transaction matches the provided hash.
   *
   * @return An {@link Optional} of type {@link Hash256} containing the account transaction ID.
   */
  @Override
  public Optional<Hash256> accountTransactionId() {
    return accountTransactionId;
  }

  /**
   * A bit-map of boolean flags.
   */
  @JsonProperty("Flags")
  public Flags flags() {
    return flags;
  }

  /**
   * Highest ledger index this transaction can appear in. Specifying this field places a strict upper limit on how long
   * the transaction can wait to be validated or rejected.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the last ledger sequence.
   */
  @Override
  public Optional<UnsignedInteger> lastLedgerSequence() {
    return lastLedgerSequence;
  }

  /**
   * Additional arbitrary information used to identify this {@link Transaction}.
   *
   * @return A {@link List} of {@link MemoWrapper}s.
   */
  @Override
  public List<MemoWrapper> memos() {
    return memos;
  }

  /**
   * Array of {@link SignerWrapper}s that represent a multi-signature which authorizes this {@link Transaction}.
   *
   * @return A {@link List} of {@link SignerWrapper}s.
   */
  @Override
  public List<SignerWrapper> signers() {
    return signers;
  }

  /**
   * Arbitrary {@link UnsignedInteger} used to identify the reason for this {@link Transaction}, or a sender on whose
   * behalf this {@link Transaction} is made.
   *
   * @return An {@link Optional} {@link UnsignedInteger} representing the source account's tag.
   */
  @Override
  public Optional<UnsignedInteger> sourceTag() {
    return sourceTag;
  }

  /**
   * The {@link PublicKey} that corresponds to the private key used to sign this transaction. If an empty string, ie
   * {@link PublicKey#MULTI_SIGN_PUBLIC_KEY}, indicates a multi-signature is present in the
   * {@link Transaction#signers()} field instead.
   *
   * @return A {@link PublicKey} containing the public key of the account submitting the transaction, or
   *   {@link PublicKey#MULTI_SIGN_PUBLIC_KEY} if the transaction is multi-signed.
   */
  @JsonProperty("SigningPubKey")
  public PublicKey signingPublicKey() {
    return signingPublicKey;
  }

  /**
   * The signature that verifies this transaction as originating from the account it says it is from.
   *
   * @return An {@link Optional} {@link String} containing the transaction signature.
   */
  @Override
  public Optional<Signature> transactionSignature() {
    return transactionSignature;
  }

  @Override
  public Optional<NetworkId> networkId() {
    return networkId;
  }

  /**
   * Unique hash for the ledger, as hexadecimal.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  @JsonProperty("hash")
  public Hash256 hash() {
    return hash;
  }

  /**
   * The index of the ledger that this transaction was included in.
   *
   * @return The {@link LedgerIndex} that this transaction was included in.
   */
  @JsonProperty("ledger_index")
  public LedgerIndex ledgerIndex() {
    return ledgerIndex;
  }

  /**
   * The approximate close time (using Ripple Epoch) of the ledger containing this transaction.
   * This is an undocumented field.
   *
   * @return An optionally-present {@link UnsignedLong}.
   */
  @JsonProperty("date")
  public Optional<UnsignedLong> closeDate() {
    return closeDate;
  }

  /**
   * The approximate close time in UTC offset.
   * This is derived from undocumented field.
   *
   * @return An optionally-present {@link ZonedDateTime}.
   */
  public Optional<ZonedDateTime> closeDateHuman() {
    return closeDate().map(TimeUtils::xrplTimeToZonedDateTime);
  }

  @JsonAnySetter
  private void putUnknown(String key, Object value) {
    unknowns.put(key, value);
  }

  /**
   * Map of all unknown and not mapped JSON nodes.
   */
  @JsonIgnore
  public Map<String, Object> unknowns() {
    return unknowns;
  }
}
