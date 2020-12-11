package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class Xrpl4jModule extends SimpleModule {

  private static final String NAME = "Xrpl4jModule";

  public Xrpl4jModule() {
    super(
        NAME,
        new Version(
            1,
            0,
            0,
            null,
            "org.xrpl.xrpl4j",
            "xrpl4j"
        )
    );

    addSerializer(Address.class, new AddressSerializer());
    addDeserializer(Address.class, new AddressDeserializer());

    addSerializer(Hash256.class, new Hash256Serializer());
    addDeserializer(Hash256.class, new Hash256Deserializer());

    addSerializer(XrpCurrencyAmount.class, new XrpCurrencyAmountSerializer());
    addDeserializer(XrpCurrencyAmount.class, new XrpCurrencyAmountDeserializer());

    addDeserializer(CurrencyAmount.class, new CurrencyAmountDeserializer());

    addSerializer(LedgerIndex.class, new LedgerIndexSerializer());
    addDeserializer(LedgerIndex.class, new LedgerIndexDeserializer());

    addDeserializer(Transaction.class, new TransactionDeserializer());
  }
}
