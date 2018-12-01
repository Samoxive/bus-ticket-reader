package com.glb301.busticketreader.database

data class DaoResult(val isError: Boolean, val error: String) {
    companion object {
        fun err(error: String) = DaoResult(true, error)
        fun ok() = DaoResult(false, "")
    }
}