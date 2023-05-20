package org.xrpl.xrpl4j.client.websocket.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.immutables.Wrapped;
import org.xrpl.xrpl4j.model.immutables.Wrapper;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("TypeName")
public final class IdWrappers {
  
  private IdWrappers() {}
  
  /**
   * A wrapped {@link UUID} representing a SubscriptionId.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = SubscriptionId.class, using = ToStringSerializer.class)
  @JsonDeserialize(as = SubscriptionId.class)
  abstract static class _SubscriptionId extends Wrapper<UUID> implements Serializable {
    
    /**
     * Generate a random ID.
     *
     * @return A new random {@link SubscriptionId}.
     */
    public static SubscriptionId randomId() {
      return SubscriptionId.of(UUID.randomUUID());
    }
    
    @Override
    public String toString() {
      return this.value().toString();
    }
    
  }
  
  /**
   * A wrapped {@link UUID} representing a ClientId.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = ClientId.class, using = ToStringSerializer.class)
  @JsonDeserialize(as = ClientId.class)
  abstract static class _ClientId extends Wrapper<UUID> implements Serializable {
    
    /**
     * Generate a random ID.
     *
     * @return A new random {@link ClientId}.
     */
    public static ClientId randomId() {
      return ClientId.of(UUID.randomUUID());
    }
    
    @Override
    public String toString() {
      return this.value().toString();
    }
    
  }
  
  // TODO: Used?
  /**
   * A wrapped {@link UUID} representing a XrplTransactionId.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = XrplTransactionId.class, using = ToStringSerializer.class)
  @JsonDeserialize(as = XrplTransactionId.class)
  abstract static class _XrplTransactionId extends Wrapper<UUID> implements Serializable {
    
    /**
     * Generate a random ID.
     *
     * @return A new random {@link XrplTransactionId}.
     */
    public static XrplTransactionId randomId() {
      return XrplTransactionId.of(UUID.randomUUID());
    }
    
    @Override
    public String toString() {
      return this.value().toString();
    }
    
  }
  
  /**
   * A wrapped {@link UUID} representing a SubscriptionXrplTransactionId.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = SubscriptionXrplTransactionId.class, using = ToStringSerializer.class)
  @JsonDeserialize(as = SubscriptionXrplTransactionId.class)
  abstract static class _SubscriptionXrplTransactionId extends Wrapper<UUID> implements Serializable {
    
    /**
     * Generate a random ID.
     *
     * @return A new random {@link SubscriptionXrplTransactionId}.
     */
    public static SubscriptionXrplTransactionId randomId() {
      return SubscriptionXrplTransactionId.of(UUID.randomUUID());
    }
    
    @Override
    public String toString() {
      return this.value().toString();
    }
    
  }
  
}