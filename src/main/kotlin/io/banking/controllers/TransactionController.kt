package io.banking.controllers

import io.banking.models.*
import io.banking.request.TransferRequest
import io.banking.response.Response
import io.banking.response.ResponseContainer
import io.javalin.http.Context

object TransactionController {

    fun getAll(ctx: Context, transactionDao: TransactionDao){
        val response = hashMapOf(
            "status" to 200,
            "message" to "Success",
            "users" to transactionDao.transactions.values.toTypedArray()
        )
        ctx.status(200)
        ctx.json(response)
    }

    fun transferMoneyAPI(ctx: Context, userDao: UserDao, walletDao: WalletDao, transactionDao: TransactionDao){

        // get request object
        // Should contain (amount to transfer, currency, from 'wallet', to 'wallet')
        var request: TransferRequest? = ctx.bodyValidator<TransferRequest>()
            .check({it.amount != null && it.from_wallet_id != null && it.to_wallet_id != null}, "All fields are required")
            .check({it.amount > 0}, "Transfer amount can not be negative")
            .check({it.from_wallet_id >= 0}, "Wrong wallet id format")
            .check({it.to_wallet_id >= 0}, "Wrong wallet id format")
            .check({it.to_wallet_id != it.from_wallet_id}, "Can not transfer between one wallet")
            .getOrNull()

        if(request==null) return



        val response = transferMoney(request, userDao, walletDao, transactionDao)

        ctx.status(response.status)
        if(response.resObj!=null) ctx.json(response.resObj)
    }

    fun transferMoney(request: TransferRequest, userDao: UserDao, walletDao: WalletDao, transactionDao: TransactionDao): ResponseContainer{
        // fetch from and to wallets entities
        val fromWallet = walletDao.findById(request.from_wallet_id)
        val toWallet = walletDao.findById(request.to_wallet_id)
        // handle failures in wallet retrieval
        if(fromWallet==null || toWallet==null){
            return ResponseContainer(404, null)
        }

        var balance: Float? = request.amount
        var movedBalance: Float? = request.amount

        // make sure both wallets have same currency
        if(!fromWallet.currency.equals(toWallet.currency)){
            // handle currency conversion
            movedBalance = walletDao.moneyConversion(balance!!, fromWallet.currency, toWallet.currency)

            if(movedBalance==null){
                return ResponseContainer(400, Response(400, "Could not do currency conversion"))
            }
        }

        // make sure amount is available in the from wallet balance
        if(fromWallet.balance<balance!!){
            return ResponseContainer(400, Response(400, "User balance is not sufficient"))
        }

        // make the transfer between the wallets and save changes
        // deduct from fromWallet
        walletDao.deductBalance(fromWallet.id, balance!!)
        // add to toWallet
        walletDao.addBalance(toWallet.id, movedBalance!!)

        // create a transaction for this action for both wallets
        val fromUser = userDao.findById(fromWallet.user_id)
        val toUser = userDao.findById(toWallet.user_id)

        var fromName = ""
        if(fromUser!=null) fromName = fromUser.name
        var toName = ""
        if(toUser!=null) toName = toUser.name

        // create a deduct transaction for fromWallet
        transactionDao.save("Sent a transfer of ${movedBalance} ${fromWallet.currency} to User ${toName}", movedBalance, fromWallet.id)
        // create a add transaction for toWallet
        transactionDao.save("Received a transfer of ${movedBalance} ${toWallet.currency} from User ${fromName}", movedBalance, toWallet.id)

        return ResponseContainer(200, Response(200, "Money transfer is successful"))
    }
}