#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "secp256k1_mpt.h"
#include <openssl/rand.h>

void test_link_proof() {
    secp256k1_context* ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);

    unsigned char sk[32], r[32], rho[32], context_id[32];
    uint64_t amount = 12345;
    secp256k1_pubkey pk, c1, c2, pcm;
    unsigned char proof[195];

    printf("DEBUG: Starting Link Proof test with NUMS generators...\n");

    // 1. Setup Keys and Randomness
    do { RAND_bytes(sk, 32); } while (!secp256k1_ec_seckey_verify(ctx, sk));
    assert(secp256k1_ec_pubkey_create(ctx, &pk, sk));

    RAND_bytes(r, 32);
    RAND_bytes(rho, 32);
    RAND_bytes(context_id, 32);

    // 2. Create ElGamal Ciphertext (C1, C2)
    // C1 = r * G
    assert(secp256k1_ec_pubkey_create(ctx, &c1, r));
    // C2 = m * G + r * Pk
    secp256k1_pubkey mG, rPk;
    unsigned char m_scalar[32] = {0};
    for(int i=0; i<8; i++) m_scalar[31-i] = (amount >> (i*8)) & 0xFF;
    assert(secp256k1_ec_pubkey_create(ctx, &mG, m_scalar));
    rPk = pk;
    assert(secp256k1_ec_pubkey_tweak_mul(ctx, &rPk, r));
    const secp256k1_pubkey* addends[] = {&mG, &rPk};
    assert(secp256k1_ec_pubkey_combine(ctx, &c2, addends, 2));

    // 3. Create Pedersen Commitment (PCm)
    // Using the NEW centralized function that uses NUMS H internally
    assert(secp256k1_mpt_pedersen_commit(ctx, &pcm, amount, rho));

    // 4. Generate Proof
    assert(secp256k1_elgamal_pedersen_link_prove(ctx, proof, &c1, &c2, &pk, &pcm, amount, r, rho, context_id));
    printf("SUCCESS: Link Proof generated.\n");

    // 5. Verify Proof
    assert(secp256k1_elgamal_pedersen_link_verify(ctx, proof, &c1, &c2, &pk, &pcm, context_id));
    printf("SUCCESS: Link Proof verified against NUMS H.\n");

    // 6. Test Failure (Wrong Amount)
    secp256k1_pubkey pcm_wrong;
    assert(secp256k1_mpt_pedersen_commit(ctx, &pcm_wrong, amount + 1, rho));
    assert(secp256k1_elgamal_pedersen_link_verify(ctx, proof, &c1, &c2, &pk, &pcm_wrong, context_id) == 0);
    printf("SUCCESS: Invalid commitment correctly rejected.\n");

    secp256k1_context_destroy(ctx);
}

int main() {
    test_link_proof();
    return 0;
}
