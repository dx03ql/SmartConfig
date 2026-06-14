package com.example.smartconfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BuildProfile
 * ─────────────────────────────────────────────────────────────────────────
 * The "smart" engine. Takes the 10 questionnaire answers and turns them into:
 *
 *   1. A budget allocation per component category (how many € each part gets)
 *   2. A set of preference tags (rgb, quiet, small form factor, upgradeable…)
 *
 * This is pure rule-based scoring — no AI, no network. Each answer nudges
 * category weights up or down. At the end we normalize the weights to 100%
 * and multiply by the total budget to get a € target per category.
 *
 * GeneratingBuildActivity then asks a PartRepository for the best part in
 * each category that fits its € target + respects the preference tags.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class BuildProfile {

    // Component categories (match ScratchBuildActivity COMPONENTS ids)
    public static final String[] CATEGORIES = {
            "cpu", "gpu", "ram", "mobo", "storage", "psu", "cooling", "case"
    };

    public int budget;

    // Weight per category — starts at a sensible baseline, answers adjust it
    public final Map<String, Double> weights = new LinkedHashMap<>();

    // Final € target per category (computed in finalizeAllocation)
    public final Map<String, Double> allocation = new LinkedHashMap<>();

    // Preference tags used to filter parts (not budget — style/behavior)
    public boolean wantsRgb        = false;
    public boolean wantsQuiet      = false;
    public boolean wantsSmallForm  = false;
    public boolean wantsUpgradable = false;
    public String  aesthetic       = "minimal_clean"; // for case selection
    public String  primaryUse      = "general";

    public BuildProfile(int budget) {
        this.budget = budget;
        // Baseline weights — a balanced general-purpose PC
        weights.put("cpu",     22.0);
        weights.put("gpu",     20.0);
        weights.put("ram",     12.0);
        weights.put("mobo",    12.0);
        weights.put("storage", 12.0);
        weights.put("psu",     9.0);
        weights.put("cooling", 6.0);
        weights.put("case",    7.0);
    }

    private void bump(String category, double amount) {
        Double current = weights.get(category);
        if (current != null) weights.put(category, Math.max(1.0, current + amount));
    }

    /**
     * Main entry point: feed the answers map (key -> value) and compute
     * the final allocation.
     */
    public static BuildProfile fromAnswers(int budget, Map<String, String> a) {
        BuildProfile p = new BuildProfile(budget);

        // ── Q1: primary_use ────────────────────────────────────────────────
        String use = val(a, "primary_use");
        p.primaryUse = use;
        switch (use) {
            case "gaming":
                p.bump("gpu", 18); p.bump("cpu", 4);
                p.bump("psu", 3); p.bump("storage", -2);
                break;
            case "creative":
                p.bump("cpu", 12); p.bump("ram", 8);
                p.bump("storage", 6); p.bump("gpu", 4);
                break;
            case "productivity":
                p.bump("cpu", 6); p.bump("ram", 4);
                p.bump("gpu", -12); p.bump("storage", 2);
                break;
            case "general":
            default:
                p.bump("gpu", -10); p.bump("cpu", -2);
                p.bump("storage", 2);
                break;
        }

        // ── Q2: gaming_type ────────────────────────────────────────────────
        switch (val(a, "gaming_type")) {
            case "competitive_fps":
                // high frame rate → CPU + GPU both matter
                p.bump("cpu", 8); p.bump("gpu", 8);
                break;
            case "aaa_story":
                // visual fidelity → GPU heavy
                p.bump("gpu", 14); p.bump("cpu", 2);
                break;
            case "casual_indie":
                p.bump("gpu", -6);
                break;
            case "no_gaming":
                p.bump("gpu", -14); p.bump("cpu", 4);
                break;
        }

        // ── Q3: creative_work ──────────────────────────────────────────────
        switch (val(a, "creative_work")) {
            case "video_editing":
                p.bump("cpu", 8); p.bump("ram", 8); p.bump("storage", 8); p.bump("gpu", 4);
                break;
            case "music_production":
                p.bump("cpu", 6); p.bump("ram", 6); p.bump("storage", 4);
                p.wantsQuiet = true;
                break;
            case "graphic_design":
                p.bump("ram", 6); p.bump("gpu", 4); p.bump("storage", 4);
                break;
            case "none":
            default:
                break;
        }

        // ── Q4: portability / form factor ──────────────────────────────────
        switch (val(a, "portability")) {
            case "small_form_factor":
                p.wantsSmallForm = true;
                p.bump("case", 4); p.bump("cooling", 2);
                break;
            case "desktop_movable":
                p.bump("case", 1);
                break;
            case "travel_user":
                p.wantsSmallForm = true;
                break;
            case "desktop_stationary":
            default:
                break;
        }

        // ── Q5: aesthetics ─────────────────────────────────────────────────
        p.aesthetic = val(a, "aesthetics");
        switch (p.aesthetic) {
            case "gaming_flashy":
                p.wantsRgb = true; p.bump("case", 4);
                break;
            case "minimal_clean":
                p.bump("case", 2);
                break;
            case "dark_stealth":
                p.bump("case", 2);
                break;
            case "performance_only":
                p.bump("case", -3); p.bump("gpu", 3);
                break;
        }

        // ── Q6: rgb ────────────────────────────────────────────────────────
        switch (val(a, "rgb")) {
            case "rgb_max":
                p.wantsRgb = true; p.bump("case", 3); p.bump("cooling", 2);
                break;
            case "rgb_subtle":
                p.wantsRgb = true;
                break;
            case "rgb_none":
                p.wantsRgb = false;
                break;
        }

        // ── Q7: multitasking ───────────────────────────────────────────────
        switch (val(a, "multitasking")) {
            case "heavy_multitasking":
                p.bump("ram", 10); p.bump("cpu", 4);
                break;
            case "moderate_multitasking":
                p.bump("ram", 4);
                break;
            case "dual_monitor":
                p.bump("ram", 4); p.bump("gpu", 4);
                break;
            case "light_multitasking":
            default:
                p.bump("ram", -2);
                break;
        }

        // ── Q8: storage_needs ──────────────────────────────────────────────
        switch (val(a, "storage_needs")) {
            case "storage_massive":
                p.bump("storage", 14);
                break;
            case "storage_heavy":
                p.bump("storage", 8);
                break;
            case "storage_medium":
                p.bump("storage", 3);
                break;
            case "storage_light":
            default:
                p.bump("storage", -4);
                break;
        }

        // ── Q9: upgrade_plans ──────────────────────────────────────────────
        switch (val(a, "upgrade_plans")) {
            case "upgrade_friendly":
                p.wantsUpgradable = true;
                p.bump("mobo", 5); p.bump("psu", 4); // headroom for future parts
                break;
            case "gpu_upgrade_plan":
                p.wantsUpgradable = true;
                p.bump("psu", 5); p.bump("gpu", -4); // save on GPU now, beef PSU
                break;
            case "no_upgrades":
                p.bump("mobo", -3);
                break;
        }

        // ── Q10: noise_preference ──────────────────────────────────────────
        switch (val(a, "noise_preference")) {
            case "silent_priority":
            case "audio_critical":
                p.wantsQuiet = true;
                p.bump("cooling", 6);
                break;
            case "quiet_preferred":
                p.wantsQuiet = true;
                p.bump("cooling", 2);
                break;
            case "noise_indifferent":
            default:
                break;
        }

        p.finalizeAllocation();
        return p;
    }

    /** Normalize weights to 100% and convert to € per category. */
    private void finalizeAllocation() {
        double total = 0;
        for (double w : weights.values()) total += w;
        if (total <= 0) total = 1;

        for (String cat : CATEGORIES) {
            double w = weights.getOrDefault(cat, 0.0);
            double euros = (w / total) * budget;
            allocation.put(cat, Math.round(euros * 100.0) / 100.0);
        }
    }

    /** Build a JSON snapshot — handy for passing to the next activity / debugging. */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("budget", budget);
            json.put("primaryUse", primaryUse);
            json.put("aesthetic", aesthetic);
            json.put("wantsRgb", wantsRgb);
            json.put("wantsQuiet", wantsQuiet);
            json.put("wantsSmallForm", wantsSmallForm);
            json.put("wantsUpgradable", wantsUpgradable);

            JSONObject alloc = new JSONObject();
            for (Map.Entry<String, Double> e : allocation.entrySet()) {
                alloc.put(e.getKey(), e.getValue());
            }
            json.put("allocation", alloc);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static String val(Map<String, String> a, String key) {
        String v = a.get(key);
        return v != null ? v : "not_specified";
    }
}