package org.xrpl.xrpl4j.crypto.mpt.keys;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

/**
 * A reference to an ElGamal private key stored in an external system.
 *
 * <p>Implementations of this interface do not contain actual key material.
 * They only contain an identifier that can be used to locate the key in
 * an external system such as a Hardware Security Module (HSM) or
 * Key Management Service (KMS).</p>
 *
 * <p>This design allows for secure key management where the private key
 * never leaves the external system, and all cryptographic operations
 * are performed within that system.</p>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * public class AwsKmsElGamalKeyReference implements ElGamalPrivateKeyReference {
 *   private final String kmsKeyArn;
 *
 *   public AwsKmsElGamalKeyReference(String kmsKeyArn) {
 *     this.kmsKeyArn = kmsKeyArn;
 *   }
 *
 *   @Override
 *   public String keyIdentifier() {
 *     return kmsKeyArn;
 *   }
 * }
 * }</pre>
 *
 * @see ElGamalPrivateKeyable
 * @see ElGamalPrivateKey
 */
public interface ElGamalPrivateKeyReference extends ElGamalPrivateKeyable {

  /**
   * The unique identifier for the private key in the external system.
   *
   * <p>The format of this identifier depends on the external system being used.
   * Examples include:</p>
   * <ul>
   *   <li>HSM key handle or label</li>
   *   <li>AWS KMS key ARN</li>
   *   <li>Google Cloud KMS key resource name</li>
   *   <li>Azure Key Vault key identifier</li>
   * </ul>
   *
   * @return A {@link String} identifier for the key.
   */
  String keyIdentifier();
}

