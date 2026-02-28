/**
 * @file proof_pok_sk.c
 * @brief Schnorr Proof of Knowledge (PoK) for Discrete Logarithm.
 *
 * This module implements a non-interactive zero-knowledge proof (NIZK) that
 * allows a user to prove possession of the secret key `sk` corresponding to
 * a public key `pk`, without revealing `sk`.
 *
 * @details
 * **Protocol (Schnorr Identification Scheme):**
 *
 * Given a public key \f$ P = sk \cdot G \f$, the protocol proves knowledge of \f$ sk \f$.
 * It is made non-interactive using the Fiat-Shamir transform.
 *
 * 1. **Commitment:**
 * Prover samples random nonce \f$ k \leftarrow \mathbb{Z}_q \f$ and computes:
 * \f[ T = k \cdot G \f]
 *
 * 2. **Challenge (Fiat-Shamir):**
 * The challenge \f$ e \f$ is derived deterministically:
 * \f[ e = H(\text{"MPT_POK_SK_REGISTER"} \parallel P \parallel T \parallel \text{ContextID}) \f]
 *
 * 3. **Response:**
 * Prover computes scalar:
 * \f[ s = k + e \cdot sk \pmod{n} \f]
 *
 * 4. **Verification:**
 * Verifier reconstructs \f$ e \f$ and checks:
 * \f[ s \cdot G \stackrel{?}{=} T + e \cdot P \f]
 *
 * **Security Context:**
 * This proof is used during account initialization/registration to prevent
 * Rogue Key attacks (where an attacker registers a key constructed from another
 * user's key to cancel it out) and to ensure the user actually controls the
 * ElGamal key being registered.
 *
 * @see [Spec (ConfidentialMPT_20260201.pdf) Section 3.3.2] Proof of Knowledge of Secret Key
 */
#include "secp256k1_mpt.h"
#include <string.h>
#include <openssl/sha.h>
#include <openssl/rand.h>
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

static void build_pok_challenge(
        const secp256k1_context* ctx,
        unsigned char* e_out,
        const secp256k1_pubkey* pk,
        const secp256k1_pubkey* T,
        const unsigned char* context_id)
{
    SHA256_CTX sha;
    unsigned char buf[33];
    unsigned char h[32];
    size_t len;
    const char* domain = "MPT_POK_SK_REGISTER";

    SHA256_Init(&sha);
    SHA256_Update(&sha, domain, strlen(domain));

    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, buf, &len, pk, SECP256K1_EC_COMPRESSED);
    SHA256_Update(&sha, buf, 33);

    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, buf, &len, T, SECP256K1_EC_COMPRESSED);
    SHA256_Update(&sha, buf, 33);

    if (context_id) {
        SHA256_Update(&sha, context_id, 32);
    }

    SHA256_Final(h, &sha);
    secp256k1_mpt_scalar_reduce32(e_out, h);
}

/* --- Public API --- */

int secp256k1_mpt_pok_sk_prove(
        const secp256k1_context* ctx,
        unsigned char* proof_out,
        const secp256k1_pubkey* pk,
        const unsigned char* sk,
        const unsigned char* context_id)
{
    unsigned char k[32];
    unsigned char e[32];
    unsigned char s[32];
    unsigned char term[32];
    secp256k1_pubkey T;
    size_t len;
    int ok = 0;

    if (!secp256k1_ec_seckey_verify(ctx, sk)) return 0;
    if (!generate_random_scalar(ctx, k)) goto cleanup;
    if (!secp256k1_ec_pubkey_create(ctx, &T, k)) goto cleanup;

    build_pok_challenge(ctx, e, pk, &T, context_id);

    // s = k + e*sk
    memcpy(term, sk, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
    memcpy(s, k, 32);
    if (!secp256k1_ec_seckey_tweak_add(ctx, s, term)) goto cleanup;

    // Serialize: T (33) || s (32)
    unsigned char* ptr = proof_out;
    len = 33;
    if (!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T, SECP256K1_EC_COMPRESSED)) goto cleanup;
    ptr += 33;
    memcpy(ptr, s, 32);

    ok = 1;

    cleanup:
    OPENSSL_cleanse(k, 32);
    OPENSSL_cleanse(term, 32);
    OPENSSL_cleanse(s, 32);
    return ok;
}

int secp256k1_mpt_pok_sk_verify(
        const secp256k1_context* ctx,
        const unsigned char* proof,  // Caller MUST ensure this is at least 65 bytes
        const secp256k1_pubkey* pk,
        const unsigned char* context_id)
{
    secp256k1_pubkey T, LHS, RHS, ePk;
    unsigned char e[32], s[32];
    const unsigned char* ptr = proof;
    int ok = 0;

    /* 1. Parse T (33 bytes) */
    if (!secp256k1_ec_pubkey_parse(ctx, &T, ptr, 33)) goto cleanup;
    ptr += 33;

    /* 2. Parse s (32 bytes) */
    memcpy(s, ptr, 32);
    if (!secp256k1_ec_seckey_verify(ctx, s)) goto cleanup;

    /* 3. Recompute Challenge */
    build_pok_challenge(ctx, e, pk, &T, context_id);

    /* 4. Verify Equation: s*G == T + e*Pk */
    if (!secp256k1_ec_pubkey_create(ctx, &LHS, s)) goto cleanup;

    ePk = *pk;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &ePk, e)) goto cleanup;

    const secp256k1_pubkey* addends[2] = {&T, &ePk};
    if (!secp256k1_ec_pubkey_combine(ctx, &RHS, addends, 2)) goto cleanup;

    if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;

    ok = 1;

    cleanup:
    return ok;
}
