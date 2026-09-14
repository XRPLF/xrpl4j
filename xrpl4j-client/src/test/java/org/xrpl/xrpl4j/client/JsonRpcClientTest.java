package org.xrpl.xrpl4j.client;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.model.client.XrplResult;

import java.util.Collections;

/**
 * Unit test for {@link JsonRpcClient}.
 */
class JsonRpcClientTest {

  private JsonRpcClient jsonRpcClient;

  @Mock
  JsonNode jsonResponseNodeMock; // <-- The main response

  @Mock
  JsonNode jsonResultNodeMock; // <-- resp.result

  @Mock
  JsonNode jsonStatusNodeMock; // <-- result.status

  @Mock
  JsonNode jsonErrorNodeMock; // <-- result.error

  @Mock
  JsonNode jsonErrorMessageNodeMock; // <-- result.error_message

  @Mock
  JsonNode jsonErrorExceptionNodeMock; // <-- result.error_exception

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    jsonRpcClient = rpcRequest -> jsonResponseNodeMock;

    // See https://xrpl.org/docs/references/http-websocket-apis/api-conventions/error-formatting/#json-rpc-format
    when(jsonStatusNodeMock.asText()).thenReturn(JsonRpcClient.ERROR);

    when(jsonErrorNodeMock.asText()).thenReturn("error_foo");
    when(jsonErrorMessageNodeMock.asText()).thenReturn("error_message_foo");
    when(jsonErrorExceptionNodeMock.asText()).thenReturn("error_exception_foo");

    // By default, there's a result.
    when(jsonResponseNodeMock.has("result")).thenReturn(true);
    when(jsonResponseNodeMock.get("result")).thenReturn(jsonResultNodeMock);

    // Default to an empty fieldNames() iterator so the missing-result branch can format its message.
    when(jsonResponseNodeMock.fieldNames()).thenReturn(Collections.emptyIterator());

    // By default, there's an error.
    when(jsonResultNodeMock.has("status")).thenReturn(true);
    when(jsonResultNodeMock.get("status")).thenReturn(jsonStatusNodeMock);

    hasError(true); // <-- By default, there's a `result.error`
    hasErrorMessage(false); // <-- By default, there's no `result.error_message`
    hasErrorException(false); // <-- By default, there's no `result.error_exception`
  }

  //////////////////
  // checkForError()
  //////////////////

  @Test
  void testCheckForErrorWhenResponseHasNoResultField() throws JsonRpcClientErrorException {
    // Do nothing if no "result" field
    when(jsonResponseNodeMock.has("result")).thenReturn(false);
    jsonRpcClient.checkForError(jsonResponseNodeMock); // <-- No error should be thrown.
  }

  @Test
  void testCheckForErrorWhenResponseHasNoStatusFields() throws JsonRpcClientErrorException {
    when(jsonResultNodeMock.has("status")).thenReturn(false);
    jsonRpcClient.checkForError(jsonResponseNodeMock);
  }

  @Test
  void testCheckForErrorWhenResponseHasNoErrorFields() throws JsonRpcClientErrorException {
    hasError(false);
    jsonRpcClient.checkForError(jsonResponseNodeMock);
  }

  @Test
  void testCheckForErrorWhenResponseHasResultErrorException() {
    hasErrorException(true);

    JsonRpcClientErrorException error = assertThrows(
      JsonRpcClientErrorException.class,
      () -> jsonRpcClient.checkForError(jsonResponseNodeMock)
    );
    assertThat(error.getMessage()).isEqualTo("error_foo (error_exception_foo)");
  }

  @Test
  void testCheckForErrorWhenResponseHasResultErrorMessage() {
    hasErrorMessage(true);

    JsonRpcClientErrorException error = assertThrows(
      JsonRpcClientErrorException.class,
      () -> jsonRpcClient.checkForError(jsonResponseNodeMock)
    );
    assertThat(error.getMessage()).isEqualTo("error_foo (error_message_foo)");
  }

  @Test
  void testCheckForErrorWhenResponseHasResultError() {
    hasError(true);

    JsonRpcClientErrorException error = assertThrows(JsonRpcClientErrorException.class,
      () -> jsonRpcClient.checkForError(jsonResponseNodeMock));
    assertThat(error.getMessage()).isEqualTo("error_foo (n/a)");
  }

  @Test
  void testCheckForErrorWhenResponseHasAll() {
    hasError(true);
    hasErrorMessage(true);
    hasErrorMessage(true);

    JsonRpcClientErrorException error = assertThrows(
      JsonRpcClientErrorException.class,
      () -> jsonRpcClient.checkForError(jsonResponseNodeMock)
    );
    assertThat(error.getMessage()).isEqualTo("error_foo (error_message_foo)");
  }

  //////////////////
  // send()
  //////////////////

  @Test
  void testSendThrowsWhenResponseHasNoResultField() {
    // No "result" field on the response — checkForError() returns silently,
    // and send() should throw a descriptive exception instead of NPE.
    when(jsonResponseNodeMock.has("result")).thenReturn(false);
    when(jsonResponseNodeMock.get("result")).thenReturn(null);

    JsonRpcRequest request = mock(JsonRpcRequest.class);

    JsonRpcClientErrorException error = assertThrows(
      JsonRpcClientErrorException.class,
      () -> jsonRpcClient.send(request, XrplResult.class)
    );
    assertThat(error.getMessage()).contains("Response did not contain a 'result' field");
  }

  @Test
  void testSendWithJavaTypeThrowsWhenResponseHasNoResultField() {
    // Same as above, invoked through the JavaType overload of send() for explicit coverage.
    // (The Class<T> overload also delegates here, so this is redundant but cheap.)
    when(jsonResponseNodeMock.has("result")).thenReturn(false);
    when(jsonResponseNodeMock.get("result")).thenReturn(null);

    JsonRpcRequest request = mock(JsonRpcRequest.class);
    JavaType javaType = JsonRpcClient.objectMapper.constructType(XrplResult.class);

    JsonRpcClientErrorException error = assertThrows(
      JsonRpcClientErrorException.class,
      () -> jsonRpcClient.send(request, javaType)
    );
    assertThat(error.getMessage()).contains("Response did not contain a 'result' field");
  }

  @Test
  void testSendThrowsWhenResultFieldIsJsonNull() {
    // The "result" field is present but its value is JSON `null` (a Jackson NullNode,
    // not Java null). The guard must catch this too — otherwise result.toString()
    // would produce the string "null" and fail with a confusing deserialization error.
    when(jsonResponseNodeMock.has("result")).thenReturn(true);
    when(jsonResponseNodeMock.get("result")).thenReturn(JsonNodeFactory.instance.nullNode());

    JsonRpcRequest request = mock(JsonRpcRequest.class);

    JsonRpcClientErrorException error = assertThrows(
      JsonRpcClientErrorException.class,
      () -> jsonRpcClient.send(request, XrplResult.class)
    );
    assertThat(error.getMessage()).contains("Response did not contain a 'result' field");
  }

  @Test
  void testSendBypassesGuardWhenResultFieldIsPresentAndNonNull() {
    // Cover the FALSE branch of `if (result == null || result.isNull())`:
    // result is a real, non-null, non-NullNode JsonNode. The guard is bypassed and
    // execution proceeds to deserialization. XrplResult is a marker interface with
    // no default impl, so readValue throws a JsonProcessingException that send()
    // wraps as JsonRpcClientErrorException. The point of the test is to exercise
    // the guard's false-branch — not the deserialization outcome.
    when(jsonResponseNodeMock.has("result")).thenReturn(false); // checkForError stays silent
    when(jsonResponseNodeMock.get("result")).thenReturn(JsonNodeFactory.instance.objectNode());

    JsonRpcRequest request = mock(JsonRpcRequest.class);

    JsonRpcClientErrorException error = assertThrows(
      JsonRpcClientErrorException.class,
      () -> jsonRpcClient.send(request, XrplResult.class)
    );
    // It must NOT be the "missing result" exception — the guard should have been bypassed.
    assertThat(error.getMessage()).doesNotContain("Response did not contain a 'result' field");
  }

  //////////////////
  // Private Helpers
  //////////////////

  private void hasError(boolean hasError) {
    when(jsonResultNodeMock.has(JsonRpcClient.ERROR)).thenReturn(hasError);
    if (hasError) {
      when(jsonResultNodeMock.get(JsonRpcClient.ERROR)).thenReturn(jsonErrorNodeMock);
      when(jsonResultNodeMock.hasNonNull(JsonRpcClient.ERROR)).thenReturn(true);
    } else {
      when(jsonResultNodeMock.get(JsonRpcClient.ERROR)).thenReturn(null);
    }
  }

  private void hasErrorMessage(boolean hasErrorMessage) {
    when(jsonResultNodeMock.has(JsonRpcClient.ERROR_MESSAGE)).thenReturn(hasErrorMessage);
    if (hasErrorMessage) {
      when(jsonResultNodeMock.get(JsonRpcClient.ERROR_MESSAGE)).thenReturn(jsonErrorMessageNodeMock);
      when(jsonResultNodeMock.hasNonNull(JsonRpcClient.ERROR_MESSAGE)).thenReturn(true);
    } else {
      when(jsonResultNodeMock.get(JsonRpcClient.ERROR_MESSAGE)).thenReturn(null);
    }
  }

  private void hasErrorException(boolean hasErrorException) {
    when(jsonResultNodeMock.has(JsonRpcClient.ERROR_EXCEPTION)).thenReturn(hasErrorException);
    if (hasErrorException) {
      when(jsonResultNodeMock.get(JsonRpcClient.ERROR_EXCEPTION)).thenReturn(jsonErrorExceptionNodeMock);
      when(jsonResultNodeMock.hasNonNull(JsonRpcClient.ERROR_EXCEPTION)).thenReturn(true);
    } else {
      when(jsonResultNodeMock.get(JsonRpcClient.ERROR_EXCEPTION)).thenReturn(null);
    }
  }
}