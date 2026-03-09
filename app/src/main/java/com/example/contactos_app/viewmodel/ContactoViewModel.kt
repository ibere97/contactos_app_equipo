package com.example.contactos_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contactos_app.data.Contacto
import com.example.contactos_app.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).contactoDao()

    // Lista automática desde Room
    val contactos = dao.obtenerContactos()

    private val _contacto = MutableStateFlow<Contacto?>(null)
    val contacto: StateFlow<Contacto?> = _contacto

    fun cargarContacto(id: Int) {
        viewModelScope.launch {
            _contacto.value = dao.obtenerContacto(id)
        }
    }

    fun limpiarContacto() {
        _contacto.value = null
    }

    fun guardar(contacto: Contacto) {
        viewModelScope.launch {
            dao.insertar(contacto)
        }
    }

    fun actualizar(contacto: Contacto) {
        viewModelScope.launch {
            dao.actualizar(contacto)
        }
    }

    fun eliminar(contacto: Contacto) {
        viewModelScope.launch {
            dao.eliminar(contacto)
        }
    }

    fun recargarContactos() {
        viewModelScope.launch {
            dao.obtenerContactos()
        }
    }

    fun telefonoValido(telefono: String): Boolean {
        return telefono.length == 10
    }

    // ===============================
    // VALIDACIONES
    // ===============================

    suspend fun nombreExiste(nombre: String, idActual: Int): Boolean {
        val lista = dao.obtenerContactosLista()
        return lista.any {
            it.nombre.equals(nombre, ignoreCase = true) && it.id != idActual
        }
    }

    suspend fun telefonoExiste(telefono: String, idActual: Int): Boolean {
        val lista = dao.obtenerContactosLista()
        return lista.any {
            it.telefono == telefono && it.id != idActual
        }
    }

    suspend fun correoExiste(correo: String, idActual: Int): Boolean {
        val lista = dao.obtenerContactosLista()
        return lista.any {
            it.correo.equals(correo, ignoreCase = true) && it.id != idActual
        }
    }

    // ===============================
    // VALIDAR TODO JUNTO
    // ===============================

    suspend fun validarContacto(
        nombre: String,
        telefono: String,
        correo: String,
        idActual: Int
    ): Map<String, String> {

        val errores = mutableMapOf<String, String>()

        if (nombreExiste(nombre, idActual)) {
            errores["nombre"] = "El nombre ya existe"
        }

        if (!telefonoValido(telefono)) {
            errores["telefono"] = "El teléfono debe tener 10 dígitos"
        } else if (telefonoExiste(telefono, idActual)) {
            errores["telefono"] = "El teléfono ya existe"
        }

        if (correoExiste(correo, idActual)) {
            errores["correo"] = "El correo ya existe"
        }

        return errores
    }
}