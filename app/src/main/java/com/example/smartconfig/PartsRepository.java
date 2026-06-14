package com.example.smartconfig;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * PartsRepository — the app's single entry point for parts data.
 * ─────────────────────────────────────────────────────────────────────────
 * Source of truth: the local SQLite (Room) database.
 * On first launch the table is empty, so it seeds itself from
 * PartsCatalog.getAllParts(). After that, every read comes from SQLite — so
 * the components shown in the app are driven by the database.
 *
 * Call PartsRepository.init(context) once at app start (done in
 * MainApplication). Then use getByCategory() / findById() anywhere.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class PartsRepository {

    private static AppDatabase db;

    /** Call once at app start. Seeds the catalog if the table is empty. */
    public static void init(Context context) {
        db = AppDatabase.get(context);
        runBlocking(() -> {
            PartDao dao = db.partDao();
            if (dao.count() == 0) {
                List<PartEntity> rows = new ArrayList<>();
                for (PartsCatalog.Part p : PartsCatalog.getAllParts()) {
                    rows.add(PartEntity.fromCatalog(p));
                }
                dao.insertAll(rows);
            }
        });
    }

    public static List<PartsCatalog.Part> getByCategory(String category) {
        List<PartsCatalog.Part> out = new ArrayList<>();
        runBlocking(() -> {
            for (PartEntity e : db.partDao().getByCategory(category)) {
                out.add(e.toCatalog());
            }
        });
        return out;
    }

    public static List<PartsCatalog.Part> getAll() {
        List<PartsCatalog.Part> out = new ArrayList<>();
        runBlocking(() -> {
            for (PartEntity e : db.partDao().getAll()) {
                out.add(e.toCatalog());
            }
        });
        return out;
    }

    public static PartsCatalog.Part findById(String id) {
        final PartsCatalog.Part[] result = new PartsCatalog.Part[1];
        runBlocking(() -> {
            PartEntity e = db.partDao().findById(id);
            result[0] = (e != null) ? e.toCatalog() : null;
        });
        return result[0];
    }

    // ── Run a DB task on a background thread and wait for it (catalog is tiny) ──
    private static void runBlocking(Runnable task) {
        final Object lock = new Object();
        final boolean[] done = {false};
        new Thread(() -> {
            try {
                task.run();
            } finally {
                synchronized (lock) {
                    done[0] = true;
                    lock.notifyAll();
                }
            }
        }).start();
        synchronized (lock) {
            while (!done[0]) {
                try { lock.wait(); } catch (InterruptedException ignored) { }
            }
        }
    }
}