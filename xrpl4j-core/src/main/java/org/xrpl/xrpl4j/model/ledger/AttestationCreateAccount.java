package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XChainCount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * The attestation message that witness servers sign to include in a
 * {@link org.xrpl.xrpl4j.model.transactions.XChainAddAccountCreateAttestation}.
 *
 * <p>This interface will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableAttestationCreateAccount.class)
@JsonDeserialize(as = ImmutableAttestationCreateAccount.class)
public interface AttestationCreateAccount extends Attestation {

  /**
   * Construct a {@code AttestationCreateAccount} builder.
   *
   * @return An {@link ImmutableAttestationCreateAccount.Builder}.
   */
  static ImmutableAttestationCreateAccount.Builder builder() {
    return ImmutableAttestationCreateAccount.builder();
  }

  /**
   * The number of accounts that have been created by the bridge.
   *
   * @return An {@link XChainCount}.
   */
  @JsonProperty("XChainAccountCreateCount")
  XChainCount xChainAccountCreateCount();

  /**
   * The destination account for the funds on the destination chain.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * The total amount, in XRP, to be rewarded for providing a signature for cross-chain transfer or for signing for the
   * cross-chain reward. This amount will be split among the signers.
   *
   * @return An {@link XrpCurrencyAmount}.
   */
  @JsonProperty("SignatureReward")
  XrpCurrencyAmount signatureReward();

}
