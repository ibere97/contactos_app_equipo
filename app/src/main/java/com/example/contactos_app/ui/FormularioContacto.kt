package com.example.contactos_app.ui

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.contactos_app.R
import com.example.contactos_app.data.Contacto
import com.example.contactos_app.viewmodel.ContactoViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

fun guardarImagenInterna(context: Context, uri: Uri): String {

    val input = context.contentResolver.openInputStream(uri)
    val archivo = File(context.filesDir, "contacto_${System.currentTimeMillis()}.jpg")
    val output = FileOutputStream(archivo)

    input?.copyTo(output)

    input?.close()
    output.close()

    return archivo.absolutePath
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContacto(
    navController: NavController,
    viewModel: ContactoViewModel,
    id: Int
) {

    var nombre by remember(id) { mutableStateOf("") }
    var telefono by remember(id) { mutableStateOf("") }
    var correo by remember(id) { mutableStateOf("") }
    var fotoUri by remember(id) { mutableStateOf<Uri?>(null) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var correoError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        fotoUri = uri
    }

    val contactoExistente by viewModel.contacto.collectAsStateWithLifecycle()

    LaunchedEffect(id, contactoExistente) {

        if (id == 0) {

            viewModel.limpiarContacto()

            nombre = ""
            telefono = ""
            correo = ""
            fotoUri = null

        } else {

            if (contactoExistente == null) {

                viewModel.cargarContacto(id)

            } else {

                nombre = contactoExistente!!.nombre
                telefono = contactoExistente!!.telefono
                correo = contactoExistente!!.correo

                fotoUri =
                    if (contactoExistente!!.foto.isNotEmpty())
                        Uri.parse(contactoExistente!!.foto)
                    else null
            }
        }
    }

    val titulo = if (id == 0) "Nuevo Contacto" else "Editar Contacto"

    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text(titulo, color = Color.White) },

                navigationIcon = {

                    IconButton(onClick = { navController.popBackStack() }) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )

                    }

                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3F51B5)
                )

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()

        ) {

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3F51B5))
                    .padding(vertical = 32.dp),

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                Box(contentAlignment = Alignment.BottomEnd) {

                    AsyncImage(

                        model = fotoUri
                            ?: contactoExistente?.foto
                            ?: R.drawable.ic_launcher_foreground,

                        contentDescription = "Foto",

                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.2f)),

                        contentScale = ContentScale.Crop

                    )

                    SmallFloatingActionButton(

                        onClick = { launcher.launch("image/*") },

                        containerColor = Color.White,
                        shape = CircleShape

                    ) {

                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color(0xFF3F51B5)
                        )

                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    nombre.ifBlank { "Nombre del Contacto" },
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

            }

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(

                    value = nombre,

                    onValueChange = {

                        if (it.length <= 50) {

                            nombre = it

                            nombreError =
                                if (nombre.isBlank())
                                    "El nombre es requerido"
                                else null
                        }

                    },

                    label = { Text("Nombre") },

                    leadingIcon = { Icon(Icons.Default.Person, null) },

                    trailingIcon = { Text("${nombre.length}/50") },

                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->

                            if (!focusState.isFocused && nombre.isNotBlank()) {

                                scope.launch {

                                    if (viewModel.nombreExiste(nombre, id)) {
                                        nombreError = "Este nombre ya existe"
                                    }

                                }

                            }

                        },

                    isError = nombreError != null,
                    supportingText = { nombreError?.let { Text(it) } }

                )

                OutlinedTextField(

                    value = correo,

                    onValueChange = {

                        correo = it

                        correoError =
                            when {
                                correo.isBlank() -> null
                                !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> "Correo no válido"
                                else -> null
                            }

                    },

                    label = { Text("Correo (opcional)") },

                    leadingIcon = { Icon(Icons.Default.Email, null) },

                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->

                            if (!focusState.isFocused && correo.isNotBlank()) {

                                scope.launch {

                                    if (viewModel.correoExiste(correo, id)) {
                                        correoError = "Este correo ya está registrado"
                                    }

                                }

                            }

                        },

                    isError = correoError != null,
                    supportingText = { correoError?.let { Text(it) } }

                )

                OutlinedTextField(

                    value = telefono,

                    onValueChange = {

                        if (it.length <= 10) {

                            telefono = it

                            telefonoError =
                                when {
                                    telefono.isBlank() -> "El teléfono es requerido"
                                    telefono.length < 10 -> "Debe tener 10 dígitos"
                                    else -> null
                                }

                        }

                    },

                    label = { Text("Móvil") },

                    leadingIcon = { Icon(Icons.Default.Phone, null) },

                    trailingIcon = { Text("${telefono.length}/10") },

                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->

                            if (!focusState.isFocused && telefono.length == 10) {

                                scope.launch {

                                    if (viewModel.telefonoExiste(telefono, id)) {
                                        telefonoError = "Este número ya está registrado"
                                    }

                                }

                            }

                        },

                    isError = telefonoError != null,
                    supportingText = { telefonoError?.let { Text(it) } }

                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(

                    onClick = {

                        scope.launch {

                            nombreError = null
                            correoError = null
                            telefonoError = null

                            var hayErrores = false

                            if (nombre.isBlank()) {
                                nombreError = "El nombre es requerido"
                                hayErrores = true
                            }

                            if (telefono.isBlank()) {
                                telefonoError = "El teléfono es requerido"
                                hayErrores = true
                            } else if (telefono.length < 10) {
                                telefonoError = "Debe tener 10 dígitos"
                                hayErrores = true
                            }

                            if (correo.isNotBlank()) {

                                if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                                    correoError = "Correo no válido"
                                    hayErrores = true
                                }

                                if (viewModel.correoExiste(correo, id)) {
                                    correoError = "Este correo ya está registrado"
                                    hayErrores = true
                                }

                            }

                            if (viewModel.nombreExiste(nombre, id)) {
                                nombreError = "Este nombre ya existe"
                                hayErrores = true
                            }

                            if (viewModel.telefonoExiste(telefono, id)) {
                                telefonoError = "Este número ya está registrado"
                                hayErrores = true
                            }

                            if (hayErrores) return@launch

                            val rutaFoto = if (
                                fotoUri != null &&
                                !fotoUri.toString().startsWith("/")
                            ) {

                                guardarImagenInterna(context, fotoUri!!)

                            } else {

                                fotoUri?.toString() ?: ""

                            }

                            val contacto = Contacto(
                                id = id,
                                nombre = nombre,
                                telefono = telefono,
                                correo = correo,
                                foto = rutaFoto
                            )

                            if (id == 0)
                                viewModel.guardar(contacto)
                            else
                                viewModel.actualizar(contacto)

                            navController.popBackStack()

                        }

                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F51B5)
                    ),

                    shape = RoundedCornerShape(12.dp)

                ) {

                    Text(
                        if (id == 0) "Guardar" else "Actualizar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                }

            }

        }

    }

}