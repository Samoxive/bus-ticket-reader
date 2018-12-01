package com.glb301.busticketreader.database

import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface TicketLogDao: SqlObject {
    @SqlUpdate("CREATE TABLE IF NOT EXISTS ticketLogs (id INTEGER PRIMARY KEY AUTO_INCREMENT, userId INTEGER, ")
    fun createTable()

    @SqlUpdate("INSERT INTO stations (id, name) VALUES (:station.id, :station.name)")
    fun insert(station: Station)

    @SqlQuery("SELECT * FROM station WHERE id = :id")
    fun findById(id: Int): Station?

    @SqlUpdate("UPDATE stations SET name = :station.name WHERE id = :station.id")
    fun update(station: Station)
}