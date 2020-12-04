package org.xrpl.xrpl4j.keypairs.exceptions;

public class SigningException extends RuntimeException {

  public SigningException() {
    super();
  }

  public SigningException(String message) {
    super(message);
  }

  public SigningException(String message, Throwable cause) {
    super(message, cause);
  }

  public SigningException(Throwable cause) {
    super(cause);
  }
}
