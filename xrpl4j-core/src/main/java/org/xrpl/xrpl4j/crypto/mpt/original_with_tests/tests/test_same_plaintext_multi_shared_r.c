#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <openssl/rand.h>
#include <stdlib.h> // Added for malloc/free

#include "secp256k1_mpt.h"

/* --- Test Constants --- */
#define N_RECIPIENTS 3

/* --- Helper: Generate Valid Scalar --- */
static int generate_random_scalar(const secp256k1_context* ctx, unsigned char* scalar) {
    do {
        if (RAND_bytes(scalar, 32) != 1) return 0;
    } while (secp256k1_ec_seckey_verify(ctx, scalar) != 1);
    return 1;
}

void run_test_equality_shared_r() {
    printf("=== Running Test: Proof of Equality (Shared Randomness) ===\n");

    /* 1. Setup Context */
    secp256k1_context* ctx = secp256k1_context_create(SECP256K1_CONTEXT_VERIFY | SECP256K1_CONTEXT_SIGN);
    unsigned char seed[32];
    if (RAND_bytes(seed, 32) == 1) {
        secp256k1_context_randomize(ctx, seed);
    }

    /* 2. Setup Variables */
    uint64_t amount = 123456789; // The secret amount
    unsigned char r_shared[32];  // The shared randomness
    unsigned char sk[N_RECIPIENTS][32];
    secp256k1_pubkey Pk[N_RECIPIENTS];

    secp256k1_pubkey C1;               // r * G
    secp256k1_pubkey C2[N_RECIPIENTS]; // m * G + r * Pk_i

    /* 3. Generate Keys & Shared Randomness */
    assert(generate_random_scalar(ctx, r_shared));

    // Calculate C1 = r * G
    assert(secp256k1_ec_pubkey_create(ctx, &C1, r_shared));

    // Prepare Amount Scalar (m * G)
    unsigned char m_scalar[32] = {0};
    for (int i = 0; i < 8; i++) m_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;

    secp256k1_pubkey mG;
    assert(secp256k1_ec_pubkey_create(ctx, &mG, m_scalar));

    /* 4. Encrypt for N recipients */
    for (int i = 0; i < N_RECIPIENTS; i++) {
        // Generate Recipient Keypair
        assert(generate_random_scalar(ctx, sk[i]));
        assert(secp256k1_ec_pubkey_create(ctx, &Pk[i], sk[i]));

        // Calculate r * Pk_i
        secp256k1_pubkey rPk = Pk[i];
        assert(secp256k1_ec_pubkey_tweak_mul(ctx, &rPk, r_shared));

        // Calculate C2_i = m * G + r * Pk_i
        const secp256k1_pubkey* pts[2] = {&mG, &rPk};
        assert(secp256k1_ec_pubkey_combine(ctx, &C2[i], pts, 2));
    }

    /* 5. Generate Proof */
    // User manually allocates correct size (Correct pattern!)
    size_t proof_len = secp256k1_mpt_proof_equality_shared_r_size(N_RECIPIENTS);
    unsigned char* proof = (unsigned char*)malloc(proof_len);
    unsigned char tx_id[32];
    RAND_bytes(tx_id, 32);

    printf("Generating proof for %d recipients...\n", N_RECIPIENTS);

    // UPDATED CALL: Removed '&proof_len'
    int res = secp256k1_mpt_prove_equality_shared_r(
            ctx, proof,
            amount, r_shared, N_RECIPIENTS,
            &C1, C2, Pk, tx_id
    );
    assert(res == 1);
    printf("Proof generated. Size: %zu bytes.\n", proof_len);

    /* 6. Verify Proof (Positive Case) */
    printf("Verifying proof (Expecting Success)...\n");

    // UPDATED CALL: Removed 'proof_len'
    res = secp256k1_mpt_verify_equality_shared_r(
            ctx, proof,
            N_RECIPIENTS,
            &C1, C2, Pk, tx_id
    );
    assert(res == 1);
    printf("Verified: OK.\n");

    /* 7. Negative Test: Tamper with Amount */
    printf("Verifying proof with wrong ciphertext (Expecting Failure)...\n");
    secp256k1_pubkey C2_tampered[N_RECIPIENTS];
    memcpy(C2_tampered, C2, sizeof(C2));

    // Add 1 to the first ciphertext
    unsigned char one[32] = {0}; one[31] = 1;
    assert(secp256k1_ec_pubkey_tweak_add(ctx, &C2_tampered[0], one));

    // UPDATED CALL: Removed 'proof_len'
    res = secp256k1_mpt_verify_equality_shared_r(
            ctx, proof,
            N_RECIPIENTS,
            &C1, C2_tampered, Pk, tx_id
    );
    assert(res == 0);
    printf("Tamper detection: OK.\n");

    /* 8. Negative Test: Tamper with Transaction Context */
    printf("Verifying proof with wrong TxID (Expecting Failure)...\n");
    unsigned char tx_id_fake[32];
    memcpy(tx_id_fake, tx_id, 32);
    tx_id_fake[0] ^= 0xFF; // Flip bits

    // UPDATED CALL: Removed 'proof_len'
    res = secp256k1_mpt_verify_equality_shared_r(
            ctx, proof,
            N_RECIPIENTS,
            &C1, C2, Pk, tx_id_fake
    );
    assert(res == 0);
    printf("TxID binding check: OK.\n");

    /* Cleanup */
    free(proof);
    secp256k1_context_destroy(ctx);
    printf("=== Test Passed Successfully ===\n");
}

int main() {
    run_test_equality_shared_r();
    return 0;
}
