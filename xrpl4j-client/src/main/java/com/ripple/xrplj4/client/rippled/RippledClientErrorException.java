package com.ripple.xrplj4.client.rippled;

/**
 * Wrapper for errors related to calling rippled JSON RPC API.
 */
public class RippledClientErrorException extends Throwable {

  public RippledClientErrorException(String error) {
    super(error);
  }

}
