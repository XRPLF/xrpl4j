package org.xrpl.xrpl4j.model.flags;

import com.google.common.annotations.Beta;
import org.xrpl.xrpl4j.model.transactions.VaultDeposit;

/**
 * A set of static {@link TransactionFlags} which can be set on {@link VaultDeposit} transactions.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
public class VaultDepositFlags extends TransactionFlags {

  /**
   * Constant {@link VaultDepositFlags} for the {@code tfVaultDonate} flag.
   */
  public static final VaultDepositFlags VAULT_DONATE = new VaultDepositFlags(0x00010000L);

  private VaultDepositFlags(long value) {
    super(value);
  }

  private VaultDepositFlags() {
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Construct {@link VaultDepositFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link VaultDepositFlags}.
   *
   * @return New {@link VaultDepositFlags}.
   */
  public static VaultDepositFlags of(long value) {
    return new VaultDepositFlags(value);
  }

  private static VaultDepositFlags of(boolean tfFullyCanonicalSig, boolean tfVaultDonate) {
    long value = Flags.of(
      tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfVaultDonate ? VAULT_DONATE : UNSET
    ).getValue();
    return new VaultDepositFlags(value);
  }

  /**
   * Construct an empty instance of {@link VaultDepositFlags}. Transactions with empty flags will not be serialized
   * with a {@code Flags} field.
   *
   * @return An empty {@link VaultDepositFlags}.
   */
  public static VaultDepositFlags empty() {
    return new VaultDepositFlags();
  }

  /**
   * If enabled, the deposited assets are donated to the vault, meaning the depositor receives no shares in return.
   * Only the vault owner may donate to a vault.
   *
   * @return {@code true} if {@code tfVaultDonate} is set, otherwise {@code false}.
   */
  public boolean tfVaultDonate() {
    return this.isSet(VaultDepositFlags.VAULT_DONATE);
  }

  /**
   * A builder class for {@link VaultDepositFlags} flags.
   */
  public static class Builder {

    private boolean tfVaultDonate = false;

    /**
     * Set {@code tfVaultDonate} to the given value.
     *
     * @param tfVaultDonate A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfVaultDonate(boolean tfVaultDonate) {
      this.tfVaultDonate = tfVaultDonate;
      return this;
    }

    /**
     * Build a new {@link VaultDepositFlags} from the current boolean values.
     *
     * @return A new {@link VaultDepositFlags}.
     */
    public VaultDepositFlags build() {
      return VaultDepositFlags.of(true, tfVaultDonate);
    }
  }
}
