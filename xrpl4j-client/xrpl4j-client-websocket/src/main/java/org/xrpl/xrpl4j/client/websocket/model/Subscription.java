package org.xrpl.xrpl4j.client.websocket.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import okhttp3.HttpUrl;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSubscription.class)
@JsonDeserialize(as = ImmutableSubscription.class)
public interface Subscription {
  
  /**
   * Construct a {@code Subscription} builder.
   *
   * @return An {@link ImmutableSubscription.Builder}.
   */
  static ImmutableSubscription.Builder builder() {
    return ImmutableSubscription.builder();
  }
  
  SubscriptionId id();
  
  ClientId clientId();
  
  Address address();
  
  Optional<HttpUrl> xrplUrl();
  
  Optional<HttpUrl> webhookUrl();
}
