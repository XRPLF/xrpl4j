package org.xrpl.xrpl4j.model.transactions;

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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TransactionResultCodesTest {

  @Test
  public void testTecCodes() {
    assertThat("tecCLAIM").isEqualTo(TransactionResultCodes.TEC_CLAIM);
    assertThat("tecCRYPTOCONDITION_ERROR").isEqualTo(TransactionResultCodes.TEC_CRYPTOCONDITION_ERROR);
    assertThat("tecDIR_FULL").isEqualTo(TransactionResultCodes.TEC_DIR_FULL);
    assertThat("tecDUPLICATE").isEqualTo(TransactionResultCodes.TEC_DUPLICATE);
    assertThat("tecDST_TAG_NEEDED").isEqualTo(TransactionResultCodes.TEC_DST_TAG_NEEDED);
    assertThat("tecEXPIRED").isEqualTo(TransactionResultCodes.TEC_EXPIRED);
    assertThat("tecFAILED_PROCESSING").isEqualTo(TransactionResultCodes.TEC_FAILED_PROCESSING);
    assertThat("tecFROZEN").isEqualTo(TransactionResultCodes.TEC_FROZEN);
    assertThat("tecHAS_OBLIGATIONS").isEqualTo(TransactionResultCodes.TEC_HAS_OBLIGATIONS);
    assertThat("tecINSUF_RESERVE_LINE").isEqualTo(TransactionResultCodes.TEC_INSUF_RESERVE_LINE);
    assertThat("tecINSUF_RESERVE_OFFER").isEqualTo(TransactionResultCodes.TEC_INSUF_RESERVE_OFFER);
    assertThat("tecINSUFF_FEE").isEqualTo(TransactionResultCodes.TEC_INSUFF_FEE);
    assertThat("tecINSUFFICIENT_RESERVE").isEqualTo(TransactionResultCodes.TEC_INSUFFICIENT_RESERVE);
    assertThat("tecINTERNAL").isEqualTo(TransactionResultCodes.TEC_INTERNAL);
    assertThat("tecINVARIANT_FAILED").isEqualTo(TransactionResultCodes.TEC_INVARIANT_FAILED);
    assertThat("tecKILLED").isEqualTo(TransactionResultCodes.TEC_KILLED);
    assertThat("tecNEED_MASTER_KEY").isEqualTo(TransactionResultCodes.TEC_NEED_MASTER_KEY);
    assertThat("tecNO_ALTERNATIVE_KEY").isEqualTo(TransactionResultCodes.TEC_NO_ALTERNATIVE_KEY);
    assertThat("tecNO_AUTH").isEqualTo(TransactionResultCodes.TEC_NO_AUTH);
    assertThat("tecNO_DST").isEqualTo(TransactionResultCodes.TEC_NO_DST);
    assertThat("tecNO_DST_INSUF_XRP").isEqualTo(TransactionResultCodes.TEC_NO_DST_INSUF_XRP);
    assertThat("tecNO_ENTRY").isEqualTo(TransactionResultCodes.TEC_NO_ENTRY);
    assertThat("tecNO_ISSUER").isEqualTo(TransactionResultCodes.TEC_NO_ISSUER);
    assertThat("tecNO_LINE").isEqualTo(TransactionResultCodes.TEC_NO_LINE);
    assertThat("tecNO_LINE_INSUF_RESERVE").isEqualTo(TransactionResultCodes.TEC_NO_LINE_INSUF_RESERVE);
    assertThat("tecNO_LINE_REDUNDANT").isEqualTo(TransactionResultCodes.TEC_NO_LINE_REDUNDANT);
    assertThat("tecNO_PERMISSION").isEqualTo(TransactionResultCodes.TEC_NO_PERMISSION);
    assertThat("tecNO_REGULAR_KEY").isEqualTo(TransactionResultCodes.TEC_NO_REGULAR_KEY);
    assertThat("tecNO_TARGET").isEqualTo(TransactionResultCodes.TEC_NO_TARGET);
    assertThat("tecOVERSIZE").isEqualTo(TransactionResultCodes.TEC_OVERSIZE);
    assertThat("tecOWNERS").isEqualTo(TransactionResultCodes.TEC_OWNERS);
    assertThat("tecPATH_DRY").isEqualTo(TransactionResultCodes.TEC_PATH_DRY);
    assertThat("tecPATH_PARTIAL").isEqualTo(TransactionResultCodes.TEC_PATH_PARTIAL);
    assertThat("tecTOO_SOON").isEqualTo(TransactionResultCodes.TEC_TOO_SOON);
    assertThat("tecUNFUNDED").isEqualTo(TransactionResultCodes.TEC_UNFUNDED);
    assertThat("tecUNFUNDED_ADD").isEqualTo(TransactionResultCodes.TEC_UNFUNDED_ADD);
    assertThat("tecUNFUNDED_PAYMENT").isEqualTo(TransactionResultCodes.TEC_UNFUNDED_PAYMENT);
    assertThat("tecUNFUNDED_OFFER").isEqualTo(TransactionResultCodes.TEC_UNFUNDED_OFFER);
  }

  @Test
  public void testTefCodes() {
    assertThat("tefALREADY").isEqualTo(TransactionResultCodes.TEF_ALREADY);
    assertThat("tefBAD_ADD_AUTH").isEqualTo(TransactionResultCodes.TEF_BAD_ADD_AUTH);
    assertThat("tefBAD_AUTH").isEqualTo(TransactionResultCodes.TEF_BAD_AUTH);
    assertThat("tefBAD_AUTH_MASTER").isEqualTo(TransactionResultCodes.TEF_BAD_AUTH_MASTER);
    assertThat("tefBAD_LEDGER").isEqualTo(TransactionResultCodes.TEF_BAD_LEDGER);
    assertThat("tefBAD_QUORUM").isEqualTo(TransactionResultCodes.TEF_BAD_QUORUM);
    assertThat("tefBAD_SIGNATURE").isEqualTo(TransactionResultCodes.TEF_BAD_SIGNATURE);
    assertThat("tefCREATED").isEqualTo(TransactionResultCodes.TEF_CREATED);
    assertThat("tefEXCEPTION").isEqualTo(TransactionResultCodes.TEF_EXCEPTION);
    assertThat("tefFAILURE").isEqualTo(TransactionResultCodes.TEF_FAILURE);
    assertThat("tefINTERNAL").isEqualTo(TransactionResultCodes.TEF_INTERNAL);
    assertThat("tefINVARIANT_FAILED").isEqualTo(TransactionResultCodes.TEF_INVARIANT_FAILED);
    assertThat("tefMASTER_DISABLED").isEqualTo(TransactionResultCodes.TEF_MASTER_DISABLED);
    assertThat("tefMAX_LEDGER").isEqualTo(TransactionResultCodes.TEF_MAX_LEDGER);
    assertThat("tefNO_AUTH_REQUIRED").isEqualTo(TransactionResultCodes.TEF_NO_AUTH_REQUIRED);
    assertThat("tefNO_TICKET").isEqualTo(TransactionResultCodes.TEF_NO_TICKET);
    assertThat("tefNOT_MULTI_SIGNING").isEqualTo(TransactionResultCodes.TEF_NOT_MULTI_SIGNING);
    assertThat("tefPAST_SEQ").isEqualTo(TransactionResultCodes.TEF_PAST_SEQ);
    assertThat("tefTOO_BIG").isEqualTo(TransactionResultCodes.TEF_TOO_BIG);
    assertThat("tefWRONG_PRIOR").isEqualTo(TransactionResultCodes.TEF_WRONG_PRIOR);
  }

  @Test
  public void testTelCodes() {
    assertThat("telBAD_DOMAIN").isEqualTo(TransactionResultCodes.TEL_BAD_DOMAIN);
    assertThat("telBAD_PATH_COUNT").isEqualTo(TransactionResultCodes.TEL_BAD_PATH_COUNT);
    assertThat("telBAD_PUBLIC_KEY").isEqualTo(TransactionResultCodes.TEL_BAD_PUBLIC_KEY);
    assertThat("telCAN_NOT_QUEUE").isEqualTo(TransactionResultCodes.TEL_CAN_NOT_QUEUE);
    assertThat("telCAN_NOT_QUEUE_BALANCE").isEqualTo(TransactionResultCodes.TEL_CAN_NOT_QUEUE_BALANCE);
    assertThat("telCAN_NOT_QUEUE_BLOCKS").isEqualTo(TransactionResultCodes.TEL_CAN_NOT_QUEUE_BLOCKS);
    assertThat("telCAN_NOT_QUEUE_BLOCKED").isEqualTo(TransactionResultCodes.TEL_CAN_NOT_QUEUE_BLOCKED);
    assertThat("telCAN_NOT_QUEUE_FEE").isEqualTo(TransactionResultCodes.TEL_CAN_NOT_QUEUE_FEE);
    assertThat("telCAN_NOT_QUEUE_FULL").isEqualTo(TransactionResultCodes.TEL_CAN_NOT_QUEUE_FULL);
    assertThat("telFAILED_PROCESSING").isEqualTo(TransactionResultCodes.TEL_FAILED_PROCESSING);
    assertThat("telINSUF_FEE_P").isEqualTo(TransactionResultCodes.TEL_INSUF_FEE_P);
    assertThat("telLOCAL_ERROR").isEqualTo(TransactionResultCodes.TEL_LOCAL_ERROR);
    assertThat("telNO_DST_PARTIAL").isEqualTo(TransactionResultCodes.TEL_NO_DST_PARTIAL);
  }

  @Test
  public void testTemCodes() {
    assertThat("temBAD_AMOUNT").isEqualTo(TransactionResultCodes.TEM_BAD_AMOUNT);
    assertThat("temBAD_AUTH_MASTER").isEqualTo(TransactionResultCodes.TEM_BAD_AUTH_MASTER);
    assertThat("temBAD_CURRENCY").isEqualTo(TransactionResultCodes.TEM_BAD_CURRENCY);
    assertThat("temBAD_EXPIRATION").isEqualTo(TransactionResultCodes.TEM_BAD_EXPIRATION);
    assertThat("temBAD_FEE").isEqualTo(TransactionResultCodes.TEM_BAD_FEE);
    assertThat("temBAD_ISSUER").isEqualTo(TransactionResultCodes.TEM_BAD_ISSUER);
    assertThat("temBAD_LIMIT").isEqualTo(TransactionResultCodes.TEM_BAD_LIMIT);
    assertThat("temBAD_OFFER").isEqualTo(TransactionResultCodes.TEM_BAD_OFFER);
    assertThat("temBAD_PATH").isEqualTo(TransactionResultCodes.TEM_BAD_PATH);
    assertThat("temBAD_PATH_LOOP").isEqualTo(TransactionResultCodes.TEM_BAD_PATH_LOOP);
    assertThat("temBAD_SEND_XRP_LIMIT").isEqualTo(TransactionResultCodes.TEM_BAD_SEND_XRP_LIMIT);
    assertThat("temBAD_SEND_XRP_MAX").isEqualTo(TransactionResultCodes.TEM_BAD_SEND_XRP_MAX);
    assertThat("temBAD_SEND_XRP_NO_DIRECT").isEqualTo(TransactionResultCodes.TEM_BAD_SEND_XRP_NO_DIRECT);
    assertThat("temBAD_SEND_XRP_PARTIAL").isEqualTo(TransactionResultCodes.TEM_BAD_SEND_XRP_PARTIAL);
    assertThat("temBAD_SEND_XRP_PATHS").isEqualTo(TransactionResultCodes.TEM_BAD_SEND_XRP_PATHS);
    assertThat("temBAD_SEQUENCE").isEqualTo(TransactionResultCodes.TEM_BAD_SEQUENCE);
    assertThat("temBAD_SIGNATURE").isEqualTo(TransactionResultCodes.TEM_BAD_SIGNATURE);
    assertThat("temBAD_SRC_ACCOUNT").isEqualTo(TransactionResultCodes.TEM_BAD_SRC_ACCOUNT);
    assertThat("temBAD_TRANSFER_RATE").isEqualTo(TransactionResultCodes.TEM_BAD_TRANSFER_RATE);
    assertThat("temCANNOT_PREAUTH_SELF").isEqualTo(TransactionResultCodes.TEM_CANNOT_PREAUTH_SELF);
    assertThat("temDST_IS_SRC").isEqualTo(TransactionResultCodes.TEM_DST_IS_SRC);
    assertThat("temDST_NEEDED").isEqualTo(TransactionResultCodes.TEM_DST_NEEDED);
    assertThat("temINVALID").isEqualTo(TransactionResultCodes.TEM_INVALID);
    assertThat("temINVALID_COUNT").isEqualTo(TransactionResultCodes.TEM_INVALID_COUNT);
    assertThat("temINVALID_FLAG").isEqualTo(TransactionResultCodes.TEM_INVALID_FLAG);
    assertThat("temMALFORMED").isEqualTo(TransactionResultCodes.TEM_MALFORMED);
    assertThat("temREDUNDANT").isEqualTo(TransactionResultCodes.TEM_REDUNDANT);
    assertThat("temREDUNDANT_SEND_MAX").isEqualTo(TransactionResultCodes.TEM_REDUNDANT_SEND_MAX);
    assertThat("temRIPPLE_EMPTY").isEqualTo(TransactionResultCodes.TEM_RIPPLE_EMPTY);
    assertThat("temBAD_WEIGHT").isEqualTo(TransactionResultCodes.TEM_BAD_WEIGHT);
    assertThat("temBAD_SIGNER").isEqualTo(TransactionResultCodes.TEM_BAD_SIGNER);
    assertThat("temBAD_QUORUM").isEqualTo(TransactionResultCodes.TEM_BAD_QUORUM);
    assertThat("temUNCERTAIN").isEqualTo(TransactionResultCodes.TEM_UNCERTAIN);
    assertThat("temUNKNOWN").isEqualTo(TransactionResultCodes.TEM_UNKNOWN);
    assertThat("temDISABLED").isEqualTo(TransactionResultCodes.TEM_DISABLED);
  }

  @Test
  public void testTerCodes() {
    assertThat("terFUNDS_SPENT").isEqualTo(TransactionResultCodes.TER_FUNDS_SPENT);
    assertThat("terINSUF_FEE_B").isEqualTo(TransactionResultCodes.TER_INSUF_FEE_B);
    assertThat("terLAST").isEqualTo(TransactionResultCodes.TER_LAST);
    assertThat("terNO_ACCOUNT").isEqualTo(TransactionResultCodes.TER_NO_ACCOUNT);
    assertThat("terNO_AUTH").isEqualTo(TransactionResultCodes.TER_NO_AUTH);
    assertThat("terNO_LINE").isEqualTo(TransactionResultCodes.TER_NO_LINE);
    assertThat("terNO_RIPPLE").isEqualTo(TransactionResultCodes.TER_NO_RIPPLE);
    assertThat("terOWNERS").isEqualTo(TransactionResultCodes.TER_OWNERS);
    assertThat("terPRE_SEQ").isEqualTo(TransactionResultCodes.TER_PRE_SEQ);
    assertThat("terPRE_TICKET").isEqualTo(TransactionResultCodes.TER_PRE_TICKET);
    assertThat("terRETRY").isEqualTo(TransactionResultCodes.TER_RETRY);
    assertThat("terQUEUED").isEqualTo(TransactionResultCodes.TER_QUEUED);
  }

  @Test
  public void testTesCode() {
    assertThat("tesSUCCESS").isEqualTo(TransactionResultCodes.TES_SUCCESS);
  }
}
