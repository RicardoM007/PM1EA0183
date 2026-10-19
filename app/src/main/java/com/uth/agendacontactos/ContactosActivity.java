package com.uth.agendacontactos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.uth.agendacontactos.dao.ContactoDAO;
import com.uth.agendacontactos.models.Contacto;
import java.util.ArrayList;
import java.util.Locale;

// CONTROLADOR de la segunda pantalla: consulta y acciones sobre registros SQLite.
public class ContactosActivity extends AppCompatActivity {
    private ContactoDAO dao;
    private ListView lista;
    private EditText txtBuscar;
    private ArrayList<Contacto> todos = new ArrayList<>();
    private ArrayList<Contacto> visibles = new ArrayList<>();
    private Contacto seleccionado;
    private ArrayAdapter<Contacto> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);
        dao = new ContactoDAO(this);
        lista = findViewById(R.id.listaContactos);
        lista.setEmptyView(findViewById(R.id.txtVacio));
        txtBuscar = findViewById(R.id.txtBuscar);

        adaptador = new ArrayAdapter<Contacto>(this, R.layout.item_contacto, R.id.itemNombre, visibles) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View vista = convertView;
                if (vista == null) {
                    vista = getLayoutInflater().inflate(R.layout.item_contacto, parent, false);
                }
                vista.setBackgroundColor(getColor(position % 2 == 0
                        ? R.color.row_blue : R.color.white));
                Contacto contacto = getItem(position);
                TextView nombre = vista.findViewById(R.id.itemNombre);
                TextView telefono = vista.findViewById(R.id.itemTelefono);
                ImageView foto = vista.findViewById(R.id.miniFoto);
                if (contacto != null) {
                    nombre.setText(contacto.getNombre());
                    telefono.setText(contacto.getNumeroCompleto());
                    foto.setImageURI(Uri.parse(contacto.getImagen()));
                }
                return vista;
            }
        };
        lista.setAdapter(adaptador);
        lista.setOnItemClickListener((parent, view, position, id) -> {
            seleccionado = visibles.get(position);
            lista.setItemChecked(position, true);
            TextView ayuda = findViewById(R.id.txtAyuda);
            ayuda.setText("Seleccionado: " + seleccionado.getNombre());
        });

        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }
            @Override public void afterTextChanged(Editable editable) { }
        });

        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
        findViewById(R.id.btnCompartir).setOnClickListener(v -> compartir());
        findViewById(R.id.btnVerImagen).setOnClickListener(v -> verImagen());
        findViewById(R.id.btnEliminar).setOnClickListener(v -> confirmarEliminacion());
        findViewById(R.id.btnActualizar).setOnClickListener(v -> actualizar());
        findViewById(R.id.btnLlamar).setOnClickListener(v -> llamar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    private void cargarLista() {
        try {
            todos = dao.listar();
            seleccionado = null;
            filtrar(txtBuscar.getText().toString());
        } catch (Exception e) {
            Toast.makeText(this, "Error al consultar contactos", Toast.LENGTH_SHORT).show();
        }
    }

    private void filtrar(String texto) {
        String consulta = texto.toLowerCase(Locale.ROOT).trim();
        visibles.clear();
        for (Contacto c : todos) {
            if (c.getNombre().toLowerCase(Locale.ROOT).contains(consulta)
                    || c.getTelefono().contains(consulta)) {
                visibles.add(c);
            }
        }
        seleccionado = null;
        lista.clearChoices();
        adaptador.notifyDataSetChanged();
        TextView ayuda = findViewById(R.id.txtAyuda);
        ayuda.setText("Selecciona un contacto de la lista");
    }

    private boolean verificarSeleccion() {
        if (seleccionado == null) {
            Toast.makeText(this, "Primero selecciona un contacto", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void compartir() {
        if (!verificarSeleccion()) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contacto: " + seleccionado.getNombre());
        intent.putExtra(Intent.EXTRA_TEXT,
                "Nombre: " + seleccionado.getNombre() + "\n"
                        + "País: " + seleccionado.getPais() + "\n"
                        + "Teléfono: " + seleccionado.getNumeroCompleto() + "\n"
                        + "Nota: " + seleccionado.getNota());
        startActivity(Intent.createChooser(intent, "Compartir contacto con"));
    }

    private void verImagen() {
        if (!verificarSeleccion()) return;
        ImageView imagen = new ImageView(this);
        imagen.setPadding(16, 16, 16, 16);
        imagen.setAdjustViewBounds(true);
        imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imagen.setMinimumHeight((int) (260 * getResources().getDisplayMetrics().density));
        imagen.setImageURI(Uri.parse(seleccionado.getImagen()));
        new AlertDialog.Builder(this)
                .setTitle("Fotografía de " + seleccionado.getNombre())
                .setView(imagen)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void confirmarEliminacion() {
        if (!verificarSeleccion()) return;
        new AlertDialog.Builder(this)
                .setTitle("Eliminar contacto")
                .setMessage("¿Desea eliminar a " + seleccionado.getNombre() + "?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", (dialog, which) -> {
                    if (dao.eliminar(seleccionado.getId()) > 0) {
                        Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                        cargarLista();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void actualizar() {
        if (!verificarSeleccion()) return;
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("contacto_id", seleccionado.getId());
        startActivity(intent);
    }

    private void llamar() {
        if (!verificarSeleccion()) return;
        Intent intent = new Intent(this, LlamarActivity.class);
        intent.putExtra("contacto_id", seleccionado.getId());
        startActivity(intent);
    }
}
