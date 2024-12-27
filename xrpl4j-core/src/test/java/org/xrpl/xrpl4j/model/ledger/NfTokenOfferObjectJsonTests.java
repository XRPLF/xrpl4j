package org.xrpl.xrpl4j.model.ledger;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.NfTokenOfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

class NfTokenOfferObjectJsonTests extends AbstractJsonTest{
    @Test
    void testFullJson() throws JSONException, JsonProcessingException {
        String json = "{\n" + //
            "\"Amount\": \"0\",\n" + //
            "\"Destination\": \"rPt4GkSQ2Zi2c8NPSJ6SeLGyernWPvWpf6\",\n" + //
            "\"Flags\": 1,\n" + //
            "\"LedgerEntryType\": \"NFTokenOffer\",\n" + //
            "\"NFTokenID\": \"0008001EFB09BCE5738AE4C7ECEAADD69DB80B16F0DAF7AE619C329B00232900\",\n" + //
            "\"NFTokenOfferNode\": \"0\",\n" + //
            "\"Owner\": \"rpQqJo7thmNWAJCDne6jp2WreZ9VQgrbAd\",\n" + //
            "\"OwnerNode\": \"0\",\n" + //
            "\"PreviousTxnID\": \"0B447AB1C8793CB5976E15EC888CA8C8CFF0C1A2865E6F21F3D257372A1E84C5\",\n" + //
            "\"PreviousTxnLgrSeq\": 2305005,\n" + //
            "\"index\": \"DD5BB396D4901E69CAD478A1CFDEFBC6154E58D9CEF211513376E3C2CE3CEF46\"\n" + //
        "}";

        NfTokenOfferObject nftOffer = NfTokenOfferObject.builder()
            .amount(XrpCurrencyAmount.of(UnsignedLong.valueOf("0")))
            .destination(Address.of("rPt4GkSQ2Zi2c8NPSJ6SeLGyernWPvWpf6"))
            .flags(NfTokenOfferFlags.of(1))
            .nfTokenId(NfTokenId.of("0008001EFB09BCE5738AE4C7ECEAADD69DB80B16F0DAF7AE619C329B00232900"))
            .offerNode("1")
            .owner(Address.of("rpQqJo7thmNWAJCDne6jp2WreZ9VQgrbAd"))
            .ownerNode("0")
            .previousTransactionId(Hash256.of("0B447AB1C8793CB5976E15EC888CA8C8CFF0C1A2865E6F21F3D257372A1E84C5"))
            .previousTransactionLedgerSequence(UnsignedInteger.valueOf("2305005"))
            .index(Hash256.of("DD5BB396D4901E69CAD478A1CFDEFBC6154E58D9CEF211513376E3C2CE3CEF46"))
            .build();

        assertCanSerializeAndDeserialize(nftOffer, json);
    }
}
