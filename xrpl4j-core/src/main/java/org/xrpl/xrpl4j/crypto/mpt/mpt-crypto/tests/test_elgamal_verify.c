#include "secp256k1_mpt.h"
#include "test_utils.h"
#include <secp256k1.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(void)
{
  // 1. Setup Context
  secp256k1_context *ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN |
                                                    SECP256K1_CONTEXT_VERIFY);
  EXPECT(ctx != NULL);

  // Standardized Context Randomization
  unsigned char seed[32];
  random_bytes(seed);
  EXPECT(secp256k1_context_randomize(ctx, seed) == 1);

  unsigned char priv_q[32], r[32], r_bad[32];
  secp256k1_pubkey pub_q, c1, c2;
  uint64_t amount = 12345;
  uint64_t amount_bad = 54321;

  printf("DEBUG: Starting ElGamal Verification test...\n");

  // 2. Generate Identity and Randomness
  EXPECT(secp256k1_elgamal_generate_keypair(ctx, priv_q, &pub_q) == 1);

  // REFACTOR: Use standard helper instead of generating a full keypair
  random_scalar(ctx, r);

  memcpy(r_bad, r, 32);
  r_bad[31] ^= 0xFF; // Flip bits to create a bad randomness factor

  // 3. Perform Encryption
  EXPECT(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pub_q, amount, r) == 1);
  printf("DEBUG: Encryption successful.\n");

  // 4. Test Case 1: Valid Verification (The Happy Path)
  int res1 =
      secp256k1_elgamal_verify_encryption(ctx, &c1, &c2, &pub_q, amount, r);
  EXPECT(res1 == 1);
  printf("SUCCESS: Valid encryption correctly verified.\n");

  // 5. Test Case 2: Invalid Amount
  int res2 =
      secp256k1_elgamal_verify_encryption(ctx, &c1, &c2, &pub_q, amount_bad, r);
  EXPECT(res2 == 0);
  printf("SUCCESS: Incorrect amount correctly rejected.\n");

  // 6. Test Case 3: Invalid Randomness (r)
  int res3 =
      secp256k1_elgamal_verify_encryption(ctx, &c1, &c2, &pub_q, amount, r_bad);
  EXPECT(res3 == 0);
  printf("SUCCESS: Incorrect randomness correctly rejected.\n");

  // 7. Cleanup
  secp256k1_context_destroy(ctx);
  printf("DEBUG: All ElGamal Verification tests passed!\n");
  return 0;
}
