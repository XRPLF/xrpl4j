package org.xrpl.xrpl4j.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClient;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.JsonRpcRequest;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.XrplMethods;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenerateTxMetadataFileIT {

  Logger logger = LoggerFactory.getLogger(GenerateTxMetadataFileIT.class);

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  /**
   * This is not a test. This method queries 100 ledgers at regular intervals between the first ledger
   * and the latest ledger and writes each transaction's metadata to the resources/tx_metadata_fixtures.json file. This
   * file can then be copied into the {@code xrpl-core}'s src/test/resources directory and used in
   * {@code TransactionMetadataTest}.
   *
   * <p>The test is commented out so that it does not run during CI, but can be uncommented and run to generate
   * new fixtures.
   *
   * @throws JsonRpcClientErrorException If something goes wrong while talking to rippled.
   * @throws IOException                 If writing to the file fails.
   */
  //  @Test
  void generateTxMetadataFixtures() throws JsonRpcClientErrorException, IOException {
    XrplClient xrplClient = new XrplClient(HttpUrl.get("https://s2-reporting.ripple.com:51234"));

    Range<UnsignedLong> completeLedgers = xrplClient.serverInformation().info().completeLedgers().get(0);
    UnsignedLong lowestLedger = completeLedgers.lowerEndpoint();
    UnsignedLong highestLedger = completeLedgers.upperEndpoint();

    UnsignedLong minus = highestLedger.minus(lowestLedger);
    UnsignedLong interval = minus.dividedBy(UnsignedLong.valueOf(100));

    LedgerIndex currentIndex = LedgerIndex.of(UnsignedInteger.valueOf(lowestLedger.longValue()));

    File resultFile = new File("src/test/resources/tx_metadata_fixtures.json");

    List<JsonNode> metaList = new ArrayList<>();
    int ledgerCount = 0;
    while (FluentCompareTo.is(currentIndex.unsignedIntegerValue())
      .lessThanOrEqualTo(UnsignedInteger.valueOf(highestLedger.longValue()))) {
      logger.info("Getting ledger {}", currentIndex.unsignedIntegerValue());
      try {
        JsonRpcClient rpcClient = JsonRpcClient.construct(HttpUrl.get("https://s2-reporting.ripple.com:51234"));
        JsonNode ledgerResult = rpcClient.postRpcRequest(
          JsonRpcRequest.builder()
            .method(XrplMethods.LEDGER)
            .addParams(
              LedgerRequestParams.builder()
                .ledgerSpecifier(LedgerSpecifier.of(currentIndex))
                .transactions(true)
                .build()
            )
            .build()
        );
        ledgerResult.get("result").get("ledger").get("transactions")
          .forEach(transaction -> metaList.add(transaction.get("metaData")));
      } catch (Exception e) {
        logger.error("Exception!", e);
      }

      objectMapper.writerWithDefaultPrettyPrinter().writeValue(resultFile, metaList);

      ledgerCount++;
      currentIndex = currentIndex.plus(UnsignedInteger.valueOf(interval.intValue()));
    }

    FileOutputStream fos = new FileOutputStream("src/test/resources/tx_metadata_fixtures.json.zip");
    ZipOutputStream zipOut = new ZipOutputStream(fos);

    FileInputStream fis = new FileInputStream(resultFile);
    ZipEntry zipEntry = new ZipEntry(resultFile.getName());
    zipOut.putNextEntry(zipEntry);

    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }

    zipOut.close();
    fis.close();
    fos.close();

    logger.info("Read metadata for {} ledgers", ledgerCount);
  }
}
