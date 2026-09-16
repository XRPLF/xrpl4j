package org.xrpl.xrpl4j.model.client.vault;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * Request parameters for the {@code vault_info} rippled API method.
 *
 * <p>A Vault can be identified in two ways:</p>
 * <ul>
 *   <li>By vault ID using {@link #vaultId()}</li>
 *   <li>By owner and sequence using {@link #owner()} and {@link #seq()}</li>
 * </ul>
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVaultInfoRequestParams.class)
@JsonDeserialize(as = ImmutableVaultInfoRequestParams.class)
@Beta
public interface VaultInfoRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableVaultInfoRequestParams.Builder}.
   */
  static ImmutableVaultInfoRequestParams.Builder builder() {
    return ImmutableVaultInfoRequestParams.builder();
  }

  /**
   * Construct a new {@link VaultInfoRequestParams} for the given vault ID.
   *
   * @param vaultId The {@link Hash256} ID of the vault.
   *
   * @return A {@link VaultInfoRequestParams}.
   */
  static VaultInfoRequestParams of(Hash256 vaultId) {
    return ImmutableVaultInfoRequestParams.builder()
      .vaultId(vaultId)
      .build();
  }

  /**
   * Construct a new {@link VaultInfoRequestParams} for the given owner and sequence.
   *
   * @param owner The {@link Address} of the account that owns the vault.
   * @param seq   The sequence number used when creating the vault.
   *
   * @return A {@link VaultInfoRequestParams}.
   */
  static VaultInfoRequestParams of(Address owner, UnsignedInteger seq) {
    return ImmutableVaultInfoRequestParams.builder()
      .owner(owner)
      .seq(seq)
      .build();
  }

  /**
   * The unique 256-bit vault identifier. This field is mutually exclusive with {@link #owner()} and {@link #seq()}.
   *
   * @return An {@link Optional} {@link Hash256} containing the vault ID.
   */
  @JsonProperty("vault_id")
  Optional<Hash256> vaultId();

  /**
   * The account that owns the vault. This field must be specified together with {@link #seq()}, and is mutually
   * exclusive with {@link #vaultId()}.
   *
   * @return An {@link Optional} {@link Address} containing the vault owner.
   */
  Optional<Address> owner();

  /**
   * The sequence number used when creating the vault. This field must be specified together with {@link #owner()},
   * and is mutually exclusive with {@link #vaultId()}.
   *
   * @return An {@link Optional} {@link UnsignedInteger} containing the sequence number.
   */
  Optional<UnsignedInteger> seq();

  /**
   * Validates that either {@link #vaultId()} is present, or both {@link #owner()} and {@link #seq()} are present,
   * but not both identification methods at the same time.
   */
  @Value.Check
  default void check() {
    boolean hasVaultId = vaultId().isPresent();
    boolean hasOwnerAndSeq = owner().isPresent() && seq().isPresent();

    if (!hasVaultId && !hasOwnerAndSeq) {
      throw new IllegalArgumentException(
        "VaultInfoRequestParams must specify either vaultId, or both owner and seq"
      );
    }

    if (hasVaultId && (owner().isPresent() || seq().isPresent())) {
      throw new IllegalArgumentException(
        "VaultInfoRequestParams cannot specify both vaultId and owner/seq"
      );
    }

    if (owner().isPresent() != seq().isPresent()) {
      throw new IllegalArgumentException(
        "VaultInfoRequestParams must specify both owner and seq together"
      );
    }
  }

}
