package org.xrpl.xrpl4j.model.client.serverinfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Range;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A marker interface that identifies instances of ServerInfo as returned by the XRP Ledger, but provides utilities to
 * handle subclasses in an implementation specific manner.
 */
public interface ServerInfo {

  /**
   * Handle this {@link ServerInfo} depending on its actual polymorphic sub-type.
   *
   * @param rippledServerInfoHandler       A {@link Consumer} that is called if this instance is of type
   *                                       {@link RippledServerInfo}.
   * @param clioServerInfoHandler          A {@link Consumer} that is called if this instance is of type
   *                                       {@link ClioServerInfo}.
   * @param reportingModeServerInfoHandler A {@link Consumer} that is called if this instance is of type
   *                                       {@link ReportingModeServerInfo}.
   */
  default void handle(
    final Consumer<RippledServerInfo> rippledServerInfoHandler,
    final Consumer<ClioServerInfo> clioServerInfoHandler,
    final Consumer<ReportingModeServerInfo> reportingModeServerInfoHandler
  ) {
    Objects.requireNonNull(rippledServerInfoHandler);
    Objects.requireNonNull(clioServerInfoHandler);
    Objects.requireNonNull(reportingModeServerInfoHandler);

    if (RippledServerInfo.class.isAssignableFrom(this.getClass())) {
      rippledServerInfoHandler.accept((RippledServerInfo) this);
    } else if (ClioServerInfo.class.isAssignableFrom(this.getClass())) {
      clioServerInfoHandler.accept((ClioServerInfo) this);
    } else if (ReportingModeServerInfo.class.isAssignableFrom(this.getClass())) {
      reportingModeServerInfoHandler.accept((ReportingModeServerInfo) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported ServerInfo Type: %s", this.getClass()));
    }
  }

  /**
   * Map this {@link ServerInfo} to an instance of {@link R}, depending on its actualy polymorphic sub-type.
   *
   * @param rippledServerInfoMapper       A {@link Function} that is called if this instance is of type
   *                                      {@link RippledServerInfo}.
   * @param clioServerInfoMapper          A {@link Function} that is called if this instance is  of type
   *                                      {@link ClioServerInfo}.
   * @param reportingModeServerInfoMapper A {@link Function} that is called if this instance is  of type
   *                                      {@link ReportingModeServerInfo}.
   * @param <R>                           The type of object to return after mapping.
   *
   * @return A {@link R} that is constructed by the appropriate mapper function.
   */
  default <R> R map(
    final Function<RippledServerInfo, R> rippledServerInfoMapper,
    final Function<ClioServerInfo, R> clioServerInfoMapper,
    final Function<ReportingModeServerInfo, R> reportingModeServerInfoMapper
  ) {
    Objects.requireNonNull(rippledServerInfoMapper);
    Objects.requireNonNull(clioServerInfoMapper);
    Objects.requireNonNull(reportingModeServerInfoMapper);

    if (RippledServerInfo.class.isAssignableFrom(this.getClass())) {
      return rippledServerInfoMapper.apply((RippledServerInfo) this);
    } else if (ClioServerInfo.class.isAssignableFrom(this.getClass())) {
      return clioServerInfoMapper.apply((ClioServerInfo) this);
    } else if (ReportingModeServerInfo.class.isAssignableFrom(this.getClass())) {
      return reportingModeServerInfoMapper.apply((ReportingModeServerInfo) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported ServerInfo Type: %s", this.getClass()));
    }
  }

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
   * Transforms {@link #completeLedgers()} from a range expression to a {@code List<Range<UnsignedLong>>}.
   *
   * @return A {@link List} of {@link Range} of type {@link UnsignedLong} containing the range of ledgers that a rippled
   *   node contains in its history.
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
   * (Admin only) Detailed information about the current load state of the server.
   *
   * @return An optionally-present {@link ServerInfoLoad}.
   */
  @JsonProperty("load")
  Optional<ServerInfoLoad> load();

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
   * How many other rippled servers this one is currently connected to.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the number of peers of this server.
   */
  Optional<UnsignedInteger> peers();

  /**
   * (Admin only) Public key used by this node to sign ledger validations. This validation key pair is derived from the
   * {@code [validator_token]} or {@code [validation_seed]} config field.
   *
   * @return A {@link String} containing the validator's public key.
   */
  @JsonProperty("pubkey_validator")
  Optional<String> publicKeyValidator();

  /**
   * Information about the most recent fully-validated ledger. If the most recent validated ledger is not available, the
   * response omits this field and includes {@link #closedLedger()} instead.
   *
   * @return An optionally-present {@link ServerInfoLedger} representing the latest validated ledger.
   */
  @JsonProperty("validated_ledger")
  Optional<ServerInfoLedger> validatedLedger();

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
