package org.xrpl.xrpl4j.model.client.serverinfo;

import java.util.Objects;
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

}
