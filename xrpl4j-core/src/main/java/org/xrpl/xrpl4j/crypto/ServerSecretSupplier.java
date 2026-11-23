package org.xrpl.xrpl4j.crypto;

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

import java.util.function.Supplier;

/**
 * Supplies the Server secret in an implementation agnostic manner. For example, this value could be stored in an
 * encrypted JKS file, or it could be supplied via an environment variable (for lower-security deployments).
 *
 * <p>Implementations commonly load the underlying secret material once (e.g., from a keystore or environment
 * variable) and construct a new {@link ServerSecret} from that same in-memory {@code byte[]} on every invocation of
 * {@link #get()}, for example: {@code () -> ServerSecret.of(secretBytes)}. This is safe because {@link ServerSecret}
 * defensively copies its input, so consumers of this supplier that call {@link ServerSecret#destroy()} on a returned
 * instance only zeroize that instance's private copy, never the {@code secretBytes} array held by this supplier.</p>
 */
public interface ServerSecretSupplier extends Supplier<ServerSecret> {

}
