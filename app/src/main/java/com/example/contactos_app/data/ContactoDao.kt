package com.example.contactos_app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Query("SELECT * FROM contactos ORDER BY nombre ASC")
    fun obtenerContactos(): Flow<List<Contacto>>

    @Query("SELECT * FROM contactos WHERE id = :id")
    suspend fun obtenerContacto(id: Int): Contacto?

    @Query("SELECT * FROM contactos")
    suspend fun obtenerContactosLista(): List<Contacto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(contacto: Contacto)

    @Update
    suspend fun actualizar(contacto: Contacto)

    @Delete
    suspend fun eliminar(contacto: Contacto)
}