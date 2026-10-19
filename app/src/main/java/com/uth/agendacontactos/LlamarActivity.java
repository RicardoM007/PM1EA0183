package com.uth.agendacontactos;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.uth.agendacontactos.dao.ContactoDAO;
import com.uth.agendacontactos.models.Contacto;

// CONTROLADOR de pantalla 3: confirmación y permiso CALL_PHONE en tiempo de ejecución.
public class LlamarActivity extends AppCompatActivity {
    private static final int CODIGO_PERMISO = 100;
    private Contacto contacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);
        int id = getIntent().getIntExtra("contacto_id", -1);
        contacto = new ContactoDAO(this).obtener(id);
        if (contacto == null) {
            Toast.makeText(this, "Contacto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        TextView nombre = findViewById(R.id.nombreLlamar);
        TextView numero = findViewById(R.id.telefonoLlamar);
        ImageView foto = findViewById(R.id.fotoLlamar);
        nombre.setText(contacto.getNombre());
        numero.setText(contacto.getNumeroCompleto());
        foto.setImageURI(Uri.parse(contacto.getImagen()));
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnIniciarLlamada).setOnClickListener(v -> confirmarLlamada());
    }

    private void confirmarLlamada() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar llamada")
                .setMessage("¿Desea llamar a " + contacto.getNombre() + "?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", (dialog, which) -> comprobarPermiso())
                .show();
    }

    private void comprobarPermiso() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            hacerLlamada();
        } else {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CODIGO_PERMISO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hacerLlamada();
            } else {
                Toast.makeText(this, "Se necesita permiso para llamar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void hacerLlamada() {
        String telefono = "+" + contacto.getCodigoPais() + contacto.getTelefono();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + telefono));
        try {
            startActivity(intent);
        } catch (SecurityException | ActivityNotFoundException e) {
            Toast.makeText(this, "No se puede llamar desde este dispositivo", Toast.LENGTH_LONG).show();
        }
    }
}
