package io.banking.controllers

import io.banking.models.Wallet
import io.banking.models.WalletDao
import io.banking.response.Response
import io.javalin.http.Context

object WalletController{
    fun getAll(ctx: Context, walletDao: WalletDao){
        val response = object{
            val status: Int = 200
            val message: String = "Success"
            val wallets: Array<Wallet> = walletDao.wallets.values.toTypedArray()
        }
        ctx.status(200)
        ctx.json(response)
    }

    fun getWalletBalance(ctx: Context, walletDao: WalletDao){
        val wallet_id = ctx.pathParam("wallet-id").toInt()

        val wallet = walletDao.findById(wallet_id)
        if (wallet == null){
            ctx.status(404)
            return
        }

        val response = object{
            val status: Int = 200
            val message: String = "Success"
            val balance: Float = wallet.balance
        }
        ctx.status(200)
        ctx.json(response)
    }

    fun topUpWalletBalance(ctx: Context, walletDao: WalletDao){
        val addedBalance: Float = ctx.pathParam("balance").toFloat()
        val wallet_id = ctx.pathParam("wallet-id").toInt()

        val wallet = walletDao.findById(wallet_id)
        if (wallet == null){
            ctx.status(404)
            return
        }

        if(addedBalance <= 0){
            ctx.status(400)
            ctx.json(Response(400, "Added balance must be more the zero"))
            return
        }

        walletDao.addBalance(wallet.id, addedBalance)

        val response = object{
            val status: Int = 200
            val message: String = "Success"
            val balance: Float = wallet.balance+addedBalance
        }
        ctx.status(200)
        ctx.json(response)
    }
}