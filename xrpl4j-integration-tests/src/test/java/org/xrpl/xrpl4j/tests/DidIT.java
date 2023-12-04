package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.DidObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.DidData;
import org.xrpl.xrpl4j.model.transactions.DidDelete;
import org.xrpl.xrpl4j.model.transactions.DidDocument;
import org.xrpl.xrpl4j.model.transactions.DidSet;
import org.xrpl.xrpl4j.model.transactions.DidUri;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.metadata.CreatedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.DeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaDidObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;
import org.xrpl.xrpl4j.model.transactions.metadata.ModifiedNode;

import java.util.List;
import java.util.stream.Collectors;

@DisabledIf(value = "shouldRun", disabledReason = "DidIT only runs with local rippled nodes.")
public class DidIT extends AbstractIT {

  static boolean shouldRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useDevnet") != null ||
      System.getProperty("useClioTestnet") != null;
  }

  @Test
  void testCreateAndUpdateDid() throws JsonRpcClientErrorException, JsonProcessingException {
    TestDid did = createNewDid();

    AccountInfoResult sourceAccountInfo = this.getValidatedAccountInfo(
      did.ownerKeyPair().publicKey().deriveAddress());

    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee();
    DidSet updateDid = DidSet.builder()
      .account(did.ownerKeyPair().publicKey().deriveAddress())
      .sequence(sourceAccountInfo.accountData().sequence())
      .fee(fee)
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .signingPublicKey(did.ownerKeyPair().publicKey())
      .didDocument(DidDocument.of(""))
      .uri(DidUri.of("ABCD"))
      .build();

    TransactionResult<DidSet> didSetResult = this.signSubmitAndWait(
      updateDid,
      did.ownerKeyPair(),
      DidSet.class
    );

    assertThat(didSetResult.metadata()).isNotEmpty();
    List<ModifiedNode<MetaDidObject>> modifiedNodes = didSetResult.metadata().get().affectedNodes().stream()
      .filter(node -> node.ledgerEntryType().equals(MetaLedgerEntryType.DID))
      .filter(node -> ModifiedNode.class.isAssignableFrom(node.getClass()))
      .filter(node -> MetaDidObject.class.isAssignableFrom(((ModifiedNode<?>) node).finalFields().get().getClass()))
      .filter(node -> MetaDidObject.class.isAssignableFrom(((ModifiedNode<?>) node).previousFields().get().getClass()))
      .map(node -> (ModifiedNode<MetaDidObject>) node)
      .collect(Collectors.toList());

    assertThat(modifiedNodes.size()).isEqualTo(1);
    ModifiedNode<MetaDidObject> modifiedNode = modifiedNodes.get(0);
    assertThat(modifiedNode.previousFields()).isNotEmpty();
    MetaDidObject previousFields = modifiedNode.previousFields().get();
    assertThat(previousFields.didDocument()).isNotEmpty().isEqualTo(did.object().didDocument());
    assertThat(previousFields.uri()).isNotEmpty().isEqualTo(did.object().uri());
    assertThat(previousFields.data()).isEmpty();
    assertThat(previousFields.account()).isEmpty();
    assertThat(previousFields.flags()).isEqualTo(Flags.UNSET);

    assertThat(modifiedNode.previousFields()).isNotEmpty();
    MetaDidObject finalFields = modifiedNode.finalFields().get();
    assertThat(finalFields.didDocument()).isEmpty();
    assertThat(finalFields.uri()).isNotEmpty().isEqualTo(updateDid.uri());
    assertThat(finalFields.data()).isNotEmpty().isEqualTo(did.object().data());
    assertThat(finalFields.account()).isNotEmpty().get().isEqualTo(sourceAccountInfo.accountData().account());
    assertThat(finalFields.flags()).isEqualTo(Flags.UNSET);

    List<DidObject> accountObjects = this.getValidatedAccountObjects(
      did.ownerKeyPair().publicKey().deriveAddress(),
      DidObject.class
    );
    assertThat(accountObjects.size()).isEqualTo(1);
    DidObject didFromAccountObjects = accountObjects.get(0);
    assertThat(didFromAccountObjects.account()).isEqualTo(sourceAccountInfo.accountData().account());
    assertThat(didFromAccountObjects.didDocument()).isEmpty();
    assertThat(didFromAccountObjects.uri()).isNotEmpty().isEqualTo(updateDid.uri());
    assertThat(didFromAccountObjects.data()).isNotEmpty().isEqualTo(did.object().data());
    assertThat(didFromAccountObjects.flags()).isEqualTo(did.object().flags());

    LedgerEntryResult<DidObject> didFromLedgerEntryIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        modifiedNode.ledgerIndex(),
        DidObject.class,
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(didFromLedgerEntryIndex.node()).isEqualTo(didFromAccountObjects);

    LedgerEntryResult<DidObject> didFromLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.did(
        did.ownerKeyPair().publicKey().deriveAddress(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(didFromLedgerEntry.node()).isEqualTo(didFromAccountObjects);
  }

  @Test
  void testCreateAndDeleteDid() throws JsonRpcClientErrorException, JsonProcessingException {
    TestDid did = createNewDid();

    AccountInfoResult sourceAccountInfo = this.getValidatedAccountInfo(
      did.ownerKeyPair().publicKey().deriveAddress());

    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee();
    DidDelete didDelete = DidDelete.builder()
      .account(did.ownerKeyPair().publicKey().deriveAddress())
      .sequence(sourceAccountInfo.accountData().sequence())
      .fee(fee)
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .signingPublicKey(did.ownerKeyPair().publicKey())
      .build();

    TransactionResult<DidDelete> result = this.signSubmitAndWait(didDelete, did.ownerKeyPair(), DidDelete.class);

    logger.info(ObjectMapperFactory.create().writerWithDefaultPrettyPrinter().writeValueAsString(result));
    assertThat(result.metadata()).isNotEmpty();
    List<DeletedNode<MetaDidObject>> deletedNodes = result.metadata().get().affectedNodes().stream()
      .filter(node -> node.ledgerEntryType().equals(MetaLedgerEntryType.DID))
      .filter(node -> DeletedNode.class.isAssignableFrom(node.getClass()))
      .filter(node -> MetaDidObject.class.isAssignableFrom(((DeletedNode<?>) node).finalFields().getClass()))
      .map(node -> (DeletedNode<MetaDidObject>) node)
      .collect(Collectors.toList());

    assertThat(deletedNodes.size()).isEqualTo(1);
    DeletedNode<MetaDidObject> deletedNode = deletedNodes.get(0);
    MetaDidObject finalFields = deletedNode.finalFields();
    assertThat(finalFields.didDocument()).isNotEmpty().isEqualTo(did.object().didDocument());
    assertThat(finalFields.uri()).isNotEmpty().isEqualTo(did.object().uri());
    assertThat(finalFields.data()).isNotEmpty().isEqualTo(did.object().data());
    assertThat(finalFields.account()).isNotEmpty().get().isEqualTo(sourceAccountInfo.accountData().account());
    assertThat(finalFields.flags()).isEqualTo(Flags.UNSET);

    List<DidObject> accountObjects = this.getValidatedAccountObjects(
      sourceAccountInfo.accountData().account(),
      DidObject.class
    );

    assertThat(accountObjects).asList().isEmpty();
  }

  private TestDid createNewDid() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair sourceKeyPair = this.createRandomAccountEd25519();

    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee();

    DidSet didSet = DidSet.builder()
      .account(sourceAccountInfo.accountData().account())
      .sequence(sourceAccountInfo.accountData().sequence())
      .fee(fee)
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .signingPublicKey(sourceKeyPair.publicKey())
      .data(DidData.of("617474657374"))
      .uri(DidUri.of("6469645F6578616D706C65"))
      .didDocument(DidDocument.of("646F63"))
      .build();

    TransactionResult<DidSet> didSetResult = this.signSubmitAndWait(didSet, sourceKeyPair, DidSet.class);
    assertThat(didSetResult.metadata()).isNotEmpty();
    List<CreatedNode<MetaDidObject>> createdNodes = didSetResult.metadata().get().affectedNodes().stream()
      .filter(node -> node.ledgerEntryType().equals(MetaLedgerEntryType.DID))
      .filter(node -> CreatedNode.class.isAssignableFrom(node.getClass()))
      .filter(node -> MetaDidObject.class.isAssignableFrom(((CreatedNode<?>) node).newFields().getClass()))
      .map(node -> (CreatedNode<MetaDidObject>) node)
      .collect(Collectors.toList());

    assertThat(createdNodes.size()).isEqualTo(1);
    CreatedNode<MetaDidObject> createdNode = createdNodes.get(0);
    MetaDidObject createdDid = createdNode.newFields();
    assertThat(createdDid.didDocument()).isNotEmpty().isEqualTo(didSet.didDocument());
    assertThat(createdDid.uri()).isNotEmpty().isEqualTo(didSet.uri());
    assertThat(createdDid.data()).isNotEmpty().isEqualTo(didSet.data());
    assertThat(createdDid.account()).isNotEmpty().get().isEqualTo(sourceAccountInfo.accountData().account());
    assertThat(createdDid.flags()).isEqualTo(Flags.UNSET);

    List<DidObject> accountObjects = this.getValidatedAccountObjects(
      sourceKeyPair.publicKey().deriveAddress(),
      DidObject.class
    );
    assertThat(accountObjects.size()).isEqualTo(1);
    DidObject didFromAccountObjects = accountObjects.get(0);
    assertThat(didFromAccountObjects.account()).isEqualTo(sourceAccountInfo.accountData().account());
    assertThat(didFromAccountObjects.didDocument()).isNotEmpty().isEqualTo(createdDid.didDocument());
    assertThat(didFromAccountObjects.uri()).isNotEmpty().isEqualTo(createdDid.uri());
    assertThat(didFromAccountObjects.data()).isNotEmpty().isEqualTo(createdDid.data());
    assertThat(didFromAccountObjects.flags()).isEqualTo(createdDid.flags());

    LedgerEntryResult<DidObject> didFromLedgerEntryIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        createdNode.ledgerIndex(),
        DidObject.class,
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(didFromLedgerEntryIndex.node()).isEqualTo(didFromAccountObjects);

    LedgerEntryResult<DidObject> didFromLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.did(
        sourceKeyPair.publicKey().deriveAddress(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(didFromLedgerEntry.node()).isEqualTo(didFromAccountObjects);

    return TestDid.builder()
      .object(didFromLedgerEntry.node())
      .ownerKeyPair(sourceKeyPair)
      .build();
  }

  @Immutable
  @JsonSerialize(as = ImmutableTestDid.class)
  @JsonDeserialize(as = ImmutableTestDid.class)
  interface TestDid {

    /**
     * Construct a {@code TestDid} builder.
     *
     * @return An {@link ImmutableTestDid.Builder}.
     */
    static ImmutableTestDid.Builder builder() {
      return ImmutableTestDid.builder();
    }

    KeyPair ownerKeyPair();

    DidObject object();

  }
}
