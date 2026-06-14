package com.example.smartconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BuildsRepository — save and read the user's builds from SQLite.
 * Uses the same AppDatabase initialised by PartsRepository.init().
 */
public class BuildsRepository {

    /** Save a new build. Returns the generated build id. */
    public static String save(String name, String partsJson, int partCount,
                              double totalPrice, String mode) {
        final BuildEntity b = new BuildEntity();
        b.id         = UUID.randomUUID().toString();
        b.name       = (name == null || name.trim().isEmpty()) ? "Untitled Build" : name.trim();
        b.createdAt  = System.currentTimeMillis();
        b.partsJson  = partsJson;
        b.partCount  = partCount;
        b.totalPrice = totalPrice;
        b.mode       = mode;

        runBlocking(() -> AppDatabase.get(ctx()).buildDao().insert(b));
        return b.id;
    }

    public static List<BuildEntity> getAll() {
        final List<BuildEntity> out = new ArrayList<>();
        runBlocking(() -> out.addAll(AppDatabase.get(ctx()).buildDao().getAll()));
        return out;
    }

    public static int getBuildCount() {
        final int[] n = {0};
        runBlocking(() -> n[0] = AppDatabase.get(ctx()).buildDao().countBuilds());
        return n[0];
    }

    public static int getTotalPartsSaved() {
        final int[] n = {0};
        runBlocking(() -> n[0] = AppDatabase.get(ctx()).buildDao().totalPartsSaved());
        return n[0];
    }

    public static void delete(String buildId) {
        runBlocking(() -> AppDatabase.get(ctx()).buildDao().deleteById(buildId));
    }

    // AppDatabase caches the application context internally after init(); we reuse it.
    private static android.content.Context ctx() {
        return AppContext.get();
    }

    private static void runBlocking(Runnable task) {
        final Object lock = new Object();
        final boolean[] done = {false};
        new Thread(() -> {
            try { task.run(); }
            finally { synchronized (lock) { done[0] = true; lock.notifyAll(); } }
        }).start();
        synchronized (lock) {
            while (!done[0]) {
                try { lock.wait(); } catch (InterruptedException ignored) { }
            }
        }
    }
}