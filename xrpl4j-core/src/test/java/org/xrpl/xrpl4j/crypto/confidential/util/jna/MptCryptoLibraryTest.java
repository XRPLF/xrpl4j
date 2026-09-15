package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MptCryptoLibrary#load(java.util.function.Supplier)} — the native-library loader's
 * error-handling seam, exercised without the native library present.
 */
class MptCryptoLibraryTest {

  @Test
  void loadReturnsLoaderResult() {
    MptCryptoLibrary lib = mock(MptCryptoLibrary.class);
    assertThat(MptCryptoLibrary.load(() -> lib)).isSameAs(lib);
  }

  @Test
  void loadWrapsUnsatisfiedLinkErrorWithGuidance() {
    UnsatisfiedLinkError cause = new UnsatisfiedLinkError("boom");
    assertThatThrownBy(() -> MptCryptoLibrary.load(() -> {
      throw cause;
    }))
      .isInstanceOf(UnsatisfiedLinkError.class)
      .hasMessageContaining("Unable to load the native 'mpt-crypto' library")
      .hasMessageContaining("jna.library.path")
      .hasMessageContaining("boom")
      .hasCause(cause);
  }
}
