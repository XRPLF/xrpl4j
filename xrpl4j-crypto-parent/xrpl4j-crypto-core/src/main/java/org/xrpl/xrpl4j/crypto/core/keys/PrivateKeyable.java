package org.xrpl.xrpl4j.crypto.core.keys;

import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.VersionType;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface PrivateKeyable {

  /**
   * The type of this key.
   *
   * @return A {@link VersionType}.
   */
  VersionType versionType();

  /**
   * Handle this {@link PrivateKeyable} depending on which specifier is present.
   *
   * @param privateKeyable      A {@link Consumer} that is called if this object is an instance of {@link PrivateKey}.
   * @param privateKeyReference A {@link Consumer} that is called if this object is an instance of
   *                            {@link PrivateKeyReference}.
   */
  // TODO: TEST!
  @Value.Auxiliary
  default void handle(
    final Consumer<PrivateKey> privateKeyable,
    final Consumer<PrivateKeyReference> privateKeyReference
  ) {
    Objects.requireNonNull(privateKeyable);
    Objects.requireNonNull(privateKeyReference);

    if (PrivateKey.class.isAssignableFrom(
      this.getClass())) {
      privateKeyable.accept((PrivateKey) this);
    } else if (PrivateKeyReference.class.isAssignableFrom(this.getClass())) {
      privateKeyReference.accept((PrivateKeyReference) this);
    } else {
      // TODO: FIXME!
      throw new RuntimeException();
    }
  }

  /**
   * Map this {@link PrivateKeyable} to an instance of {@link R}, depending on which specifier is present.
   *
   * @param privateKeyMapper          A {@link Function} that is called if this object is an instance of
   *                                  {@link PrivateKey}.
   * @param privateKeyReferenceMapper A {@link Function} that is called if this object is an instance of
   *                                  {@link PrivateKeyReference}.
   * @param <R>                       The type of object to return after mapping.
   *
   * @return A {@link R} that is constructed by the appropriate mapper function.
   */
  // TODO: TEST!
  @Value.Auxiliary
  default <R> R map(
    final Function<PrivateKey, R> privateKeyMapper,
    final Function<PrivateKeyReference, R> privateKeyReferenceMapper
  ) {
    Objects.requireNonNull(privateKeyMapper);
    Objects.requireNonNull(privateKeyReferenceMapper);

    if (PrivateKey.class.isAssignableFrom(this.getClass())) {
      return privateKeyMapper.apply((PrivateKey) this);
    } else if (PrivateKeyReference.class.isAssignableFrom(this.getClass())) {
      return privateKeyReferenceMapper.apply((PrivateKeyReference) this);
    } else {
      // TODO: FIXME!
      throw new RuntimeException();
    }
  }

}
