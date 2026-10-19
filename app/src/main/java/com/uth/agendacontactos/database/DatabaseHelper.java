package com.uth.agendacontactos.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Igual que en el proyecto de Personas: SQLiteOpenHelper crea la tabla.
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String NOMBRE_BD = "contactos.db";
    public static final String TABLA_CONTACTOS = "contactos";
    private static final int VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_CONTACTOS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "pais TEXT NOT NULL, "
                + "nombre TEXT NOT NULL, "
                + "telefono TEXT NOT NULL, "
                + "nota TEXT NOT NULL, "
                + "imagen TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int anterior, int nueva) {
        // No borrar la tabla existente: queremos conservar los datos guardados.
        // Cuando se necesiten columnas nuevas, aquí se aplica una migración ALTER TABLE.
    }
}
