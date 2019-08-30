package io.banking.models

// represents user entity should handle authentication events and other user related actions
data class User(var id: Int, var name: String, var email: String, var pass: String)

// represents the user Account entity. A user can have multiple accounts (for each currency) under the same user entity
data class Wallet(var id: Int, var user_id: Int, var balance: Float, var currency: String)

// transaction entity represents the money transfer record. It is associated with the wallet corresponding entity
data class Transaction(var id: Int, var desc: String, var amount: Float, var wallet_id: Int)
