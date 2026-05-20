package com.example.smartconfig;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PartsListActivity extends AppCompatActivity {

    private String catId;
    private String catLabel;
    private String selectedPartId;

    private LinearLayout llPartsList;
    private List<Part> allParts;

    // ---- Static parts database ----
    private static final List<Part> CPU_PARTS = new ArrayList<>();
    private static final List<Part> GPU_PARTS = new ArrayList<>();
    private static final List<Part> RAM_PARTS = new ArrayList<>();
    private static final List<Part> PSU_PARTS = new ArrayList<>();
    private static final List<Part> STORAGE_PARTS = new ArrayList<>();
    private static final List<Part> MOBO_PARTS = new ArrayList<>();
    private static final List<Part> CASE_PARTS = new ArrayList<>();
    private static final List<Part> COOLING_PARTS = new ArrayList<>();

    static {
        // CPUs
        CPU_PARTS.add(new Part("c1", "AMD Ryzen 7 9850X3D 4.7GHz 8-Core AM5",       493.00,  65,  18000, "AM5 • 8 Cores",         true,  false));
        CPU_PARTS.add(new Part("c2", "AMD Ryzen 7 9800X3D 4.7GHz 8-Core AM5",       404.00,  65,  17500, "AM5 • 8 Cores",         false, false));
        CPU_PARTS.add(new Part("c3", "AMD Ryzen 9 9950X3D 4.3GHz 16-Core AM5",      626.00,  105, 25000, "AM5 • 16 Cores",        false, false));
        CPU_PARTS.add(new Part("c4", "AMD Ryzen 9 9950X3D2 4.3GHz 16-Core AM5",     906.00,  105, 27000, "AM5 • 16 Cores",        false, false));
        CPU_PARTS.add(new Part("c5", "Intel Core i9-14900K 3.2GHz 24-Core LGA1700", 399.00,  125, 22000, "LGA1700 • 24 Cores",    false, false));
        CPU_PARTS.add(new Part("c6", "Intel Core i5-14600K 3.5GHz 14-Core LGA1700", 219.00,  65,  14000, "LGA1700 • 14 Cores",    false, false));

        // GPUs
        GPU_PARTS.add(new Part("g1", "NVIDIA RTX PRO 6000 Blackwell 96GB GDDR7",         11053.51, 300, 95000, "NVIDIA • RTX PRO 6000",        false, true));
        GPU_PARTS.add(new Part("g2", "ROG Astral GeForce RTX 5090 32GB GDDR7 OC",        4968.65,  575, 75000, "NVIDIA • GeForce RTX 5090",    false, true));
        GPU_PARTS.add(new Part("g3", "Gigabyte AERO OC GeForce RTX 5060 8GB GDDR7",      380.81,   150, 22000, "NVIDIA • GeForce RTX 5060",    false, true));
        GPU_PARTS.add(new Part("g4", "ASUS PRIME GeForce RTX 5070 12GB GDDR7 OC",        699.00,   200, 38000, "NVIDIA • GeForce RTX 5070",    false, true));
        GPU_PARTS.add(new Part("g5", "PNY RTX 5060 Ti EPIC-X 16GB GDDR7",                479.00,   165, 28000, "NVIDIA • GeForce RTX 5060 Ti", false, true));

        // RAM
        RAM_PARTS.add(new Part("r1", "Corsair Vengeance RGB 64GB (2×32GB) DDR5 6000 CL30 White", 1195.33, 8,  0, "6000 MHz • DDR5", false, true));
        RAM_PARTS.add(new Part("r2", "Corsair Vengeance RGB 32GB (2×16GB) DDR5 6000 CL30 Black",  542.04,  5,  0, "6000 MHz • DDR5", false, true));
        RAM_PARTS.add(new Part("r3", "Corsair Vengeance RGB 32GB (2×16GB) DDR5 6000 CL36 White",  434.00,  5,  0, "6000 MHz • DDR5", false, true));
        RAM_PARTS.add(new Part("r4", "G.Skill Flare X5 256GB (4×64GB) DDR5 6000 CL36 Black",     4158.97, 15, 0, "6000 MHz • DDR5", false, true));
        RAM_PARTS.add(new Part("r5", "Kingston Fury Beast 32GB (2×16GB) DDR5 5600 Black",          129.99,  5,  0, "5600 MHz • DDR5", false, false));

        // PSUs
        PSU_PARTS.add(new Part("p1", "Corsair RM1000e (2025) Black ATX 1000W Fully Modular", 149.90, 0, 0, "1000W • 80+ Gold",     true,  true));
        PSU_PARTS.add(new Part("p2", "NZXT C850 (2024) Black 850W Fully Modular 80+ Gold",   321.05, 0, 0, "850W • 80+ Gold",      false, true));
        PSU_PARTS.add(new Part("p3", "Lian Li EDGE Black ATX 1300W Fully Modular 80+ Plat.", 353.99, 0, 0, "1300W • 80+ Platinum", false, true));
        PSU_PARTS.add(new Part("p4", "Lian Li EDGE White 850W Fully Modular 80+ Gold",       248.94, 0, 0, "850W • 80+ Gold",      false, true));

        // Storage
        STORAGE_PARTS.add(new Part("s1", "Samsung 990 EVO 2TB SSD M.2-2280 PCIe 5.0 X2 NVMe",    473.03, 4, 0, "2000 GB • SSD",   false, false));
        STORAGE_PARTS.add(new Part("s2", "Crucial T500 1TB SSD M.2-2280 PCIe 4.0 x4 NVMe",        199.99, 3, 0, "1000 GB • SSD",   false, false));
        STORAGE_PARTS.add(new Part("s3", "Western Digital WD Red Pro 26TB 3.5\" HDD 7200RPM SATA", 875.99, 9, 0, "26000 GB • HDD",  false, false));
        STORAGE_PARTS.add(new Part("s4", "Kingston NV2 1TB SSD M.2-2280 PCIe 4.0 x4 NVMe",        149.90, 3, 0, "1000 GB • SSD",   false, false));

        // Motherboards
        MOBO_PARTS.add(new Part("m1", "ASUS ROG STRIX X870E-E GAMING WIFI",        459.99, 8, 0, "AM5 • ATX",      false, false));
        MOBO_PARTS.add(new Part("m2", "MSI MEG X870E ACE",                          499.99, 8, 0, "AM5 • ATX",      false, false));
        MOBO_PARTS.add(new Part("m3", "Gigabyte B650E AORUS ELITE X AX",            249.99, 6, 0, "AM5 • ATX",      false, false));
        MOBO_PARTS.add(new Part("m4", "ASRock Z790 Taichi Aqua LGA1700",            399.99, 7, 0, "LGA1700 • ATX",  false, false));

        // Cases
        CASE_PARTS.add(new Part("cs1", "Fractal Design Torrent Compact Black",      129.99, 0, 0, "ATX Mid Tower",    true,  false));
        CASE_PARTS.add(new Part("cs2", "Lian Li PC-O11 Dynamic EVO XL White",       179.99, 0, 0, "E-ATX Full Tower", false, false));
        CASE_PARTS.add(new Part("cs3", "NZXT H9 Flow Matte White",                  149.99, 0, 0, "ATX Mid Tower",    false, false));
        CASE_PARTS.add(new Part("cs4", "be quiet! Pure Base 500DX Black",            99.99, 0, 0, "ATX Mid Tower",    false, false));

        // Cooling
        COOLING_PARTS.add(new Part("cl1", "Noctua NH-D15 chromax.black",          99.90,  0, 0, "Air Cooler • AM5/LGA1700",  false, false));
        COOLING_PARTS.add(new Part("cl2", "be quiet! Dark Rock Pro 5",             79.90,  0, 0, "Air Cooler • AM5/LGA1700",  false, false));
        COOLING_PARTS.add(new Part("cl3", "NZXT Kraken 360 RGB Black",            149.99, 0, 0, "360mm AIO • AM5/LGA1700",   false, false));
        COOLING_PARTS.add(new Part("cl4", "Corsair iCUE H150i Elite LCD XT",      199.99, 0, 0, "360mm AIO • AM5/LGA1700",   false, false));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parts_list);

        catId          = getIntent().getStringExtra("catId");
        catLabel       = getIntent().getStringExtra("catLabel");
        selectedPartId = getIntent().getStringExtra("selectedPartId");

        TextView tvTitle = findViewById(R.id.tvPartsTitle);
        tvTitle.setText(catLabel + "s");

        llPartsList = findViewById(R.id.llPartsList);
        allParts    = getPartsForCategory(catId);

        renderParts(allParts);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterParts(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnPartsBack).setOnClickListener(v -> finish());
    }

    private void filterParts(String query) {
        if (query.isEmpty()) {
            renderParts(allParts);
            return;
        }
        List<Part> filtered = new ArrayList<>();
        for (Part p : allParts) {
            if (p.name.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }
        renderParts(filtered);
    }

    private void renderParts(List<Part> parts) {
        llPartsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Part p : parts) {
            View item = inflater.inflate(R.layout.item_part_card, llPartsList, false);

            TextView tvName      = item.findViewById(R.id.tvPartName);
            TextView tvPrice     = item.findViewById(R.id.tvPartPrice);
            TextView tvSpec      = item.findViewById(R.id.tvPartSpec);
            TextView tvSponsored = item.findViewById(R.id.tvSponsored);
            TextView tvAdd       = item.findViewById(R.id.tvAddBtn);
            TextView tv3D        = item.findViewById(R.id.tv3DBadge);

            tvName.setText(p.name);
            tvPrice.setText(String.format("€%.2f", p.price));
            tvSpec.setText(p.spec);

            tvSponsored.setVisibility(p.sponsored ? View.VISIBLE : View.GONE);
            tv3D.setVisibility(p.has3d ? View.VISIBLE : View.GONE);

            boolean isAdded = p.id.equals(selectedPartId);
            updateAddButton(tvAdd, isAdded);

            tvAdd.setOnClickListener(v -> {
                boolean nowAdded = p.id.equals(selectedPartId);
                if (nowAdded) {
                    // Deselect
                    selectedPartId = null;
                    updateAddButton(tvAdd, false);
                    returnResult(null, true);
                } else {
                    selectedPartId = p.id;
                    // Refresh all buttons
                    renderParts(getPartsForCategory(catId));
                    returnResult(p, false);
                }
            });

            llPartsList.addView(item);
        }
    }

    private void updateAddButton(TextView btn, boolean added) {
        if (added) {
            btn.setText("✓  Added");
            btn.setBackgroundResource(R.drawable.bg_button_accent);
            btn.setTextColor(getResources().getColor(R.color.bg_dark));
        } else {
            btn.setText("+  Add to Build");
            btn.setBackgroundResource(R.drawable.bg_add_button_outline);
            btn.setTextColor(getResources().getColor(R.color.accent));
        }
    }

    private void returnResult(Part p, boolean removed) {
        Intent result = new Intent();
        result.putExtra("catId",   catId);
        result.putExtra("removed", removed);
        if (p != null) {
            result.putExtra("partId",    p.id);
            result.putExtra("partName",  p.name);
            result.putExtra("partPrice", p.price);
            result.putExtra("partWatts", p.watts);
            result.putExtra("partScore", p.score);
        }
        setResult(RESULT_OK, result);
        // Don't finish() — let user keep browsing; parent refreshes on resume
    }

    private List<Part> getPartsForCategory(String cat) {
        switch (cat) {
            case "cpu":     return CPU_PARTS;
            case "gpu":     return GPU_PARTS;
            case "ram":     return RAM_PARTS;
            case "psu":     return PSU_PARTS;
            case "storage": return STORAGE_PARTS;
            case "mobo":    return MOBO_PARTS;
            case "case":    return CASE_PARTS;
            case "cooling": return COOLING_PARTS;
            default:        return new ArrayList<>();
        }
    }

    // ---- Data class ----
    public static class Part {
        public String  id;
        public String  name;
        public double  price;
        public int     watts;
        public int     score;
        public String  spec;
        public boolean sponsored;
        public boolean has3d;

        public Part(String id, String name, double price, int watts, int score,
                    String spec, boolean sponsored, boolean has3d) {
            this.id        = id;
            this.name      = name;
            this.price     = price;
            this.watts     = watts;
            this.score     = score;
            this.spec      = spec;
            this.sponsored = sponsored;
            this.has3d     = has3d;
        }
    }
}
