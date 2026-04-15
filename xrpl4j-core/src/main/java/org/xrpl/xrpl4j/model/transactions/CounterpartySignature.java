package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a counterparty's signature for dual-signed transactions such as {@link LoanSet}.
 *
 * <p>A {@code CounterpartySignature} must contain exactly one of:</p>
 * <ul>
 *   <li>Both {@code signingPubKey} and {@code txnSignature} (for single-signing), or</li>
 *   <li>A non-empty {@code signers} list (for multi-signing).</li>
 * </ul>
 *
 * <p>When multi-signing, signers are automatically sorted by account address during construction.</p>
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCounterpartySignature.class)
@JsonDeserialize(as = ImmutableCounterpartySignature.class)
@Beta
public interface CounterpartySignature {

  /**
   * Construct a {@code CounterpartySignature} builder.
   *
   * @return An {@link ImmutableCounterpartySignature.Builder}.
   */
  static ImmutableCounterpartySignature.Builder builder() {
    return ImmutableCounterpartySignature.builder();
  }

  /**
   * Construct a {@code CounterpartySignature} for single-signing.
   *
   * @param signingPublicKey The counterparty's {@link PublicKey}.
   * @param signature        The counterparty's {@link Signature}.
   *
   * @return A {@link CounterpartySignature}.
   */
  static CounterpartySignature of(PublicKey signingPublicKey, Signature signature) {
    return builder()
      .signingPubKey(signingPublicKey.base16Value())
      .txnSignature(signature.base16Value())
      .build();
  }

  /**
   * Construct a {@code CounterpartySignature} for multi-signing from a set of {@link Signer}s. Signers are
   * automatically sorted by account address.
   *
   * @param signers A {@link Set} of {@link Signer}s.
   *
   * @return A {@link CounterpartySignature}.
   */
  static CounterpartySignature of(Set<Signer> signers) {
    return builder()
      .signers(signers.stream().map(SignerWrapper::of).collect(Collectors.toList()))
      .build();
  }

  /**
   * The public key used by the counterparty to sign the transaction.
   *
   * @return An optionally-present {@link String}.
   */
  @JsonProperty("SigningPubKey")
  Optional<String> signingPubKey();

  /**
   * The counterparty's transaction signature.
   *
   * @return An optionally-present {@link String}.
   */
  @JsonProperty("TxnSignature")
  Optional<String> txnSignature();

  /**
   * Whether the signers list has already been sorted. This is an internal flag used to prevent re-sorting during
   * object construction and is not serialized to JSON.
   *
   * @return {@code true} if signers have been sorted.
   */
  @JsonIgnore
  @Default
  default boolean sortedSigners() {
    return false;
  }

  /**
   * A list of {@link SignerWrapper}s for multi-signed counterparty authorization (sorted by account address).
   *
   * <p>When building a {@code CounterpartySignature}, you can provide signers in any order, and they will be
   * automatically sorted by account address during construction via {@link #checkAndNormalize()}.</p>
   *
   * @return A {@link List} of {@link SignerWrapper}s.
   */
  @Default
  @JsonProperty("Signers")
  default List<SignerWrapper> signers() {
    return Lists.newArrayList();
  }

  /**
   * Validates that the {@code CounterpartySignature} has either direct signing fields or multi-sig signers (but not
   * both), and normalizes the signer order by account address if multi-signing.
   *
   * @return A normalized {@link CounterpartySignature}.
   */
  @Value.Check
  default CounterpartySignature checkAndNormalize() {
    // Reject half-filled direct-signing (e.g. signingPubKey without txnSignature or vice versa)
    Preconditions.checkState(
      signingPubKey().isPresent() == txnSignature().isPresent(),
      "CounterpartySignature must have both SigningPubKey and TxnSignature, or neither"
    );

    boolean hasDirectSigning = signingPubKey().isPresent() && txnSignature().isPresent();
    boolean hasMultiSig = !signers().isEmpty();

    Preconditions.checkState(
      hasDirectSigning || hasMultiSig,
      "CounterpartySignature must have either (SigningPubKey and TxnSignature) or non-empty Signers array"
    );

    Preconditions.checkState(
      !(hasDirectSigning && hasMultiSig),
      "CounterpartySignature cannot have both direct signing fields and Signers array"
    );

    if (hasMultiSig && !sortedSigners()) {
      // Normalize the order of the signers by account address (required by XRPL)
      return ImmutableCounterpartySignature.builder()
        .from(this)
        .signers(signers().stream()
          .sorted(Comparator.comparing(signature -> new BigInteger(
            AddressCodec.getInstance().decodeAccountId(signature.signer().account()).hexValue(), 16
          )))
          .collect(Collectors.toList())
        )
        .sortedSigners(true)
        .build();
    }
    return this;
  }

}
