package io.banking

import io.banking.controllers.TransactionController
import io.banking.controllers.UserController
import io.banking.controllers.WalletController
import io.banking.models.TransactionDao
import io.banking.models.UserDao
import io.banking.models.WalletDao
import io.banking.response.Response
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.BadRequestResponse
import java.lang.Exception

fun main() {

    val userDao = UserDao()
    val transactionDao = TransactionDao()
    val walletDao = WalletDao()

    val app = Javalin.create().apply{
        exception(Exception::class.java) {e, ctx -> ctx.json(Response(500, e.message.orEmpty()))}
        exception(BadRequestResponse::class.java) { e, ctx -> ctx.json(Response(400, e.message.orEmpty()))}
        error(404) {ctx -> ctx.json(Response(404, "Resource not found!!")) }
    }.start(7000)

    app.routes {
        path("users"){
            get { ctx -> UserController.getAll(ctx, userDao) }
            post { ctx -> UserController.createAPI(ctx, userDao, walletDao) }
        }

        path("wallets"){
            get("/:wallet-id/balance") { ctx -> WalletController.getWalletBalanceAPI(ctx, walletDao) }
            put("/:wallet-id/balance/:balance") { ctx -> WalletController.topUpWalletBalanceAPI(ctx, walletDao) }
            get { ctx -> WalletController.getAll(ctx, walletDao)}
            post { ctx -> WalletController.createAPI(ctx, userDao, walletDao)}
        }

        path("transactions"){
            get { ctx -> TransactionController.getAll(ctx, transactionDao)}
        }

        post("/transfer") { ctx -> TransactionController.transferMoneyAPI(ctx, userDao, walletDao, transactionDao)}
    }

}



