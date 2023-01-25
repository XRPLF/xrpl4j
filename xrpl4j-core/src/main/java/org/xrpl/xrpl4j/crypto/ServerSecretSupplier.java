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

import java.util.function.Supplier;

/**
 * Supplies the Server secret in an implementation agnostic manner. For example, this value could be stored in an
 * encrypted JKS file, or it could be supplied via an environment variable (for lower-security deployments).
 *
 * @deprecated consider using the variant from org.xrpl.xrpl4j.crypto.core.
 */
@Deprecated
public interface ServerSecretSupplier extends Supplier<byte[]> {

}
