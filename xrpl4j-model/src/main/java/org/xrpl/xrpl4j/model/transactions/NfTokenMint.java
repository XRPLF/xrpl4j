package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;

import java.util.Optional;

/**
 * The {@link NfTokenMint} transaction creates an NfToken object and adds it to the
 * relevant NfTokenPage object of the minter(issuer). If the transaction is
 * successful, the newly minted token will be owned by the minter(issuer) account
 * specified by the transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenMint.class)
@JsonDeserialize(as = ImmutableNfTokenMint.class)
public interface NfTokenMint extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNfTokenMint.Builder}.
   */
  static ImmutableNfTokenMint.Builder builder() {
    return ImmutableNfTokenMint.builder();
  }

  /**
   * Indicates the taxon associated with this token. The taxon is generally a
   * value chosen by the minter of the token and a given taxon may be used for
   * multiple tokens. The implementation reserves taxon identifiers greater
   * than or equal to 2147483648 (0x80000000).
   *
   * @return Taxon of type {@link int} associated with the Token.
   */
  @JsonProperty("TokenTaxon")
  UnsignedLong tokenTaxon();

  /**
   * Indicates the account that should be the issuer of this token. This value
   * is optional and should only be specified if the account executing the
   * transaction is not the Issuer of the `NfToken` object. If it is
   * present, the MintAccount field in the AccountRoot of the `Issuer`
   * field must match the `Account`, otherwise the transaction will fail.
   *
   * @return An {@link Optional} field Issuer of type {@link Address}.
   */
  @JsonProperty("Issuer")
  Optional<Address> issuer();

  /**
   * Specifies the fee charged by the issuer for secondary sales of the Token,
   * if such sales are allowed. Valid values for this field are between 0 and
   * 50000 inclusive, allowing transfer rates between 0.000% and 50.000% in
   * increments of 0.001%. This field must NOT be present if the
   * `tfTransferable` flag is not set.
   *
   * @return An {@link Optional} field TransferFee of type {@link CurrencyAmount}.
   */
  @JsonProperty("TransferFee")
  Optional<TransferFee> transferFee();

  /**
   * URI that points to the data and/or metadata associated with the NfT.
   * This field need not be an HTTP or HTTPS URL; it could be an IPFS URI, a
   * magnet link, immediate data encoded as an RFC2379 "data" URL, or even an
   * opaque issuer-specific encoding. The URI is NOT checked for validity, but
   * the field is limited to a maximum length of 256 bytes.
   *
   * @return An {@link Optional} field URI of type {@link String}.
   */
  @JsonProperty("URI")
  Optional<Uri> uri();

  /**
   * Set of {@link Flags.NfTokenMintFlags}s for this {@link NfTokenMint}.
   *
   * @return The {@link org.xrpl.xrpl4j.model.flags.Flags.NfTokenMintFlags} for this transaction.
   */
  @JsonProperty("Flags")
  @Value.Default
  default Flags.NfTokenMintFlags flags() {
    return Flags.NfTokenMintFlags.builder().tfFullyCanonicalSig(true).build();
  }
  
  /**
   * For secondary sale/brokered mode, tfTransferable flag must be set.
   */
  @Value.Check
  default void checkIfFlagIsSet() {
    if (transferFee().isPresent()) {
      Preconditions.checkArgument(flags().tfTransferable(),
        "tfTransferable flag must be set for secondary sale.");
    }
  }
}