package org.xrpl.xrpl4j.model.client.serverinfo;

public enum ServerInfoType {
  /**
   * The {@link ServerInfoType} for {@link ClioServerInfo}.
   */
  CLIO_SERVER_INFO,

  /**
   * The {@link ServerInfoType} for {@link ReportingModeServerInfo}.
   */
  REPORTING_MODE_SERVER_INFO,

  /**
   * The {@link ServerInfoType} for {@link RippledServerInfo}.
   */
  RIPPLED_SERVER_INFO
}
