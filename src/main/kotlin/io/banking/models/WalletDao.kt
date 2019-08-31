package io.banking.models

import java.util.concurrent.atomic.AtomicInteger

class WalletDao {
    val wallets = HashMap<Int, Wallet>()

    val currencies = hashMapOf<String, HashMap<String, Float>>(
        "euro" to hashMapOf<String, Float>(
            "usd" to 1.10f,
            "sterling" to 0.90f
        ),
        "usd" to hashMapOf<String, Float>(
            "euro" to 0.91f,
            "sterling" to 0.82f
        ),
        "sterling" to hashMapOf<String, Float>(
            "usd" to 1.22f,
            "euro" to 1.1f
        )
    )

    var lastId: AtomicInteger = AtomicInteger(wallets.size - 1)

    fun save(user_id: Int, currency: String): Int?{
        val id = lastId.incrementAndGet()
        wallets[id] = Wallet(id = id, user_id = user_id, balance = 0.0f, currency = currency)
        return id
    }

    fun findById(id: Int): Wallet? {
        return wallets[id]
    }

    fun findByUserId(user_id: Int): Wallet? {
        return wallets.values.find { it.user_id == user_id }
    }

    fun findByUserIdAndCurrency(user_id: Int, currency: String): Wallet? {
        return wallets.values.find { it.user_id == user_id && it.currency.equals(currency)}
    }

    fun addBalance(id: Int, balance: Float) {
        val wallet = findById(id)
        if (wallet != null) {
            wallets[id] = Wallet(id = id, user_id = wallet.user_id, balance = wallet.balance + balance,
                currency = wallet.currency)
        }
    }

    fun deductBalance(id: Int, balance: Float) {
        val wallet = findById(id)
        if (wallet != null) {
            wallets[id] = Wallet(id = id, user_id = wallet.user_id, balance = wallet.balance - balance,
                currency = wallet.currency)
        }
    }

    fun moneyConversion(amount: Float, fromCurrency: String, toCurrency: String): Float?{
        if(currencies[fromCurrency] == null || currencies[toCurrency] == null) return null
        return amount*currencies[fromCurrency]!![toCurrency]!!
    }

    fun update(id: Int, wallet: Wallet) {
        wallets.put(id,
            Wallet(id = id, user_id = wallet.user_id, balance = 0.0f, currency = wallet.currency)
        )
    }

    fun delete(id: Int) {
        wallets.remove(id)
    }
}