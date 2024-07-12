package org.xrpl.xrpl4j.client;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test for {@link JsonRpcClient}.
 */
class JsonRpcClientTest {

  private JsonRpcClient jsonRpcClient;

  @Mock
  JsonNode jsonNodeMock;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    jsonRpcClient = rpcRequest -> jsonNodeMock;
  }

  //////////////////
  // checkForError()
  //////////////////

  @Test
  void testCheckForErrorWhenResponseHasNoResultField() throws JsonRpcClientErrorException {
    // Do nothing if no "result" field
    when(jsonNodeMock.has("result")).thenReturn(false);
    jsonRpcClient.checkForError(jsonNodeMock);
  }

  @Test
  void testCheckForErrorWhenResponseHasNoErrorFields() throws JsonRpcClientErrorException {
    // Do nothing if "result" but not "error" field
    when(jsonNodeMock.has("result")).thenReturn(true);
    JsonNode resultNodeMock = mock(JsonNode.class);
    when(jsonNodeMock.get("result")).thenReturn(resultNodeMock);

    when(resultNodeMock.has("error")).thenReturn(false);
    jsonRpcClient.checkForError(jsonNodeMock);
  }

  @Test
  void testCheckForErrorWhenResponseHasErrorException() {
    when(jsonNodeMock.has("result")).thenReturn(true);
    JsonNode resultNodeMock = mock(JsonNode.class);
    when(jsonNodeMock.get("result")).thenReturn(resultNodeMock);
    when(resultNodeMock.has("error")).thenReturn(true);

    JsonNode errorExceptionNodeMock = mock(JsonNode.class);
    when(errorExceptionNodeMock.asText()).thenReturn("error_exception_foo");
    when(resultNodeMock.get("error_exception")).thenReturn(errorExceptionNodeMock);

    JsonRpcClientErrorException error = assertThrows(JsonRpcClientErrorException.class,
      () -> jsonRpcClient.checkForError(jsonNodeMock));
    assertThat(error.getMessage()).isEqualTo("error_exception_foo");
  }

  @Test
  void testCheckForErrorWhenResponseHasErrorMessage() {
    when(jsonNodeMock.has("result")).thenReturn(true);
    JsonNode resultNodeMock = mock(JsonNode.class);
    when(jsonNodeMock.get("result")).thenReturn(resultNodeMock);
    when(resultNodeMock.has("error")).thenReturn(true);

    JsonNode errorExceptionNodeMock = mock(JsonNode.class);
    when(errorExceptionNodeMock.asText()).thenReturn("error_message_foo");
    when(resultNodeMock.get("error_message")).thenReturn(errorExceptionNodeMock);

    JsonRpcClientErrorException error = assertThrows(JsonRpcClientErrorException.class,
      () -> jsonRpcClient.checkForError(jsonNodeMock));
    assertThat(error.getMessage()).isEqualTo("error_message_foo");
  }

  @Test
  void testCheckForErrorWhenResponseHasOnlyError() {
    when(jsonNodeMock.has("result")).thenReturn(true);
    JsonNode resultNodeMock = mock(JsonNode.class);
    when(jsonNodeMock.get("result")).thenReturn(resultNodeMock);
    when(resultNodeMock.has("error")).thenReturn(true);

    JsonNode errorNodeMock = mock(JsonNode.class);
    when(errorNodeMock.asText()).thenReturn("error_foo");
    when(resultNodeMock.get("error")).thenReturn(errorNodeMock);

    JsonRpcClientErrorException error = assertThrows(JsonRpcClientErrorException.class,
      () -> jsonRpcClient.checkForError(jsonNodeMock));
    assertThat(error.getMessage()).isEqualTo("error_foo");
  }
}