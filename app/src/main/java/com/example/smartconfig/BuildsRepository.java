package com.example.smartconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        b.userId     = currentUserId();
        b.name       = (name == null || name.trim().isEmpty()) ? "Untitled Build" : name.trim();
        b.createdAt  = System.currentTimeMillis();
        b.partsJson  = partsJson;
        b.partCount  = partCount;
        b.totalPrice = totalPrice;
        b.mode       = mode;

        runBlocking(() -> AppDatabase.get(ctx()).buildDao().insert(b));
        return b.id;
    }

    /** Overwrite an existing build (same id) — used when editing a scratch build. */
    public static void update(String id, String name, String partsJson, int partCount,
                              double totalPrice, String mode) {
        runBlocking(() -> {
            BuildDao dao = AppDatabase.get(ctx()).buildDao();
            BuildEntity existing = dao.findById(id);
            BuildEntity b = new BuildEntity();
            b.id         = id;
            b.userId     = (existing != null && existing.userId != null)
                    ? existing.userId : currentUserId();
            b.name       = (name == null || name.trim().isEmpty()) ? "Untitled Build" : name.trim();
            b.createdAt  = (existing != null) ? existing.createdAt : System.currentTimeMillis();
            b.partsJson  = partsJson;
            b.partCount  = partCount;
            b.totalPrice = totalPrice;
            b.mode       = mode;
            dao.insert(b);   // @Insert(REPLACE) updates the row with the same id
        });
    }

    public static List<BuildEntity> getAll() {
        final String uid = currentUserId();
        final List<BuildEntity> out = new ArrayList<>();
        runBlocking(() -> out.addAll(AppDatabase.get(ctx()).buildDao().getAllForUser(uid)));
        return out;
    }

    public static int getBuildCount() {
        final String uid = currentUserId();
        final int[] n = {0};
        runBlocking(() -> n[0] = AppDatabase.get(ctx()).buildDao().countBuildsForUser(uid));
        return n[0];
    }

    public static int getTotalPartsSaved() {
        final String uid = currentUserId();
        final int[] n = {0};
        runBlocking(() -> n[0] = AppDatabase.get(ctx()).buildDao().totalPartsSavedForUser(uid));
        return n[0];
    }

    public static void delete(String buildId) {
        runBlocking(() -> AppDatabase.get(ctx()).buildDao().deleteById(buildId));
    }

    // The logged-in user's id (Firebase uid), or "guest" when signed out.
    private static String currentUserId() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return (u != null) ? u.getUid() : "guest";
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