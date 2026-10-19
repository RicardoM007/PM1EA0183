package com.uth.agendacontactos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.uth.agendacontactos.dao.ContactoDAO;
import com.uth.agendacontactos.models.Contacto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int CODIGO_GALERIA = 1;
    private static final int CODIGO_CAMARA = 2;

    private Spinner spinnerPais;
    private TextInputEditText txtNombre, txtTelefono, txtNota;
    private ImageView imgFoto;
    private ContactoDAO contactoDAO;
    private String rutaFoto = "";
    private int idEditar = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactoDAO = new ContactoDAO(this);
        spinnerPais = findViewById(R.id.spinnerPais);
        txtNombre = findViewById(R.id.txtNombre);
        txtTelefono = findViewById(R.id.txtTelefono);
        txtNota = findViewById(R.id.txtNota);
        imgFoto = findViewById(R.id.imgFoto);

        findViewById(R.id.btnFoto).setOnClickListener(v -> elegirFoto());
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarContacto());
        findViewById(R.id.btnVerContactos).setOnClickListener(v -> {
            Intent intent = new Intent(this, ContactosActivity.class);
            startActivity(intent);
        });

        idEditar = getIntent().getIntExtra("contacto_id", -1);
        if (idEditar != -1) {
            TextView titulo = findViewById(R.id.tituloRegistro);
            titulo.setText("Editar contacto");
            com.google.android.material.button.MaterialButton boton = findViewById(R.id.btnGuardar);
            boton.setText("Actualizar Contacto");
            if (savedInstanceState == null) cargarContacto();
        }

        if (savedInstanceState != null) {
            rutaFoto = savedInstanceState.getString("rutaFoto", "");
            mostrarFoto();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("rutaFoto", rutaFoto);
        super.onSaveInstanceState(outState);
    }

    // Elegimos una foto de la galería o tomamos una con la cámara.
    private void elegirFoto() {
        String[] opciones = {"Tomar fotografía", "Elegir de la galería"};
        new AlertDialog.Builder(this)
                .setTitle("Agregar fotografía")
                .setItems(opciones, (dialog, opcion) -> {
                    if (opcion == 0) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        try {
                            startActivityForResult(intent, CODIGO_CAMARA);
                        } catch (android.content.ActivityNotFoundException e) {
                            Toast.makeText(this, "No hay una aplicación de cámara", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        try {
                            startActivityForResult(intent, CODIGO_GALERIA);
                        } catch (android.content.ActivityNotFoundException e) {
                            Toast.makeText(this, "No se encontró la galería", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    // Aquí recibimos la imagen seleccionada o tomada con la cámara.
    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;

        try {
            if (requestCode == CODIGO_GALERIA && data.getData() != null) {
                Uri uri = data.getData();
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                rutaFoto = uri.toString();
            } else if (requestCode == CODIGO_CAMARA && data.getExtras() != null) {
                Object imagen = data.getExtras().get("data");
                if (imagen instanceof Bitmap) {
                    // La cámara entrega una foto reducida; la guardamos en el teléfono.
                    Bitmap foto = (Bitmap) imagen;
                    File archivo = new File(getFilesDir(),
                            "contacto_" + System.currentTimeMillis() + ".jpg");
                    try (FileOutputStream salida = new FileOutputStream(archivo)) {
                        if (!foto.compress(Bitmap.CompressFormat.JPEG, 90, salida)) {
                            throw new IOException("No se pudo guardar la fotografía");
                        }
                    }
                    rutaFoto = Uri.fromFile(archivo).toString();
                } else {
                    Toast.makeText(this, "La cámara no devolvió una fotografía", Toast.LENGTH_SHORT).show();
                }
            }
            mostrarFoto();
        } catch (IOException | SecurityException e) {
            Toast.makeText(this, "No se pudo guardar la fotografía", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarFoto() {
        if (!rutaFoto.isEmpty()) {
            imgFoto.setPadding(0, 0, 0, 0);
            imgFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgFoto.setImageURI(Uri.parse(rutaFoto));
        }
    }

    private void cargarContacto() {
        Contacto contacto = contactoDAO.obtener(idEditar);
        if (contacto == null) {
            Toast.makeText(this, "No se encontró el contacto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        txtNombre.setText(contacto.getNombre());
        txtTelefono.setText(contacto.getTelefono());
        txtNota.setText(contacto.getNota());
        rutaFoto = contacto.getImagen();
        mostrarFoto();

        ArrayAdapter<?> paises = (ArrayAdapter<?>) spinnerPais.getAdapter();
        for (int i = 0; i < paises.getCount(); i++) {
            if (paises.getItem(i).toString().equals(contacto.getPais())) {
                spinnerPais.setSelection(i);
                break;
            }
        }
    }

    private void guardarContacto() {
        txtNombre.setError(null);
        txtTelefono.setError(null);
        txtNota.setError(null);

        String nombre = txtNombre.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String nota = txtNota.getText().toString().trim();
        String pais = spinnerPais.getSelectedItem().toString();

        if (nombre.isEmpty()) {
            txtNombre.setError("Escriba el nombre");
            txtNombre.requestFocus();
            return;
        }
        if (!nombre.matches("[\\p{L} ]+")) {
            txtNombre.setError("El nombre solo debe contener letras");
            txtNombre.requestFocus();
            return;
        }
        if (telefono.isEmpty()) {
            txtTelefono.setError("Escriba el teléfono");
            txtTelefono.requestFocus();
            return;
        }
        if (!telefono.matches("[0-9]{8}")) {
            txtTelefono.setError("El teléfono debe tener 8 dígitos");
            txtTelefono.requestFocus();
            return;
        }
        if (nota.isEmpty()) {
            txtNota.setError("Escriba una nota");
            txtNota.requestFocus();
            return;
        }
        if (rutaFoto.isEmpty()) {
            Toast.makeText(this, "Agregue una fotografía", Toast.LENGTH_SHORT).show();
            return;
        }

        Contacto contacto = new Contacto();
        contacto.setPais(pais);
        contacto.setNombre(nombre);
        contacto.setTelefono(telefono);
        contacto.setNota(nota);
        contacto.setImagen(rutaFoto);

        if (idEditar == -1) {
            long resultado = contactoDAO.insertar(contacto);
            if (resultado > 0) {
                Toast.makeText(this, "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
                txtNombre.setText("");
                txtTelefono.setText("");
                txtNota.setText("");
                rutaFoto = "";
                imgFoto.setPadding(10, 10, 10, 10);
                imgFoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imgFoto.setImageResource(android.R.drawable.ic_menu_camera);
                txtNombre.requestFocus();
            } else {
                Toast.makeText(this, "No se pudo guardar el contacto", Toast.LENGTH_SHORT).show();
            }
        } else {
            contacto.setId(idEditar);
            int resultado = contactoDAO.actualizar(contacto);
            if (resultado > 0) {
                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "No se pudo actualizar el contacto", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
