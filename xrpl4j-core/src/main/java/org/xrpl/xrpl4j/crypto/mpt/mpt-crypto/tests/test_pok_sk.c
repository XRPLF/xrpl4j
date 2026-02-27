#include "secp256k1_mpt.h"
#include "test_utils.h"
#include <secp256k1.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void test_pok_sk()
{
  secp256k1_context *ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN |
                                                    SECP256K1_CONTEXT_VERIFY);
  EXPECT(ctx != NULL);

  unsigned char sk[32], context_id[32];
  unsigned char proof[65]; // Schnorr proof is 64 bytes + 1 byte header usually,
                           // or just 64 depending on impl.
  // The previous code used 65, so we keep 65.
  secp256k1_pubkey pk;

  printf("DEBUG: Starting PoK SK Registration test...\n");

  // Setup: Generate keypair and random context
  random_scalar(ctx, sk);
  EXPECT(secp256k1_ec_pubkey_create(ctx, &pk, sk));

  EXPECT(RAND_bytes(context_id, 32) == 1);

  // Test 1: Generate and Verify Valid Proof
  // Returns 1 on success
  EXPECT(secp256k1_mpt_pok_sk_prove(ctx, proof, &pk, sk, context_id) == 1);

  // Returns 1 on success
  EXPECT(secp256k1_mpt_pok_sk_verify(ctx, proof, &pk, context_id) == 1);
  printf("SUCCESS: Valid PoK verified.\n");

  // Test 2: Invalid Context
  // Proof should bind to the specific context_id. Changing it should fail
  // verification.
  unsigned char wrong_context[32];
  memcpy(wrong_context, context_id, 32);
  wrong_context[0] ^= 0xFF; // Corrupt the context

  EXPECT(secp256k1_mpt_pok_sk_verify(ctx, proof, &pk, wrong_context) == 0);
  printf("SUCCESS: Invalid context correctly rejected.\n");

  // Test 3: Corrupted Proof Scalar
  // Corrupt the last byte of the proof signature/scalar
  proof[64] ^= 0xFF;

  EXPECT(secp256k1_mpt_pok_sk_verify(ctx, proof, &pk, context_id) == 0);
  printf("SUCCESS: Corrupted proof correctly rejected.\n");

  secp256k1_context_destroy(ctx);
}

int main()
{
  test_pok_sk();
  printf("ALL TESTS PASSED\n");
  return 0;
}
