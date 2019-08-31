package io.banking.controllers

import io.banking.models.User
import io.banking.models.UserDao
import io.banking.models.WalletDao
import io.banking.response.Response
import io.banking.response.ResponseContainer
import io.javalin.http.Context

object UserController {
    fun createAPI(ctx: Context, userDao: UserDao, walletDao: WalletDao){
        val user = ctx.body<User>()
        val response = createUser(user, userDao, walletDao)

        ctx.status(response.status)
        if(response.resObj!=null) ctx.json(response.resObj)
    }

    fun createUser(user: User, userDao: UserDao, walletDao: WalletDao): ResponseContainer{
        // check if user exists
        if(userDao.findByEmail(user.email) != null){
            return ResponseContainer(500, Response(500, "User already exists!!"))
        }

        // create user account
        val userId = userDao.save(user.name, user.email, user.pass)

        // create Wallet and connected to the user with base currency Euro
        val walletId = walletDao.save(userId, "euro")

        val response = hashMapOf<String, Any>(
            "status" to 200,
            "message" to "Success",
            "user_id" to userId
        )
        return ResponseContainer(200, response)
    }

    fun getAll(ctx: Context, userDao: UserDao){
        val response = hashMapOf(
            "status" to 200,
            "message" to "Success",
            "users" to userDao.users.values.toTypedArray()
        )
        ctx.status(200)
        ctx.json(response)
    }
}