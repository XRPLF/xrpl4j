package org.xrpl.xrpl4j.model.client.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.AccountPermission;
import org.xrpl.xrpl4j.model.transactions.AccountPermissionWrapper;
import org.xrpl.xrpl4j.model.transactions.DelegateSet;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaDelegateObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Verifies that a real {@code tx} RPC response for a validated {@link DelegateSet} that delegates
 * {@code NFTokenModify} (as emitted by xrpld on Testnet) can be deserialized into a {@link TransactionResult}. Before
 * {@link TransactionType#NFTOKEN_MODIFY} existed, both the transaction and its {@code Delegate} metadata failed to
 * deserialize with {@code Unknown permission value: NFTokenModify}.
 *
 * <p>The fixture in {@code src/test/resources/delegate-transactions} is the unmodified {@code result} object of an
 * API v1 {@code tx} call against Testnet, named {@code tx-result-<ledger_index>-<first 8 hex of hash>.json}.
 */
class DelegateSetTransactionResultFixtureTest {

  private static final String FIXTURE_DIR = "src/test/resources/delegate-transactions/";

  private static final File FIXTURE = new File(FIXTURE_DIR + "tx-result-21867514-80F1983B.json");

  private static final List<TransactionType> EXPECTED_PERMISSIONS = Arrays.asList(
    TransactionType.CREDENTIAL_CREATE,
    TransactionType.CREDENTIAL_DELETE,
    TransactionType.NFTOKEN_MINT,
    TransactionType.NFTOKEN_MODIFY,
    TransactionType.NFTOKEN_BURN,
    TransactionType.NFTOKEN_CREATE_OFFER,
    TransactionType.NFTOKEN_CANCEL_OFFER
  );

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void deserializeDelegateSetWithNfTokenModifyPermission() throws IOException {
    TransactionResult<DelegateSet> result = objectMapper.readValue(
      FIXTURE, new TypeReference<TransactionResult<DelegateSet>>() {}
    );

    assertThat(result.hash())
      .isEqualTo(Hash256.of("80F1983BFF069EFD5D79381EA382232D5A2384970BE8A7767AEBCEBED414EA91"));
    assertThat(result.ledgerIndex()).hasValue(LedgerIndex.of(UnsignedInteger.valueOf(21867514)));
    assertThat(result.validated()).isTrue();

    DelegateSet delegateSet = result.transaction();
    assertThat(transactionTypes(delegateSet.permissions())).containsExactlyElementsOf(EXPECTED_PERMISSIONS);

    assertThat(result.metadata()).isPresent();
    TransactionMetadata meta = result.metadata().get();
    assertThat(meta.transactionResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    MetaDelegateObject delegate = meta.affectedNodes().stream()
      .filter(node -> node.ledgerEntryType().equals(MetaLedgerEntryType.DELEGATE))
      .findFirst()
      .map(node -> node.<MetaDelegateObject, MetaDelegateObject>map(
        createdNode -> createdNode.newFields(),
        modifiedNode -> {
          throw new AssertionError("Expected a CreatedNode");
        },
        deletedNode -> {
          throw new AssertionError("Expected a CreatedNode");
        }
      ))
      .orElseThrow(() -> new AssertionError("Expected a Delegate node"));
    assertThat(delegate.permissions()).isPresent();
    assertThat(transactionTypes(delegate.permissions().get())).containsExactlyElementsOf(EXPECTED_PERMISSIONS);
  }

  @Test
  void deserializeDelegateSetAsGenericTransaction() throws IOException {
    TransactionResult<Transaction> result = objectMapper.readValue(
      FIXTURE, new TypeReference<TransactionResult<Transaction>>() {}
    );

    assertThat(result.transaction()).isInstanceOf(DelegateSet.class);
  }

  private static List<TransactionType> transactionTypes(List<AccountPermissionWrapper> permissions) {
    return permissions.stream()
      .map(AccountPermissionWrapper::permission)
      .map(AccountPermission::permissionValue)
      .map(permission -> permission.map(
        transactionTypePermission -> transactionTypePermission.transactionType(),
        granularPermissionValue -> {
          throw new AssertionError("Expected only transaction type permissions");
        }
      ))
      .collect(Collectors.toList());
  }
}
