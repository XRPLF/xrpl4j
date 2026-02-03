package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;

import java.util.Optional;

/**
 * Representation of the {@code MPTokenIssuanceSet} transaction.
 */
@Immutable
@JsonSerialize(as = ImmutableMpTokenIssuanceSet.class)
@JsonDeserialize(as = ImmutableMpTokenIssuanceSet.class)
public interface MpTokenIssuanceSet extends Transaction {

  /**
   * Construct a {@code MpTokenIssuanceSet} builder.
   *
   * @return An {@link ImmutableMpTokenIssuanceSet.Builder}.
   */
  static ImmutableMpTokenIssuanceSet.Builder builder() {
    return ImmutableMpTokenIssuanceSet.builder();
  }

  /**
   * A set of {@link MpTokenIssuanceSetFlags}.
   *
   * @return An {@link MpTokenIssuanceSetFlags}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default MpTokenIssuanceSetFlags flags() {
    return MpTokenIssuanceSetFlags.empty();
  }

  /**
   * The {@link MpTokenIssuanceId} of the issuance to update.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * An optional XRPL Address of an individual token holder balance to lock/unlock. If omitted, this transaction will
   * apply to all accounts holding MPTs.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Holder")
  Optional<Address> holder();

  /**
   * The 33-byte EC-ElGamal public key used for the issuer's mirror balances.
   *
   * <p>This key is used to encrypt confidential amounts that the issuer can decrypt to monitor
   * the total supply of confidential tokens.</p>
   *
   * @return An optionally-present {@link ElGamalPublicKey}.
   */
  @JsonProperty("IssuerElGamalPublicKey")
  Optional<ElGamalPublicKey> issuerElGamalPublicKey();

  /**
   * The 33-byte EC-ElGamal public key used for regulatory oversight (if applicable).
   *
   * <p>This key is used to encrypt confidential amounts that an auditor can decrypt for
   * compliance and regulatory purposes.</p>
   *
   * @return An optionally-present {@link ElGamalPublicKey}.
   */
  @JsonProperty("AuditorElGamalPublicKey")
  Optional<ElGamalPublicKey> auditorElGamalPublicKey();

}
