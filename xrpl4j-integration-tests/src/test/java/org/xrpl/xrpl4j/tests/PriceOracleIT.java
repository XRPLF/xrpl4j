package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.Finality;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.OracleLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.oracle.GetAggregatePriceRequestParams;
import org.xrpl.xrpl4j.model.client.oracle.GetAggregatePriceResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.OracleObject;
import org.xrpl.xrpl4j.model.transactions.AssetPrice;
import org.xrpl.xrpl4j.model.transactions.OracleDelete;
import org.xrpl.xrpl4j.model.transactions.OracleDocumentId;
import org.xrpl.xrpl4j.model.transactions.OracleProvider;
import org.xrpl.xrpl4j.model.transactions.OracleSet;
import org.xrpl.xrpl4j.model.transactions.OracleUri;
import org.xrpl.xrpl4j.model.transactions.PriceData;
import org.xrpl.xrpl4j.model.transactions.PriceDataWrapper;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@EnabledIf(value = "shouldRun", disabledReason = "PriceOracleIT only runs runs with local rippled nodes.")
public class PriceOracleIT extends AbstractIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(PriceOracleIT.class);

  /**
   * If any "real" testnet is being used (i.e., the environment specified is not a local one) then this test should not
   * be run.
   *
   * @return {@code true} if test/dev/clio networks are the execution environment; {@code false} otherwise.
   */
  private static boolean shouldRun() {
    return System.getProperty("useTestnet") == null &&
      System.getProperty("useDevnet") == null &&
      System.getProperty("useClioTestnet") == null;
  }

  /**
   * This test requires the Ledger Acceptor to be disabled, in order to tightly control advancement of ledgers. Because
   * of this, some of the tests do not execute when running against real networks (because controlling ledger
   * advancement is not possible).
   */
  @BeforeAll
  static void setupTest() {
    // Turn the LedgerAcceptor off
    LOGGER.info("########### STOPPING LEDGER ACCEPTOR #########");
    xrplEnvironment.stopLedgerAcceptor();
    LOGGER.info("########### LEDGER ACCEPTOR STOPPED #########");
  }

  /**
   * Because this test requires the Ledger Acceptor to be disabled, once the test completes, the Ledger Acceptor must be
   * enabled again so that follow-on tests execute as expected.
   */
  @AfterAll
  static void cleanupTest() {
    // Turn the LedgerAcceptor off
    LOGGER.info("########### STARTING LEDGER ACCEPTOR #########");
    xrplEnvironment.startLedgerAcceptor(POLL_INTERVAL);
    LOGGER.info("########### LEDGER ACCEPTOR STARTED #########");

  }

  private static final String xrpl4jCoin =
    Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

  @Test
  void createAndUpdateAndDeleteOracle() throws JsonRpcClientErrorException, JsonProcessingException {
    xrplEnvironment.acceptLedger(); // <-- Progress the ledger to ensure the above tx becomes Validated.
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    xrplEnvironment.acceptLedger(); // <-- Progress the ledger to ensure the above tx becomes Validated.

    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    FeeResult feeResult = xrplClient.fee();
    xrplEnvironment.acceptLedger(); // <-- Progress the ledger to ensure the above tx becomes Validated.

    OracleProvider provider = OracleProvider.of(BaseEncoding.base16().encode("DIA".getBytes()));
    OracleUri uri = OracleUri.of(BaseEncoding.base16().encode("https://example.com".getBytes()));
    String assetClass = BaseEncoding.base16().encode("currency".getBytes());
    PriceDataWrapper priceData1 = PriceDataWrapper.of(
      PriceData.builder()
        .baseAsset("XRP")
        .quoteAsset(xrpl4jCoin)
        .assetPrice(AssetPrice.of(UnsignedLong.ONE))
        .scale(UnsignedInteger.valueOf(10))
        .build()
    );
    PriceDataWrapper priceData2 = PriceDataWrapper.of(
      PriceData.builder()
        .baseAsset("XRP")
        .quoteAsset("EUR")
        .assetPrice(AssetPrice.of(UnsignedLong.ONE))
        .scale(UnsignedInteger.valueOf(10))
        .build()
    );

    UnsignedInteger lastUpdateTime = closeTimeHuman();

    OracleSet oracleSet = OracleSet.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .signingPublicKey(sourceKeyPair.publicKey())
      .lastLedgerSequence(accountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue())
      .oracleDocumentId(OracleDocumentId.of(UnsignedInteger.ONE))
      .provider(provider)
      .uri(uri)
      .lastUpdateTime(lastUpdateTime)
      .assetClass(assetClass)
      .addPriceDataSeries(priceData1, priceData2)
      .build();

    SingleSignedTransaction<OracleSet> signedOracleSet = signatureService.sign(sourceKeyPair.privateKey(), oracleSet);
    SubmitResult<OracleSet> oracleSetSubmitResult = xrplClient.submit(signedOracleSet);
    assertThat(oracleSetSubmitResult.engineResult()).isEqualTo("tesSUCCESS");
    xrplEnvironment.acceptLedger(); // <-- Progress the ledger to ensure the above tx becomes Validated.

    Finality finality = scanForFinality(
      signedOracleSet.hash(),
      accountInfo.ledgerIndexSafe(),
      oracleSet.lastLedgerSequence().orElseThrow(RuntimeException::new),
      oracleSet.sequence(),
      sourceKeyPair.publicKey().deriveAddress()
    );
    assertThat(finality.finalityStatus()).isEqualTo(FinalityStatus.VALIDATED_SUCCESS);

    LedgerEntryResult<OracleObject> ledgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.oracle(
        OracleLedgerEntryParams.builder()
          .oracleDocumentId(oracleSet.oracleDocumentId())
          .account(sourceKeyPair.publicKey().deriveAddress())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    OracleObject oracleObject = ledgerEntry.node();
    assertThat(oracleObject.owner()).isEqualTo(sourceKeyPair.publicKey().deriveAddress());
    assertThat(oracleObject.provider()).isEqualTo(provider);
    assertThat(oracleObject.assetClass()).isEqualTo(assetClass);
    assertThat(oracleObject.lastUpdateTime()).isEqualTo(lastUpdateTime);
    assertThat(oracleObject.uri()).isNotEmpty().get().isEqualTo(uri);
    assertThat(oracleObject.priceDataSeries()).containsExactlyInAnyOrder(priceData1, priceData2);

    PriceDataWrapper newPriceData = PriceDataWrapper.of(
      PriceData.builder()
        .baseAsset("XRP")
        .quoteAsset("USD")
        .assetPrice(AssetPrice.of(UnsignedLong.ONE))
        .scale(UnsignedInteger.valueOf(10))
        .build()
    );
    PriceDataWrapper updatedPriceData = PriceDataWrapper.of(
      PriceData.builder().from(priceData2.priceData())
        .assetPrice(AssetPrice.of(UnsignedLong.valueOf(1000)))
        .build()
    );
    UnsignedInteger moreRecentLastUpdateTime = closeTimeHuman();

    OracleSet oracleUpdate = OracleSet.builder().from(oracleSet)
      .lastLedgerSequence(ledgerEntry.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue())
      .lastUpdateTime(moreRecentLastUpdateTime)
      .sequence(oracleSet.sequence().plus(UnsignedInteger.ONE))
      .priceDataSeries(Lists.newArrayList(
        // New asset pair should get added
        newPriceData,
        // Same asset pair without assetPrice should delete
        PriceDataWrapper.of(
          PriceData.builder().from(priceData1.priceData())
            .scale(Optional.empty())
            .assetPrice(Optional.empty())
            .build()
        ),
        // Updating assetPrice should update an existing price data entry.
        updatedPriceData
      ))
      .build();

    SingleSignedTransaction<OracleSet> signedOracleUpdate = signatureService.sign(
      sourceKeyPair.privateKey(), oracleUpdate
    );
    SubmitResult<OracleSet> oracleUpdateSubmitResult = xrplClient.submit(signedOracleUpdate);
    assertThat(oracleUpdateSubmitResult.engineResult()).isEqualTo("tesSUCCESS");
    xrplEnvironment.acceptLedger(); // <-- Progress the ledger to ensure the above tx becomes Validated.

    Finality updateFinality = scanForFinality(
      signedOracleUpdate.hash(),
      accountInfo.ledgerIndexSafe(),
      oracleUpdate.lastLedgerSequence().orElseThrow(RuntimeException::new),
      oracleUpdate.sequence(),
      sourceKeyPair.publicKey().deriveAddress()
    );
    assertThat(updateFinality.finalityStatus()).isEqualTo(FinalityStatus.VALIDATED_SUCCESS);

    ledgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.oracle(
        OracleLedgerEntryParams.builder()
          .oracleDocumentId(oracleSet.oracleDocumentId())
          .account(sourceKeyPair.publicKey().deriveAddress())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    oracleObject = ledgerEntry.node();
    assertThat(oracleObject.owner()).isEqualTo(sourceKeyPair.publicKey().deriveAddress());
    assertThat(oracleObject.provider()).isEqualTo(provider);
    assertThat(oracleObject.assetClass()).isEqualTo(assetClass);
    assertThat(oracleObject.lastUpdateTime()).isEqualTo(moreRecentLastUpdateTime);
    assertThat(oracleObject.uri()).isNotEmpty().get().isEqualTo(uri);
    assertThat(oracleObject.priceDataSeries()).containsExactlyInAnyOrder(newPriceData, updatedPriceData);

    OracleDelete oracleDelete = OracleDelete.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(oracleSet.fee())
      .sequence(oracleUpdate.sequence().plus(UnsignedInteger.ONE))
      .lastLedgerSequence(ledgerEntry.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue())
      .signingPublicKey(sourceKeyPair.publicKey())
      .oracleDocumentId(oracleSet.oracleDocumentId())
      .build();
    SingleSignedTransaction<OracleDelete> signedOracleDelete = signatureService.sign(
      sourceKeyPair.privateKey(), oracleDelete
    );
    SubmitResult<OracleDelete> oracleDeleteSubmitResult = xrplClient.submit(signedOracleDelete);
    assertThat(oracleDeleteSubmitResult.engineResult()).isEqualTo("tesSUCCESS");
    xrplEnvironment.acceptLedger(); // <-- Progress the ledger to ensure the above tx becomes Validated.

    Finality deleteFinality = scanForFinality(
      signedOracleDelete.hash(),
      accountInfo.ledgerIndexSafe(),
      oracleDelete.lastLedgerSequence().orElseThrow(RuntimeException::new),
      oracleDelete.sequence(),
      sourceKeyPair.publicKey().deriveAddress()
    );
    assertThat(deleteFinality.finalityStatus()).isEqualTo(FinalityStatus.VALIDATED_SUCCESS);

    assertThatThrownBy(() -> xrplClient.ledgerEntry(
      LedgerEntryRequestParams.oracle(
        OracleLedgerEntryParams.builder()
          .oracleDocumentId(oracleSet.oracleDocumentId())
          .account(sourceKeyPair.publicKey().deriveAddress())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    )).isInstanceOf(JsonRpcClientErrorException.class).hasMessage("entryNotFound (n/a)");
  }

  @Test
  void createTwoOraclesAndGetAggregatePrice() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    xrplEnvironment.acceptLedger(); // <-- Progress the ledger to ensure the above tx becomes Validated.

    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    FeeResult feeResult = xrplClient.fee();
    OracleProvider provider = OracleProvider.of(BaseEncoding.base16().encode("DIA".getBytes()));
    OracleUri uri = OracleUri.of(BaseEncoding.base16().encode("https://example.com".getBytes()));
    String assetClass = BaseEncoding.base16().encode("currency".getBytes());
    PriceDataWrapper priceData1 = PriceDataWrapper.of(
      PriceData.builder()
        .baseAsset("XRP")
        .quoteAsset("EUR")
        .assetPrice(AssetPrice.of(UnsignedLong.ONE))
        .build()
    );
    UnsignedInteger lastUpdateTime = closeTimeHuman();

    OracleSet oracleSet = OracleSet.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .signingPublicKey(sourceKeyPair.publicKey())
      .lastLedgerSequence(accountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue())
      .oracleDocumentId(OracleDocumentId.of(UnsignedInteger.ONE))
      .provider(provider)
      .uri(uri)
      .lastUpdateTime(lastUpdateTime)
      .assetClass(assetClass)
      .addPriceDataSeries(priceData1)
      .build();

    SingleSignedTransaction<OracleSet> signedOracleSet = signatureService.sign(sourceKeyPair.privateKey(), oracleSet);
    SubmitResult<OracleSet> oracleSetSubmitResult = xrplClient.submit(signedOracleSet);
    assertThat(oracleSetSubmitResult.engineResult()).isEqualTo("tesSUCCESS");
    xrplEnvironment.acceptLedger(); // <-- Progress the ledger to ensure the above tx becomes Validated.

    Finality finality = scanForFinality(
      signedOracleSet.hash(),
      accountInfo.ledgerIndexSafe(),
      oracleSet.lastLedgerSequence().orElseThrow(RuntimeException::new),
      oracleSet.sequence(),
      sourceKeyPair.publicKey().deriveAddress()
    );
    assertThat(finality.finalityStatus()).isEqualTo(FinalityStatus.VALIDATED_SUCCESS);

    PriceDataWrapper priceData2 = PriceDataWrapper.of(
      PriceData.builder()
        .baseAsset("XRP")
        .quoteAsset("EUR")
        .assetPrice(AssetPrice.of(UnsignedLong.valueOf(2)))
        .build()
    );

    UnsignedInteger lastUpdateTime2 = closeTimeHuman();

    OracleSet oracleSet2 = OracleSet.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(sourceKeyPair.publicKey())
      .lastLedgerSequence(accountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue())
      .oracleDocumentId(OracleDocumentId.of(UnsignedInteger.valueOf(2)))
      .provider(provider)
      .uri(uri)
      .lastUpdateTime(lastUpdateTime2)
      .assetClass(assetClass)
      .addPriceDataSeries(priceData2)
      .build();

    SingleSignedTransaction<OracleSet> signedOracleSet2 = signatureService.sign(sourceKeyPair.privateKey(), oracleSet2);
    SubmitResult<OracleSet> oracleSetSubmitResult2 = xrplClient.submit(signedOracleSet2);
    assertThat(oracleSetSubmitResult2.engineResult()).isEqualTo("tesSUCCESS");
    xrplEnvironment.acceptLedger(); // <-- Progress the ledger to ensure the above tx becomes Validated.

    Finality finality2 = scanForFinality(
      signedOracleSet2.hash(),
      accountInfo.ledgerIndexSafe(),
      oracleSet2.lastLedgerSequence().orElseThrow(RuntimeException::new),
      oracleSet2.sequence(),
      sourceKeyPair.publicKey().deriveAddress()
    );
    assertThat(finality2.finalityStatus()).isEqualTo(FinalityStatus.VALIDATED_SUCCESS);

    GetAggregatePriceResult aggregatePrice = xrplClient.getAggregatePrice(
      GetAggregatePriceRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .baseAsset("XRP")
        .quoteAsset("EUR")
        .trim(UnsignedInteger.ONE)
        .addOracles(
          OracleLedgerEntryParams.builder()
            .account(sourceKeyPair.publicKey().deriveAddress())
            .oracleDocumentId(oracleSet.oracleDocumentId())
            .build(),
          OracleLedgerEntryParams.builder()
            .account(sourceKeyPair.publicKey().deriveAddress())
            .oracleDocumentId(oracleSet2.oracleDocumentId())
            .build()
        )
        .build()
    );
    assertThat(aggregatePrice.median()).isEqualTo(BigDecimal.valueOf(1.5));
    assertThat(aggregatePrice.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(aggregatePrice.entireSet().mean()).isEqualTo(BigDecimal.valueOf(1.5));
    assertThat(aggregatePrice.entireSet().size()).isEqualTo(UnsignedLong.valueOf(2));
    assertThat(aggregatePrice.trimmedSet()).isNotEmpty().get().isEqualTo(aggregatePrice.entireSet());
  }

  /**
   * Get the ledger's view of clock-time by inspecting the `close_time_human` property on the most recently validated
   * ledger (i.e., The time this ledger was closed, in human-readable format. Always uses the UTC time zone).
   *
   * <p>This value is used instead of the unix-time returned by the JVM because these two versions of time will diverge
   * when ledgers are closed very quickly, as is the case with the default ledger acceptor polling interval (see
   * {@link AbstractIT#POLL_INTERVAL}).
   *
   * @return An {@link UnsignedInteger} representing the seconds from the unix epoch of 1970-01-01T00:00:00Z.
   *
   * @see "https://xrpl.org/docs/references/http-websocket-apis/public-api-methods/ledger-methods/ledger"
   */
  private UnsignedInteger closeTimeHuman() throws JsonRpcClientErrorException {
    Objects.requireNonNull(xrplClient);

    final long closeTimeHuman = xrplClient.ledger(
        LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.VALIDATED).build()
      )
      .ledger().closeTimeHuman()
      .orElseThrow(() -> new RuntimeException("closeTimeHuman must exist"))
      .toInstant()
      .getEpochSecond();

    return UnsignedInteger.valueOf(closeTimeHuman);
  }


}
