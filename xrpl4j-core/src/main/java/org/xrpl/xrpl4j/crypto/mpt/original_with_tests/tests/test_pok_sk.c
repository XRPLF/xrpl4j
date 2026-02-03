#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "secp256k1_mpt.h"
#include <openssl/rand.h>

void test_pok_sk() {
    secp256k1_context* ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);
    unsigned char sk[32], context_id[32], proof[65];
    secp256k1_pubkey pk;

    printf("DEBUG: Starting PoK SK Registration test...\n");

    // Setup: Generate keypair and random context
    do { RAND_bytes(sk, 32); } while (!secp256k1_ec_seckey_verify(ctx, sk));
    assert(secp256k1_ec_pubkey_create(ctx, &pk, sk));
    RAND_bytes(context_id, 32);

    // Test 1: Generate and Verify Valid Proof
    assert(secp256k1_mpt_pok_sk_prove(ctx, proof, &pk, sk, context_id) == 1);
    assert(secp256k1_mpt_pok_sk_verify(ctx, proof, &pk, context_id) == 1);
    printf("SUCCESS: Valid PoK verified.\n");

    // Test 2: Invalid Context
    unsigned char wrong_context[32];
    memcpy(wrong_context, context_id, 32);
    wrong_context[0] ^= 0xFF;
    assert(secp256k1_mpt_pok_sk_verify(ctx, proof, &pk, wrong_context) == 0);
    printf("SUCCESS: Invalid context correctly rejected.\n");

    // Test 3: Corrupted Proof Scalar
    proof[64] ^= 0xFF;
    assert(secp256k1_mpt_pok_sk_verify(ctx, proof, &pk, context_id) == 0);
    printf("SUCCESS: Corrupted proof correctly rejected.\n");

    secp256k1_context_destroy(ctx);
}

int main() {
    test_pok_sk();
    return 0;
}
