package com.glb301.busticketreader

import com.glb301.busticketreader.database.Station
import com.glb301.busticketreader.database.User
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
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

            get("/stations/{stationId}") {
                val stationIdRaw = context.parameters["userId"]!!.toIntOrNull()
                val stationId = if (stationIdRaw != null) {
                    stationIdRaw
                } else {
                    context.respond(HttpStatusCode.BadRequest, "Bad station id.")
                    return@get
                }

                val station = jd.open().use { handle -> User.findById(handle, stationId) }
                if (station == null) {
                    context.respond(HttpStatusCode.NotFound, "Station not found!")
                    return@get
                }

                context.respond(station)
            }

            post("/stations/new") {
                val body = context.receiveOrNull<Station>()
                if (body == null) {
                    context.respond(HttpStatusCode.BadRequest, "Bad station body.")
                    return@post
                }

                try {
                    jd.open().use { handle -> Station.insert(handle, body) }
                    context.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    context.respond(HttpStatusCode.BadRequest, "Could not create station.")
                }
            }

            get("/users") {
                context.respond(jd.open().use { handle -> User.findAll(handle) })
            }

            get("/users/{userId}") {
                val userIdRaw = context.parameters["userId"]!!.toIntOrNull()
                val userId = if (userIdRaw != null) {
                    userIdRaw
                } else {
                    context.respond(HttpStatusCode.BadRequest, "Bad user id.")
                    return@get
                }

                val user = jd.open().use { handle -> User.findById(handle, userId) }
                if (user == null) {
                    context.respond(HttpStatusCode.NotFound, "User not found!")
                    return@get
                }

                context.respond(user)
            }

            post("/users/new") {
                val body = context.receiveOrNull<User>()
                if (body == null) {
                    context.respond(HttpStatusCode.BadRequest, "Bad user body.")
                    return@post
                }

                try {
                    jd.open().use { handle -> User.insert(handle, body) }
                    context.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    context.respond(HttpStatusCode.BadRequest, "Could not create user.")
                }
            }

            post("/users/{userId}/withdrawTicket") {
                val userIdRaw = context.parameters["userId"]!!.toIntOrNull()
                val userId = if (userIdRaw != null) {
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