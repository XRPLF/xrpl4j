package org.xrpl.xrpl4j.model.flags;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.model.transactions.VaultSet;

/**
 * A set of static {@link TransactionFlags} which can be set on {@link VaultSet} transactions.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/pull/469"
 */
@Beta
public class VaultSetFlags extends TransactionFlags {

  /**
   * Constant {@link VaultSetFlags} for the {@code tfVaultDepositBlock} flag.
   */
  public static final VaultSetFlags VAULT_DEPOSIT_BLOCK = new VaultSetFlags(0x00010000L);

  /**
   * Constant {@link VaultSetFlags} for the {@code tfVaultDepositUnblock} flag.
   */
  public static final VaultSetFlags VAULT_DEPOSIT_UNBLOCK = new VaultSetFlags(0x00020000L);

  /**
   * Constant {@link VaultSetFlags} for the {@code tfInnerBatchTxn} flag.
   *
   * @see "https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0056-batch"
   */
  public static final VaultSetFlags INNER_BATCH_TXN =
    new VaultSetFlags(TransactionFlags.INNER_BATCH_TXN.getValue());

  private VaultSetFlags(long value) {
    super(value);
  }

  private VaultSetFlags() {
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
   * Construct {@link VaultSetFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link VaultSetFlags}.
   *
   * @return New {@link VaultSetFlags}.
   */
  public static VaultSetFlags of(long value) {
    return new VaultSetFlags(value);
  }

  private static VaultSetFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfVaultDepositBlock,
    boolean tfVaultDepositUnblock,
    boolean tfInnerBatchTxn
  ) {
    Preconditions.checkState(
      !(tfVaultDepositBlock && tfVaultDepositUnblock),
      "tfVaultDepositBlock and tfVaultDepositUnblock must not both be set"
    );

    long value = Flags.of(
      tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfVaultDepositBlock ? VAULT_DEPOSIT_BLOCK : UNSET,
      tfVaultDepositUnblock ? VAULT_DEPOSIT_UNBLOCK : UNSET,
      tfInnerBatchTxn ? TransactionFlags.INNER_BATCH_TXN : UNSET
    ).getValue();
    return new VaultSetFlags(value);
  }

  /**
   * Construct an empty instance of {@link VaultSetFlags}. Transactions with empty flags will not be serialized with a
   * {@code Flags} field.
   *
   * @return An empty {@link VaultSetFlags}.
   */
  public static VaultSetFlags empty() {
    return new VaultSetFlags();
  }

  /**
   * If enabled, deposits into the vault are blocked. This flag may only be used if the vault has the
   * {@code lsfVaultOwnerCanBlockDeposit} flag set, and may not be combined with {@code tfVaultDepositUnblock}.
   *
   * @return {@code true} if {@code tfVaultDepositBlock} is set, otherwise {@code false}.
   */
  public boolean tfVaultDepositBlock() {
    return this.isSet(VaultSetFlags.VAULT_DEPOSIT_BLOCK);
  }

  /**
   * If enabled, deposits into the vault are unblocked. This flag may only be used if the vault has the
   * {@code lsfVaultOwnerCanBlockDeposit} flag set, and may not be combined with {@code tfVaultDepositBlock}.
   *
   * @return {@code true} if {@code tfVaultDepositUnblock} is set, otherwise {@code false}.
   */
  public boolean tfVaultDepositUnblock() {
    return this.isSet(VaultSetFlags.VAULT_DEPOSIT_UNBLOCK);
  }

  /**
   * Whether the {@code tfInnerBatchTxn} flag is set.
   *
   * @return {@code true} if {@code tfInnerBatchTxn} is set, otherwise {@code false}.
   */
  @Override
  public boolean tfInnerBatchTxn() {
    return this.isSet(VaultSetFlags.INNER_BATCH_TXN);
  }

  /**
   * A builder class for {@link VaultSetFlags} flags.
   */
  public static class Builder {

    private boolean tfVaultDepositBlock = false;
    private boolean tfVaultDepositUnblock = false;
    private boolean tfInnerBatchTxn = false;

    /**
     * Set {@code tfVaultDepositBlock} to the given value.
     *
     * @param tfVaultDepositBlock A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfVaultDepositBlock(boolean tfVaultDepositBlock) {
      this.tfVaultDepositBlock = tfVaultDepositBlock;
      return this;
    }

    /**
     * Set {@code tfVaultDepositUnblock} to the given value.
     *
     * @param tfVaultDepositUnblock A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfVaultDepositUnblock(boolean tfVaultDepositUnblock) {
      this.tfVaultDepositUnblock = tfVaultDepositUnblock;
      return this;
    }

    /**
     * Set {@code tfInnerBatchTxn} to the given value.
     *
     * @param tfInnerBatchTxn A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfInnerBatchTxn(boolean tfInnerBatchTxn) {
      this.tfInnerBatchTxn = tfInnerBatchTxn;
      return this;
    }

    /**
     * Build a new {@link VaultSetFlags} from the current boolean values.
     *
     * @return A new {@link VaultSetFlags}.
     */
    public VaultSetFlags build() {
      return VaultSetFlags.of(
        true,
        tfVaultDepositBlock,
        tfVaultDepositUnblock,
        tfInnerBatchTxn
      );
    }
  }
}
