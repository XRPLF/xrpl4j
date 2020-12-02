package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags.PaymentFlags;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import com.ripple.xrpl4j.model.transactions.PathStep;
import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.junit.Test;

// FIXME: These tests should probably be replaced with a parameterized test that loads in payment json examples from
//  a file.  Will do this after merging with Neil's initial codec pass.
public class PaymentFlagsJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    Payment payment = Payment.builder()
        .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
        .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
        .amount(XrpCurrencyAmount.ofDrops(25000000))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .flags(PaymentFlags.builder().fullyCanonicalSig(false).build())
        .sequence(UnsignedInteger.valueOf(2))
        .build();

    String json = "{\n" +
        "                \"Account\": \"r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb\",\n" +
        "                \"Destination\": \"r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C\",\n" +
        "                \"TransactionType\": \"Payment\",\n" +
        "                \"Amount\": \"25000000\",\n" +
        "                \"Fee\": \"10\",\n" +
        "                \"Flags\": 0,\n" +
        "                \"Sequence\": 2\n" +
        "            }";

    assertCanSerializeAndDeserialize(payment, json);
  }

  @Test
  public void testJsonWithFlags() throws JsonProcessingException, JSONException {

    Payment payment = Payment.builder()
        .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
        .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
        .amount(XrpCurrencyAmount.ofDrops(25000000))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .build();

    String json = "{\n" +
        "                \"Account\": \"r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb\",\n" +
        "                \"Destination\": \"r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C\",\n" +
        "                \"TransactionType\": \"Payment\",\n" +
        "                \"Amount\": \"25000000\",\n" +
        "                \"Fee\": \"10\",\n" +
        "                \"Flags\": 2147483648,\n" +
        "                \"Sequence\": 2\n" +
        "            }";


    assertCanSerializeAndDeserialize(payment, json);
  }

  @Test
  public void testComplicatedJson() throws JsonProcessingException, JSONException {
    Payment payment = Payment.builder()
        .account(Address.of("rHXUjUtk5eiPFYpg27izxHeZ1t4x835Ecn"))
        .destination(Address.of("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK"))
        .amount(IssuedCurrencyAmount.builder()
            .currency("CNY")
            .value("5000")
            .issuer(Address.of("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK"))
            .build()
        )
        .fee(XrpCurrencyAmount.ofDrops(12))
        .sendMax(IssuedCurrencyAmount.builder()
            .currency("CNY")
            .value("5050")
            .issuer(Address.of("rHXUjUtk5eiPFYpg27izxHeZ1t4x835Ecn"))
            .build()
        )
        .flags(PaymentFlags.builder().fullyCanonicalSig(false).build())
        .sequence(UnsignedInteger.valueOf(6))
        .addPaths(Lists.newArrayList(
            PathStep.builder()
                .account(Address.of("razqQKzJRdB4UxFPWf5NEpEG3WMkmwgcXA"))
                .build()
        ))
        .destinationTag(UnsignedInteger.valueOf(736049272))
        .build();

    String json = "{\n" +
        "                \"Account\": \"rHXUjUtk5eiPFYpg27izxHeZ1t4x835Ecn\",\n" +
        "                \"Destination\": \"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\",\n" +
        "                \"TransactionType\": \"Payment\",\n" +
        "                \"Amount\": {\n" +
        "                    \"currency\": \"CNY\",\n" +
        "                    \"value\": \"5000\",\n" +
        "                    \"issuer\": \"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\"\n" +
        "                },\n" +
        "                \"Fee\": \"12\",\n" +
        "                \"SendMax\": {\n" +
        "                    \"currency\": \"CNY\",\n" +
        "                    \"value\": \"5050\",\n" +
        "                    \"issuer\": \"rHXUjUtk5eiPFYpg27izxHeZ1t4x835Ecn\"\n" +
        "                },\n" +
        "                \"Flags\": 0,\n" +
        "                \"Sequence\": 6,\n" +
        "                \"Paths\": [[{\n" +
        "                    \"account\": \"razqQKzJRdB4UxFPWf5NEpEG3WMkmwgcXA\"\n" +
        "                }]],\n" +
        "                \"DestinationTag\": 736049272\n" +
        "            }";

    assertCanSerializeAndDeserialize(payment, json);
  }
}
