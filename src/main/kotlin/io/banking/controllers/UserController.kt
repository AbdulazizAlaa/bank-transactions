package io.banking.controllers

import io.banking.models.User
import io.banking.models.UserDao
import io.banking.models.WalletDao
import io.banking.response.Response
import io.javalin.http.Context

object UserController {
    fun create(ctx: Context, userDao: UserDao, walletDao: WalletDao){
        val user = ctx.body<User>()
        // check if user exists
        if(userDao.findByEmail(user.email) != null){
            ctx.status(500)
            ctx.json(Response(500, "User already exists!!"))
            return
        }

        // create user account
        val userId = userDao.save(user.name, user.email, user.pass)

        // create Wallet and connected to the user with base currency Euro
        val walletId = walletDao.save(userId, "euro")

        val response = object{
            val status: Int = 200
            val message: String = "Success"
            val user_id: Int = userId
        }
        ctx.status(200)
        ctx.json(response)
    }

    fun getAll(ctx: Context, userDao: UserDao){
        val response = object{
            val status: Int = 200
            val message: String = "Success"
            val users: Array<User> = userDao.users.values.toTypedArray()
        }
        ctx.status(200)
        ctx.json(response)
    }
}