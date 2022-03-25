package org.xrpl.xrpl4j.crypto;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;

/**
 * A class that assists in loading {@link KeyStore} objects from the classpath.
 */
public final class JavaKeystoreLoader {

  /**
   * No-args Constructor, to avoid instantiation.
   */
  private JavaKeystoreLoader() {
  }

  /**
   * Helper method to load a {@link KeyStore} from the filesystem.
   *
   * @param keystoreFileName The name of the Java KeyStore file to load.
   * @param keystorePassword An optional password to unlock the JKS file.
   *
   * @return A {@link KeyStore}.
   */
  public static KeyStore loadFromClasspath(final String keystoreFileName, final char[] keystorePassword) {
    Objects.requireNonNull(keystoreFileName);
    Objects.requireNonNull(keystorePassword);
    // Load Secret0 from Keystore.
    final KeyStore keyStore;
    try (InputStream keyStoreStream = JavaKeystoreLoader.class.getResourceAsStream("/" + keystoreFileName)) {
      if (keyStoreStream == null) {
        throw new FileNotFoundException("'" + "/" + keystoreFileName + "' not found on classpath");
      }

      keyStore = KeyStore.getInstance("PKCS12");
      keyStore.load(keyStoreStream, keystorePassword);

      return keyStore;
    } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
      throw new RuntimeException(e);
    }
  }
}
