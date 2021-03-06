package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Maps the fields inside the "info" section of the "server_info" API call.
 */
@Immutable
@JsonSerialize(as = ImmutableServerInfo.class)
@JsonDeserialize(as = ImmutableServerInfo.class)
public interface ServerInfo {

  static ImmutableServerInfo.Builder builder() {
    return ImmutableServerInfo.builder();
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
   * Information on the most recently closed ledger that has not been validated by consensus. If the most
   * recently validated ledger is available, the response omits this field and includes {@link #validatedLedger()}
   * instead.
   *
   * @return An optionally-present {@link ServerInfoLedger} containing information about the server's view of the
   *     most recently closed ledger.
   */
  @JsonProperty("closed_ledger")
  Optional<ServerInfoLedger> closedLedger();

  /**
   * Range expression indicating the sequence numbers of the ledger versions the local rippled has in its database.
   * This may be a disjoint sequence such as {@code 24900901-24900984,24901116-24901158}.
   * If the server does not have any complete ledgers (for example, it recently started syncing with the network),
   * this will be an empty {@link String}.
   *
   * @return A {@link String} representing a range of ledger sequences.
   */
  @JsonProperty("complete_ledgers")
  String completeLedgers();

  /**
   * On an admin request, returns the hostname of the server running the rippled instance;
   * otherwise, returns a single RFC-1751 word based on the node public key.
   *
   * @return A {@link String} containing the host name of the serverk.
   */
  @JsonProperty("hostid")
  String hostId();

  /**
   * Amount of time spent waiting for I/O operations, in milliseconds. If this number is not very, very low,
   * then the rippled server is probably having serious load issues.
   *
   * @return An {@link UnsignedLong} representing the I/O latency.
   */
  @JsonProperty("io_latency_ms")
  UnsignedLong ioLatencyMs();

  /**
   * The number of times (since starting up) that this server has had over 250 transactions waiting to be processed
   * at once. A large number here may mean that your server is unable to handle the transaction load of the
   * XRP Ledger network. For detailed recommendations of future-proof server specifications, see
   * <a href="https://xrpl.org/capacity-planning.html">Capacity Planning</a>.
   *
   * @return A {@link String}.
   */
  @JsonProperty("jq_trans_overflow")
  String jqTransOverflow();

  /**
   * Information about the last time the server closed a ledger, including the amount of time it took to reach a
   * consensus and the number of trusted validators participating.
   *
   * @return A {@link ServerInfoLastClose}.
   */
  @JsonProperty("last_close")
  ServerInfoLastClose lastClose();

  /**
   * (Admin only) Detailed information about the current load state of the server.
   *
   * @return An optionally-present {@link ServerInfoLoad}.
   */
  @JsonProperty("load")
  Optional<ServerInfoLoad> load();

  /**
   * The load-scaled open ledger transaction cost the server is currently enforcing, as a multiplier on the
   * base transaction cost. For example, at 1000 load factor and a reference transaction cost of 10 drops of XRP,
   * the load-scaled transaction cost is 10,000 drops (0.01 XRP). The load factor is determined by the highest
   * of the individual server's load factor, the cluster's load factor, the open ledger cost and the overall
   * network's load factor.
   *
   * @return An {@link UnsignedInteger} representing the load factor.
   */
  @JsonProperty("load_factor")
  UnsignedInteger loadFactor();

  /**
   * Current multiplier to the transaction cost based on load to this server.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the local load factor.
   */
  @JsonProperty("load_factor_local")
  Optional<UnsignedInteger> loadFactorLocal();

  /**
   * Current multiplier to the transaction cost being used by the rest of the network
   * (estimated from other servers' reported load values).
   *
   * @return An optionally-present {@link UnsignedInteger} representing the network load factor.
   */
  @JsonProperty("load_factor_net")
  Optional<UnsignedInteger> loadFactorNet();

  /**
   * Current multiplier to the transaction cost based on load to servers in this cluster.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the cluster load factor.
   */
  @JsonProperty("load_factor_cluster")
  Optional<UnsignedInteger> loadFactorCluster();

  /**
   * The current multiplier to the transaction cost that a transaction must pay to get into the open ledger.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the open ledger load factor.
   */
  @JsonProperty("load_factor_fee_escalation")
  Optional<UnsignedInteger> loadFactorFeeEscalation();

  /**
   * The current multiplier to the transaction cost that a transaction must pay to get into the queue,
   * if the queue is full.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the queue load factor.
   */
  @JsonProperty("load_factor_fee_queue")
  Optional<UnsignedInteger> loadFactorFeeQueue();

  /**
   * The load factor the server is enforcing, not including the open ledger cost.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the server load factor.
   */
  @JsonProperty("load_factor_server")
  Optional<UnsignedInteger> loadFactorServer();

  /**
   * How many other rippled servers this one is currently connected to.
   *
   * @return An {@link UnsignedInteger} representing the number of peers of this server.
   */
  UnsignedInteger peers();

  /**
   * Public key used to verify this server for peer-to-peer communications. This node key pair is automatically
   * generated by the server the first time it starts up. (If deleted, the server can create a new pair of keys.)
   * You can set a persistent value in the config file using the {@code [node_seed]} config option, which is useful
   * for clustering.
   *
   * @return A {@link String} containing the node's public key.
   */
  @JsonProperty("pubkey_node")
  String publicKeyNode();

  /**
   * (Admin only) Public key used by this node to sign ledger validations. This validation key pair is derived
   * from the {@code [validator_token]} or {@code [validation_seed]} config field.
   *
   * @return A {@link String} containing the validator's public key.
   */
  @JsonProperty("pubkey_validator")
  Optional<String> publicKeyValidator();

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
  @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss.SSSSSS z")
  ZonedDateTime time();

  /**
   * Number of consecutive seconds that the server has been operational.
   *
   * @return An {@link UnsignedLong} representing the server uptime, in seconds.
   */
  @JsonProperty("uptime")
  UnsignedLong upTime();

  /**
   * Information about the most recent fully-validated ledger. If the most recent validated ledger is not available,
   * the response omits this field and includes {@link #closedLedger()} instead.
   *
   * @return An optionally-present {@link ServerInfoLedger} representing the latest validated ledger.
   */
  @JsonProperty("validated_ledger")
  Optional<ServerInfoLedger> validatedLedger();

  /**
   * Minimum number of trusted validations required to validate a ledger version. Some circumstances may cause
   * the server to require more validations.
   *
   * @return An {@link UnsignedInteger} representing the quorum.
   */
  @JsonProperty("validation_quorum")
  UnsignedInteger validationQuorum();

  /**
   * (Admin only) Either the human readable time, in UTC, when the current validator list will expire,
   * the string {@code "unknown"} if the server has yet to load a published validator list or the string
   * {@code "never"} if the server uses a static validator list.
   *
   * @return An optionally-present {@link String} containing the validator expiration list.
   */
  @JsonProperty("validator_list_expires")
  Optional<String> validatorListExpires();

}
