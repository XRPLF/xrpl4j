package org.xrpl.xrpl4j.model.flags;

import org.xrpl.xrpl4j.model.ledger.SignerListObject;

/**
 * A set of static {@link Flags} which can be set on {@link SignerListObject}s.
 */
public class SignerListFlags extends Flags {

  /**
   * Constant for an unset flag.
   */
  public static final SignerListFlags UNSET = new SignerListFlags(0);

  /**
   * Constant {@link SignerListFlags} for the {@code lsfOneOwner} flag.
   */
  public static final SignerListFlags ONE_OWNER_COUNT = new SignerListFlags(0x00010000);

  private SignerListFlags(long value) {
    super(value);
  }

  /**
   * Construct {@link SignerListFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link SignerListFlags}.
   *
   * @return New {@link SignerListFlags}.
   */
  public static SignerListFlags of(long value) {
    return new SignerListFlags(value);
  }

  /**
   * If this flag is enabled, this SignerList counts as one item for purposes of the owner reserve.
   * Otherwise, this list counts as N+2 items, where N is the number of signers it contains. This flag is
   * automatically enabled if you add or update a signer list after the MultiSignReserve amendment is enabled.
   *
   * @return {@code true} if {@code lsfOneOwnerCount} is set, otherwise {@code false}.
   */
  public boolean lsfOneOwnerCount() {
    return this.isSet(SignerListFlags.ONE_OWNER_COUNT);
  }
}
