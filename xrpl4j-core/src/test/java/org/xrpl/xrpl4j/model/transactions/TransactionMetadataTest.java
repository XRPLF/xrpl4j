package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.NfTokenOfferFlags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.metadata.AffectedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.CreatedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.DeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableDeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableMetaNfTokenOfferObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaAccountRootObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaAmmObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaCheckObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaDepositPreAuthObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaEscrowObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaNfTokenOfferObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaNfTokenPageObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaOfferObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaPayChannelObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaRippleStateObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaSignerListObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaTicketObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaUnknownObject;
import org.xrpl.xrpl4j.model.transactions.metadata.ModifiedNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class TransactionMetadataTest {

  Logger logger = LoggerFactory.getLogger(TransactionMetadataTest.class);

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void deserializeMetadataFromXrplOrg() {
    String json = "{\n" +
      "  \"AffectedNodes\": [\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"r9ZoLsJHzMMJLpvsViWQ4Jgx17N8cz1997\",\n" +
      "          \"Balance\": \"77349986\",\n" +
      "          \"Flags\": 0,\n" +
      "          \"OwnerCount\": 2,\n" +
      "          \"Sequence\": 9\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"AccountRoot\",\n" +
      "        \"LedgerIndex\": \"1E7E658C2D3DF91EFAE5A12573284AD6F526B8F64DD12F013C6F889EF45BEA97\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"OwnerCount\": 3\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"55C11248ACEFC2EFD59755BF88867783AC18EA078517108F942069C2FBE4CF5C\",\n" +
      "        \"PreviousTxnLgrSeq\": 35707468\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"2298.927882138068\"\n" +
      "          },\n" +
      "          \"Flags\": 1114112,\n" +
      "          \"HighLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rLEsXccBGNR3UPuPu2hUXPjziKC3qKSBun\",\n" +
      "            \"value\": \"0\"\n" +
      "          },\n" +
      "          \"HighNode\": \"000000000000006B\",\n" +
      "          \"LowLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rpvvAvaZ7TXHkNLM8UJwCTU6yBU2jDTJ1P\",\n" +
      "            \"value\": \"1000000000\"\n" +
      "          },\n" +
      "          \"LowNode\": \"0000000000000007\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"RippleState\",\n" +
      "        \"LedgerIndex\": \"220DDA7164F3F41F3C5223FA3125D4CD368EBB4FB954B5FBFFB6D1EA6DACDD5E\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"2297.927882138068\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"1DB2F9C67C3F42F7B8AB02BA2264254A78A201EC8A9974A1CACEFD51545B1263\",\n" +
      "        \"PreviousTxnLgrSeq\": 43081739\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"33403.80553244443\"\n" +
      "          },\n" +
      "          \"Flags\": 1114112,\n" +
      "          \"HighLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq\",\n" +
      "            \"value\": \"0\"\n" +
      "          },\n" +
      "          \"HighNode\": \"0000000000001A40\",\n" +
      "          \"LowLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rd5Sx93pCMgfxwBuofjen2csoFYmY8VrT\",\n" +
      "            \"value\": \"1000000000\"\n" +
      "          },\n" +
      "          \"LowNode\": \"0000000000000000\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"RippleState\",\n" +
      "        \"LedgerIndex\": \"38569918AF54B520463CFDDD00EB5ADD8768039BD94E61A5E25C387EA4FDC9A3\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"33402.80752845242\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"38A0E82ADC2DA6C6D59929B73E9812CD1E1384E452FD23D0717EA0037E2FC9E3\",\n" +
      "        \"PreviousTxnLgrSeq\": 43251694\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"rBndiPPKs9k5rjBb7HsEiqXKrz8AfUnqWq\",\n" +
      "          \"BookDirectory\": \"4627DFFCFF8B5A265EDBD8AE8C14A52325DBFEDAF4F5C32E5B09B13AC59DBA5E\",\n" +
      "          \"BookNode\": \"0000000000000000\",\n" +
      "          \"Flags\": 0,\n" +
      "          \"OwnerNode\": \"0000000000000000\",\n" +
      "          \"Sequence\": 407556,\n" +
      "          \"TakerGets\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "            \"value\": \"75.1379833998197\"\n" +
      "          },\n" +
      "          \"TakerPays\": \"204986996\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"Offer\",\n" +
      "        \"LedgerIndex\": \"557BDD35E40EAFFE0AC98108A0F4AC4BB812A168CFD5B4E35475F42A60ABD9C8\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"TakerGets\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "            \"value\": \"76.1399833998197\"\n" +
      "          },\n" +
      "          \"TakerPays\": \"207720593\"\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"961C575073788979815F103D065CEE449D2EA6EFE8FC8C33C26EC08586925D90\",\n" +
      "        \"PreviousTxnLgrSeq\": 43251680\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"r9KG7Du7aFmABzMvDnwuvPaEoMu4Eurwok\",\n" +
      "          \"Balance\": \"8080207629\",\n" +
      "          \"Flags\": 0,\n" +
      "          \"OwnerCount\": 6,\n" +
      "          \"Sequence\": 1578765\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"AccountRoot\",\n" +
      "        \"LedgerIndex\": \"5A667CB5FBAB4143EDEFBD6EDDD4B6D19C905209C8EE16486D5D7CD6CB083E78\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": \"8080152531\",\n" +
      "          \"Sequence\": 1578764\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"E3CDFD288620871455634DC1E56439136AACA1DDBCE987BE12F97486AB477375\",\n" +
      "        \"PreviousTxnLgrSeq\": 43251694\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"DeletedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"r9ZoLsJHzMMJLpvsViWQ4Jgx17N8cz1997\",\n" +
      "          \"BookDirectory\": \"A6D5D1C1CC92D56FDDFD4434FB10BD31F63EB991DA3C756653071AFD498D0000\",\n" +
      "          \"BookNode\": \"0000000000000000\",\n" +
      "          \"Flags\": 0,\n" +
      "          \"OwnerNode\": \"0000000000000000\",\n" +
      "          \"PreviousTxnID\": \"DB028A461E98B0398CAD65F2871B381A6D0B9A21662CA5B033438D83C518C0F2\",\n" +
      "          \"PreviousTxnLgrSeq\": 35686129,\n" +
      "          \"Sequence\": 7,\n" +
      "          \"TakerGets\": {\n" +
      "            \"currency\": \"EUR\",\n" +
      "            \"issuer\": \"rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq\",\n" +
      "            \"value\": \"2.5\"\n" +
      "          },\n" +
      "          \"TakerPays\": {\n" +
      "            \"currency\": \"ETH\",\n" +
      "            \"issuer\": \"rcA8X3TVMST1n3CJeAdGk1RdRCHii7N2h\",\n" +
      "            \"value\": \"0.05\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"Offer\",\n" +
      "        \"LedgerIndex\": \"6AA7E5121FEB456F0A899E3D6F25D62ABB408BB67B91C9270E13714401ED72B5\"\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"rd5Sx93pCMgfxwBuofjen2csoFYmY8VrT\",\n" +
      "          \"Balance\": \"8251028196\",\n" +
      "          \"Flags\": 0,\n" +
      "          \"OwnerCount\": 4,\n" +
      "          \"Sequence\": 274\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"AccountRoot\",\n" +
      "        \"LedgerIndex\": \"6F830A1B38F827CD4BEC946A40F1E2DF726FC22AFC3918FD621567AF17F49F3A\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": \"8253816902\"\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"38A0E82ADC2DA6C6D59929B73E9812CD1E1384E452FD23D0717EA0037E2FC9E3\",\n" +
      "        \"PreviousTxnLgrSeq\": 43251694\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"rd5Sx93pCMgfxwBuofjen2csoFYmY8VrT\",\n" +
      "          \"BookDirectory\": \"79C54A4EBD69AB2EADCE313042F36092BE432423CC6A4F784E0CB6D74F25A336\",\n" +
      "          \"BookNode\": \"0000000000000000\",\n" +
      "          \"Flags\": 0,\n" +
      "          \"OwnerNode\": \"0000000000000000\",\n" +
      "          \"Sequence\": 273,\n" +
      "          \"TakerGets\": \"8246341599\",\n" +
      "          \"TakerPays\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq\",\n" +
      "            \"value\": \"2951.147613535471\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"Offer\",\n" +
      "        \"LedgerIndex\": \"7FD1EAAE17B7D68AE640FFC56CECC3999B4F938EFFF6EA6887B6CC8BD9DBDC63\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"TakerGets\": \"8249130305\",\n" +
      "          \"TakerPays\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq\",\n" +
      "            \"value\": \"2952.145617527486\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"38A0E82ADC2DA6C6D59929B73E9812CD1E1384E452FD23D0717EA0037E2FC9E3\",\n" +
      "        \"PreviousTxnLgrSeq\": 43251694\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"-11.68225001668339\"\n" +
      "          },\n" +
      "          \"Flags\": 131072,\n" +
      "          \"HighLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"value\": \"5000\"\n" +
      "          },\n" +
      "          \"HighNode\": \"0000000000000000\",\n" +
      "          \"LowLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "            \"value\": \"0\"\n" +
      "          },\n" +
      "          \"LowNode\": \"000000000000004A\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"RippleState\",\n" +
      "        \"LedgerIndex\": \"826CF5BFD28F3934B518D0BDF3231259CBD3FD0946E3C3CA0C97D2C75D2D1A09\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"-10.68225001668339\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"28B271F7C27C1A267F32FFCD8B1795C5D3B1DC761AD705E3A480139AA8B61B09\",\n" +
      "        \"PreviousTxnLgrSeq\": 43237130\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"rBndiPPKs9k5rjBb7HsEiqXKrz8AfUnqWq\",\n" +
      "          \"Balance\": \"8276201534\",\n" +
      "          \"Flags\": 0,\n" +
      "          \"OwnerCount\": 5,\n" +
      "          \"Sequence\": 407558\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"AccountRoot\",\n" +
      "        \"LedgerIndex\": \"880C6FB7B9C0083211F950E4449AD45895C0EC1114B5112CE1320AC7275E3237\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": \"8273467937\"\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"CB4B54942F11510A47D2731C3260429093F24016B366CBF15D8EC4B705372F02\",\n" +
      "        \"PreviousTxnLgrSeq\": 43251683\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"-6557.745685633666\"\n" +
      "          },\n" +
      "          \"Flags\": 2228224,\n" +
      "          \"HighLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rBndiPPKs9k5rjBb7HsEiqXKrz8AfUnqWq\",\n" +
      "            \"value\": \"1000000000\"\n" +
      "          },\n" +
      "          \"HighNode\": \"0000000000000000\",\n" +
      "          \"LowLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "            \"value\": \"0\"\n" +
      "          },\n" +
      "          \"LowNode\": \"0000000000000512\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"RippleState\",\n" +
      "        \"LedgerIndex\": \"8A9FEE5192E334195314B5C162BC78F7452ADB14E06839D48943BAE05EE1967F\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"-6558.747685633666\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"961C575073788979815F103D065CEE449D2EA6EFE8FC8C33C26EC08586925D90\",\n" +
      "        \"PreviousTxnLgrSeq\": 43251680\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"GCB\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"9990651675.348776\"\n" +
      "          },\n" +
      "          \"Flags\": 3211264,\n" +
      "          \"HighLimit\": {\n" +
      "            \"currency\": \"GCB\",\n" +
      "            \"issuer\": \"rHaans8PtgwbacHvXAL3u6TG28gTAtCwr8\",\n" +
      "            \"value\": \"0\"\n" +
      "          },\n" +
      "          \"HighNode\": \"0000000000000000\",\n" +
      "          \"LowLimit\": {\n" +
      "            \"currency\": \"GCB\",\n" +
      "            \"issuer\": \"r9KG7Du7aFmABzMvDnwuvPaEoMu4Eurwok\",\n" +
      "            \"value\": \"10000000000\"\n" +
      "          },\n" +
      "          \"LowNode\": \"0000000000000000\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"RippleState\",\n" +
      "        \"LedgerIndex\": \"A2B41EE7818A5756B6A2276BDBB3CE0ED3A3B350787FD6B76E5EA1354A8F20D2\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"GCB\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"9990651678.137482\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"961C575073788979815F103D065CEE449D2EA6EFE8FC8C33C26EC08586925D90\",\n" +
      "        \"PreviousTxnLgrSeq\": 43251680\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"0\"\n" +
      "          },\n" +
      "          \"Flags\": 65536,\n" +
      "          \"HighLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rLEsXccBGNR3UPuPu2hUXPjziKC3qKSBun\",\n" +
      "            \"value\": \"0\"\n" +
      "          },\n" +
      "          \"HighNode\": \"0000000000000002\",\n" +
      "          \"LowLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"value\": \"1\"\n" +
      "          },\n" +
      "          \"LowNode\": \"0000000000000000\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"RippleState\",\n" +
      "        \"LedgerIndex\": \"C493ABA2619D0FC6355BA862BC8312DF8266FBE76AFBA9636E857F7EAC874A99\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"1\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"28B271F7C27C1A267F32FFCD8B1795C5D3B1DC761AD705E3A480139AA8B61B09\",\n" +
      "        \"PreviousTxnLgrSeq\": 43237130\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"r9KG7Du7aFmABzMvDnwuvPaEoMu4Eurwok\",\n" +
      "          \"BookDirectory\": \"E6E8A9842EA2ED1FD5D0599343692CE1EBF977AEA751B7DC5B038D7EA4C68000\",\n" +
      "          \"BookNode\": \"0000000000000000\",\n" +
      "          \"Flags\": 65536,\n" +
      "          \"OwnerNode\": \"0000000000000000\",\n" +
      "          \"Sequence\": 39018,\n" +
      "          \"TakerGets\": {\n" +
      "            \"currency\": \"GCB\",\n" +
      "            \"issuer\": \"rHaans8PtgwbacHvXAL3u6TG28gTAtCwr8\",\n" +
      "            \"value\": \"9990651675.348776\"\n" +
      "          },\n" +
      "          \"TakerPays\": \"9990651675348776\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"Offer\",\n" +
      "        \"LedgerIndex\": \"C939B9B2C5803DD6D89B792E72470F79CBE9F9E999691789E0B68C3808BDDD8E\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"TakerGets\": {\n" +
      "            \"currency\": \"GCB\",\n" +
      "            \"issuer\": \"rHaans8PtgwbacHvXAL3u6TG28gTAtCwr8\",\n" +
      "            \"value\": \"9990651678.137482\"\n" +
      "          },\n" +
      "          \"TakerPays\": \"9990651678137482\"\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"961C575073788979815F103D065CEE449D2EA6EFE8FC8C33C26EC08586925D90\",\n" +
      "        \"PreviousTxnLgrSeq\": 43251680\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"2963.413395452545\"\n" +
      "          },\n" +
      "          \"Flags\": 65536,\n" +
      "          \"HighLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq\",\n" +
      "            \"value\": \"0\"\n" +
      "          },\n" +
      "          \"HighNode\": \"0000000000001A97\",\n" +
      "          \"LowLimit\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rpvvAvaZ7TXHkNLM8UJwCTU6yBU2jDTJ1P\",\n" +
      "            \"value\": \"0\"\n" +
      "          },\n" +
      "          \"LowNode\": \"0000000000000007\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"RippleState\",\n" +
      "        \"LedgerIndex\": \"E4D1FBD5CB72A1D3EE38C21F3BCB13E454FCB469CD01C1366E0008A031E6A7FC\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": {\n" +
      "            \"currency\": \"USD\",\n" +
      "            \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "            \"value\": \"2964.413395452545\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"1DB2F9C67C3F42F7B8AB02BA2264254A78A201EC8A9974A1CACEFD51545B1263\",\n" +
      "        \"PreviousTxnLgrSeq\": 43081739\n" +
      "      }\n" +
      "    }\n" +
      "  ],\n" +
      "  \"DeliveredAmount\": {\n" +
      "    \"currency\": \"GCB\",\n" +
      "    \"issuer\": \"rHaans8PtgwbacHvXAL3u6TG28gTAtCwr8\",\n" +
      "    \"value\": \"2.788706\"\n" +
      "  },\n" +
      "  \"TransactionIndex\": 38,\n" +
      "  \"TransactionResult\": \"tesSUCCESS\",\n" +
      "  \"delivered_amount\": {\n" +
      "    \"currency\": \"GCB\",\n" +
      "    \"issuer\": \"rHaans8PtgwbacHvXAL3u6TG28gTAtCwr8\",\n" +
      "    \"value\": \"2.788706\"\n" +
      "  }\n" +
      "}";

    assertThatNoException().isThrownBy(() -> objectMapper.readValue(json, TransactionMetadata.class));
  }

  @Test
  void testMetadataWithNfTokenOfferDeletedNode() throws JsonProcessingException {
    String json = "{\n" +
      "  \"AffectedNodes\": [\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Flags\": 0,\n" +
      "          \"IndexPrevious\": \"1\",\n" +
      "          \"Owner\": \"rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu\",\n" +
      "          \"RootIndex\": \"DB2B7599894EAE13B565B539990BEADBE6F2BED04A0ADCD89DC4BB68C09CF06C\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"DirectoryNode\",\n" +
      "        \"LedgerIndex\": \"0BF5AFC91640BE6B1F595949A57FA9D113D013FEB0C19DFFCFFC521A2D0590CD\"\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"DeletedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Amount\": {\n" +
      "            \"currency\": \"534F4C4F00000000000000000000000000000000\",\n" +
      "            \"issuer\": \"rHZwvHEs56GCmHupwjA4RY7oPA3EoAJWuN\",\n" +
      "            \"value\": \"0.4\"\n" +
      "          },\n" +
      "          \"Flags\": 1,\n" +
      "          \"NFTokenID\": \"00080BB86F12FFF50C3C44827709AA868A910613902F810FA11F9798000000FD\",\n" +
      "          \"NFTokenOfferNode\": \"0\",\n" +
      "          \"Owner\": \"rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu\",\n" +
      "          \"OwnerNode\": \"2\",\n" +
      "          \"PreviousTxnID\": \"78D3B7A4B07BFC1F5D7EBD9844B25209F3D5885F347EBA0868FEF2672A91F9DF\",\n" +
      "          \"PreviousTxnLgrSeq\": 39480038\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"NFTokenOffer\",\n" +
      "        \"LedgerIndex\": \"0F512CD1108EF19E3662FB0F830C803853DCBAE8B5FEF2A46E2A99ACCE1E8177\"\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Flags\": 2,\n" +
      "          \"NFTokenID\": \"00080BB86F12FFF50C3C44827709AA868A910613902F810FA11F9798000000FD\",\n" +
      "          \"RootIndex\": \"CF89EB7D051F954EB3FCE9115D5532BA2BF39F1311BB69F2633C002A87972B71\"\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"DirectoryNode\",\n" +
      "        \"LedgerIndex\": \"CF89EB7D051F954EB3FCE9115D5532BA2BF39F1311BB69F2633C002A87972B71\"\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu\",\n" +
      "          \"Balance\": \"953788095\",\n" +
      "          \"BurnedNFTokens\": 126,\n" +
      "          \"EmailHash\": \"1D1382344586ECFF844DACFF698C2EFB\",\n" +
      "          \"Flags\": 0,\n" +
      "          \"MintedNFTokens\": 254,\n" +
      "          \"OwnerCount\": 82,\n" +
      "          \"Sequence\": 35260003\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"AccountRoot\",\n" +
      "        \"LedgerIndex\": \"D6C4EE995A40D6A22172016806CE813DE1578B11158B4EAA5115C563B4DC4B29\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Balance\": \"953788294\",\n" +
      "          \"OwnerCount\": 83,\n" +
      "          \"Sequence\": 35260002\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"A39E1C58CC442D99123D6EA0BA3E25995BC6221D98F9A191FAC04FDDC583BF63\",\n" +
      "        \"PreviousTxnLgrSeq\": 39480040\n" +
      "      }\n" +
      "    }\n" +
      "  ],\n" +
      "  \"TransactionIndex\": 1,\n" +
      "  \"TransactionResult\": \"tesSUCCESS\"\n" +
      "}";

    TransactionMetadata transactionMetadata = objectMapper.readValue(json, TransactionMetadata.class);
    AffectedNode nftDeletedNode = transactionMetadata.affectedNodes().get(1);
    AffectedNode expectedNode = ImmutableDeletedNode.builder()
      .ledgerEntryType(MetaLedgerEntryType.NFTOKEN_OFFER)
      .ledgerIndex(Hash256.of("0F512CD1108EF19E3662FB0F830C803853DCBAE8B5FEF2A46E2A99ACCE1E8177"))
      .finalFields(
        ImmutableMetaNfTokenOfferObject.builder()
          .amount(
            IssuedCurrencyAmount.builder()
              .currency("534F4C4F00000000000000000000000000000000")
              .issuer(Address.of("rHZwvHEs56GCmHupwjA4RY7oPA3EoAJWuN"))
              .value("0.4")
              .build()
          )
          .flags(NfTokenOfferFlags.BUY_TOKEN)
          .nfTokenId(NfTokenId.of("00080BB86F12FFF50C3C44827709AA868A910613902F810FA11F9798000000FD"))
          .owner(Address.of("rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu"))
          .ownerNode("2")
          .previousTransactionId(Hash256.of("78D3B7A4B07BFC1F5D7EBD9844B25209F3D5885F347EBA0868FEF2672A91F9DF"))
          .previousTransactionLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(39480038)))
          .offerNode("0")
          .build()
      )
      .build();

    assertThat(nftDeletedNode).isEqualTo(expectedNode);
  }

  @Test
  void deserializeMetadataWithDelegateObject() throws JsonProcessingException {
    String json = "{\n" +
      "  \"AffectedNodes\": [\n" +
      "    {\n" +
      "      \"CreatedNode\": {\n" +
      "        \"LedgerEntryType\": \"Delegate\",\n" +
      "        \"LedgerIndex\": \"1E7E658C2D3DF91EFAE5A12573284AD6F526B8F64DD12F013C6F889EF45BEA97\",\n" +
      "        \"NewFields\": {\n" +
      "          \"Account\": \"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoXo\",\n" +
      "          \"Authorize\": \"rJ73aumLPTQQmy5wnGhvrogqf5DDhjuzc9\",\n" +
      "          \"Permissions\": [\n" +
      "            {\"Permission\": {\"PermissionValue\": \"Payment\"}},\n" +
      "            {\"Permission\": {\"PermissionValue\": \"TrustSet\"}}\n" +
      "          ]\n" +
      "        }\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"ModifiedNode\": {\n" +
      "        \"LedgerEntryType\": \"Delegate\",\n" +
      "        \"LedgerIndex\": \"2F8E658C2D3DF91EFAE5A12573284AD6F526B8F64DD12F013C6F889EF45BEA98\",\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoXo\",\n" +
      "          \"Authorize\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\",\n" +
      "          \"Permissions\": [\n" +
      "            {\"Permission\": {\"PermissionValue\": \"OfferCreate\"}}\n" +
      "          ]\n" +
      "        },\n" +
      "        \"PreviousFields\": {\n" +
      "          \"Permissions\": [\n" +
      "            {\"Permission\": {\"PermissionValue\": \"Payment\"}}\n" +
      "          ]\n" +
      "        }\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"DeletedNode\": {\n" +
      "        \"LedgerEntryType\": \"Delegate\",\n" +
      "        \"LedgerIndex\": \"3A9F758D3E4EF92FFBF6B13684395BE7G637C9G75EE23G124D7G9A0FG56CFC99\",\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoXo\",\n" +
      "          \"Authorize\": \"rLHzPsX6oXkzU9rFSnR8KBvdvreGKoZST3\",\n" +
      "          \"Permissions\": []\n" +
      "        }\n" +
      "      }\n" +
      "    }\n" +
      "  ],\n" +
      "  \"TransactionIndex\": 1,\n" +
      "  \"TransactionResult\": \"tesSUCCESS\"\n" +
      "}";

    TransactionMetadata transactionMetadata = objectMapper.readValue(json, TransactionMetadata.class);

    // Verify we have 3 affected nodes
    assertThat(transactionMetadata.affectedNodes().size()).isEqualTo(3);

    // Verify CreatedNode<MetaDelegateObject>
    AffectedNode createdNode = transactionMetadata.affectedNodes().get(0);
    assertThat(createdNode).isInstanceOf(CreatedNode.class);
    assertThat(createdNode.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.DELEGATE);

    // Verify ModifiedNode<MetaDelegateObject>
    AffectedNode modifiedNode = transactionMetadata.affectedNodes().get(1);
    assertThat(modifiedNode).isInstanceOf(ModifiedNode.class);
    assertThat(modifiedNode.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.DELEGATE);

    // Verify DeletedNode<MetaDelegateObject>
    AffectedNode deletedNode = transactionMetadata.affectedNodes().get(2);
    assertThat(deletedNode).isInstanceOf(DeletedNode.class);
    assertThat(deletedNode.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.DELEGATE);
  }

  /**
   * Tests deserialization of transaction metadata contained in the tx_metadata_fixtures.json file. That file
   * is generated by {@code GenerateTxMetadataFileIT} by pulling 100 ledgers from mainnet and writing each
   * transactions' metadata to the file. However, not all types of transactions are represented by that data,
   * hence the need for {@link #deserializeManualFixtures()}
   *
   * @throws IOException If reading the file fails.
   */
  @Test
  void deserializeGeneratedMainnetFixtures() throws IOException {
    // Note that the actual file is a .zip to save space in Git. deserializeFixtures will unzip that file into
    // tx_metadata_fixtures.json
    deserializeFixtures("tx_metadata_fixtures.json");
  }

  /**
   * Not all transaction types are represented in the data generated into tx_metadata_fixtures.json, so
   * not all types of transaction metadata are tested by {@link #deserializeGeneratedMainnetFixtures()}.
   * This test uses transaction metadata generated by xrpl4j ITs on testnet to cover non-covered transaction
   * metadata types.
   *
   * @throws IOException If reading the file fails.
   */
  @Test
  void deserializeManualFixtures() throws IOException {
    // Note that the actual file is a .zip to save space in Git. deserializeFixtures will unzip that file into
    // tx_meta_manual_fixtures.json
    deserializeFixtures("tx_meta_manual_fixtures.json");
  }

  @Test
  void deserializeUnrecognizedNodeType() {
    String nodeType = "WeirdNode";
    String json = String.format("{\n" +
      "  \"AffectedNodes\": [\n" +
      "    {\n" +
      "      \"%s\": {\n" +
      "        \"FinalFields\": {\n" +
      "          \"Account\": \"r9ZoLsJHzMMJLpvsViWQ4Jgx17N8cz1997\",\n" +
      "          \"Balance\": \"77349986\",\n" +
      "          \"Flags\": 0,\n" +
      "          \"OwnerCount\": 2,\n" +
      "          \"Sequence\": 9\n" +
      "        },\n" +
      "        \"LedgerEntryType\": \"AccountRoot\",\n" +
      "        \"LedgerIndex\": \"1E7E658C2D3DF91EFAE5A12573284AD6F526B8F64DD12F013C6F889EF45BEA97\",\n" +
      "        \"PreviousFields\": {\n" +
      "          \"OwnerCount\": 3\n" +
      "        },\n" +
      "        \"PreviousTxnID\": \"55C11248ACEFC2EFD59755BF88867783AC18EA078517108F942069C2FBE4CF5C\",\n" +
      "        \"PreviousTxnLgrSeq\": 35707468\n" +
      "      }\n" +
      "    }\n" +
      "]}", nodeType);

    assertThatThrownBy(
      () -> objectMapper.readValue(json, TransactionMetadata.class)
    ).isInstanceOf(JsonMappingException.class)
      .hasMessageContaining("Unrecognized AffectedNode type " + nodeType);
  }

  private void deserializeFixtures(String fileName) throws IOException {
    unzipFile(fileName + ".zip");
    File jsonFile = new File("src/test/resources/" + fileName);
    List<JsonNode> metadatas = objectMapper.readValue(
      jsonFile,
      objectMapper.getTypeFactory().constructParametricType(List.class, JsonNode.class)
    );

    metadatas.forEach(meta -> {
      try {
        TransactionMetadata transactionMetadata = objectMapper.treeToValue(meta, TransactionMetadata.class);
        assertThat(transactionMetadata.transactionResult()).isEqualTo(meta.get("TransactionResult").asText());
        assertThat(transactionMetadata.transactionIndex().longValue()).isEqualTo(meta.get("TransactionIndex").asLong());
        assertThat(transactionMetadata.deliveredAmount()).isEqualTo(
          Optional.ofNullable(meta.get("delivered_amount"))
            .map(deliveredAmount -> {
              try {
                return objectMapper.treeToValue(deliveredAmount, CurrencyAmount.class);
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            })
        );

        assertThat(meta.get("AffectedNodes").size()).isEqualTo(transactionMetadata.affectedNodes().size());
        for (int i = 0; i < transactionMetadata.affectedNodes().size(); i++) {
          Map.Entry<String, JsonNode> node = meta.get("AffectedNodes").get(i).fields().next();
          if (node.getKey().equals("CreatedNode")) {
            assertThat(CreatedNode.class).isAssignableFrom(transactionMetadata.affectedNodes().get(i).getClass());
            if (node.getValue().get("NewFields") != null) {
              MetaLedgerObject newFields = ((CreatedNode<?>) transactionMetadata.affectedNodes().get(i)).newFields();
              assertThat(MetaLedgerEntryType.of(node.getValue().get("LedgerEntryType").asText()).ledgerObjectType())
                .isAssignableFrom(newFields.getClass());
            }
          } else if (node.getKey().equals("ModifiedNode")) {
            assertThat(ModifiedNode.class).isAssignableFrom(transactionMetadata.affectedNodes().get(i).getClass());
            if (node.getValue().get("PreviousFields") != null) {
              Optional<?> previousFields = ((ModifiedNode<?>) transactionMetadata.affectedNodes().get(i))
                .previousFields();
              assertThat(previousFields).isPresent();
              assertThat(MetaLedgerEntryType.of(node.getValue().get("LedgerEntryType").asText()).ledgerObjectType())
                .isAssignableFrom(previousFields.get().getClass());
            }

            if (node.getValue().get("FinalFields") != null) {
              Optional<?> finalFields = ((ModifiedNode<?>) transactionMetadata.affectedNodes().get(i)).finalFields();
              assertThat(finalFields).isPresent();
              assertThat(MetaLedgerEntryType.of(node.getValue().get("LedgerEntryType").asText()).ledgerObjectType())
                .isAssignableFrom(finalFields.get().getClass());
            }
          } else if (node.getKey().equals("DeletedNode")) {
            assertThat(DeletedNode.class).isAssignableFrom(transactionMetadata.affectedNodes().get(i).getClass());
            if (node.getValue().get("FinalFields") != null) {
              MetaLedgerObject finalFields = ((DeletedNode<?>) transactionMetadata.affectedNodes().get(i))
                .finalFields();
              assertThat(MetaLedgerEntryType.of(node.getValue().get("LedgerEntryType").asText()).ledgerObjectType())
                .isAssignableFrom(finalFields.getClass());
            }
          }
        }
      } catch (JsonProcessingException e) {
        logger.error("Failed to deserialize tx metadata {}", meta.toString());
        throw new RuntimeException(e);
      }
    });

    jsonFile.delete();
  }

  private void unzipFile(String fileName) throws IOException {
    File destDir = new File("src/test/resources");
    byte[] buffer = new byte[1024];
    ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get("src/test/resources/" + fileName)));
    ZipEntry zipEntry = zis.getNextEntry();
    File newFile = newFile(destDir, zipEntry);

    // fix for Windows-created archives
    File parent = newFile.getParentFile();
    if (!parent.isDirectory() && !parent.mkdirs()) {
      throw new IOException("Failed to create directory " + parent);
    }

    // write file content
    FileOutputStream fos = new FileOutputStream(newFile);
    int len;
    while ((len = zis.read(buffer)) > 0) {
      fos.write(buffer, 0, len);
    }
    fos.close();

    zis.closeEntry();
    zis.close();
  }

  private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }
}