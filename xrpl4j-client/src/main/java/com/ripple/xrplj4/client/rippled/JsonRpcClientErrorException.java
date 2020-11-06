package com.ripple.xrplj4.client.rippled;

/**
 * Wrapper for errors related to calling rippled JSON RPC API.
 */
public class JsonRpcClientErrorException extends Throwable {

  public JsonRpcClientErrorException(String error) {
    super(error);
  }

  public JsonRpcClientErrorException(Throwable cause) {
    super(cause);
  }
}
