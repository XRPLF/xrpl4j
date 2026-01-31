#include "secp256k1_mpt.h"
#include <openssl/rand.h>
#include <string.h>
#include <openssl/sha.h>
// ... implementation of secp256k1_elgamal_generate_keypair ...

int secp256k1_elgamal_generate_keypair(
        const secp256k1_context* ctx,
        unsigned char* privkey,
        secp256k1_pubkey* pubkey)
{
    // 1. Generate 32 random bytes for the private key
    do {
        if (RAND_bytes(privkey, 32) != 1) {
            return 0; // Failure
        }
        // 2. Verify the random data is a valid private key.
    } while (secp256k1_ec_seckey_verify(ctx, privkey) != 1);

    // 3. Create the corresponding public key.
    if (secp256k1_ec_pubkey_create(ctx, pubkey, privkey) != 1) {
        return 0; // Failure
    }

    return 1; // Success
}

// ... implementation of secp256k1_elgamal_encrypt ...

int secp256k1_elgamal_encrypt(
        const secp256k1_context* ctx,
        secp256k1_pubkey* c1,
        secp256k1_pubkey* c2,
        const secp256k1_pubkey* pubkey_Q,
        uint64_t amount,
        const unsigned char* blinding_factor
) {
    secp256k1_pubkey S;

    // First, calculate C1 = k * G
    if (secp256k1_ec_pubkey_create(ctx, c1, blinding_factor) != 1) {
        return 0;
    }

    // Next, calculate the shared secret S = k * Q
    S = *pubkey_Q;
    if (secp256k1_ec_pubkey_tweak_mul(ctx, &S, blinding_factor) != 1) {
        return 0;
    }

    // --- Handle the amount ---
    if (amount == 0) {
        // For amount = 0, C2 = S.
        *c2 = S;
    } else {
        unsigned char amount_scalar[32] = {0};
        secp256k1_pubkey M;
        const secp256k1_pubkey* points_to_add[2];

        // Convert amount to a 32-byte BIG-ENDIAN scalar.
        for (int i = 0; i < 8; ++i) {
            amount_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
        }

        // Calculate M = amount * G
        if (secp256k1_ec_pubkey_create(ctx, &M, amount_scalar) != 1) {
            return 0;
        }

        // Calculate C2 = M + S
        points_to_add[0] = &M;
        points_to_add[1] = &S;
        if (secp256k1_ec_pubkey_combine(ctx, c2, points_to_add, 2) != 1) {
            return 0;
        }
    }

    return 1; // Success
}
// ... implementation of secp256k1_elgamal_decrypt ...
int secp256k1_elgamal_decrypt(
        const secp256k1_context* ctx,
        uint64_t* amount,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const unsigned char* privkey
) {

    secp256k1_pubkey S, M, G_point, current_M, next_M;
    const secp256k1_pubkey* points_to_add[2];
    unsigned char c2_bytes[33], s_bytes[33], m_bytes[33], current_m_bytes[33];
    size_t len;
    uint64_t i;

    /* Create the scalar '1' in big-endian format */
    unsigned char one_scalar[32] = {0};
    one_scalar[31] = 1;

    /* --- Executable Code --- */

    // 1. Calculate S = privkey * C1
    S = *c1;
    if (secp256k1_ec_pubkey_tweak_mul(ctx, &S, privkey) != 1) {
        return 0;
    }

    // 2. Check for amount = 0 by comparing serialized points
    len = sizeof(c2_bytes);
    if (secp256k1_ec_pubkey_serialize(ctx, c2_bytes, &len, c2, SECP256K1_EC_COMPRESSED) != 1) return 0;
    len = sizeof(s_bytes);
    if (secp256k1_ec_pubkey_serialize(ctx, s_bytes, &len, &S, SECP256K1_EC_COMPRESSED) != 1) return 0;
    if (memcmp(c2_bytes, s_bytes, sizeof(c2_bytes)) == 0) {
        *amount = 0;
        return 1;
    }

    // 3. Recover M = C2 - S
    if (secp256k1_ec_pubkey_negate(ctx, &S) != 1) return 0;
    points_to_add[0] = c2;
    points_to_add[1] = &S;
    if (secp256k1_ec_pubkey_combine(ctx, &M, points_to_add, 2) != 1) {
        return 0;
    }

    // 4. Serialize M once for comparison in the loop
    len = sizeof(m_bytes);
    if (secp256k1_ec_pubkey_serialize(ctx, m_bytes, &len, &M, SECP256K1_EC_COMPRESSED) != 1) return 0;

    // 5. Brute-force search loop
    if (secp256k1_ec_pubkey_create(ctx, &G_point, one_scalar) != 1) return 0;
    current_M = G_point;

    for (i = 1; i <= 1000000; ++i) {
        len = sizeof(current_m_bytes);
        if (secp256k1_ec_pubkey_serialize(ctx, current_m_bytes, &len, &current_M, SECP256K1_EC_COMPRESSED) != 1) return 0;
        if (memcmp(m_bytes, current_m_bytes, sizeof(m_bytes)) == 0) {
            *amount = i;
            return 1;
        }

        points_to_add[0] = &current_M;
        points_to_add[1] = &G_point;
        if (secp256k1_ec_pubkey_combine(ctx, &next_M, points_to_add, 2) != 1) return 0;
        current_M = next_M;
    }

    return 0; // Not found
}

int secp256k1_elgamal_add(
        const secp256k1_context* ctx,
        secp256k1_pubkey* sum_c1,
        secp256k1_pubkey* sum_c2,
        const secp256k1_pubkey* a_c1,
        const secp256k1_pubkey* a_c2,
        const secp256k1_pubkey* b_c1,
        const secp256k1_pubkey* b_c2
) {
    const secp256k1_pubkey* c1_points[2] = {a_c1, b_c1};
    if (secp256k1_ec_pubkey_combine(ctx, sum_c1, c1_points, 2) != 1) {
        return 0;
    }

    const secp256k1_pubkey* c2_points[2] = {a_c2, b_c2};
    if (secp256k1_ec_pubkey_combine(ctx, sum_c2, c2_points, 2) != 1) {
        return 0;
    }
    return 1;
}

int secp256k1_elgamal_subtract(
        const secp256k1_context* ctx,
        secp256k1_pubkey* diff_c1,
        secp256k1_pubkey* diff_c2,
        const secp256k1_pubkey* a_c1,
        const secp256k1_pubkey* a_c2,
        const secp256k1_pubkey* b_c1,
        const secp256k1_pubkey* b_c2
) {
    // To subtract, we add the negation: (A - B) is (A + (-B))
    // Make a local, modifiable copy of B's points.
    secp256k1_pubkey neg_b_c1 = *b_c1;
    secp256k1_pubkey neg_b_c2 = *b_c2;

    // Negate the copies
    if (secp256k1_ec_pubkey_negate(ctx, &neg_b_c1) != 1 ||
        secp256k1_ec_pubkey_negate(ctx, &neg_b_c2) != 1) {
        return 0; // Negation failed
    }

    // Now, add A and the negated copies of B
    const secp256k1_pubkey* c1_points[2] = {a_c1, &neg_b_c1};
    if (secp256k1_ec_pubkey_combine(ctx, diff_c1, c1_points, 2) != 1) {
        return 0;
    }

    const secp256k1_pubkey* c2_points[2] = {a_c2, &neg_b_c2};
    if (secp256k1_ec_pubkey_combine(ctx, diff_c2, c2_points, 2) != 1) {
        return 0;
    }

    return 1; // Success
}

// Helper function to concatenate data for hashing
static void build_hash_input(
        unsigned char* output_buffer,
        size_t buffer_size,
        const unsigned char* account_id,    // 20 bytes
        const unsigned char* mpt_issuance_id // 24 bytes
) {
    const char* domain_separator = "EncZero";
    size_t domain_len = strlen(domain_separator);
    size_t offset = 0;

    // Ensure buffer is large enough (should be checked by caller if necessary)
    // Size = strlen("EncZero") + 20 + 24 = 7 + 20 + 24 = 51 bytes

    memcpy(output_buffer + offset, domain_separator, domain_len);
    offset += domain_len;

    memcpy(output_buffer + offset, account_id, 20);
    offset += 20;

    memcpy(output_buffer + offset, mpt_issuance_id, 24);
    // offset += 24; // Final size is offset + 24
}

//The canonical encrypted zero

int generate_canonical_encrypted_zero(
        const secp256k1_context* ctx,
        secp256k1_pubkey* enc_zero_c1,
        secp256k1_pubkey* enc_zero_c2,
        const secp256k1_pubkey* pubkey,
        const unsigned char* account_id,     // 20 bytes
        const unsigned char* mpt_issuance_id // 24 bytes
) {

    unsigned char deterministic_scalar[32];
    unsigned char hash_input[51]; // Size calculated above

    /* 1. Create the input buffer for hashing */
    build_hash_input(hash_input, sizeof(hash_input), account_id, mpt_issuance_id);

    /* 2. Hash the buffer to create the deterministic scalar 'r' */
    do {
        // Hash the concatenated bytes
        SHA256(hash_input, sizeof(hash_input), deterministic_scalar);

        /* Note: If the hash output could be invalid (0 or >= n),
         * you might need to add a nonce/counter to hash_input
         * and re-hash in a loop until a valid scalar is produced. */
    } while (secp256k1_ec_seckey_verify(ctx, deterministic_scalar) != 1);

    /* 3. Encrypt the amount 0 using the deterministic scalar */
    return secp256k1_elgamal_encrypt(
            ctx,
            enc_zero_c1,
            enc_zero_c2,
            pubkey,
            0, /* The amount is zero */
            deterministic_scalar
    );
}

/**
 * Verifies that (c1, c2) is a valid ElGamal encryption of 'amount'
 * for 'pubkey_Q' using the revealed 'blinding_factor'.
 */
int secp256k1_elgamal_verify_encryption(
        const secp256k1_context* ctx,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pubkey_Q,
        uint64_t amount,
        const unsigned char* blinding_factor
) {
    secp256k1_pubkey expected_c1, mG, s_shared, expected_c2;
    unsigned char amount_scalar[32] = {0};
    unsigned char ser1[33], ser2[33];
    size_t len = 33;

    /* 1. Verify C1: Does blinding_factor * G == c1? */
    if (secp256k1_ec_pubkey_create(ctx, &expected_c1, blinding_factor) != 1) {
        return 0;
    }

    secp256k1_ec_pubkey_serialize(ctx, ser1, &len, c1, SECP256K1_EC_COMPRESSED);
    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser2, &len, &expected_c1, SECP256K1_EC_COMPRESSED);
    if (memcmp(ser1, ser2, 33) != 0) return 0;

    /* 2. Verify C2: Does amount * G + blinding_factor * Q == c2? */
    // Calculate mG
    for (int i = 0; i < 8; ++i) {
        amount_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
    }
    if (secp256k1_ec_pubkey_create(ctx, &mG, amount_scalar) != 1) return 0;

    // Calculate Shared Secret S = k * Q
    s_shared = *pubkey_Q;
    if (secp256k1_ec_pubkey_tweak_mul(ctx, &s_shared, blinding_factor) != 1) return 0;

    // Combine M + S
    const secp256k1_pubkey* pts[2] = {&mG, &s_shared};
    if (secp256k1_ec_pubkey_combine(ctx, &expected_c2, pts, 2) != 1) return 0;

    // Compare C2
    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser1, &len, c2, SECP256K1_EC_COMPRESSED);
    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser2, &len, &expected_c2, SECP256K1_EC_COMPRESSED);
    if (memcmp(ser1, ser2, 33) != 0) return 0;

    return 1; // Success: Encryption is valid
}