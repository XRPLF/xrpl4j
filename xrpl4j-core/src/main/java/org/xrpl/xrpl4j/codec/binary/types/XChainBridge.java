package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;

/**
 * JSON mapping object for the XChainBridge serialized type.
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
   * @return The address of the door account as a {@link JsonNode}.
   */
  @JsonProperty("IssuingChainDoor")
  JsonNode issuingChainDoor();

  /**
   * The asset that is minted and burned on the issuing chain. For an IOU-IOU bridge, the issuer of the asset must be
   * the door account on the issuing chain, to avoid supply issues.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("IssuingChainIssue")
  JsonNode issuingChainIssue();

  /**
   * The door account on the locking chain.
   *
   * @return The address of the door account as a {@link JsonNode}.
   */
  @JsonProperty("LockingChainDoor")
  JsonNode lockingChainDoor();

  /**
   * The asset that is locked and unlocked on the locking chain.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("LockingChainIssue")
  JsonNode lockingChainIssue();


}
