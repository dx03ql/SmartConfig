package com.example.smartconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PartsCatalog — THE single source of truth for all PC parts.
 * Used by: PartsListActivity (scratch flow), GeneratingBuildActivity (smart
 * flow), PartDetailActivity, and the compatibility engine in
 * ScratchBuildActivity. Swap getAllParts() for a Firestore fetch later.
 */
public class PartsCatalog {

    public static final int PLACEHOLDER_IMG = R.drawable.bg_part_placeholder;

    // ── Part model (every field any screen needs) ─────────────────────────────
    public static class Part {
        public String id;
        public String category;     // cpu, gpu, ram, mobo, storage, psu, cooling, case
        public String name;
        public String manufacturer;
        public double price;         // €
        public int    watts;         // power DRAW; for PSU this is CAPACITY
        public int    score;
        public int    imageRes;
        public String spec;          // short human-readable spec line
        public String socket;        // CPU/mobo: "LGA1700" / "AM5"
        public String ddr;           // mobo/ram: "DDR4" / "DDR5"
        public String formFactor;    // mobo/case: "ITX","MATX","ATX","EATX"
        public boolean rgb;
        public boolean quiet;
        public boolean sff;
        public boolean igpu;
        public boolean sponsored;    // UI badge in the parts list
        public boolean has3d;        // UI badge in the parts list

        Part(String id, String category, String manufacturer, String name,
             double price, int watts, int score, String spec) {
            this.id = id;
            this.category = category;
            this.manufacturer = manufacturer;
            this.name = name;
            this.price = price;
            this.watts = watts;
            this.score = score;
            this.spec = spec;
            this.imageRes = PLACEHOLDER_IMG;
        }

        // Public constructor + factory so Room entities can rebuild a Part.
        public Part() { this.imageRes = PLACEHOLDER_IMG; }

        public static Part fromEntity(PartEntity e) {
            Part p = new Part();
            p.id           = e.id;
            p.category     = e.category;
            p.name         = e.name;
            p.manufacturer = e.manufacturer;
            p.price        = e.price;
            p.watts        = e.watts;
            p.score        = e.score;
            p.imageRes     = e.imageRes != 0 ? e.imageRes : PLACEHOLDER_IMG;
            p.spec         = e.spec;
            p.socket       = e.socket;
            p.ddr          = e.ddr;
            p.formFactor   = e.formFactor;
            p.rgb          = e.rgb;
            p.quiet        = e.quiet;
            p.sff          = e.sff;
            p.igpu         = e.igpu;
            p.sponsored    = e.sponsored;
            p.has3d        = e.has3d;
            return p;
        }

        Part socket(String s) { this.socket = s; return this; }
        Part ddr(String d)    { this.ddr = d;    return this; }
        Part form(String f)   { this.formFactor = f; return this; }
        Part rgb()   { this.rgb = true;   return this; }
        Part quiet() { this.quiet = true; return this; }
        Part sff()   { this.sff = true;   return this; }
        Part igpu()  { this.igpu = true;  return this; }
        Part sponsored() { this.sponsored = true; return this; }
        Part has3d()     { this.has3d = true;     return this; }
        Part img(int res){ this.imageRes = res;   return this; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  THE CATALOG
    // ══════════════════════════════════════════════════════════════════════
    public static List<Part> getAllParts() {
        List<Part> p = new ArrayList<>();

        // ── CPUs (Intel = LGA1700, AMD = AM5) ───────────────────────────────
        p.add(new Part("cpu_i3_12100f", "cpu", "Intel", "Core i3-12100F",  90,  58, 12000, "LGA1700 • 4 cores · 8 threads").socket("LGA1700").img(R.drawable.core_i3_12th));
        p.add(new Part("cpu_i5_12400f", "cpu", "Intel", "Core i5-12400F", 150,  65, 19000, "LGA1700 • 6 cores · 12 threads").socket("LGA1700").sponsored().img(R.drawable.core_i5_12th));
        p.add(new Part("cpu_i5_13400",  "cpu", "Intel", "Core i5-13400",  220,  65, 24000, "LGA1700 • 10 cores · iGPU").socket("LGA1700").igpu().img(R.drawable.core_i5_13th));
        p.add(new Part("cpu_i5_13600k", "cpu", "Intel", "Core i5-13600K", 290, 125, 28000, "LGA1700 • 14 cores · 20 threads").socket("LGA1700").igpu().img(R.drawable.core_i5_13th));
        p.add(new Part("cpu_i7_13700k", "cpu", "Intel", "Core i7-13700K", 400, 125, 35000, "LGA1700 • 16 cores · 24 threads").socket("LGA1700").igpu().img(R.drawable.core_i7_13th));
        p.add(new Part("cpu_i9_14900k", "cpu", "Intel", "Core i9-14900K", 540, 125, 43000, "LGA1700 • 24 cores · 32 threads").socket("LGA1700").igpu().img(R.drawable.core_i9_14th));
        p.add(new Part("cpu_r5_7600",   "cpu", "AMD",   "Ryzen 5 7600",     200,  65, 23000, "AM5 • 6 cores · iGPU").socket("AM5").igpu().img(R.drawable.ryzen_5_7600));
        p.add(new Part("cpu_r7_7700",   "cpu", "AMD",   "Ryzen 7 7700",     310,  65, 30000, "AM5 • 8 cores · iGPU").socket("AM5").igpu().img(R.drawable.ryzen_7_7700));
        p.add(new Part("cpu_r7_9800x3d","cpu", "AMD",   "Ryzen 7 9800X3D",  450, 120, 36000, "AM5 • 8 cores · 3D V-Cache").socket("AM5").igpu().sponsored().img(R.drawable.ryzen_7_9800x3d));
        p.add(new Part("cpu_r9_9950x3d","cpu", "AMD",   "Ryzen 9 9950X3D",  650, 170, 45000, "AM5 • 16 cores · 3D V-Cache").socket("AM5").igpu().img(R.drawable.ryzen_7_9950x3d));

        // ── GPUs ──────────────────────────────────────────────────────────
        p.add(new Part("gpu_none",      "gpu", "—",      "Integrated Graphics (no dedicated GPU)",   0,   0,  1500, "Uses the CPU's built-in graphics").img(R.drawable.intergrated_gpu));
        p.add(new Part("gpu_gtx1650",   "gpu", "NVIDIA", "GeForce GTX 1650",                       160,  75,  9000, "4GB GDDR6 · 1080p entry").has3d().img(R.drawable.geforce_gtx_1650));
        p.add(new Part("gpu_rx6600",    "gpu", "AMD",    "Radeon RX 6600",                         220, 132, 14000, "8GB GDDR6 · 1080p high").has3d().img(R.drawable.radeon_rx_6600));
        p.add(new Part("gpu_rtx4060",   "gpu", "NVIDIA", "GeForce RTX 4060",                       310, 115, 19000, "8GB GDDR6 · DLSS 3").has3d().img(R.drawable.geforce_rtx_4060));
        p.add(new Part("gpu_rx7700xt",  "gpu", "AMD",    "Radeon RX 7700 XT",                      430, 245, 26000, "12GB GDDR6 · 1440p").has3d().img(R.drawable.radeon_rx_7700_xt));
        p.add(new Part("gpu_rtx4070",   "gpu", "NVIDIA", "GeForce RTX 4070",                       590, 200, 29000, "12GB GDDR6X · 1440p high").has3d().img(R.drawable.geforce_rtx_4070));
        p.add(new Part("gpu_rtx5070",   "gpu", "NVIDIA", "GeForce RTX 5070",                       700, 250, 33000, "12GB GDDR7 · 1440p/4K").has3d().img(R.drawable.geforce_rtx_5070));
        p.add(new Part("gpu_rx7900xt",  "gpu", "AMD",    "Radeon RX 7900 XT",                      800, 315, 36000, "20GB GDDR6 · 4K").has3d().img(R.drawable.radeon_rx_7900_xt));
        p.add(new Part("gpu_rtx4080s",  "gpu", "NVIDIA", "GeForce RTX 4080 Super",                1050, 320, 42000, "16GB GDDR6X · 4K").has3d().img(R.drawable.geforce_rtx_4080_super));
        p.add(new Part("gpu_rtx5090",   "gpu", "NVIDIA", "GeForce RTX 5090",                      2100, 575, 75000, "32GB GDDR7 · flagship").has3d().sponsored().img(R.drawable.geforce_rtx_5090));

        // ── RAM ───────────────────────────────────────────────────────────
        p.add(new Part("ram_8_ddr4",  "ram", "Crucial",  "8GB DDR4-3200 (1x8GB)",    25, 5,  3000, "DDR4 • single channel").ddr("DDR4").img(R.drawable.crucial_8gb_ddr4_3200_1x8));
        p.add(new Part("ram_16_ddr4", "ram", "Corsair",  "16GB DDR4-3200 (2x8GB)",   40, 6,  6000, "DDR4 • dual channel").ddr("DDR4").img(R.drawable.corsair_16gb_ddr4_3600_2x8));
        p.add(new Part("ram_32_ddr4", "ram", "G.Skill",  "32GB DDR4-3600 (2x16GB)",  75, 8, 10000, "DDR4 • dual channel").ddr("DDR4").img(R.drawable.gskill_32gb_ddr4_5600_2x16));
        p.add(new Part("ram_16_ddr5", "ram", "Corsair",  "16GB DDR5-5600 (2x8GB)",   55, 7,  7000, "DDR5 • dual channel").ddr("DDR5").img(R.drawable.corsair_16gb_ddr4_6000_2x8));
        p.add(new Part("ram_32_ddr5", "ram", "G.Skill",  "32GB DDR5-6000 (2x16GB)", 105, 9, 11000, "DDR5 • CL30 · dual channel").ddr("DDR5").sponsored().img(R.drawable.gskill_32gb_ddr4_6000_2x16));
        p.add(new Part("ram_64_ddr5", "ram", "Corsair",  "64GB DDR5-6000 (2x32GB)", 210, 12,13000, "DDR5 • dual channel").ddr("DDR5").img(R.drawable.corsair_64gb_ddr5_6000_2x32));

        // ── Motherboards ────────────────────────────────────────────────────
        p.add(new Part("mobo_b660m_ddr4", "mobo", "MSI",     "PRO B660M-A DDR4",       110, 25, 0, "LGA1700 • mATX • DDR4").socket("LGA1700").ddr("DDR4").form("MATX").img(R.drawable.msi_pro_b66m_a_ddr4));
        p.add(new Part("mobo_b760m_ddr4", "mobo", "ASUS",    "PRIME B760M-K DDR4",     130, 25, 0, "LGA1700 • mATX • DDR4").socket("LGA1700").ddr("DDR4").form("MATX").img(R.drawable.asus_prime_b76_m_k_ddr4));
        p.add(new Part("mobo_z790_ddr5",  "mobo", "ASUS",    "TUF Gaming Z790 DDR5",   250, 35, 0, "LGA1700 • ATX • DDR5").socket("LGA1700").ddr("DDR5").form("ATX"));
        p.add(new Part("mobo_b650_ddr5",  "mobo", "MSI",     "PRO B650-P WiFi",        160, 30, 0, "AM5 • ATX • DDR5").socket("AM5").ddr("DDR5").form("ATX").img(R.drawable.msi_pro_b650_p_wifi));
        p.add(new Part("mobo_b650i_itx",  "mobo", "ASRock",  "B650E PG-ITX",           230, 30, 0, "AM5 • Mini-ITX • DDR5").socket("AM5").ddr("DDR5").form("ITX").sff());
        p.add(new Part("mobo_x870e",      "mobo", "ASUS",    "ROG STRIX X870E-E",      450, 40, 0, "AM5 • ATX • DDR5").socket("AM5").ddr("DDR5").form("ATX").sponsored().img(R.drawable.asus_rog_strix_x870e_e));

        // ── Storage ─────────────────────────────────────────────────────────
        p.add(new Part("ssd_500",       "storage", "Kingston", "NV2 500GB NVMe",        40,  5, 0, "PCIe 4.0 • ~3500 MB/s").img(R.drawable.kingston_nv2_500gb_nvme));
        p.add(new Part("ssd_1tb",       "storage", "Crucial",  "P3 1TB NVMe",            60,  6, 0, "PCIe 3.0 • ~3500 MB/s").img(R.drawable.crucial_p3_1tb_nvme));
        p.add(new Part("ssd_2tb",       "storage", "Samsung",  "990 EVO 2TB NVMe",      130,  7, 0, "PCIe 4.0 • ~5000 MB/s").sponsored().img(R.drawable.samsung_990_evo_2tb_nvme));
        p.add(new Part("combo_1tb_2tb", "storage", "Mixed",    "1TB NVMe SSD + 2TB HDD",120, 12, 0, "Fast SSD + bulk HDD").img(R.drawable.tb2_hdd));
        p.add(new Part("ssd_4tb",       "storage", "WD",       "Black SN850X 4TB NVMe", 280,  8, 0, "PCIe 4.0 • ~7300 MB/s").img(R.drawable.wd_black_sn850x_4tb_nvme));

        // ── PSUs (watts = capacity) ───────────────────────────────────────────
        p.add(new Part("psu_450",   "psu", "EVGA",    "450W 80+ Bronze",         45, 450, 0, "450W • 80+ Bronze").img(R.drawable.evga_450w_80_bronze));
        p.add(new Part("psu_550",   "psu", "Corsair", "CV550 80+ Bronze",        55, 550, 0, "550W • 80+ Bronze").img(R.drawable.corsair_cv550_80_bronze));
        p.add(new Part("psu_650g",  "psu", "Corsair", "RM650e 80+ Gold",         80, 650, 0, "650W • 80+ Gold · modular").quiet().img(R.drawable.corsair_rm650e_80_gold));
        p.add(new Part("psu_750g",  "psu", "Corsair", "RM750e 80+ Gold",        100, 750, 0, "750W • 80+ Gold · modular").quiet().sponsored().img(R.drawable.corsair_rm750e_80_gold));
        p.add(new Part("psu_850g",  "psu", "Corsair", "RM850e 80+ Gold",        130, 850, 0, "850W • 80+ Gold · modular").quiet().img(R.drawable.corsair_rm850e_80_gold));
        p.add(new Part("psu_1000p", "psu", "Corsair", "HX1000 80+ Platinum",    190,1000, 0, "1000W • 80+ Platinum").quiet());

        // ── Cooling ───────────────────────────────────────────────────────────
        p.add(new Part("cool_stock",     "cooling", "—",            "Stock CPU Cooler (included)",         0, 0, 0, "Comes with the CPU"));
        p.add(new Part("cool_air_basic", "cooling", "DeepCool",     "AK400",                              30, 3, 0, "Air cooler · AM5/LGA1700").quiet().img(R.drawable.deepcool_ak400));
        p.add(new Part("cool_air_tower", "cooling", "Thermalright", "Peerless Assassin 120",              45, 4, 0, "Dual-tower air · AM5/LGA1700").quiet().sponsored().img(R.drawable.thermalright_peerless_assassin_120));
        p.add(new Part("cool_aio_240q",  "cooling", "ARCTIC",       "Liquid Freezer III 240",             90, 6, 0, "240mm AIO · AM5/LGA1700").quiet().img(R.drawable.arctic_liquid_freezer_iii_240));
        p.add(new Part("cool_aio_360q",  "cooling", "ARCTIC",       "Liquid Freezer III 360",            110, 8, 0, "360mm AIO · AM5/LGA1700").quiet().img(R.drawable.arctic_liquid_freezer_iii_360));
        p.add(new Part("cool_aio_360rgb","cooling", "NZXT",         "Kraken 360 RGB",                    180, 8, 0, "360mm AIO · RGB · AM5/LGA1700").rgb());

        // ── Cases ──────────────────────────────────────────────────────────────
        p.add(new Part("case_budget",    "case", "DeepCool",      "CC560 (ATX)",            55, 0, 0, "ATX mid-tower").form("ATX").img(R.drawable.deepcool_cc560_atx));
        p.add(new Part("case_glass",     "case", "NZXT",          "H5 Flow",                90, 0, 0, "ATX · tempered glass").form("ATX").img(R.drawable.nzxt_h5_flow));
        p.add(new Part("case_rgb",       "case", "Lian Li",       "Lancool 216 RGB",       100, 0, 0, "ATX · RGB fans").form("ATX").rgb().sponsored().img(R.drawable.lian_li_lancool_216_rgb));
        p.add(new Part("case_itx",       "case", "Cooler Master", "NR200 (Mini-ITX)",      100, 0, 0, "Mini-ITX small form factor").form("ITX").sff().img(R.drawable.cooler_master_nr200_mini_itx));
        p.add(new Part("case_stealth",   "case", "Fractal Design","North (Charcoal)",      130, 0, 0, "ATX · dark wood front").form("ATX").img(R.drawable.fractal_design_north_charcoal));
        p.add(new Part("case_premium",   "case", "Lian Li",       "O11 Dynamic EVO",       150, 0, 0, "ATX · dual-chamber").form("ATX").rgb().img(R.drawable.lian_li_011_dynamic_evo));
        p.add(new Part("case_fulltower", "case", "Lian Li",       "V3000 Plus (Full Tower)",260, 0, 0, "E-ATX full tower").form("EATX").img(R.drawable.lian_li_v3000_plus_full_tower));

        return p;
    }

    // ── Lookups ───────────────────────────────────────────────────────────────
    public static List<Part> getByCategory(String category) {
        List<Part> out = new ArrayList<>();
        for (Part p : getAllParts()) if (p.category.equals(category)) out.add(p);
        return out;
    }

    public static Part findById(String id) {
        if (id == null) return null;
        for (Part p : getAllParts()) if (p.id.equals(id)) return p;
        return null;
    }

    private static Part findById(List<Part> list, String id) {
        for (Part p : list) if (p.id.equals(id)) return p;
        return null;
    }

    private static List<Part> byCategory(List<Part> all, String cat) {
        List<Part> out = new ArrayList<>();
        for (Part p : all) if (p.category.equals(cat)) out.add(p);
        return out;
    }

    private static Part bestWithinBudget(List<Part> candidates, double target) {
        Part best = null, cheapest = null;
        for (Part p : candidates) {
            if (cheapest == null || p.price < cheapest.price) cheapest = p;
            if (p.price <= target + 0.01) {
                if (best == null || p.price > best.price) best = p;
            }
        }
        return best != null ? best : cheapest;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MATCHING ENGINE (Smart flow)
    // ══════════════════════════════════════════════════════════════════════
    public static Map<String, Part> selectBuild(BuildProfile profile) {
        List<Part> all = getAllParts();
        Map<String, Part> build = new LinkedHashMap<>();
        Map<String, Double> alloc = profile.allocation;
        double gpuBudget = alloc.getOrDefault("gpu", 0.0);

        List<Part> gpus = byCategory(all, "gpu");
        Part cheapestDedicated = null;
        for (Part g : gpus) if (g.price > 0 && (cheapestDedicated == null || g.price < cheapestDedicated.price)) cheapestDedicated = g;
        boolean useIntegrated = (cheapestDedicated == null) || (gpuBudget < cheapestDedicated.price);
        Part gpu;
        if (useIntegrated) {
            gpu = findById(gpus, "gpu_none");
        } else {
            List<Part> dedicated = new ArrayList<>();
            for (Part g : gpus) if (g.price > 0) dedicated.add(g);
            gpu = bestWithinBudget(dedicated, gpuBudget);
        }
        build.put("gpu", gpu);

        List<Part> cpus = byCategory(all, "cpu");
        List<Part> cpuCandidates = new ArrayList<>();
        for (Part c : cpus) { if (useIntegrated && !c.igpu) continue; cpuCandidates.add(c); }
        if (cpuCandidates.isEmpty()) cpuCandidates = cpus;
        Part cpu = bestWithinBudget(cpuCandidates, alloc.getOrDefault("cpu", 0.0));
        build.put("cpu", cpu);

        List<Part> mobos = byCategory(all, "mobo");
        List<Part> moboCandidates = new ArrayList<>();
        for (Part m : mobos) { if (cpu != null && cpu.socket != null && !cpu.socket.equals(m.socket)) continue; moboCandidates.add(m); }
        if (moboCandidates.isEmpty()) moboCandidates = mobos;
        Part mobo = pickPreferred(moboCandidates, alloc.getOrDefault("mobo", 0.0), profile.wantsSmallForm, false);
        build.put("mobo", mobo);

        List<Part> rams = byCategory(all, "ram");
        List<Part> ramCandidates = new ArrayList<>();
        for (Part r : rams) { if (mobo != null && mobo.ddr != null && !mobo.ddr.equals(r.ddr)) continue; ramCandidates.add(r); }
        if (ramCandidates.isEmpty()) ramCandidates = rams;
        build.put("ram", bestWithinBudget(ramCandidates, alloc.getOrDefault("ram", 0.0)));

        build.put("storage", bestWithinBudget(byCategory(all, "storage"), alloc.getOrDefault("storage", 0.0)));
        build.put("cooling", pickPreferred(byCategory(all, "cooling"), alloc.getOrDefault("cooling", 0.0), false, profile.wantsRgb, profile.wantsQuiet));
        build.put("case", pickPreferred(byCategory(all, "case"), alloc.getOrDefault("case", 0.0), profile.wantsSmallForm, profile.wantsRgb));

        int draw = 0;
        for (Part part : build.values()) if (part != null && !"psu".equals(part.category)) draw += part.watts;
        int required = (int) Math.round(draw * 1.3);
        build.put("psu", pickPsu(byCategory(all, "psu"), required, alloc.getOrDefault("psu", 0.0), profile.wantsQuiet));

        return build;
    }

    private static Part pickPreferred(List<Part> candidates, double target, boolean preferSff, boolean preferRgb) {
        return pickPreferred(candidates, target, preferSff, preferRgb, false);
    }

    private static Part pickPreferred(List<Part> candidates, double target, boolean preferSff, boolean preferRgb, boolean preferQuiet) {
        List<Part> preferred = new ArrayList<>();
        for (Part p : candidates) {
            boolean ok = true;
            if (preferSff && !p.sff) ok = false;
            if (preferRgb && !p.rgb) ok = false;
            if (preferQuiet && !p.quiet) ok = false;
            if (ok) preferred.add(p);
        }
        if (!preferred.isEmpty()) {
            Part chosen = bestWithinBudget(preferred, target);
            if (chosen != null) return chosen;
        }
        return bestWithinBudget(candidates, target);
    }

    private static Part pickPsu(List<Part> psus, int requiredCapacity, double target, boolean preferQuiet) {
        Part best = null;
        for (Part p : psus) {
            if (p.watts < requiredCapacity) continue;
            if (preferQuiet && !p.quiet) continue;
            if (best == null || p.price < best.price) best = p;
        }
        if (best != null) return best;
        for (Part p : psus) { if (p.watts < requiredCapacity) continue; if (best == null || p.price < best.price) best = p; }
        if (best != null) return best;
        for (Part p : psus) if (best == null || p.watts > best.watts) best = p;
        return best;
    }
}