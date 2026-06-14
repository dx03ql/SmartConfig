package com.example.smartconfig;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * PartEntity — one row in the SQLite "parts" table (via Room).
 * Mirrors every field of PartsCatalog.Part so the catalog can live in the DB.
 */
@Entity(tableName = "parts")
public class PartEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String category;
    public String name;
    public String manufacturer;
    public double price;
    public int    watts;
    public int    score;
    public int    imageRes;
    public String spec;
    public String socket;
    public String ddr;
    public String formFactor;
    public boolean rgb;
    public boolean quiet;
    public boolean sff;
    public boolean igpu;
    public boolean sponsored;
    public boolean has3d;

    public PartEntity() { }

    /** Build a row from a catalog Part. */
    public static PartEntity fromCatalog(PartsCatalog.Part p) {
        PartEntity e = new PartEntity();
        e.id           = p.id;
        e.category     = p.category;
        e.name         = p.name;
        e.manufacturer = p.manufacturer;
        e.price        = p.price;
        e.watts        = p.watts;
        e.score        = p.score;
        e.imageRes     = p.imageRes;
        e.spec         = p.spec;
        e.socket       = p.socket;
        e.ddr          = p.ddr;
        e.formFactor   = p.formFactor;
        e.rgb          = p.rgb;
        e.quiet        = p.quiet;
        e.sff          = p.sff;
        e.igpu         = p.igpu;
        e.sponsored    = p.sponsored;
        e.has3d        = p.has3d;
        return e;
    }

    /** Convert a DB row back into the shared PartsCatalog.Part model. */
    public PartsCatalog.Part toCatalog() {
        return PartsCatalog.Part.fromEntity(this);
    }
}