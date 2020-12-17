package org.xrpl.xrpl4j.keypairs.exceptions;

/**
 * A {@link RuntimeException} thrown when an error occurs during signing.
 */
public class SigningException extends RuntimeException {

  /**
   * No-arg constructor.
   */
  public SigningException() {
    super();
  }

  /**
   * Required-args constructor.
   *
   * @param message A detail message {@link String}.
   */
  public SigningException(String message) {
    super(message);
  }

  /**
   * Required-args constructor.
   *
   * @param message A detail message {@link String}.
   * @param cause   The {@link Throwable} that caused this exception.
   */
  public SigningException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Required-args constructor.
   *
   * @param cause The {@link Throwable} that caused this exception.
   */
  public SigningException(Throwable cause) {
    super(cause);
  }
}
