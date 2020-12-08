package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.time.ZonedDateTime;

/**
 * Maps the fields inside the "info" section of the "server_info" API call. At the moment, this class only
 * maps a subset of the response data.
 */
@Immutable
@JsonSerialize(as = ImmutableServerInfo.class)
@JsonDeserialize(as = ImmutableServerInfo.class)
public interface ServerInfo {

  @JsonProperty("build_version")
  String buildVersion();

  @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss.SSSSSS z")
  ZonedDateTime time();


  public static void main(String[] args) throws JsonProcessingException {
    ObjectMapper mapper = ObjectMapperFactory.create();

    String json ="{ \"build_version\":\"\", \"time\":\"2020-Dec-07 21:38:33.426999 UTC\" }";

    ServerInfo result = mapper.readValue(json, ServerInfo.class);
    System.out.println(result);
  }

}
