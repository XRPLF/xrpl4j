package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.ImmutableDidObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.DidData;
import org.xrpl.xrpl4j.model.transactions.DidDocument;
import org.xrpl.xrpl4j.model.transactions.DidUri;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * A {@code DID} ledger entry holds references to, or data associated with a single DID.
 *
 * <p>This interface will be marked {@link Beta} until the featureDID amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableMetaDidObject.class)
@JsonDeserialize(as = ImmutableMetaDidObject.class)
public interface MetaDidObject extends MetaLedgerObject {

  /**
   * A bit-map of boolean flags. No flags are defined for {@link MetaDidObject}, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The account that controls the DID.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Optional<Address> account();

  /**
   * The W3C standard DID document associated with the DID. This field isn't checked for validity and is limited to a
   * maximum length of 256 bytes.
   *
   * @return An optionally-present {@link DidDocument}.
   */
  @JsonProperty("DIDDocument")
  Optional<DidDocument> didDocument();

  /**
   * The public attestations of identity credentials associated with the DID. This field isn't checked for validity and
   * is limited to a maximum length of 256 bytes.
   *
   * @return An optionally-present {@link DidData}.
   */
  @JsonProperty("Data")
  Optional<DidData> data();

  /**
   * The Universal Resource Identifier that points to the corresponding DID document or the data associated with the
   * DID. This field can be an HTTP(S) URL or IPFS URI. This field isn't checked for validity and is limited to a
   * maximum length of 256 bytes.
   *
   * @return An optionally-present {@link DidUri}.
   */
  @JsonProperty("URI")
  Optional<DidUri> uri();

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
