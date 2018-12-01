package com.glb301.busticketreader.database

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.sqlobject.statement.SqlUpdate

private var stationIdCounter = 1

data class Station(val id: Int, val name: String) {
    companion object {
        @SqlUpdate()
        fun createTable(handle: Handle) {
            handle.createUpdate("CREATE TABLE IF NOT EXISTS stations (id INTEGER PRIMARY KEY, name TEXT)").execute()
        }

        @SqlUpdate()
        fun insert(handle: Handle, station: Station) {
            handle.createUpdate("INSERT INTO stations (id, name) VALUES (:id, :name)")
                .bind("id", station.id)
                .bind("name", station.name)
                .execute()
        }

        fun insertAutoIncrement(handle: Handle, station: Station) {
            insert(handle, station.copy(id = stationIdCounter))
            stationIdCounter++
        }

        fun findById(handle: Handle, id: Int): Station? = handle.createQuery("SELECT * FROM stations WHERE id = :id")
            .bind("id", id)
            .mapTo<Station>()
            .firstOrNull()

        fun findAll(handle: Handle): List<Station> = handle.createQuery("SELECT * FROM stations")
            .mapTo<Station>()
            .list()

        fun update(handle: Handle, station: Station) {
            handle.createUpdate("UPDATE stations SET name = :name WHERE id = :id")
                .bind("id", station.id)
                .bind("name", station.name)
                .execute()
        }

        fun insertTestEntities(handle: Handle) {
            insertAutoIncrement(handle, Station(0, "Alpaslan"))
            insertAutoIncrement(handle, Station(0, "Beyazsehir"))
            insertAutoIncrement(handle, Station(0, "Meydan"))
            insertAutoIncrement(handle, Station(0, "Talas"))
            insertAutoIncrement(handle, Station(0, "Erciyes"))
            insertAutoIncrement(handle, Station(0, "AGU"))
        }
    }
}
