package com.glb301.busticketreader.database

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

const val studentFee = 130
const val regularFee = 200
private var userIdCounter = 1

data class User(val id: Int, val name: String, val isStudent: Boolean, val balance: Int) {
    companion object {
        fun createTable(handle: Handle) {
            handle.createUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT, isStudent BOOLEAN, balance INTEGER)")
                .execute()
        }

        fun insert(handle: Handle, user: User) {
            handle.createUpdate("INSERT INTO users (id, name, isStudent, balance) VALUES (:id, :name, :isStudent, :balance)")
                .bind("id", user.id)
                .bind("name", user.name)
                .bind("isStudent", user.isStudent)
                .bind("balance", user.balance)
                .execute()
        }

        fun insertAutoIncrement(handle: Handle, user: User) {
            insert(handle, user.copy(id = userIdCounter))
            userIdCounter++
        }

        fun findById(handle: Handle, id: Int): User? = handle.createQuery("SELECT * FROM users WHERE id = :id")
            .bind("id", id)
            .mapTo<User>()
            .firstOrNull()

        fun findAll(handle: Handle): List<User> = handle.createQuery("SELECT * from users")
            .mapTo<User>()
            .list()

        fun update(handle: Handle, user: User) {
            handle.createUpdate("UPDATE users SET name = :name, isStudent = :isStudent, balance = :balance WHERE id = :id")
                .bind("id", user.id)
                .bind("name", user.name)
                .bind("isStudent", user.isStudent)
                .bind("balance", user.balance)
                .execute()
        }

        fun withdrawTicket(handle: Handle, user: User): DaoResult {
            val withdrawAmount = if (user.isStudent) studentFee else regularFee
            val newBalance = user.balance - withdrawAmount
            if (newBalance < 0) {
                return DaoResult.err("Not enough balance!")
            }

            update(handle, user.copy(balance = newBalance))
            return DaoResult.ok()
        }

        fun depositMoney(handle: Handle, user: User, amount: Int) {
            update(handle, user.copy(balance = user.balance + amount))
        }

        fun insertTestEntities(handle: Handle) {
            insertAutoIncrement(handle, User(0, "Samil", true, 1000))
            insertAutoIncrement(handle, User(0, "Ozan", false, 1000))
            insertAutoIncrement(handle, User(0, "Alina", true, 1000))
            insertAutoIncrement(handle, User(0, "Halil", true, 2000))
            insertAutoIncrement(handle, User(0, "Fatih", true, 2000))
            insertAutoIncrement(handle, User(0, "Muhammet", false, 2000))
        }
    }
}
