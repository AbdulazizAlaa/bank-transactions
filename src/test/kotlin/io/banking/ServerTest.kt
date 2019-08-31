package io.banking

import io.banking.controllers.TransactionController
import io.banking.controllers.UserController
import io.banking.controllers.WalletController
import io.banking.models.*
import io.banking.request.CreateWalletRequest
import io.banking.request.TransferRequest
import io.kotlintest.specs.StringSpec
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ServerTest: StringSpec({
    "create multiple users test" {
        val userDao = UserDao()
        val walletDao = WalletDao()
        var result = UserController.createUser(User(0, "aziz", "aziz@email.com", "123456"), userDao, walletDao)
        var status = result.status
        assertEquals(200, status, "Wrong user creation")

        // different user
        result = UserController.createUser(User(0, "David", "david@email.com", "123456"), userDao, walletDao)
        status = result.status
        assertEquals(200, status, "Wrong user creation")

        // same email
        result = UserController.createUser(User(0, "aziz", "aziz@email.com", "123456"), userDao, walletDao)
        status = result.status
        assertEquals(500, status, "user creation constraint not implemented")
    }


    "top up one of the wallets test" {
        val userDao = UserDao()
        val walletDao = WalletDao()

        var result = UserController.createUser(User(0, "aziz", "aziz@email.com", "123456"), userDao, walletDao)
        var status = result.status
        var user_one_id = (result.resObj as HashMap<String, Any>)["user_id"] as Int
        assertEquals(200, status, "Wrong user creation")

        // different user
        result = UserController.createUser(User(0, "David", "david@email.com", "123456"), userDao, walletDao)
        status = result.status
        var user_two_id = (result.resObj as HashMap<String, Any>)["user_id"] as Int
        assertEquals(200, status, "Wrong user creation")


        var wallet_one = walletDao.findByUserId(user_one_id)
        assertNotNull(wallet_one, "Wallet was not created")
        var wallet_one_id = wallet_one!!.id

        var wallet_two = walletDao.findByUserId(user_two_id)
        assertNotNull(wallet_one, "Wallet was not created")
        var wallet_two_id = wallet_two!!.id

        result = WalletController.topUpWalletBalance(wallet_one_id, 100.4f, walletDao)
        var balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(100.4f, balance, "Wrong balance amount")

        // check balance of first wallet
        result = WalletController.getWalletBalance(wallet_one_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(100.4f, balance, "Wrong balance amount")

        // check balance of second wallet
        result = WalletController.getWalletBalance(wallet_two_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(0.0f, balance, "Wrong balance amount")
    }

    "Transfer money between two users test" {
        val userDao = UserDao()
        val walletDao = WalletDao()
        val transactionDao = TransactionDao()

        var result = UserController.createUser(User(0, "aziz", "aziz@email.com", "123456"), userDao, walletDao)
        var status = result.status
        var user_one_id = (result.resObj as HashMap<String, Any>)["user_id"] as Int
        assertEquals(200, status, "Wrong user creation")

        // different user
        result = UserController.createUser(User(0, "David", "david@email.com", "123456"), userDao, walletDao)
        status = result.status
        var user_two_id = (result.resObj as HashMap<String, Any>)["user_id"] as Int
        assertEquals(200, status, "Wrong user creation")

        var wallet_one = walletDao.findByUserId(user_one_id)
        assertNotNull(wallet_one, "Wrong wallet creation")
        var wallet_one_id = wallet_one!!.id

        var wallet_two = walletDao.findByUserId(user_two_id)
        assertNotNull(wallet_one, "Wrong wallet creation")
        var wallet_two_id = wallet_two!!.id

        // add money to first wallet
        result = WalletController.topUpWalletBalance(wallet_one_id, 100.4f, walletDao)
        var balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(100.4f, balance, "Wrong balance amount")

        // check balance of first wallet
        result = WalletController.getWalletBalance(wallet_one_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(100.4f, balance, "Wrong balance amount")

        // check balance of second wallet
        result = WalletController.getWalletBalance(wallet_two_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(0.0f, balance, "Wrong balance amount")

        result = TransactionController.transferMoney(TransferRequest(wallet_two_id, wallet_one_id, 50.4f),
            userDao, walletDao, transactionDao)
        status = result.status
        assertEquals(400, status, "Wallet empty!! Transfer operation should fail")

        result = TransactionController.transferMoney(TransferRequest(wallet_one_id, wallet_two_id, 50.4f),
            userDao, walletDao, transactionDao)
        status = result.status
        assertEquals(200, status, "Transfer operation error")

        // check balance of first wallet
        result = WalletController.getWalletBalance(wallet_one_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(50.0f, balance, "Wrong balance amount")

        // check balance of second wallet
        result = WalletController.getWalletBalance(wallet_two_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(50.4f, balance, "Wrong balance amount")
    }

    "Transfer money between two users different currencies test" {
        val userDao = UserDao()
        val walletDao = WalletDao()
        val transactionDao = TransactionDao()

        var result = UserController.createUser(User(0, "aziz", "aziz@email.com", "123456"), userDao, walletDao)
        var status = result.status
        var user_one_id = (result.resObj as HashMap<String, Any>)["user_id"] as Int
        assertEquals(200, status, "Wrong user creation")

        // different user
        result = UserController.createUser(User(0, "David", "david@email.com", "123456"), userDao, walletDao)
        status = result.status
        var user_two_id = (result.resObj as HashMap<String, Any>)["user_id"] as Int
        assertEquals(200, status, "Wrong user creation")

        // create wallet with usd currency for user 2
        result = WalletController.createWallet(CreateWalletRequest(user_two_id, "usd"), userDao, walletDao)
        var wallet_two_usd_id = (result.resObj as HashMap<String, Any>)["wallet_id"] as Int

        var wallet_one = walletDao.findByUserIdAndCurrency(user_one_id, "euro")
        assertNotNull(wallet_one, "Wrong wallet creation")
        var wallet_one_id = wallet_one!!.id

        var wallet_two_euro = walletDao.findByUserIdAndCurrency(user_two_id, "euro")
        assertNotNull(wallet_two_euro, "Wrong wallet creation")
        var wallet_two_euro_id = wallet_two_euro!!.id

        // add money to first wallet
        result = WalletController.topUpWalletBalance(wallet_one_id, 100.4f, walletDao)
        var balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(100.4f, balance, "Wrong balance amount")

        // check balance of first wallet
        result = WalletController.getWalletBalance(wallet_one_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(100.4f, balance, "Wrong balance amount")

        // check balance of second wallet
        result = WalletController.getWalletBalance(wallet_two_usd_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(0.0f, balance, "Wrong balance amount")

        result = TransactionController.transferMoney(TransferRequest(wallet_two_usd_id, wallet_one_id, 50.4f),
            userDao, walletDao, transactionDao)
        status = result.status
        assertEquals(400, status, "Wallet empty!! Transfer operation should fail")

        result = TransactionController.transferMoney(TransferRequest(wallet_one_id, wallet_two_usd_id, 50.4f),
            userDao, walletDao, transactionDao)
        status = result.status
        assertEquals(200, status, "Transfer operation error")

        // check balance of first wallet
        result = WalletController.getWalletBalance(wallet_one_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(50.0f, balance, "Wrong balance amount")

        // check balance of second wallet
        result = WalletController.getWalletBalance(wallet_two_usd_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(50.4f*walletDao.currencies["euro"]!!["usd"]!!, balance, "Wrong balance amount")

        // try transfer from two wallets belongs to the same user
        result = TransactionController.transferMoney(TransferRequest(wallet_two_euro_id, wallet_two_usd_id, 50.4f),
            userDao, walletDao, transactionDao)
        status = result.status
        assertEquals(400, status, "Wallet empty!! Transfer operation should fail")

        result = TransactionController.transferMoney(TransferRequest(wallet_two_usd_id, wallet_two_euro_id, 50.4f),
            userDao, walletDao, transactionDao)
        status = result.status
        assertEquals(200, status, "Transfer operation error")

        // check balance of first wallet
        result = WalletController.getWalletBalance(wallet_two_usd_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals((50.4f*walletDao.currencies["euro"]!!["usd"]!!)-50.4f, balance, "Wrong balance amount")

        // check balance of second wallet
        result = WalletController.getWalletBalance(wallet_two_euro_id, walletDao)
        balance = (result.resObj as HashMap<String, Any>)["balance"] as Float
        assertEquals(50.4f*walletDao.currencies["usd"]!!["euro"]!!, balance, "Wrong balance amount")
    }

})