package org.xrpl.xrpl4j.model.client.serverinfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.google.common.collect.Range;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;
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
   * Range expression indicating the sequence numbers of the ledger versions the local rippled has in its database. This
   * may be a disjoint sequence such as {@code 24900901-24900984,24901116-24901158}. If the server does not have any
   * complete ledgers (for example, it recently started syncing with the network), this will be an empty
   * {@link String}.
   *
   * @return A {@link String} representing a range of ledger sequences.
   */
  @JsonProperty("complete_ledgers")
  @JsonDeserialize(using = CompleteLedgersDeserializer.class)
  List<Range<UnsignedLong>> completeLedgers();

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
    return this.completeLedgers().stream().anyMatch(range -> range.contains(ledgerIndex));
  }

  /**
   * The load-scaled open ledger transaction cost the server is currently enforcing, as a multiplier on the base
   * transaction cost. For example, at 1000 load factor and a reference transaction cost of 10 drops of XRP, the
   * load-scaled transaction cost is 10,000 drops (0.01 XRP). The load factor is determined by the highest of the
   * individual server's load factor, the cluster's load factor, the open ledger cost and the overall network's load
   * factor. Per xrpl.org docs, this field is optionally present in any server response and may be omitted because it is
   * only returned when requested via an admin host/port.
   *
   * @return An optionally-present {@link BigDecimal} representing the load factor.
   */
  @JsonProperty("load_factor")
  Optional<BigDecimal> loadFactor();

  /**
   * Information about the most recent fully-validated ledger. If the most recent validated ledger is not available, the
   * response omits this field and includes {@link ReportingModeServerInfo#closedLedger()} or
   * {@link RippledServerInfo#closedLedger()} but nothing in {@link ClioServerInfo}. Per xrpl.org docs, this field is
   * optionally present in any server response and may be omitted.
   *
   * @return An optionally-present {@link ValidatedLedger} representing the latest validated ledger.
   */
  @JsonProperty("validated_ledger")
  Optional<ValidatedLedger> validatedLedger();

  /**
   * Minimum number of trusted validations required to validate a ledger version. Some circumstances may cause the
   * server to require more validations. Additionally, some implemenations (e.g., clio) tries to get this info from
   * rippled, but if rippled does not respond for some reason, the info is not included.
   *
   * @return An {@link UnsignedInteger} representing the quorum.
   */
  @JsonProperty("validation_quorum")
  Optional<UnsignedInteger> validationQuorum();

  /**
   * Deserializes complete_ledgers field in the server_info response from hyphen-separated ledger indices to list of
   * range of UnsignedLong values.
   */
  class CompleteLedgersDeserializer extends JsonDeserializer<List<Range<UnsignedLong>>> {

    @Override
    public List<Range<UnsignedLong>> deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
      JsonNode node = jsonParser.getCodec().readTree(jsonParser);
      return LedgerRangeUtils.completeLedgersToListOfRange(node.asText());
    }
  }


  /**
   * (Admin only) Information about the rate of a job the server is doing and how much time it spends on it.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableJobType.class)
  @JsonDeserialize(as = ImmutableJobType.class)
  interface JobType {

    /**
     * Construct a builder for this class.
     *
     * @return An {@link ImmutableJobType.Builder}.
     */
    static ImmutableJobType.Builder builder() {
      return ImmutableJobType.builder();
    }

    /**
     * The type of job.
     *
     * @return A {@link String} representing the job type.
     */
    @JsonProperty("job_type")
    String jobType();

    /**
     * The number of jobs that are currently in progress.
     *
     * @return An optionally-present {@link UnsignedInteger} representing the number of jobs in progress.
     */
    @JsonProperty("in_progress")
    Optional<UnsignedInteger> inProgress();

    /**
     * The peak time of the job.
     *
     * @return An optionally-present {@link UnsignedInteger} denoting the peak time.
     */
    @JsonProperty("peak_time")
    Optional<UnsignedInteger> peakTime();

    /**
     * The number of jobs of this type performed per second.
     *
     * @return An optionally-present {@link UnsignedInteger} denoting the number of jobs.
     */
    @JsonProperty("per_second")
    Optional<UnsignedInteger> perSecond();

    /**
     * The average time, in seconds, jobs of this type take to perform.
     *
     * @return An optionally-present {@link UnsignedInteger} denoting the average time.
     */
    @JsonProperty("avg_time")
    Optional<UnsignedInteger> averageTime();

  }

  /**
   * Information about the last time the server closed a ledger, including the amount of time it took to reach a
   * consensus and the number of trusted validators participating.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableLastClose.class)
  @JsonDeserialize(as = ImmutableLastClose.class)
  interface LastClose {

    /**
     * Construct a builder for this class.
     *
     * @return An {@link ImmutableLastClose.Builder}.
     */
    static ImmutableLastClose.Builder builder() {
      return ImmutableLastClose.builder();
    }

    /**
     * The amount of time, in seconds, it took to converge.
     *
     * @return A {@link Double} representing the convergence time.
     */
    @JsonProperty("converge_time_s")
    BigDecimal convergeTimeSeconds();

    /**
     * The number of proposers in the last closed ledger.
     *
     * @return An {@link UnsignedInteger} representing the number of proposers.
     */
    UnsignedInteger proposers();
  }

  /**
   * Information about the current load state of a rippled server.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableLoad.class)
  @JsonDeserialize(as = ImmutableLoad.class)
  interface Load {

    /**
     * Construct a builder for this class.
     *
     * @return An {@link ImmutableLoad.Builder}.
     */
    static ImmutableLoad.Builder builder() {
      return ImmutableLoad.builder();
    }

    /**
     * (Admin only) Information about the rate of different types of jobs the server is doing and how much time it
     * spends on each.
     *
     * @return A {@link List} of {@link ServerInfo.JobType}.
     */
    @JsonProperty("job_types")
    List<JobType> jobTypes();

    /**
     * (Admin only) The number of threads in the server's main job pool.
     *
     * @return An {@link UnsignedInteger} representing the number of threads.
     */
    UnsignedInteger threads();
  }

  /**
   * Human-readable information about a rippled server being queried.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableValidatedLedger.class)
  @JsonDeserialize(as = ImmutableValidatedLedger.class)
  interface ValidatedLedger {

    /**
     * Construct a builder for this class.
     *
     * @return An {@link ImmutableValidatedLedger.Builder}.
     */
    static ImmutableValidatedLedger.Builder builder() {
      return ImmutableValidatedLedger.builder();
    }

    /**
     * The time since the ledger was closed, in seconds.
     *
     * @return An {@link UnsignedInteger} representing the age, in seconds.
     */
    UnsignedInteger age();

    /**
     * Unique hash for the ledger, as hexadecimal.
     *
     * @return A {@link Hash256} containing the ledger hash.
     */
    Hash256 hash();

    /**
     * Minimum amount of XRP necessary for every account to keep in reserve. Note that the server returns values in XRP,
     * whereas {@link XrpCurrencyAmount} supports both drops and XRP.
     *
     * @return An {@link XrpCurrencyAmount} representing the amount of XRP (in drops) to reserve.
     */
    @JsonProperty("reserve_base_xrp")
    @JsonSerialize(using = CurrencyAmountToXrpSerializer.class)
    @JsonDeserialize(using = XrpToCurrencyAmountDeserializer.class)
    XrpCurrencyAmount reserveBaseXrp();

    /**
     * Amount of XRP added to the account reserve for each object an account owns in the ledger. Note that the server
     * returns values in XRP, * whereas {@link XrpCurrencyAmount} supports both drops and XRP.
     *
     * @return An {@link XrpCurrencyAmount} representing the amount of XRP (in drops) added.
     */
    @JsonProperty("reserve_inc_xrp")
    @JsonSerialize(using = CurrencyAmountToXrpSerializer.class)
    @JsonDeserialize(using = XrpToCurrencyAmountDeserializer.class)
    XrpCurrencyAmount reserveIncXrp();

    /**
     * The ledger index of the ledger.
     *
     * @return A {@link LedgerIndex} indicating the sequence of the latest ledger.
     */
    @JsonProperty("seq")
    LedgerIndex sequence();

    /**
     * The base XRP cost of transaction.
     *
     * @return A {@link BigDecimal} representing base fee amount in XRP.
     */
    @JsonProperty("base_fee_xrp")
    BigDecimal baseFeeXrp();

    /**
     * Deserializes either a scientific notation or a decimal notation amount of XRP into an {@link XrpCurrencyAmount}
     * that holds drops internally.
     */
    class XrpToCurrencyAmountDeserializer extends JsonDeserializer<XrpCurrencyAmount> {

      @Override
      public XrpCurrencyAmount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return XrpCurrencyAmount.ofXrp(new BigDecimal(node.asText()));
      }
    }

    /**
     * Serializes XrpCurrencyAmount (which is by default stored in drops) into an XRP-based value as an
     * {@link BigDecimal}.
     */
    class CurrencyAmountToXrpSerializer extends StdScalarSerializer<XrpCurrencyAmount> {

      /**
       * No-args constructor.
       */
      public CurrencyAmountToXrpSerializer() {
        super(XrpCurrencyAmount.class, false);
      }

      @Override
      public void serialize(XrpCurrencyAmount amount, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
        gen.writeNumber(amount.toXrp());
      }
    }
  }
}
