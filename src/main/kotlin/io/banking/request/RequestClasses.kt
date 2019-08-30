package io.banking.request

data class TransferRequest(val from_wallet_id: Int, val to_wallet_id: Int, val amount: Float)