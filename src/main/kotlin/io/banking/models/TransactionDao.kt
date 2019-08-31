package io.banking.models

import java.util.concurrent.atomic.AtomicInteger

class TransactionDao{
    val transactions = hashMapOf<Int, Transaction>()

    var lastId: AtomicInteger = AtomicInteger(transactions.size - 1)

    fun save(desc: String, amount: Float, wallet_id: Int): Int?{
        val id = lastId.incrementAndGet()
        transactions[id] = Transaction(id = id, desc = desc, amount = amount, wallet_id = wallet_id)
        return id
    }

    fun findById(id: Int): Transaction? {
        return transactions[id]
    }

    fun update(id: Int, transaction: Transaction) {
        transactions[id] = Transaction(id = id, desc = transaction.desc, amount = transaction.amount,
            wallet_id = transaction.wallet_id)
    }

    fun delete(id: Int) {
        transactions.remove(id)
    }
}
