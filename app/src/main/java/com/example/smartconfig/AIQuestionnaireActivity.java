package com.example.smartconfig; // ← Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;



import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * AIQuestionnaireActivity
 * ─────────────────────────────────────────────────────────────────────────
 * Receives:  budget (int), isGuest (boolean)  from BuildModeActivity
 * Sends:     budget (int), isGuest (boolean), answersJson (String)
 *            → GeneratingBuildActivity
 *
 * UX pattern: ViewPager2 (non-swipeable by user — navigation driven by
 * Back / Next buttons to prevent accidental swipes losing answers).
 * Each page is inflated from fragment_question_item.xml and populated
 * with CardView option tiles from item_option_card.xml.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class AIQuestionnaireActivity extends AppCompatActivity {

    // ── Intent extras ──────────────────────────────────────────────────────
    private int budget;
    private boolean isGuest;

    // ── Views ──────────────────────────────────────────────────────────────
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private TextView tvStepLabel;
    private TextView tvStepBadge;
    private Button btnPrevious;
    private Button btnNext;

    // ── Data ───────────────────────────────────────────────────────────────
    private final int TOTAL_QUESTIONS = 10;
    private final String[] answers = new String[TOTAL_QUESTIONS]; // one answer per Q
    private List<Question> questions;
    private QuestionPagerAdapter adapter;

    // ══════════════════════════════════════════════════════════════════════
    //  Data model
    // ══════════════════════════════════════════════════════════════════════
    static class Option {
        String emoji;
        String label;
        String value; // machine-readable key sent to AI

        Option(String emoji, String label, String value) {
            this.emoji = emoji;
            this.label = label;
            this.value = value;
        }
    }

    static class Question {
        String emoji;
        String text;
        String subtitle;
        List<Option> options;

        Question(String emoji, String text, String subtitle, List<Option> options) {
            this.emoji = emoji;
            this.text = text;
            this.subtitle = subtitle;
            this.options = options;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_questionnaire);

        // Read incoming extras
        budget  = getIntent().getIntExtra("budget", 500);
        isGuest = getIntent().getBooleanExtra("isGuest", false);

        // Build question bank
        questions = buildQuestions();

        // Bind views
        viewPager    = findViewById(R.id.viewPagerQuestions);
        progressBar  = findViewById(R.id.progressBar);
        tvStepLabel  = findViewById(R.id.tvStepLabel);
        tvStepBadge  = findViewById(R.id.tvStepBadge);
        btnPrevious  = findViewById(R.id.btnPrevious);
        btnNext      = findViewById(R.id.btnNext);

        // Disable swiping — navigation is controlled by buttons only
        viewPager.setUserInputEnabled(false);

        // Set up adapter
        adapter = new QuestionPagerAdapter(questions, answers);
        viewPager.setAdapter(adapter);

        // Smooth page transitions
        viewPager.setPageTransformer(new SlideScalePageTransformer());

        // Update header on page change
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateHeader(position);
            }
        });

        // Initial header state
        updateHeader(0);

        // ── Button listeners ──────────────────────────────────────────────
        btnPrevious.setOnClickListener(v -> goToPreviousPage());
        btnNext.setOnClickListener(v -> goToNextPage());
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == 0) {
                finish();
            } else {
                goToPreviousPage();
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Navigation helpers
    // ══════════════════════════════════════════════════════════════════════
    private void goToNextPage() {
        int current = viewPager.getCurrentItem();

        // Validate: user must pick an answer before advancing
        if (answers[current] == null || answers[current].isEmpty()) {
            Toast.makeText(this,
                    "Please pick an option to continue ✌️",
                    Toast.LENGTH_SHORT).show();
            // Shake the ViewPager to hint the user
            viewPager.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.shake));
            return;
        }

        if (current < TOTAL_QUESTIONS - 1) {
            viewPager.setCurrentItem(current + 1, true);
        } else {
            // Last question — generate build
            launchGeneratingBuild();
        }
    }

    private void goToPreviousPage() {
        int current = viewPager.getCurrentItem();
        if (current > 0) {
            viewPager.setCurrentItem(current - 1, true);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Header / progress update
    // ══════════════════════════════════════════════════════════════════════
    private void updateHeader(int position) {
        int step = position + 1;
        tvStepLabel.setText("Question " + step + " of " + TOTAL_QUESTIONS);
        tvStepBadge.setText(String.valueOf(step));
        progressBar.setProgress(step);

        // Show / hide Previous button
        btnPrevious.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);

        // Change Next button label on last question
        if (position == TOTAL_QUESTIONS - 1) {
            btnNext.setText("Generate My Build ✨");
        } else {
            btnNext.setText("Next →");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Launch next screen
    // ══════════════════════════════════════════════════════════════════════
    private void launchGeneratingBuild() {
        // Collect answers into JSON
        JSONObject answersJson = new JSONObject();
        String[] keys = {
                "primary_use", "gaming_type", "creative_work",
                "portability", "aesthetics", "rgb",
                "multitasking", "storage_needs", "upgrade_plans", "noise_preference"
        };
        try {
            for (int i = 0; i < TOTAL_QUESTIONS; i++) {
                answersJson.put(keys[i], answers[i] != null ? answers[i] : "not_specified");
            }
            answersJson.put("budget", budget);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("budget",      budget);
        intent.putExtra("isGuest",     isGuest);
        intent.putExtra("answersJson", answersJson.toString());
        startActivity(intent);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Question bank — plain language, zero tech jargon
    // ══════════════════════════════════════════════════════════════════════
    private List<Question> buildQuestions() {
        List<Question> list = new ArrayList<>();

        // Q1 — Primary use
        list.add(new Question(
                "🖥️",
                "What will you mainly use your PC for?",
                "Pick the one that describes your day-to-day the best.",
                options(
                        new Option("🎮", "Gaming — I want to play games",                             "gaming"),
                        new Option("🎨", "Creative work — art, video, music, or streaming",            "creative"),
                        new Option("📚", "School or work — documents, research, video calls",          "productivity"),
                        new Option("🌐", "General browsing — Netflix, YouTube, social media",          "general")
                )
        ));

        // Q2 — Gaming type (shown to everyone; AI ignores if not a gamer)
        list.add(new Question(
                "🎮",
                "If you play games, which style fits you?",
                "Don't worry if you're not a gamer — just pick the closest match.",
                options(
                        new Option("🔫", "Fast-paced online games like shooters or battle royale",    "competitive_fps"),
                        new Option("🐉", "Big story-driven adventures or open worlds",                "aaa_story"),
                        new Option("🧩", "Casual or indie games — Minecraft, Stardew Valley, etc.",   "casual_indie"),
                        new Option("🚫", "I don't play games at all",                                 "no_gaming")
                )
        ));

        // Q3 — Creative work
        list.add(new Question(
                "🎬",
                "Do you do any creative work on your PC?",
                "Select everything that sounds like something you'd do.",
                options(
                        new Option("📹", "Video editing or recording YouTube / TikTok content",       "video_editing"),
                        new Option("🎵", "Music production or audio recording",                       "music_production"),
                        new Option("✏️", "Graphic design, digital art, or photo editing",             "graphic_design"),
                        new Option("🚫", "No creative work — just everyday tasks",                    "none")
                )
        ));

        // Q4 — Portability / form factor
        list.add(new Question(
                "🏠",
                "Where will your PC mostly live?",
                "This helps us decide on the size and shape of your build.",
                options(
                        new Option("🖥️", "On my desk at home — it never moves",                      "desktop_stationary"),
                        new Option("📦", "Mostly at home but I might move it occasionally",           "desktop_movable"),
                        new Option("🎒", "I need something compact — small bedroom or dorm room",     "small_form_factor"),
                        new Option("💼", "I travel a lot — but I still want a desktop for now",       "travel_user")
                )
        ));

        // Q5 — Aesthetics
        list.add(new Question(
                "✨",
                "What should your PC look like?",
                "Looks matter — pick the vibe that excites you most.",
                options(
                        new Option("🌊", "Sleek and minimal — clean, no clutter, professional",       "minimal_clean"),
                        new Option("🌈", "Bold and flashy — I want it to look like a gaming rig",     "gaming_flashy"),
                        new Option("🖤", "Dark and stealthy — all black, no lights",                  "dark_stealth"),
                        new Option("🤷", "I don't care about looks — just make it fast",              "performance_only")
                )
        ));

        // Q6 — RGB
        list.add(new Question(
                "💡",
                "How do you feel about colorful LED lighting (RGB)?",
                "RGB lights can be set to any color or turned off completely.",
                options(
                        new Option("🌈", "Yes! I love colorful lights — the more the better",        "rgb_max"),
                        new Option("🔵", "Just a little — one or two subtle glowing accents",        "rgb_subtle"),
                        new Option("🚫", "No thanks — I prefer no lights at all",                    "rgb_none"),
                        new Option("😐", "I don't mind either way",                                  "rgb_neutral")
                )
        ));

        // Q7 — Multitasking
        list.add(new Question(
                "📋",
                "How many things do you have open at once?",
                "Think about a typical afternoon on your computer.",
                options(
                        new Option("1️⃣", "One thing at a time — I focus on one app",                "light_multitasking"),
                        new Option("📂", "A few things — a browser, music, and one main app",        "moderate_multitasking"),
                        new Option("🤯", "Many things — tons of browser tabs, multiple apps",        "heavy_multitasking"),
                        new Option("🖥️", "I want to use two monitors at once",                       "dual_monitor")
                )
        ));

        // Q8 — Storage
        list.add(new Question(
                "💾",
                "How much stuff do you need to store on your PC?",
                "Think about photos, videos, games, and documents.",
                options(
                        new Option("📄", "Not much — mostly documents and light files",              "storage_light"),
                        new Option("📷", "A fair amount — lots of photos and some videos",           "storage_medium"),
                        new Option("🎥", "A lot — I have big video files or many large games",       "storage_heavy"),
                        new Option("🗄️", "Massive — I work with huge files regularly",              "storage_massive")
                )
        ));

        // Q9 — Future upgrading
        list.add(new Question(
                "🔧",
                "Do you want to be able to upgrade your PC later?",
                "Some builds are easier to expand — we can prioritize that for you.",
                options(
                        new Option("📈", "Yes — I want to add more parts as I can afford them",     "upgrade_friendly"),
                        new Option("🔒", "No — just build me the best for my budget right now",     "no_upgrades"),
                        new Option("🤔", "Maybe — I'm not sure yet",                                "upgrade_maybe"),
                        new Option("💰", "I might want to add a better graphics card later",        "gpu_upgrade_plan")
                )
        ));

        // Q10 — Noise / silence
        list.add(new Question(
                "🔊",
                "How important is a quiet PC to you?",
                "Fans cool your PC but can make noise under heavy load.",
                options(
                        new Option("🔇", "Very important — it must be whisper-quiet",               "silent_priority"),
                        new Option("🔉", "Somewhat — a little fan noise is fine",                   "quiet_preferred"),
                        new Option("🔊", "Doesn't matter — I use headphones anyway",                "noise_indifferent"),
                        new Option("🎧", "I stream or record audio — silence is critical",          "audio_critical")
                )
        ));

        return list;
    }

    /** Helper to build an option list cleanly */
    private List<Option> options(Option... opts) {
        List<Option> list = new ArrayList<>();
        for (Option o : opts) list.add(o);
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ViewPager2 Adapter
    // ══════════════════════════════════════════════════════════════════════
    static class QuestionPagerAdapter
            extends RecyclerView.Adapter<QuestionPagerAdapter.QuestionViewHolder> {

        private final List<Question> questions;
        private final String[] answers;

        QuestionPagerAdapter(List<Question> questions, String[] answers) {
            this.questions = questions;
            this.answers   = answers;
        }

        @NonNull
        @Override
        public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_question_item, parent, false);
            return new QuestionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
            Question q = questions.get(position);
            holder.bind(q, position, answers);
        }

        @Override
        public int getItemCount() { return questions.size(); }

        // ── ViewHolder ────────────────────────────────────────────────────
        static class QuestionViewHolder extends RecyclerView.ViewHolder {

            TextView tvQuestionEmoji;
            TextView tvQuestionText;
            TextView tvQuestionSubtitle;
            LinearLayout llOptionsContainer;

            // Track inflated option cards for visual toggling
            List<View> optionViews = new ArrayList<>();

            QuestionViewHolder(@NonNull View itemView) {
                super(itemView);
                tvQuestionEmoji    = itemView.findViewById(R.id.tvQuestionEmoji);
                tvQuestionText     = itemView.findViewById(R.id.tvQuestionText);
                tvQuestionSubtitle = itemView.findViewById(R.id.tvQuestionSubtitle);
                llOptionsContainer = itemView.findViewById(R.id.llOptionsContainer);
            }

            void bind(Question q, int questionIndex, String[] answers) {
                tvQuestionEmoji.setText(q.emoji);
                tvQuestionText.setText(q.text);
                tvQuestionSubtitle.setText(q.subtitle);

                // Remove previously bound option views
                llOptionsContainer.removeAllViews();
                optionViews.clear();

                for (int i = 0; i < q.options.size(); i++) {
                    Option opt = q.options.get(i);
                    final int optIndex = i;

                    // Inflate option card
                    View optView = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_option_card, llOptionsContainer, false);

                    TextView tvEmoji  = optView.findViewById(R.id.tvOptionEmoji);
                    TextView tvLabel  = optView.findViewById(R.id.tvOptionLabel);
                    View     dot      = optView.findViewById(R.id.viewSelectionDot);
                    CardView card     = optView.findViewById(R.id.cardOption);

                    tvEmoji.setText(opt.emoji);
                    tvLabel.setText(opt.label);

                    // Restore selected state if user already answered this question
                    boolean isSelected = opt.value.equals(answers[questionIndex]);
                    applySelectionState(card, tvLabel, dot, isSelected);

                    // Click handler
                    optView.setOnClickListener(v -> {
                        // Save answer
                        answers[questionIndex] = opt.value;

                        // Update visuals for ALL options in this question
                        for (int j = 0; j < optionViews.size(); j++) {
                            View sibling = optionViews.get(j);
                            Option sibOpt = q.options.get(j);
                            boolean selected = sibOpt.value.equals(opt.value);
                            applySelectionState(
                                    sibling.findViewById(R.id.cardOption),
                                    sibling.findViewById(R.id.tvOptionLabel),
                                    sibling.findViewById(R.id.viewSelectionDot),
                                    selected
                            );
                        }
                    });

                    llOptionsContainer.addView(optView);
                    optionViews.add(optView);
                }
            }

            /**
             * Applies or removes the selected visual state to an option card.
             * Selected:   accent border (#6EE2F5), label full white, filled dot
             * Unselected: subtle border, muted label, empty dot
             */
            private void applySelectionState(CardView card, TextView label, View dot, boolean selected) {
                if (selected) {
                    // Accent stroke + tinted background
                    card.setCardBackgroundColor(0xFF162233); // slightly lighter than #1E293B
                    // CardView doesn't support strokeColor directly in code without
                    // MaterialCardView — use a drawable background swap instead:
                    card.setBackground(
                            card.getContext().getResources().getDrawable(
                                    R.drawable.bg_option_selected, null));
                    label.setTextColor(0xFFFFFFFF);
                    dot.setBackground(
                            card.getContext().getResources().getDrawable(
                                    R.drawable.bg_radio_selected, null));
                } else {
                    card.setBackground(
                            card.getContext().getResources().getDrawable(
                                    R.drawable.bg_option_unselected, null));
                    label.setTextColor(0xFFCBD5E1);
                    dot.setBackground(
                            card.getContext().getResources().getDrawable(
                                    R.drawable.bg_radio_unselected, null));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Custom page transformer — subtle slide + scale effect
    // ══════════════════════════════════════════════════════════════════════
    static class SlideScalePageTransformer implements ViewPager2.PageTransformer {
        @Override
        public void transformPage(@NonNull View page, float position) {
            float absPos = Math.abs(position);
            page.setAlpha(1f - absPos * 0.4f);
            page.setScaleY(1f - absPos * 0.04f);
            page.setTranslationX(page.getWidth() * -position * 0.05f);
        }
    }
}
