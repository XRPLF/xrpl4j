#include "secp256k1_mpt.h"
#include "test_utils.h"
#include <secp256k1.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define BP_VALUE_BITS 64
#define BP_TOTAL_BITS(m) ((size_t)(BP_VALUE_BITS * (m)))
#define VERIFY_RUNS 5

/* ---- Helpers ---- */
static inline double elapsed_ms(struct timespec a, struct timespec b)
{
  return (b.tv_sec - a.tv_sec) * 1000.0 + (b.tv_nsec - a.tv_nsec) / 1e6;
}

/* ---- Core Test Logic ---- */
void run_test_case(secp256k1_context *ctx, const char *name, uint64_t *values,
                   size_t num_values, int run_benchmarks)
{
  EXPECT(num_values > 0);
  printf("\n[TEST] %s (num_values = %zu)\n", name, num_values);

  unsigned char (*blindings)[32] = malloc(num_values * sizeof(*blindings));
  secp256k1_pubkey *commitments = malloc(num_values * sizeof(*commitments));
  unsigned char context_id[32];

  EXPECT(blindings != NULL && commitments != NULL);
  EXPECT(RAND_bytes(context_id, 32) == 1);

  secp256k1_pubkey pk_base;
  EXPECT(secp256k1_mpt_get_h_generator(ctx, &pk_base));

  /* ---- Commitments ---- */
  for (size_t i = 0; i < num_values; i++)
  {
    random_scalar(ctx, blindings[i]);
    EXPECT(secp256k1_bulletproof_create_commitment(
        ctx, &commitments[i], values[i], blindings[i], &pk_base));
  }

  /* ---- Generator vectors ---- */
  const size_t n = BP_TOTAL_BITS(num_values);
  secp256k1_pubkey *G_vec = malloc(n * sizeof(secp256k1_pubkey));
  secp256k1_pubkey *H_vec = malloc(n * sizeof(secp256k1_pubkey));
  EXPECT(G_vec && H_vec);

  EXPECT(secp256k1_mpt_get_generator_vector(ctx, G_vec, n,
                                            (const unsigned char *)"G", 1));
  EXPECT(secp256k1_mpt_get_generator_vector(ctx, H_vec, n,
                                            (const unsigned char *)"H", 1));

  /* ---- Prove ---- */
  unsigned char proof[4096];
  size_t proof_len = sizeof(proof);

  struct timespec t_p_start, t_p_end;
  clock_gettime(CLOCK_MONOTONIC, &t_p_start);

  EXPECT(secp256k1_bulletproof_prove_agg(ctx, proof, &proof_len, values,
                                         (const unsigned char *)blindings,
                                         num_values, &pk_base, context_id));

  clock_gettime(CLOCK_MONOTONIC, &t_p_end);
  printf("  Proof size: %zu bytes\n", proof_len);
  if (run_benchmarks)
    printf("  [BENCH] Proving time: %.3f ms\n", elapsed_ms(t_p_start, t_p_end));

  /* ---- Verify ---- */
  struct timespec t_v_start, t_v_end;
  clock_gettime(CLOCK_MONOTONIC, &t_v_start);

  int ok = secp256k1_bulletproof_verify_agg(ctx, G_vec, H_vec, proof, proof_len,
                                            commitments, num_values, &pk_base,
                                            context_id);

  clock_gettime(CLOCK_MONOTONIC, &t_v_end);
  EXPECT(ok);
  printf("  PASSED (Verification)\n");
  if (run_benchmarks)
    printf("  [BENCH] Verification time: %.3f ms\n",
           elapsed_ms(t_v_start, t_v_end));

  /* ---- Benchmark Verify ---- */
  if (run_benchmarks)
  {
    double total_ms = 0.0;
    for (int i = 0; i < VERIFY_RUNS; i++)
    {
      struct timespec ts, te;
      clock_gettime(CLOCK_MONOTONIC, &ts);
      ok = secp256k1_bulletproof_verify_agg(ctx, G_vec, H_vec, proof, proof_len,
                                            commitments, num_values, &pk_base,
                                            context_id);
      clock_gettime(CLOCK_MONOTONIC, &te);
      EXPECT(ok);
      total_ms += elapsed_ms(ts, te);
    }
    printf("  [BENCH] Verification avg over %d runs: %.3f ms\n", VERIFY_RUNS,
           total_ms / VERIFY_RUNS);
  }

  /* ---- Negative Test (Tamper) ---- */
  secp256k1_pubkey *bad_commitments =
      malloc(num_values * sizeof(*bad_commitments));
  EXPECT(bad_commitments != NULL);

  memcpy(bad_commitments, commitments, sizeof(*commitments) * num_values);
  unsigned char bad_blinding[32];
  random_scalar(ctx, bad_blinding);

  /* Create fake commitment to (value + 1) */
  EXPECT(secp256k1_bulletproof_create_commitment(
      ctx, &bad_commitments[num_values - 1],
      (values[num_values - 1] == UINT64_MAX) ? values[num_values - 1] - 1
                                             : values[num_values - 1] + 1,
      bad_blinding, &pk_base));

  ok = secp256k1_bulletproof_verify_agg(ctx, G_vec, H_vec, proof, proof_len,
                                        bad_commitments, num_values, &pk_base,
                                        context_id);

  if (ok)
  {
    fprintf(stderr, "FAILED: Accepted invalid proof!\n");
    exit(EXIT_FAILURE);
  }
  printf("  PASSED (Rejected invalid proof)\n");

  free(bad_commitments);
  free(commitments);
  free(blindings);
  free(G_vec);
  free(H_vec);
}

/* ---- Main ---- */
int main(void)
{
  secp256k1_context *ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN |
                                                    SECP256K1_CONTEXT_VERIFY);
  EXPECT(ctx != NULL);

  /* 1. Single proof, value 0 (The Bug Fix) */
  uint64_t v1[] = {0};
  run_test_case(ctx, "Single Proof (Value 0)", v1, 1, 0);

  /* 2. Single proof, value 1 */
  uint64_t v2[] = {1};
  run_test_case(ctx, "Single Proof (Value 1)", v2, 1, 0);

  /* 3. Single proof, MAX VALUE (Tests opposite vector edge case) */
  uint64_t v3[] = {0xFFFFFFFFFFFFFFFF}; // 2^64 - 1
  run_test_case(ctx, "Single Proof (MAX Value)", v3, 1, 0);

  /* 4. Aggregated proof, {0, 1} */
  uint64_t v4[] = {0, 1};
  run_test_case(ctx, "Aggregated Proof (0, 1)", v4, 2, 0);

  /* 5. Aggregated proof, {0, 0} with Benchmarks */
  uint64_t v5[] = {0, 0};
  run_test_case(ctx, "Aggregated Proof (0, 0)", v5, 2, 1);

  secp256k1_context_destroy(ctx);
  printf("\n[TEST] All Bulletproof tests completed successfully\n");
  return 0;
}
