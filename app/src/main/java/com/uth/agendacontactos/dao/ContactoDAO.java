package com.uth.agendacontactos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.uth.agendacontactos.database.DatabaseHelper;
import com.uth.agendacontactos.models.Contacto;
import java.util.ArrayList;

// DAO: aquí están las consultas a la base de datos (CRUD).
public class ContactoDAO {
    private final DatabaseHelper helper;

    public ContactoDAO(Context context) {
        helper = new DatabaseHelper(context);
    }

    private ContentValues valores(Contacto contacto) {
        ContentValues values = new ContentValues();
        values.put("pais", contacto.getPais());
        values.put("nombre", contacto.getNombre());
        values.put("telefono", contacto.getTelefono());
        values.put("nota", contacto.getNota());
        values.put("imagen", contacto.getImagen());
        return values;
    }

    public long insertar(Contacto contacto) {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            return db.insert(DatabaseHelper.TABLA_CONTACTOS, null, valores(contacto));
        } catch (Exception e) {
            return -1;
        } finally {
            if (db != null) db.close();
        }
    }

    public int actualizar(Contacto contacto) {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            return db.update(DatabaseHelper.TABLA_CONTACTOS, valores(contacto),
                    "id=?", new String[]{String.valueOf(contacto.getId())});
        } catch (Exception e) {
            return 0;
        } finally {
            if (db != null) db.close();
        }
    }

    public int eliminar(int id) {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            return db.delete(DatabaseHelper.TABLA_CONTACTOS,
                    "id=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            return 0;
        } finally {
            if (db != null) db.close();
        }
    }

    private Contacto leerContacto(Cursor cursor) {
        Contacto c = new Contacto();
        c.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        c.setPais(cursor.getString(cursor.getColumnIndexOrThrow("pais")));
        c.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
        c.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow("telefono")));
        c.setNota(cursor.getString(cursor.getColumnIndexOrThrow("nota")));
        c.setImagen(cursor.getString(cursor.getColumnIndexOrThrow("imagen")));
        return c;
    }

    public Contacto obtener(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLA_CONTACTOS + " WHERE id=?",
                    new String[]{String.valueOf(id)});
            if (cursor.moveToFirst()) return leerContacto(cursor);
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    public ArrayList<Contacto> listar() {
        ArrayList<Contacto> contactos = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLA_CONTACTOS
                    + " ORDER BY nombre COLLATE NOCASE", null);
            while (cursor.moveToNext()) {
                contactos.add(leerContacto(cursor));
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return contactos;
    }
}
