#include "secp256k1_mpt.h"
#include <string.h>
#include <openssl/sha.h>
#include <openssl/rand.h>

static void build_pok_challenge(unsigned char* e, const secp256k1_context* ctx,
                                const secp256k1_pubkey* pk, const secp256k1_pubkey* T,
                                const unsigned char* context_id) {
    SHA256_CTX sha;
    unsigned char buf[33];
    size_t len = 33;

    SHA256_Init(&sha);
    // Domain Separator from LaTeX spec
    SHA256_Update(&sha, "MPT_POK_SK_REGISTER", 19);

    secp256k1_ec_pubkey_serialize(ctx, buf, &len, pk, SECP256K1_EC_COMPRESSED);
    SHA256_Update(&sha, buf, 33);

    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, buf, &len, T, SECP256K1_EC_COMPRESSED);
    SHA256_Update(&sha, buf, 33);

    SHA256_Update(&sha, context_id, 32);
    SHA256_Final(e, &sha);
}

int secp256k1_mpt_pok_sk_prove(const secp256k1_context* ctx, unsigned char* proof,
                               const secp256k1_pubkey* pk, const unsigned char* sk,
                               const unsigned char* context_id) {
    unsigned char k[32], e[32], s[32];
    secp256k1_pubkey T;

    // 1. Sample k and T = kG
    do {
        if (RAND_bytes(k, 32) != 1) return 0;
    } while (!secp256k1_ec_seckey_verify(ctx, k));

    if (!secp256k1_ec_pubkey_create(ctx, &T, k)) return 0;

    // 2. Challenge e
    build_pok_challenge(e, ctx, pk, &T, context_id);

    // 3. Response s = k + e*sk (mod n)
    memcpy(s, sk, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, s, e)) return 0;
    if (!secp256k1_ec_seckey_tweak_add(ctx, s, k)) return 0;

    // 4. Serialize Proof: T (33 bytes) || s (32 bytes)
    size_t clen = 33;
    secp256k1_ec_pubkey_serialize(ctx, proof, &clen, &T, SECP256K1_EC_COMPRESSED);
    memcpy(proof + 33, s, 32);

    return 1;
}

int secp256k1_mpt_pok_sk_verify(const secp256k1_context* ctx, const unsigned char* proof,
                                const secp256k1_pubkey* pk, const unsigned char* context_id) {
    secp256k1_pubkey T, lhs, rhs, ePk;
    unsigned char e[32], s[32];

    // 1. Parse T and s
    if (!secp256k1_ec_pubkey_parse(ctx, &T, proof, 33)) return 0;
    memcpy(s, proof + 33, 32);

    // 2. Challenge e
    build_pok_challenge(e, ctx, pk, &T, context_id);

    // 3. Verify sG = T + ePk
    // LHS = s*G
    if (!secp256k1_ec_pubkey_create(ctx, &lhs, s)) return 0;

    // RHS = T + e*Pk
    ePk = *pk;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &ePk, e)) return 0;

    const secp256k1_pubkey* addends[2] = {&T, &ePk};
    if (!secp256k1_ec_pubkey_combine(ctx, &rhs, addends, 2)) return 0;

    // 4. Compare serialized points
    unsigned char ser_lhs[33], ser_rhs[33];
    size_t clen = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser_lhs, &clen, &lhs, SECP256K1_EC_COMPRESSED);
    clen = 33;
    secp256k1_ec_pubkey_serialize(ctx, ser_rhs, &clen, &rhs, SECP256K1_EC_COMPRESSED);

    return memcmp(ser_lhs, ser_rhs, 33) == 0;
}