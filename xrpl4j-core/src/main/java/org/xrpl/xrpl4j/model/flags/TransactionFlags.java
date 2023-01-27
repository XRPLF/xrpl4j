package org.xrpl.xrpl4j.model.flags;

import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * A set of static {@link Flags} which could apply to any {@link Transaction}.
 */
public class TransactionFlags extends Flags {

  /**
   * Corresponds to the {@code tfFullyCanonicalSig} flag.
   */
  protected static final TransactionFlags FULLY_CANONICAL_SIG = new TransactionFlags(0x80000000L);

  TransactionFlags(long value) {
    super(value);
  }

  /**
   * Flags indicating that a fully-canonical signature is required. This flag is highly recommended.
   *
   * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
   * @see "https://xrpl.org/transaction-common-fields.html#flags-field"
   */
  public boolean tfFullyCanonicalSig() {
    return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
  }

  /**
   * A builder class for {@link TransactionFlags} flags.
   */
  public static class Builder {

    /**
     * Build a {@link TransactionFlags}.
     *
     * @return {@link TransactionFlags}.
     */
    public TransactionFlags build() {
      return new TransactionFlags(
        TransactionFlags.FULLY_CANONICAL_SIG.getValue()
      );
    }
  }
}
