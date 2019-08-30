package io.banking.models

import java.util.concurrent.atomic.AtomicInteger

class UserDao{

    val users = hashMapOf<Int, User>()

    var lastId: AtomicInteger = AtomicInteger(users.size - 1)

    fun save(name: String, email: String, pass: String): Int {
        val id = lastId.incrementAndGet()
        users.put(id, User(id = id, name = name, email = email, pass = pass))
        return id
    }

    fun findById(id: Int): User? {
        return users[id]
    }

    fun findByEmail(email: String): User? {
        return users.values.find { it.email == email }
    }

    fun update(id: Int, user: User) {
        users.put(id, User(name = user.name, email = user.email, id = id, pass = user.pass))
    }

    fun delete(id: Int) {
        users.remove(id)
    }

}