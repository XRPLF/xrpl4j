package com.ripple.xrplj4.client.rippled;

public class RippledClientErrorException extends Throwable {

  public RippledClientErrorException(String error) {
    super(error);
  }

}
