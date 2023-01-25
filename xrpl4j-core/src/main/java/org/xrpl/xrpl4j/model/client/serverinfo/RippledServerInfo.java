package org.xrpl.xrpl4j.model.client.serverinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * An implementation of {@link ServerInfo} that conforms to rippled server payloads.
 */
@Beta
@Value.Immutable
@JsonSerialize(as = ImmutableRippledServerInfo.class)
@JsonDeserialize(as = ImmutableRippledServerInfo.class)
public interface RippledServerInfo extends ServerInfo {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableRippledServerInfo.Builder}.
   */
  static ImmutableRippledServerInfo.Builder builder() {
    return ImmutableRippledServerInfo.builder();
  }

  /**
   * If {@code true}, this server is amendment blocked.
   *
   * @return {@code true} if this server is amendment blocked, otherwise {@code false}.
   */
  @JsonProperty("amendment_blocked")
  @Value.Default
  default boolean amendmentBlocked() {
    return false;
  }

  /**
   * The version number of the running rippled version.
   *
   * @return A {@link String} containing the version number.
   */
  @JsonProperty("build_version")
  String buildVersion();

  /**
   * On an admin request, returns the hostname of the server running the rippled instance; otherwise, returns a single
   * RFC-1751 word based on the node public key.
   *
   * @return A {@link String} containing the host name of the server.
   */
  @JsonProperty("hostid")
  String hostId();

  /**
   * Amount of time spent waiting for I/O operations, in milliseconds. If this number is not very, very low, then the
   * rippled server is probably having serious load issues.
   *
   * @return An {@link UnsignedLong} representing the I/O latency.
   */
  @JsonProperty("io_latency_ms")
  UnsignedLong ioLatencyMs();

  /**
   * The number of times (since starting up) that this server has had over 250 transactions waiting to be processed at
   * once. A large number here may mean that your server is unable to handle the transaction load of the XRP Ledger
   * network. For detailed recommendations of future-proof server specifications, see
   * <a href="https://xrpl.org/capacity-planning.html">Capacity Planning</a>.
   *
   * @return An optionally-present {@link String}.
   */
  @JsonProperty("jq_trans_overflow")
  Optional<String> jqTransOverflow();

  /**
   * Information about the last time the server closed a ledger, including the amount of time it took to reach a
   * consensus and the number of trusted validators participating.
   *
   * @return A {@link LastClose}.
   */
  @JsonProperty("last_close")
  LastClose lastClose();

  /**
   * (Admin only) Detailed information about the current load state of the server.
   *
   * @return An optionally-present {@link Load}.
   */
  @JsonProperty("load")
  Optional<Load> load();

  /**
   * Current multiplier to the transaction cost based on load to this server.  Per xrpl.org docs, this field is
   * optionally present in any server response and may be omitted.
   *
   * @return An optionally-present {@link BigDecimal} representing the local load factor.
   */
  @JsonProperty("load_factor_local")
  Optional<BigDecimal> loadFactorLocal();

  /**
   * Current multiplier to the transaction cost being used by the rest of the network (estimated from other servers'
   * reported load values). Per xrpl.org docs, this field is optionally present in any server response and may be
   * omitted.
   *
   * @return An optionally-present {@link BigDecimal} representing the network load factor.
   */
  @JsonProperty("load_factor_net")
  Optional<BigDecimal> loadFactorNet();

  /**
   * Current multiplier to the transaction cost based on load to servers in this cluster. Per xrpl.org docs, this field
   * is optionally present in any server response and may be omitted.
   *
   * @return An optionally-present {@link BigDecimal} representing the cluster load factor.
   */
  @JsonProperty("load_factor_cluster")
  Optional<BigDecimal> loadFactorCluster();

  /**
   * The current multiplier to the transaction cost that a transaction must pay to get into the open ledger. Per
   * xrpl.org docs, this field is optionally present in any server response and may be omitted.
   *
   * @return An optionally-present {@link BigDecimal} representing the open ledger load factor.
   */
  @JsonProperty("load_factor_fee_escalation")
  Optional<BigDecimal> loadFactorFeeEscalation();

  /**
   * The current multiplier to the transaction cost that a transaction must pay to get into the queue, if the queue is
   * full. Per xrpl.org docs, this field is optionally present in any server response and may be omitted.
   *
   * @return An optionally-present {@link BigDecimal} representing the queue load factor.
   */
  @JsonProperty("load_factor_fee_queue")
  Optional<BigDecimal> loadFactorFeeQueue();

  /**
   * The load factor the server is enforcing, not including the open ledger cost. Per xrpl.org docs, this field is
   * optionally present in any server response and may be omitted.
   *
   * @return An optionally-present {@link BigDecimal} representing the server load factor.
   */
  @JsonProperty("load_factor_server")
  Optional<BigDecimal> loadFactorServer();

  /**
   * How many other rippled servers this one is currently connected to.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the number of peers of this server.
   */
  @JsonProperty("peers")
  Optional<UnsignedInteger> peers();

  /**
   * Severed peer connection counter.
   *
   * @return A {@link String} representing the number of severed peer connections.
   */
  @JsonProperty("peer_disconnects")
  Optional<String> peerDisconnects();

  /**
   * Severed peer connections because of excess resource consumption.
   *
   * @return A {@link String} representing the number of severed peer connections due to excess resource consumption.
   */
  @JsonProperty("peer_disconnects_resources")
  Optional<String> peerDisconnectsResources();

  /**
   * Public key used to verify this server for peer-to-peer communications. This node key pair is automatically
   * generated by the server the first time it starts up. (If deleted, the server can create a new pair of keys.) You
   * can set a persistent value in the config file using the {@code [node_seed]} config option, which is useful for
   * clustering.
   *
   * @return A {@link String} containing the node's public key.
   */
  @JsonProperty("pubkey_node")
  String publicKeyNode();

  /**
   * A string indicating to what extent the server is participating in the network. See
   * <a href="https://xrpl.org/rippled-server-states.html">Possible Server States</a> for more details.
   *
   * @return A {@link String} containing the server state.
   */
  @JsonProperty("server_state")
  String serverState();

  /**
   * The number of consecutive microseconds the server has been in the current state.
   *
   * @return A {@link String} containing the number of seconds the server has been in the current state.
   */
  @JsonProperty("server_state_duration_us")
  String serverStateDurationUs();

  /**
   * The current time in UTC, according to the server's clock.
   *
   * @return A {@link ZonedDateTime} denoting the server clock time.
   */
  @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss.SSSSSS z", locale = "en_US")
  ZonedDateTime time();

  /**
   * Number of consecutive seconds that the server has been operational.
   *
   * @return An {@link UnsignedLong} representing the server uptime, in seconds.
   */
  @JsonProperty("uptime")
  UnsignedLong upTime();

  /**
   * Information on the most recently closed ledger that has not been validated by consensus. If the most recently
   * validated ledger is available, the response omits this field and includes {@link #validatedLedger()} instead.  Per
   * xrpl.org docs, this field is optionally present in any server response and may be omitted.
   *
   * @return An optionally-present {@link ServerInfo.ValidatedLedger} containing
   *   information about the server's view of the most recently closed ledger.
   */
  @JsonProperty("closed_ledger")
  Optional<ValidatedLedger> closedLedger();

  /**
   * (Admin only) Public key used by this node to sign ledger validations. This validation key pair is derived from the
   * {@code [validator_token]} or {@code [validation_seed]} config field. Per xrpl.org docs, this field is optionally
   * present in any server response and may be omitted because it is only returned when requested via an admin
   * host/port.
   *
   * @return A {@link String} containing the validator's public key.
   */
  @JsonProperty("pubkey_validator")
  Optional<String> publicKeyValidator();

  /**
   * (Admin only) Either the human-readable time, in UTC, when the current validator list will expire, the string
   * {@code "unknown"} if the server has yet to load a published validator list or the string {@code "never"} if the
   * server uses a static validator list. Per xrpl.org docs, this field is optionally present in any server response and
   * may be omitted because it is only returned when requested via an admin host/port.
   *
   * @return An optionally-present {@link String} containing the validator expiration list.
   */
  @JsonProperty("validator_list_expires")
  Optional<String> validatorListExpires();

}
