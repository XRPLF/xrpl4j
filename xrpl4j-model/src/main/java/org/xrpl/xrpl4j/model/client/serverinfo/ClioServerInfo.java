package org.xrpl.xrpl4j.model.client.serverinfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Range;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * An implementation of {@link ServerInfo} that conforms to Clio server payloads.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableClioServerInfo.class)
@JsonDeserialize(as = ImmutableClioServerInfo.class)
public interface ClioServerInfo extends ServerInfo {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableClioServerInfo.Builder}.
   */
  static ImmutableClioServerInfo.Builder builder() {
    return ImmutableClioServerInfo.builder();
  }

  /**
   * The version number of the running clio version.
   *
   * @return A {@link String} containing the version number.
   */
  @JsonProperty("clio_version")
  String clioVersion();

  /**
   * The version number of the running rippled version.
   *
   * @return A {@link String} containing the version number.
   */
  @JsonProperty("rippled_version")
  String rippledVersion();

  /**
   * Range expression indicating the sequence numbers of the ledger versions the local rippled has in its database. This
   * may be a disjoint sequence such as {@code 24900901-24900984,24901116-24901158}. If the server does not have any
   * complete ledgers (for example, it recently started syncing with the network), this will be an empty
   * {@link String}.
   *
   * @return A {@link String} representing a range of ledger sequences.
   */
  @JsonProperty("complete_ledgers")
  String completeLedgers();

  /**
   * Accessor for a range of ledgers.
   *
   * @return A {@link List} of type {@link Range} of type {@link UnsignedLong}.
   */
  @Value.Derived
  @JsonIgnore
  default List<Range<UnsignedLong>> completeLedgerRanges() {
    return LedgerRangeUtils.completeLedgerRanges(completeLedgers());
  }

  /**
   * Determines if the supplied {@code ledgerIndex} exists on the rippled server by inspecting
   * {@link #completeLedgers()}.
   *
   * @param ledgerIndex An {@link UnsignedLong} representing a particular ledger index.
   *
   * @return {@code true} if the rippled server includes {@code ledgerIndex} in its local database; {@code false}
   *   otherwise.
   */
  @Value.Derived
  @JsonIgnore
  default boolean isLedgerInCompleteLedgers(final UnsignedLong ledgerIndex) {
    return this.completeLedgerRanges().stream()
      .anyMatch(range -> range.contains(ledgerIndex));
  }

  /**
   * The load-scaled open ledger transaction cost the server is currently enforcing, as a multiplier on the base
   * transaction cost. For example, at 1000 load factor and a reference transaction cost of 10 drops of XRP, the
   * load-scaled transaction cost is 10,000 drops (0.01 XRP). The load factor is determined by the highest of the
   * individual server's load factor, the cluster's load factor, the open ledger cost and the overall network's load
   * factor.
   *
   * @return An optionally-present {@link BigDecimal} representing the load factor.
   */
  @JsonProperty("load_factor")
  Optional<BigDecimal> loadFactor();

  /**
   * Current multiplier to the transaction cost based on load to this server.
   *
   * @return An optionally-present {@link BigDecimal} representing the local load factor.
   */
  @JsonProperty("load_factor_local")
  Optional<BigDecimal> loadFactorLocal();

  /**
   * Current multiplier to the transaction cost being used by the rest of the network (estimated from other servers'
   * reported load values).
   *
   * @return An optionally-present {@link BigDecimal} representing the network load factor.
   */
  @JsonProperty("load_factor_net")
  Optional<BigDecimal> loadFactorNet();

  /**
   * Current multiplier to the transaction cost based on load to servers in this cluster.
   *
   * @return An optionally-present {@link BigDecimal} representing the cluster load factor.
   */
  @JsonProperty("load_factor_cluster")
  Optional<BigDecimal> loadFactorCluster();

  /**
   * The current multiplier to the transaction cost that a transaction must pay to get into the open ledger.
   *
   * @return An optionally-present {@link BigDecimal} representing the open ledger load factor.
   */
  @JsonProperty("load_factor_fee_escalation")
  Optional<BigDecimal> loadFactorFeeEscalation();

  /**
   * The current multiplier to the transaction cost that a transaction must pay to get into the queue, if the queue is
   * full.
   *
   * @return An optionally-present {@link BigDecimal} representing the queue load factor.
   */
  @JsonProperty("load_factor_fee_queue")
  Optional<BigDecimal> loadFactorFeeQueue();

  /**
   * The load factor the server is enforcing, not including the open ledger cost.
   *
   * @return An optionally-present {@link BigDecimal} representing the server load factor.
   */
  @JsonProperty("load_factor_server")
  Optional<BigDecimal> loadFactorServer();

  /**
   * Information about the most recent fully-validated ledger. If the most recent validated ledger is not available, the
   * response omits this field and includes {@link #closedLedger()} instead.
   *
   * @return An optionally-present {@link ServerInfoLedger} representing the latest validated ledger.
   */
  @JsonProperty("validated_ledger")
  Optional<ServerInfoLedger> validatedLedger();

  /**
   * Information on the most recently closed ledger that has not been validated by consensus. If the most recently
   * validated ledger is available, the response omits this field and includes {@link #validatedLedger()} instead.
   *
   * @return An optionally-present {@link ServerInfoLedger} containing information about the server's view of the most
   *   recently closed ledger.
   */
  @JsonProperty("closed_ledger")
  Optional<ServerInfoLedger> closedLedger();

  /**
   * Minimum number of trusted validations required to validate a ledger version. Some circumstances may cause the
   * server to require more validations.
   *
   * @return An {@link UnsignedInteger} representing the quorum.
   */
  @JsonProperty("validation_quorum")
  UnsignedInteger validationQuorum();

  /**
   * (Admin only) Either the human readable time, in UTC, when the current validator list will expire, the string
   * {@code "unknown"} if the server has yet to load a published validator list or the string {@code "never"} if the
   * server uses a static validator list.
   *
   * @return An optionally-present {@link String} containing the validator expiration list.
   */
  @JsonProperty("validator_list_expires")
  Optional<String> validatorListExpires();
}
