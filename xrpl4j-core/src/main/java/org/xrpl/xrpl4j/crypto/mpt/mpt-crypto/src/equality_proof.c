/**
* @file equality_proof.c
* @brief Zero-Knowledge Proof of Knowledge of Plaintext and Randomness.
*
* This module implements a Sigma protocol (Chaum-Pedersen style) to prove that
        * an ElGamal ciphertext \f$ (C_1, C_2) \f$ encrypts a specific known plaintext
* \f$ m \f$ under a public key \f$ P \f$, and that the prover knows the
* randomness \f$ r \f$ used in the encryption.
*
* @details
        * **Statement:**
* The prover demonstrates knowledge of \f$ r \in \mathbb{Z}_q \f$ such that:
* \f[ C_1 = r \cdot G \f]
* \f[ C_2 = m \cdot G + r \cdot P \f]
*
* **Protocol:**
* 1. **Commitment:**
* Prover samples \f$ t \leftarrow \mathbb{Z}_q \f$ and computes:
* \f[ T_1 = t \cdot G \f]
* \f[ T_2 = t \cdot P \f]
*
* 2. **Challenge:**
* \f[ e = H(\text{"MPT_POK_PLAINTEXT_PROOF"} \parallel C_1 \parallel C_2 \parallel P \parallel T_1 \parallel T_2 \parallel \dots) \f]
*
* 3. **Response:**
* \f[ s = t + e \cdot r \pmod{q} \f]
*
* 4. **Verification:**
* Verifier checks:
* \f[ s \cdot G \stackrel{?}{=} T_1 + e \cdot C_1 \f]
* \f[ s \cdot P \stackrel{?}{=} T_2 + e \cdot (C_2 - m \cdot G) \f]
*
* **Context:**
* This proof is used in `ConfidentialMPTConvert` (explicit randomness verification)
* and `ConfidentialMPTClawback` (where the issuer proves the ciphertext matches
        * a revealed amount using their secret key, a variant of this logic).
*
* @see [Spec (ConfidentialMPT_20260201.pdf) Section 3.3.3] Optimized Ciphertext-Amount Consistency Protocol
*/

#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <stdlib.h>

/* --- Internal Helpers --- */

static int pubkey_equal(const secp256k1_context* ctx, const secp256k1_pubkey* pk1, const secp256k1_pubkey* pk2) {
    return secp256k1_ec_pubkey_cmp(ctx, pk1, pk2) == 0;
}

static int generate_random_scalar(const secp256k1_context* ctx, unsigned char* scalar) {
    do {
        if (RAND_bytes(scalar, 32) != 1) return 0;
    } while (!secp256k1_ec_seckey_verify(ctx, scalar));
    return 1;
}

static int compute_amount_point(const secp256k1_context* ctx, secp256k1_pubkey* mG, uint64_t amount) {
    unsigned char amount_scalar[32] = {0};
    /* Convert amount to 32-byte BIG-ENDIAN scalar */
    for (int i = 0; i < 8; ++i) {
        amount_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
    }
    return secp256k1_ec_pubkey_create(ctx, mG, amount_scalar);
}

/**
 * Streaming Hash Builder (Avoids large stack buffers)
 */
static void compute_challenge_equality(
        const secp256k1_context* ctx,
        unsigned char* e_out,
        const secp256k1_pubkey* c1, const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk,
        const secp256k1_pubkey* mG, /* NULL if amount == 0 */
        const secp256k1_pubkey* T1, const secp256k1_pubkey* T2,
        const unsigned char* tx_context_id)
{
    SHA256_CTX sha;
    unsigned char buf[33];
    unsigned char h[32];
    size_t len;
    const char* domain = "MPT_POK_PLAINTEXT_PROOF";

    SHA256_Init(&sha);
    SHA256_Update(&sha, domain, strlen(domain));

    // C1, C2, Pk
    len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, c1, SECP256K1_EC_COMPRESSED); SHA256_Update(&sha, buf, 33);
    len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, c2, SECP256K1_EC_COMPRESSED); SHA256_Update(&sha, buf, 33);
    len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, pk, SECP256K1_EC_COMPRESSED); SHA256_Update(&sha, buf, 33);

    // mG (Only if nonzero, logic from original code implied this structure)
    // Note: The original code had two separate functions. We unify them here.
    if (mG) {
        len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, mG, SECP256K1_EC_COMPRESSED); SHA256_Update(&sha, buf, 33);
    }

    // T1, T2
    len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, T1, SECP256K1_EC_COMPRESSED); SHA256_Update(&sha, buf, 33);
    len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, T2, SECP256K1_EC_COMPRESSED); SHA256_Update(&sha, buf, 33);

    if (tx_context_id) {
        SHA256_Update(&sha, tx_context_id, 32);
    }

    SHA256_Final(h, &sha);
    secp256k1_mpt_scalar_reduce32(e_out, h);
}

/* --- Public API --- */

int secp256k1_equality_plaintext_prove(
        const secp256k1_context* ctx,
        unsigned char* proof,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk_recipient,
        uint64_t amount,
        const unsigned char* randomness_r,
        const unsigned char* tx_context_id)
{
    unsigned char t[32];
    unsigned char e[32];
    unsigned char s[32];
    unsigned char term[32];
    secp256k1_pubkey T1, T2;
    secp256k1_pubkey mG;
    secp256k1_pubkey* mG_ptr = NULL;
    size_t len;
    int ok = 0;

    /* 0. Validate witness */
    if (!secp256k1_ec_seckey_verify(ctx, randomness_r)) goto cleanup;

    /* 1. Generate random t */
    if (!generate_random_scalar(ctx, t)) goto cleanup;

    /* 2. Compute commitments T1 = t*G, T2 = t*Pk */
    if (!secp256k1_ec_pubkey_create(ctx, &T1, t)) goto cleanup;

    T2 = *pk_recipient;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &T2, t)) goto cleanup;

    /* 3. Compute Challenge */
    if (amount > 0) {
        if (!compute_amount_point(ctx, &mG, amount)) goto cleanup;
        mG_ptr = &mG;
    }
    compute_challenge_equality(ctx, e, c1, c2, pk_recipient, mG_ptr, &T1, &T2, tx_context_id);

    /* 4. Compute s = t + e * r */
    memcpy(s, t, 32);
    memcpy(term, randomness_r, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup; // term = e*r
    if (!secp256k1_ec_seckey_tweak_add(ctx, s, term)) goto cleanup; // s = t + e*r

    /* 5. Serialize Proof */
    unsigned char* ptr = proof;
    len = 33;
    if (!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T1, SECP256K1_EC_COMPRESSED)) goto cleanup;
    ptr += 33;

    len = 33;
    if (!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T2, SECP256K1_EC_COMPRESSED)) goto cleanup;
    ptr += 33;

    memcpy(ptr, s, 32);

    ok = 1;

    cleanup:
    OPENSSL_cleanse(t, 32);
    OPENSSL_cleanse(term, 32);
    // s is public output, but good practice to clean stack copy
    OPENSSL_cleanse(s, 32);
    return ok;
}
int secp256k1_equality_plaintext_verify(
        const secp256k1_context* ctx,
        const unsigned char* proof,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk_recipient,
        uint64_t amount,
        const unsigned char* tx_context_id)
{
    secp256k1_pubkey T1, T2;
    unsigned char s[32];
    unsigned char e[32];
    secp256k1_pubkey mG;
    secp256k1_pubkey* mG_ptr = NULL;
    const unsigned char* ptr = proof;

    secp256k1_pubkey LHS, RHS, term;
    const secp256k1_pubkey* pts[2];
    int ok = 0; // Default to failure

    /* 1. Deserialize */
    if (!secp256k1_ec_pubkey_parse(ctx, &T1, ptr, 33)) goto cleanup; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &T2, ptr, 33)) goto cleanup; ptr += 33;
    memcpy(s, ptr, 32);

    // Check s != 0
    if (!secp256k1_ec_seckey_verify(ctx, s)) goto cleanup;

    /* 2. Recompute Challenge */
    if (amount > 0) {
        if (!compute_amount_point(ctx, &mG, amount)) goto cleanup;
        mG_ptr = &mG;
    }
    compute_challenge_equality(ctx, e, c1, c2, pk_recipient, mG_ptr, &T1, &T2, tx_context_id);

    /* 3. Verify Equations */

    /* --- Eq 1: s * G == T1 + e * C1 --- */
    if (!secp256k1_ec_pubkey_create(ctx, &LHS, s)) goto cleanup; // s*G

    term = *c1;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup; // e*C1
    pts[0] = &T1; pts[1] = &term;
    if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 2)) goto cleanup; // T1 + e*C1

    if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;

    /* --- Eq 2: s * Pk == T2 + e * (C2 - mG) --- */

    /* LHS = s * Pk */
    LHS = *pk_recipient;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &LHS, s)) goto cleanup;

    /* RHS Construction: Y = C2 - mG */
    secp256k1_pubkey Y = *c2;
    if (mG_ptr) {
        secp256k1_pubkey neg_mG = *mG_ptr;
        if (!secp256k1_ec_pubkey_negate(ctx, &neg_mG)) goto cleanup;
        pts[0] = c2; pts[1] = &neg_mG;
        if (!secp256k1_ec_pubkey_combine(ctx, &Y, pts, 2)) goto cleanup;
    }

    /* RHS = T2 + e * Y */
    term = Y;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup; // e*Y
    pts[0] = &T2; pts[1] = &term;
    if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 2)) goto cleanup;

    if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;

    ok = 1;

    cleanup:
    return ok;
}
