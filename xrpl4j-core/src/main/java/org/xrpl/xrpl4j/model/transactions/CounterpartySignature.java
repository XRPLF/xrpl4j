package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

/**
 * Represents a counterparty's signature for dual-signed transactions such as {@link LoanSet}.
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
   * A list of {@link SignerWrapper}s for multi-signed counterparty authorization.
   *
   * @return A {@link List} of {@link SignerWrapper}s.
   */
  @JsonProperty("Signers")
  List<SignerWrapper> signers();

}
