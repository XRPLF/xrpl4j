#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <secp256k1.h>
#include <openssl/sha.h>
#include <openssl/rand.h>
#include "secp256k1_mpt.h"

/* Use your defined N_BITS */
#define N_BITS 64

/* Forward declarations for your implementation functions */
int secp256k1_bulletproof_prove(const secp256k1_context* ctx, unsigned char* proof_out, size_t* proof_len, uint64_t value, const unsigned char* blinding_factor, const secp256k1_pubkey* pk_base, const unsigned char* context_id, unsigned int proof_type);
int secp256k1_bulletproof_verify(const secp256k1_context* ctx, const secp256k1_pubkey* G_vec, const secp256k1_pubkey* H_vec, const unsigned char* proof, size_t proof_len, const secp256k1_pubkey* commitment_C, const secp256k1_pubkey* pk_base, const unsigned char* context_id);
int secp256k1_bulletproof_create_commitment(const secp256k1_context* ctx, secp256k1_pubkey* commitment_C, uint64_t value, const unsigned char* blinding_factor, const secp256k1_pubkey* pk_base);

/* These are your actual MPT generator functions from the other file */
extern int secp256k1_mpt_get_generator_vector(const secp256k1_context* ctx, secp256k1_pubkey* vec, size_t n, const unsigned char* label, size_t label_len);

int main() {
    /* Create context with sign/verify flags */
    secp256k1_context* ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);
/* --- STEP 0: SCALAR SANITY CHECK --- */
    printf("[TEST] Running Scalar Math Sanity Check... ");
    unsigned char a[32] = {0}, b[32] = {0}, res[32];
    a[31] = 2; // Big-endian 2
    b[31] = 3; // Big-endian 3

    secp256k1_mpt_scalar_mul(res, a, b);

    if (res[31] != 6) {
        printf("FAILED!\n");
        printf("[ERROR] Expected last byte to be 6, but got %u.\n", res[31]);
        printf("[ERROR] This usually indicates a struct layout mismatch (e.g., 8x32 vs 4x64).\n");
        printf("[ERROR] Check your USE_SCALAR_XXX definitions in CMake and mpt_scalar.c.\n");
        return 1;
    }
    printf("PASSED (2 * 3 = 6)\n");



    secp256k1_pubkey G_vec[N_BITS], H_vec[N_BITS], pk_base, commitment_C;
    unsigned char blinding_factor[32], context_id[32], proof[688];
    size_t proof_len;
    uint64_t test_value = 5000; /* Example amount */

    printf("[TEST] Initializing Bulletproof Range Proof Test (64-bit)\n");

    /* 1. Setup Generators using your official MPT logic */
    printf("[TEST] Deriving 128 NUMS generators... ");
    if (!secp256k1_mpt_get_generator_vector(ctx, G_vec, N_BITS, (const unsigned char*)"G", 1) ||
        !secp256k1_mpt_get_generator_vector(ctx, H_vec, N_BITS, (const unsigned char*)"H", 1)) {
        printf("FAILED to derive generators\n");
        return 1;
    }
    printf("DONE\n");

    /* 2. Setup Blinders and Context */
    RAND_bytes(blinding_factor, 32);
    RAND_bytes(context_id, 32);

    /* We use a random pk_base for the secondary blinder */

    unsigned char dummy_sk[32];
    do {
        RAND_bytes(dummy_sk, 32);
    } while (!secp256k1_ec_seckey_verify(ctx, dummy_sk));

    if (!secp256k1_ec_pubkey_create(ctx, &pk_base, dummy_sk)) {
        printf("FAILED to create pk_base\n");
        return 1;
    }

    /* 3. Test Case 1: Valid Proof */
    printf("[TEST] Case 1: Valid value (%llu)... ", (unsigned long long)test_value);

    secp256k1_bulletproof_create_commitment(ctx, &commitment_C, test_value, blinding_factor, &pk_base);

    if (!secp256k1_bulletproof_prove(ctx, proof, &proof_len, test_value, blinding_factor, &pk_base, context_id, 0)) {
        printf("FAILED (Prover returned 0)\n");
        return 1;
    }

    if (secp256k1_bulletproof_verify(ctx, G_vec, H_vec, proof, proof_len, &commitment_C, &pk_base, context_id)) {
        printf("PASSED\n");
    } else {
        printf("FAILED (Verification returned 0)\n");
        return 1;
    }

    /* 4. Test Case 2: Tampered Value Detection */
    printf("[TEST] Case 2: Tampered commitment... ");
    secp256k1_pubkey tampered_C;
    uint64_t different_value = test_value + 1;
    secp256k1_bulletproof_create_commitment(ctx, &tampered_C, different_value, blinding_factor, &pk_base);

    if (!secp256k1_bulletproof_verify(ctx, G_vec, H_vec, proof, proof_len, &tampered_C, &pk_base, context_id)) {
        printf("PASSED (Correctly rejected mismatch)\n");
    } else {
        printf("FAILED (Accepted invalid proof!)\n");
        return 1;
    }

    printf("[TEST] All Bulletproof tests completed successfully.\n");
    secp256k1_context_destroy(ctx);
    return 0;
}
