//Authors: NeilH, RichardAH
// (joke) test hook that doubles incoming XRP payments and sends it back
// April 1st 2021: Added (unfair) coin flip

#include <stdint.h>
#include "../hookapi.h"
#define DROPS_PER_XRP 1000000 // 1 million drops per XRP

int64_t cbak(int64_t reserved)
{
    return 0;
}

int64_t hook(int64_t reserved ) {
    uint8_t hook_account_id[20];
    if (hook_account(SBUF(hook_account_id)) < 0)
        rollback(SBUF("Vegas: Could not fetch hook account id."), 1);

    uint8_t source_account[20];
    int32_t source_account_len = otxn_field(SBUF(source_account), sfAccount);

    // compare the "From Account" (sfAccount) on the transaction with the account the hook is running on
    int fromHookAccount = 0; BUFFER_EQUAL(fromHookAccount, hook_account_id, source_account, 20);
    if (fromHookAccount)
        accept(SBUF("Vegas: Outgoing transaction. Accepting."), 1);

    // Amounts can be 384 bits or 64 bits. If the Amount is an XRP value it will be 64 bits.
    unsigned char amount_buffer[48];
    int64_t amount_len = otxn_field(SBUF(amount_buffer), sfAmount);
    if (amount_len != 8)
        rollback(SBUF("Vegas: Rejecting incoming non-XRP transaction"), 2);

    int64_t received_drops = AMOUNT_TO_DROPS(amount_buffer);
    if (received_drops > 100 * DROPS_PER_XRP)
        rollback(SBUF("Vegas: Rejecting bet greater than 100 XRP"), 3);
    if (received_drops < 1 * DROPS_PER_XRP)
        rollback(SBUF("Vegas: Rejecting bet for less than 1 XRP"), 3);

    // before we start calling hook-api functions we should tell the hook how many tx we intend to create
    etxn_reserve(1); // we are going to emit 1 transaction

    uint8_t next_nonce[32];
    nonce(next_nonce, 32);
    uint8_t hash[32];
    util_sha512h(SBUF(hash), SBUF(next_nonce));

    int64_t payment_multiplier = (hash[0] % 21);  // 0 to 20
    int64_t payment_divisor = 10;
    int64_t drops_to_send = received_drops * payment_multiplier / payment_divisor;
    // send back 1 drop if payout is zero. so you always know your bet was processed.
    if (drops_to_send == 0) drops_to_send = 1;

    int64_t fee_base = etxn_fee_base(PREPARE_PAYMENT_SIMPLE_SIZE);
    uint8_t tx[PREPARE_PAYMENT_SIMPLE_SIZE];

    // we will use an XRP payment macro, this will populate the buffer with a serialized binary transaction
    // Parameter list: ( buf_out, drops_amount, drops_fee, to_address, dest_tag, src_tag )
    PREPARE_PAYMENT_SIMPLE(tx, drops_to_send, fee_base, source_account, 0, 0);

    // emit the transaction
    uint8_t emithash[32];
    emit(SBUF(emithash), SBUF(tx));

    // accept and allow the original transaction through
    accept(SBUF("Vegas: You won! Funds emitted!"), 0);
    return 0;
}
