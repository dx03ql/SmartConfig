package com.example.smartconfig;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * BuildEntity — one saved build in the SQLite "builds" table.
 * Builds are fixed once saved, so the parts are stored as a JSON string
 * ({category: partId}) plus cached partCount + totalPrice for fast counters.
 */
@Entity(tableName = "builds")
public class BuildEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String userId;      // owner: Firebase uid, or "guest"
    public String name;
    public long   createdAt;
    public String partsJson;   // {"cpu":"cpu_i5_12400f", ...}
    public int    partCount;
    public double totalPrice;
    public String mode;        // "smart" or "scratch"

    public BuildEntity() { }
}