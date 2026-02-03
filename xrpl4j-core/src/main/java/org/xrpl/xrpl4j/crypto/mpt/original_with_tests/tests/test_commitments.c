#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "secp256k1_mpt.h"
#include <openssl/rand.h>

/* --- Helper: Safe Random Scalar Generation --- */
/* Ensures the random bytes form a valid curve scalar (0 < scalar < order) */
static void random_scalar(const secp256k1_context* ctx, unsigned char* out) {
    do {
        if (RAND_bytes(out, 32) != 1) {
            fprintf(stderr, "Randomness failure\n");
            // In a real app you might handle this gracefully, but for tests we abort
            abort(); // Force crash if RNG fails
        }
    } while (!secp256k1_ec_seckey_verify(ctx, out)); // Retry if scalar is invalid (>= curve order)
}

/* --- Tests --- */

void test_pedersen_commitment_basic() {
    secp256k1_context* ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);

    uint64_t amount = 1000;
    unsigned char rho[32];
    secp256k1_pubkey pc1, pc2;
    unsigned char ser1[33], ser2[33];
    size_t len = 33;

    printf("DEBUG: Starting Pedersen Commitment basic tests...\n");

    // Generate valid random blinding factor
    random_scalar(ctx, rho);

    // 1. Test Consistency: PC(m, rho) should always produce the same result
    assert(secp256k1_mpt_pedersen_commit(ctx, &pc1, amount, rho) == 1);
    assert(secp256k1_mpt_pedersen_commit(ctx, &pc2, amount, rho) == 1);

    secp256k1_ec_pubkey_serialize(ctx, ser1, &len, &pc1, SECP256K1_EC_COMPRESSED);
    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser2, &len, &pc2, SECP256K1_EC_COMPRESSED);

    assert(memcmp(ser1, ser2, 33) == 0);
    printf("SUCCESS: Deterministic commitment verified.\n");

    // 2. Test Binding: Changing amount should change commitment
    assert(secp256k1_mpt_pedersen_commit(ctx, &pc2, amount + 1, rho) == 1);
    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser2, &len, &pc2, SECP256K1_EC_COMPRESSED);
    assert(memcmp(ser1, ser2, 33) != 0);
    printf("SUCCESS: Binding property (amount) verified.\n");

    secp256k1_context_destroy(ctx);
}

void test_pedersen_zero_value() {
    secp256k1_context* ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);
    unsigned char rho[32];
    secp256k1_pubkey commitment, expected;
    secp256k1_pubkey H;
    unsigned char ser1[33], ser2[33];
    size_t len = 33;

    printf("DEBUG: Starting Pedersen Zero Value test...\n");

    // Generate valid random blinding factor
    random_scalar(ctx, rho);

    // 1. Commit to 0
    if (secp256k1_mpt_pedersen_commit(ctx, &commitment, 0, rho) != 1) {
        printf("FAILED: Could not commit to zero!\n");
        assert(0);
    }

    // 2. Manual Check: C should equal rho*H (since m*G is infinity)
    assert(secp256k1_mpt_get_h_generator(ctx, &H));
    expected = H;
    assert(secp256k1_ec_pubkey_tweak_mul(ctx, &expected, rho));

    // 3. Compare
    secp256k1_ec_pubkey_serialize(ctx, ser1, &len, &commitment, SECP256K1_EC_COMPRESSED);
    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser2, &len, &expected, SECP256K1_EC_COMPRESSED);

    assert(memcmp(ser1, ser2, 33) == 0);
    printf("SUCCESS: Committing to 0 correctly resulted in rho*H.\n");

    secp256k1_context_destroy(ctx);
}

void test_pedersen_homomorphic_property() {
    secp256k1_context* ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);

    uint64_t m1 = 500, m2 = 300;
    unsigned char r1[32], r2[32], r_sum[32];
    secp256k1_pubkey pc1, pc2, pc_sum_manual, pc_sum_computed;

    printf("DEBUG: Starting Pedersen Homomorphic property test...\n");

    // Generate valid random blinding factors
    random_scalar(ctx, r1);
    random_scalar(ctx, r2);

    // Compute PC1 = PC(m1, r1) and PC2 = PC(m2, r2)
    assert(secp256k1_mpt_pedersen_commit(ctx, &pc1, m1, r1) == 1);
    assert(secp256k1_mpt_pedersen_commit(ctx, &pc2, m2, r2) == 1);

    // Manual sum of points: PC1 + PC2
    const secp256k1_pubkey* points[2] = {&pc1, &pc2};
    assert(secp256k1_ec_pubkey_combine(ctx, &pc_sum_manual, points, 2) == 1);

    // Compute scalar sum of blinding factors: r_sum = r1 + r2 (mod n)
    memcpy(r_sum, r1, 32);
    assert(secp256k1_ec_seckey_tweak_add(ctx, r_sum, r2) == 1);

    // Compute PC(m1 + m2, r1 + r2)
    assert(secp256k1_mpt_pedersen_commit(ctx, &pc_sum_computed, m1 + m2, r_sum) == 1);

    // Compare
    unsigned char ser1[33], ser2[33];
    size_t len = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser1, &len, &pc_sum_manual, SECP256K1_EC_COMPRESSED);
    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser2, &len, &pc_sum_computed, SECP256K1_EC_COMPRESSED);

    assert(memcmp(ser1, ser2, 33) == 0);
    printf("SUCCESS: Homomorphic property (PC(m1,r1) + PC(m2,r2) == PC(m1+m2, r1+r2)) verified.\n");

    secp256k1_context_destroy(ctx);
}

int main() {
    test_pedersen_commitment_basic();
    test_pedersen_zero_value();
    test_pedersen_homomorphic_property();
    printf("DEBUG: All Pedersen Commitment tests passed!\n");
    return 0;
}
