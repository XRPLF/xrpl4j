package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.ledger.Issue;

/**
 * Represents a cross-chain bridge.
 *
 * <p>This interface will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableXChainBridge.class)
@JsonDeserialize(as = ImmutableXChainBridge.class)
public interface XChainBridge {

  /**
   * Construct a {@code XChainBridge} builder.
   *
   * @return An {@link ImmutableXChainBridge.Builder}.
   */
  static ImmutableXChainBridge.Builder builder() {
    return ImmutableXChainBridge.builder();
  }

  /**
   * The door account on the issuing chain. For an XRP-XRP bridge, this must be the genesis account (the account that is
   * created when the network is first started, which contains all of the XRP).
   *
   * @return The {@link Address} of the door account.
   */
  @JsonProperty("IssuingChainDoor")
  Address issuingChainDoor();

  /**
   * The asset that is minted and burned on the issuing chain. For an IOU-IOU bridge, the issuer of the asset must be
   * the door account on the issuing chain, to avoid supply issues.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("IssuingChainIssue")
  Issue issuingChainIssue();

  /**
   * The door account on the locking chain.
   *
   * @return The {@link Address} of the door account.
   */
  @JsonProperty("LockingChainDoor")
  Address lockingChainDoor();

  /**
   * The asset that is locked and unlocked on the locking chain.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("LockingChainIssue")
  Issue lockingChainIssue();

}
