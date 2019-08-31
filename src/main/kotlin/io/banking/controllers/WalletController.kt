package io.banking.controllers

import io.banking.models.UserDao
import io.banking.models.WalletDao
import io.banking.request.CreateWalletRequest
import io.banking.response.Response
import io.banking.response.ResponseContainer
import io.javalin.http.Context

object WalletController{
    fun createAPI(ctx: Context, userDao: UserDao, walletDao: WalletDao){
        val request = ctx.bodyValidator<CreateWalletRequest>()
            .check({it.currency!=null && it.user_id!=null}, "All fields are required")
            .check({walletDao.currencies[it.currency]!=null}, "Must use a valid currency")
            .getOrNull()

        if(request==null) return

        val response = createWallet(request, userDao, walletDao)

        ctx.status(response.status)
        if(response.resObj!=null) ctx.json(response.resObj)
    }

    fun createWallet(request: CreateWalletRequest, userDao: UserDao, walletDao: WalletDao): ResponseContainer{
        val user = userDao.findById(request.user_id)
        if(user==null){
            return ResponseContainer(404, null)
        }

        val wallet_id = walletDao.save(request.user_id, request.currency)

        val response = hashMapOf(
            "status" to 200,
            "message" to "Success",
            "wallet_id" to wallet_id
        )
        return ResponseContainer(200, response)
    }

    fun getAll(ctx: Context, walletDao: WalletDao){
        val response = hashMapOf(
            "status" to 200,
            "message" to "Success",
            "wallets" to walletDao.wallets.values.toTypedArray()
        )
        ctx.status(200)
        ctx.json(response)
    }

    fun getWalletBalanceAPI(ctx: Context, walletDao: WalletDao){
        val wallet_id = ctx.pathParam("wallet-id").toInt()

        val response = getWalletBalance(wallet_id, walletDao)

        ctx.status(response.status)
        if(response.resObj!=null) ctx.json(response.resObj)
    }

    fun getWalletBalance(wallet_id: Int, walletDao: WalletDao): ResponseContainer{
        val wallet = walletDao.findById(wallet_id)
        if (wallet == null){
            return ResponseContainer(404, null)
        }

        val response = hashMapOf(
            "status" to 200,
            "message" to "Success",
            "balance" to wallet.balance
        )
        return ResponseContainer(200, response)
    }

    fun topUpWalletBalanceAPI(ctx: Context, walletDao: WalletDao){
        val addedBalance: Float = ctx.pathParam("balance").toFloat()
        val wallet_id = ctx.pathParam("wallet-id").toInt()

        val response = topUpWalletBalance(wallet_id, addedBalance, walletDao)

        ctx.status(response.status)
        if(response.resObj!=null) ctx.json(response.resObj)
    }

    fun topUpWalletBalance(wallet_id: Int, addedBalance: Float, walletDao: WalletDao): ResponseContainer{
        val wallet = walletDao.findById(wallet_id)
        if (wallet == null){
            return ResponseContainer(404, null)
        }

        if(addedBalance <= 0){
            return ResponseContainer(400, Response(400, "Added balance must be more the zero"))
        }

        walletDao.addBalance(wallet.id, addedBalance)

        val response = hashMapOf(
            "status" to 200,
            "message" to "Success",
            "balance" to wallet.balance+addedBalance
        )
        return ResponseContainer(200, response)
    }
}