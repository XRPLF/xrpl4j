package org.xrpl.xrpl4j.model.flags;

import com.google.common.annotations.Beta;

/**
 * A set of static {@link Flags} which can be set on {@link org.xrpl.xrpl4j.model.ledger.VaultObject}s.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
public class VaultFlags extends Flags {

  /**
   * Constant for an unset flag.
   */
  public static final VaultFlags UNSET = new VaultFlags(0);

  /**
   * Constant {@link VaultFlags} for the {@code lsfVaultPrivate} flag.
   */
  public static final VaultFlags VAULT_PRIVATE = new VaultFlags(0x00010000);

  /**
   * Required-args Constructor.
   *
   * @param value The long-number encoded flags value of this {@link VaultFlags}.
   */
  private VaultFlags(final long value) {
    super(value);
  }

  /**
   * Construct {@link VaultFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link VaultFlags}.
   *
   * @return New {@link VaultFlags}.
   */
  public static VaultFlags of(long value) {
    return new VaultFlags(value);
  }

  /**
   * If set, indicates the vault is private and requires credentials.
   *
   * @return {@code true} if {@code lsfVaultPrivate} is set, otherwise {@code false}.
   */
  public boolean lsfVaultPrivate() {
    return this.isSet(VaultFlags.VAULT_PRIVATE);
  }

}
