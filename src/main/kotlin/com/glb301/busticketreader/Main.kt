package com.glb301.busticketreader

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.glb301.busticketreader.database.Station
import com.glb301.busticketreader.database.User
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin

fun main(args: Array<String>) {
    val hikariConfig = HikariConfig()
    hikariConfig.jdbcUrl = "jdbc:h2:mem:transport"
    val dataSource = HikariDataSource(hikariConfig)
    val jd = Jdbi.create(dataSource)
    jd.installPlugin(KotlinPlugin())
    jd.installPlugin(KotlinSqlObjectPlugin())

    jd.open().use {
        User.createTable(it)
        User.insertTestEntities(it)
        Station.createTable(it)
        Station.insertTestEntities(it)
    }

    embeddedServer(Netty, 8080) {
        install(ContentNegotiation) {
            gson { setPrettyPrinting() }
        }
        routing {
            get("/stations") {
                context.respond(jd.open().use { handle ->
                    Station.findAll(handle)
                })
            }

            get("/users") {
                context.respond(jd.open().use { handle -> User.findAll(handle) })
            }

            post("/users/{userId}/withdrawTicket") {
                val userIdRaw = context.parameters["userId"]!!.toIntOrNull()
                val userId = if (userIdRaw != null)  {
                    userIdRaw
                } else {
                    context.respond(HttpStatusCode.BadRequest, "Bad user id")
                    return@post
                }

                jd.open().use { handle ->
                    val userEntity = User.findById(handle, userId)
                    if (userEntity == null) {
                        context.respond(HttpStatusCode.NotFound, "User not found!")
                    } else {
                        User.withdrawTicket(handle, userEntity)
                        context.respond(User.findById(handle, userId)!!.balance)
                    }
                }
            }

            post("/users/{userId}/deposit/{amount}") {
                val userIdRaw = context.parameters["userId"]!!.toIntOrNull()
                val amountRaw = context.parameters["amount"]!!.toIntOrNull()
                val (userId, amount) = if (userIdRaw != null && amountRaw != null) {
                    userIdRaw to amountRaw
                } else {
                    context.respond(HttpStatusCode.BadRequest, "Bad user id and deposit amount")
                    return@post
                }

                jd.open().use { handle ->
                    val userEntity = User.findById(handle, userId)
                    if (userEntity == null) {
                        context.respond(HttpStatusCode.NotFound, "User not found!")
                    } else {
                        User.depositMoney(handle, userEntity, amount)
                        context.respond(User.findById(handle, userId)!!.balance)
                    }
                }
            }
        }
    }.start(wait = true)
    println("hello")
}