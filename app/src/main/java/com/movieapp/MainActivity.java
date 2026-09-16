package com.movieapp;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.provider.MediaStore;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.net.URL;
import android.view.ViewGroup;
import java.util.*;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;

public class MainActivity extends Activity {

    final int BG = Color.rgb(8,10,16);
    final int PANEL = Color.rgb(22,25,34);
    final int PANEL2 = Color.rgb(30,34,45);
    final int BLUE = Color.rgb(38,145,255);
    final int WHITE = Color.WHITE;
    final int GRAY = Color.rgb(160,166,178);

    // Chrome-style screen history
    ArrayList<String> screenHistory = new ArrayList<>();
    String currentScreen = "home";

    LinearLayout root;
    FirebaseFirestore db;
    FirebaseAuth auth;
    GoogleSignInClient googleClient;
    static final int GOOGLE_LOGIN_REQUEST = 7001;

    // Cineva local data
    android.content.SharedPreferences cinevaPrefs;
    ArrayList<String> myList = new ArrayList<>();
    ArrayList<String> watchHistory = new ArrayList<>();

    int cinevaCoins = 0;
    int dailyTasksCompleted = 0;
    boolean premiumActive = false;




    ArrayList<HashMap<String,Object>> firebaseMovies = new ArrayList<>();

    FrameLayout page;
    TextView logo;

    String[] movies = {
        "Stranger Things",
        "Action Zero",
        "Sacrificed",
        "The Hunter",
        "Cinematic",
        "Night Run",
        "Final Mission",
        "Lost Planet",
        "After Dark",
        "The Journey",
        "Dark City",
        "Red Moon"
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        cinevaPrefs = getSharedPreferences("cineva_local", MODE_PRIVATE);
        loadLocalData();
        loadFeatureData();

        GoogleSignInOptions googleOptions =
            new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
            )
            .requestIdToken(getString(
                getResources().getIdentifier(
                    "default_web_client_id",
                    "string",
                    getPackageName()
                )
            ))
            .requestEmail()
            .build();

        googleClient =
            GoogleSignIn.getClient(this, googleOptions);

        loadFirebaseMovies();

        showSplash();
    }


    void loadFeatureData() {
        cinevaCoins = cinevaPrefs.getInt("coins", 0);
        dailyTasksCompleted = cinevaPrefs.getInt("daily_tasks_completed", 0);
        premiumActive = cinevaPrefs.getBoolean("premium_active", false);
    }

    void saveFeatureData() {
        cinevaPrefs.edit()
            .putInt("coins", cinevaCoins)
            .putInt("daily_tasks_completed", dailyTasksCompleted)
            .putBoolean("premium_active", premiumActive)
            .apply();
    }

    void addCoins(int amount) {
        cinevaCoins += amount;
        saveFeatureData();
        Toast.makeText(
            this,
            "+" + amount + " Coins",
            Toast.LENGTH_SHORT
        ).show();
    }

    void showCoinsTasks() {
        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15), dp(10), dp(15), dp(25));

        TextView back = txt("‹   Coins & Daily Tasks",20,WHITE,true);
        back.setPadding(0,0,0,15);
        back.setOnClickListener(v -> goBackHistory());
        c.addView(back);

        TextView coins = center(
            "🪙  " + cinevaCoins + " Coins",
            26, WHITE, true
        );
        coins.setBackground(bg(PANEL2,12));

        LinearLayout.LayoutParams coinLp =
            new LinearLayout.LayoutParams(-1,dp(90));
        coinLp.setMargins(0,0,0,15);
        c.addView(coins,coinLp);

        c.addView(section("Daily Tasks"));

        TextView watchTask =
            txt("▶  Watch a movie     +10 Coins",15,WHITE,true);
        watchTask.setPadding(dp(15),dp(16),dp(10),dp(16));
        watchTask.setBackground(bg(PANEL,8));

        LinearLayout.LayoutParams taskLp =
            new LinearLayout.LayoutParams(-1,dp(58));
        taskLp.setMargins(0,3,0,3);
        c.addView(watchTask,taskLp);

        TextView listTask =
            txt("＋  Add a movie to My List     +5 Coins",
                15,WHITE,true);
        listTask.setPadding(dp(15),dp(16),dp(10),dp(16));
        listTask.setBackground(bg(PANEL,8));
        c.addView(listTask,taskLp);

        TextView info = txt(
            "\nComplete tasks to collect Cineva Coins.\n"
            + "Coins can later be used for Cineva rewards.\n"
            + "Premium subscription will be added later.",
            13,GRAY,false
        );
        c.addView(info);

        page.addView(c);
    }

    void showSubscription() {
        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15),dp(10),dp(15),dp(25));

        TextView back =
            txt("‹   Cineva Premium",20,WHITE,true);
        back.setPadding(0,0,0,15);
        back.setOnClickListener(v -> goBackHistory());
        c.addView(back);

        c.addView(
            center(
                premiumActive
                    ? "💎 Premium Active"
                    : "💎 Cineva Premium",
                25, WHITE, true
            ),
            new LinearLayout.LayoutParams(-1,dp(75))
        );

        c.addView(section("Premium Benefits"));

        String[] benefits = {
            "✓  Ad-free watching",
            "✓  Premium features",
            "✓  Better viewing experience",
            "✓  Future premium rewards",
            "✓  Premium badge"
        };

        for(String b : benefits) {
            TextView item = txt(b,15,WHITE,false);
            item.setPadding(dp(15),dp(14),dp(10),dp(14));
            item.setBackground(bg(PANEL,8));

            LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1,dp(50));
            lp.setMargins(0,2,0,2);
            c.addView(item,lp);
        }

        TextView activate =
            center(
                premiumActive
                    ? "✓  Premium Active"
                    : "Activate Premium (Free Demo)",
                15,WHITE,true
            );

        activate.setBackground(bg(PANEL2,10));

        LinearLayout.LayoutParams ap =
            new LinearLayout.LayoutParams(-1,dp(55));
        ap.setMargins(0,dp(15),0,0);
        c.addView(activate,ap);

        activate.setOnClickListener(v -> {
            premiumActive = true;
            saveFeatureData();
            activate.setText("✓  Premium Active");
            Toast.makeText(
                this,
                "Premium activated",
                Toast.LENGTH_SHORT
            ).show();
        });

        c.addView(
            txt(
                "\nPayment/subscription billing can be connected later.",
                12,GRAY,false
            )
        );

        page.addView(c);
    }

    void showProfile() {
        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15),dp(10),dp(15),dp(25));

        TextView back =
            txt("‹   Profile",20,WHITE,true);
        back.setPadding(0,0,0,15);
        back.setOnClickListener(v -> goBackHistory());
        c.addView(back);

        FirebaseUser user =
            auth != null ? auth.getCurrentUser() : null;

        String name = "Guest User";
        String email = "Not signed in";

        if(user != null) {
            if(user.getDisplayName() != null &&
               !user.getDisplayName().isEmpty()) {
                name = user.getDisplayName();
            }

            if(user.getEmail() != null &&
               !user.getEmail().isEmpty()) {
                email = user.getEmail();
            }
        }

        c.addView(
            center("●",55,BLUE,true),
            new LinearLayout.LayoutParams(-1,dp(75))
        );

        c.addView(
            center(name,21,WHITE,true),
            new LinearLayout.LayoutParams(-1,dp(45))
        );

        c.addView(
            center(email,13,GRAY,false),
            new LinearLayout.LayoutParams(-1,dp(40))
        );

        c.addView(section("Cineva Account"));

        c.addView(
            center("🪙  " + cinevaCoins + " Coins",16,WHITE,true),
            new LinearLayout.LayoutParams(-1,dp(55))
        );

        c.addView(
            center(
                premiumActive
                    ? "💎 Premium Active"
                    : "Free Plan",
                15,WHITE,true
            ),
            new LinearLayout.LayoutParams(-1,dp(55))
        );

        page.addView(c);
    }

    void loadLocalData() {
        myList.clear();
        watchHistory.clear();

        String list = cinevaPrefs.getString("my_list", "");
        String history = cinevaPrefs.getString("watch_history", "");

        if (!list.isEmpty()) {
            myList.addAll(Arrays.asList(list.split("\\|")));
        }

        if (!history.isEmpty()) {
            watchHistory.addAll(Arrays.asList(history.split("\\|")));
        }
    }

    void saveLocalData() {
        cinevaPrefs.edit()
            .putString("my_list", joinLocalData(myList))
            .putString("watch_history", joinLocalData(watchHistory))
            .apply();
    }

    String joinLocalData(ArrayList<String> data) {
        StringBuilder b = new StringBuilder();

        for (String x : data) {
            if (x == null || x.trim().isEmpty()) continue;

            if (b.length() > 0) b.append("|");

            b.append(x.replace("|", ""));
        }

        return b.toString();
    }

    boolean isInMyList(String title) {
        return myList.contains(title);
    }

    void toggleMyList(String title) {
        if (title == null || title.trim().isEmpty()) return;

        if (myList.contains(title)) {
            myList.remove(title);

            Toast.makeText(
                this,
                "Removed from My List",
                Toast.LENGTH_SHORT
            ).show();

        } else {
            myList.add(0, title);

            Toast.makeText(
                this,
                "Added to My List",
                Toast.LENGTH_SHORT
            ).show();
        }

        saveLocalData();
    }

    void addWatchHistory(String title) {
        if (title == null || title.trim().isEmpty()) return;

        watchHistory.remove(title);
        watchHistory.add(0, title);

        while (watchHistory.size() > 50) {
            watchHistory.remove(watchHistory.size() - 1);
        }

        saveLocalData();
    }

    void loadFirebaseMovies() {
        db.collection("movies")
            .get()
            .addOnSuccessListener(snapshot -> {
                android.util.Log.d("CINEVA_FIREBASE",
                    "MOVIES FOUND = " + snapshot.size());
                firebaseMovies.clear();

                for (com.google.firebase.firestore.DocumentSnapshot d : snapshot.getDocuments()) {
                    HashMap<String,Object> movie = new HashMap<>();
                    movie.put("id", d.getId());
                    movie.putAll(d.getData());
                    firebaseMovies.add(movie);
                }

                runOnUiThread(() -> {
                    // Home refresh only after login.
                    if (auth != null && auth.getCurrentUser() != null) {
                        showHome();
                    }
                });
            })
            .addOnFailureListener(e -> {
                runOnUiThread(() -> {
                    Toast.makeText(
                        this,
                        "Firebase: " + e.getMessage(),
                        Toast.LENGTH_LONG
                    ).show();
                });
            });
    }

    String[] getFirebaseMovieTitles() {
        if (firebaseMovies.isEmpty()) {
            return new String[0];
        }

        String[] result = new String[firebaseMovies.size()];

        for (int i = 0; i < firebaseMovies.size(); i++) {
            Object title = firebaseMovies.get(i).get("title");
            result[i] = title == null ? "Untitled" : String.valueOf(title);
        }

        return result;
    }

    HashMap<String,Object> findFirebaseMovie(String title) {
        for (HashMap<String,Object> movie : firebaseMovies) {
            Object t = movie.get("title");

            if (t != null && String.valueOf(t).equalsIgnoreCase(title)) {
                return movie;
            }
        }

        return null;
    }

    String movieValue(HashMap<String,Object> movie, String key, String fallback) {
        if (movie == null) return fallback;

        Object value = movie.get(key);

        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return fallback;
        }

        return String.valueOf(value);
    }

    void applySystemInsets(View v) {
        v.setOnApplyWindowInsetsListener((view, insets) -> {
            android.graphics.Insets bars =
                insets.getInsets(android.view.WindowInsets.Type.systemBars());

            view.setPadding(
                view.getPaddingLeft(),
                bars.top,
                view.getPaddingRight(),
                bars.bottom
            );

            return insets;
        });

        v.requestApplyInsets();
    }

    GradientDrawable bg(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        return g;
    }

    TextView txt(String s, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);

        if (bold)
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        return t;
    }

    TextView center(String s, float size, int color, boolean bold) {
        TextView t = txt(s,size,color,bold);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    TextView button(String s) {
        TextView b = center(s,13,WHITE,true);
        b.setBackground(bg(BLUE,12));
        b.setPadding(18,0,18,0);
        return b;
    }

    void showSplash() {

        LinearLayout splash = new LinearLayout(this);
        splash.setOrientation(LinearLayout.VERTICAL);
        splash.setGravity(Gravity.CENTER);
        splash.setBackgroundColor(Color.rgb(15,19,29));

        TextView l = center("cineva",52,BLUE,true); l.setIncludeFontPadding(true);
        splash.addView(l,new LinearLayout.LayoutParams(-1,150));

        TextView spin = center("◌",28,GRAY,false);
        splash.addView(spin,new LinearLayout.LayoutParams(-1,dp(65)));

        setContentView(splash);
        applySystemInsets(splash);

        new Handler().postDelayed(() -> {
            if (auth != null && auth.getCurrentUser() != null) {
                showHome();
            } else {
                showLogin();
            }
        },1200);
    }


    void showLogin() {

        screenHistory.clear();
        currentScreen = "login";

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER);
        c.setPadding(30,30,30,30);
        c.setBackgroundColor(BG);

        TextView logoText =
            center("cineva",42,BLUE,true);

        c.addView(
            logoText,
            new LinearLayout.LayoutParams(-1,dp(90))
        );

        TextView welcome =
            center(
                "Welcome to CINEVA",
                22,WHITE,true
            );

        c.addView(
            welcome,
            new LinearLayout.LayoutParams(-1,dp(55))
        );

        TextView sub =
            center(
                "Sign in to continue",
                14,GRAY,false
            );

        c.addView(
            sub,
            new LinearLayout.LayoutParams(-1,dp(45))
        );

        TextView google =
            button("Continue with Google");

        LinearLayout.LayoutParams gp =
            new LinearLayout.LayoutParams(-1,dp(55));

        gp.setMargins(0,30,0,12);

        c.addView(google,gp);

        google.setBackground(bg(Color.WHITE,12));
        google.setTextColor(Color.BLACK);

        google.setOnClickListener(
            v -> startGoogleLogin()
        );

        TextView guest =
            center(
                "Continue as Guest",
                14,WHITE,true
            );

        guest.setBackground(bg(PANEL2,12));

        LinearLayout.LayoutParams guestLp =
            new LinearLayout.LayoutParams(-1,dp(55));

        c.addView(guest,guestLp);

        guest.setOnClickListener(
            v -> startGuestLogin()
        );

        TextView note =
            center(
                "Guest mode does not require an account",
                12,GRAY,false
            );

        c.addView(
            note,
            new LinearLayout.LayoutParams(-1,dp(50))
        );

        setContentView(c);
        applySystemInsets(c);
    }

    void startGoogleLogin() {

        if (googleClient == null) {
            Toast.makeText(
                this,
                "Google login is not ready",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent intent =
            googleClient.getSignInIntent();

        startActivityForResult(
            intent,
            GOOGLE_LOGIN_REQUEST
        );
    }

    void startGuestLogin() {

        auth.signInAnonymously()
            .addOnSuccessListener(result -> {

                screenHistory.clear();
                currentScreen = "home";

                showHome();

                Toast.makeText(
                    this,
                    "Guest login successful",
                    Toast.LENGTH_SHORT
                ).show();

            })
            .addOnFailureListener(e -> {

                Toast.makeText(
                    this,
                    "Guest login failed: " + e.getMessage(),
                    Toast.LENGTH_LONG
                ).show();
            });
    }



    void logoutUser() {

        auth.signOut();

        if (googleClient != null) {
            googleClient.signOut();
        }

        screenHistory.clear();
        currentScreen = "login";

        showLogin();
    }

    void base() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        page = new FrameLayout(this);

        root.addView(
            page,
            new LinearLayout.LayoutParams(
                -1,0,1
            )
        );

        root.addView(bottom());

        setContentView(root);
    }

    View bottom() {

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(3), 0, dp(3), 0);
        nav.setBackgroundColor(Color.rgb(13,15,21));

        String[] names = {
            "⌂\nHome",
            "⌕\nSearch",
            "◉\nComing Soon",
            "⇩\nDownloads",
            "☰\nMy List"
        };

        for (String n : names) {

            TextView v = center(n, 14, GRAY, false);
            v.setGravity(Gravity.CENTER);
            v.setIncludeFontPadding(true);
            v.setLineSpacing(0, 0.9f);

            LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                    0, dp(68), 1
                );

            nav.addView(v, lp);

            if (n.startsWith("⌂"))
                v.setOnClickListener(x -> navigateTopLevel("home"));

            if (n.startsWith("⌕"))
                v.setOnClickListener(x -> showSearch());

            if (n.startsWith("◉"))
                v.setOnClickListener(x -> showComingSoon());

            if (n.startsWith("⇩"))
                v.setOnClickListener(x -> navigateTopLevel("downloads"));

            if (n.startsWith("☰"))
                v.setOnClickListener(x -> navigateTopLevel("mylist"));
        }

        return nav;
    }

    void topBar(LinearLayout c, boolean back) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(10, 8, 10, 8);
        bar.setMinimumHeight(dp(80));

        if (back) {
            TextView b = center("‹",38,WHITE,false);
            bar.addView(
                b,
                new LinearLayout.LayoutParams(dp(52),dp(64))
            );
            b.setOnClickListener(v -> showHome());
        }

        logo = txt("cineva",36,BLUE,true);
        logo.setIncludeFontPadding(true);
        logo.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams logoLp =
            new LinearLayout.LayoutParams(0,dp(64),1);

        bar.addView(logo, logoLp);

        TextView cast = center("▣",27,WHITE,false);
        TextView search = center("⌕",31,WHITE,false);

        cast.setGravity(Gravity.CENTER);
        search.setGravity(Gravity.CENTER);

        bar.addView(
            cast,
            new LinearLayout.LayoutParams(dp(58),dp(64))
        );

        bar.addView(
            search,
            new LinearLayout.LayoutParams(dp(58),dp(64))
        );

        search.setOnClickListener(v -> showSearch());

        c.addView(
            bar,
            new LinearLayout.LayoutParams(
                -1,
                dp(80)
            )
        );
    }

    TextView section(String s) {
        TextView t = txt(s,19,WHITE,true);
        t.setPadding(2,14,2,8);
        return t;
    }

    int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    TextView posterLabel(String name) {
        TextView t = center(name, 14, WHITE, true);
        t.setPadding(dp(3), dp(5), dp(3), dp(5));
        t.setMaxLines(2);
        return t;
    }

    ImageView posterImage(String name, String url) {

        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setBackground(bg(PANEL2, dp(8)));
        img.setContentDescription(name);

        img.setOnClickListener(v -> { screenHistory.add(currentScreen); currentScreen = "details"; showDetails(name); });

        String finalUrl = url;

        HashMap<String,Object> movie = findFirebaseMovie(name);

        if (movie != null) {
            String firebasePoster =
                movieValue(movie, "posterUrl", "");

            if (firebasePoster.isEmpty())
                firebasePoster =
                    movieValue(movie, "poster", "");

            if (!firebasePoster.isEmpty())
                finalUrl = firebasePoster;
        }

        final String imageUrl = finalUrl;

        new Thread(() -> {
            try {

                InputStream input =
                    new URL(imageUrl).openStream();

                Bitmap bitmap =
                    BitmapFactory.decodeStream(input);

                input.close();

                runOnUiThread(() -> {
                    if (bitmap != null)
                        img.setImageBitmap(bitmap);
                });

            } catch (Exception ignored) {}
        }).start();

        return img;
    }

    void posterRow(LinearLayout c, String[] names) {

        HorizontalScrollView hsv =
            new HorizontalScrollView(this);

        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setClipToPadding(false);
        hsv.setPadding(0, 0, 0, dp(4));

        LinearLayout row =
            new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);

        String[] fallbackUrls = {
            "https://picsum.photos/seed/cineva01/500/750",
            "https://picsum.photos/seed/cineva02/500/750",
            "https://picsum.photos/seed/cineva03/500/750",
            "https://picsum.photos/seed/cineva04/500/750",
            "https://picsum.photos/seed/cineva05/500/750",
            "https://picsum.photos/seed/cineva06/500/750"
        };

        for (int i = 0; i < names.length; i++) {

            LinearLayout card =
                new LinearLayout(this);

            card.setOrientation(
                LinearLayout.VERTICAL
            );

            ImageView image =
                posterImage(
                    names[i],
                    fallbackUrls[i % fallbackUrls.length]
                );

            card.addView(
                image,
                new LinearLayout.LayoutParams(
                    dp(170),
                    dp(255)
                )
            );

            TextView title =
                posterLabel(names[i]);

            card.addView(
                title,
                new LinearLayout.LayoutParams(
                    dp(170),
                    dp(52)
                )
            );

            LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(
                    dp(170),
                    dp(307)
                );

            cp.setMargins(
                0, 0, dp(10), 0
            );

            row.addView(card, cp);
        }

        hsv.addView(row);

        c.addView(
            hsv,
            new LinearLayout.LayoutParams(
                -1,
                dp(315)
            )
        );
    }

    void navigateTopLevel(String screen) {

        // Home / Downloads / Profile / Settings are top-level pages.
        // Going from one top-level page to another always makes
        // Home the Back destination.
        if ("home".equals(screen)) {
            screenHistory.clear();
            currentScreen = "home";
            showHome();
            return;
        }

        screenHistory.clear();
        screenHistory.add("home");
        currentScreen = screen;

        openScreen(screen);
    }

    void navigateTo(String screen) {

        if (screen == null) return;

        if (!screen.equals(currentScreen)) {
            screenHistory.add(currentScreen);
            currentScreen = screen;
        }

        openScreen(screen);
    }

    void openScreen(String screen) {

        if ("home".equals(screen)) {
            showHome();
        } else if ("downloads".equals(screen)) {
            showDownloads();
        } else if ("profiles".equals(screen)) {
            showProfiles();
        } else if ("mylist".equals(screen)) {
            showMyList();
        } else if ("settings".equals(screen)) {
            showSettings();
        } else if ("profile".equals(screen)) {
            showProfile();
        } else if ("subscription".equals(screen)) {
            showSubscription();
        } else if ("tasks".equals(screen)) {
            showCoinsTasks();
        }
    }

    void goBackHistory() {

        if (screenHistory.isEmpty()) {
            finish();
            return;
        }

        String previous =
            screenHistory.remove(
                screenHistory.size() - 1
            );

        currentScreen = previous;

        openScreen(previous);
    }

    void showHome() {

        base();

        ScrollView scroll = new ScrollView(this);

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(10,5,10,20);

        topBar(c,false);

        // HERO
        FrameLayout hero = new FrameLayout(this);

        GradientDrawable heroBg =
            new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                    Color.rgb(32,75,112),
                    Color.rgb(18,29,43),
                    Color.rgb(8,11,17)
                }
            );

        hero.setBackground(heroBg);

        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.VERTICAL);
        h.setGravity(Gravity.BOTTOM);
        h.setPadding(18,15,18,18);

        h.addView(
            txt("CINEVA",11,BLUE,true)
        );

        String heroTitle = "Welcome to CINEVA";

        if(firebaseMovies.size() > 0) {
            heroTitle =
                movieValue(
                    firebaseMovies.get(0),
                    "title",
                    "Welcome to CINEVA"
                );
        }

        h.addView(
            txt(heroTitle,28,WHITE,true)
        );

        h.addView(
            txt(
                "Watch movies and discover new stories.",
                11,GRAY,false
            )
        );

        if(firebaseMovies.size() > 0) {

            String firstTitle =
                movieValue(
                    firebaseMovies.get(0),
                    "title",
                    ""
                );

            TextView play = button("▶  Play");

            play.setOnClickListener(
                v -> { screenHistory.add(currentScreen); currentScreen = "details"; showDetails(firstTitle); }
            );

            h.addView(play);
        }

        hero.addView(
            h,
            new FrameLayout.LayoutParams(-1,-1)
        );

        c.addView(
            hero,
            new LinearLayout.LayoutParams(-1,dp(300))
        );

        // FIREBASE MOVIES
        c.addView(section("All Movies"));

        if(firebaseMovies.size() > 0) {

            posterGrid(
                c,
                getFirebaseMovieTitles()
            );

        } else {

            c.addView(
                center(
                    "No movies available",
                    14,GRAY,false
                ),
                new LinearLayout.LayoutParams(
                    -1,dp(100)
                )
            );
        }

        // GENRES
        c.addView(section("Browse by Genre"));

        genreRow(c);

        scroll.addView(c);

        page.addView(scroll);
    }

    void genreRow(LinearLayout c) {

        HorizontalScrollView hsv =
            new HorizontalScrollView(this);

        LinearLayout row = new LinearLayout(this);

        String[] genres = {
            "Action","Drama","Comedy","Animation",
            "Romance","Thriller","Sci-Fi"
        };

        for(String g:genres) {

            TextView chip =
                center(g,11,WHITE,false);

            chip.setBackground(bg(PANEL2,15));

            LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(dp(90),dp(38));

            lp.setMargins(0,0,7,0);

            row.addView(chip,lp);
        }

        hsv.addView(row);
        c.addView(hsv,new LinearLayout.LayoutParams(-1,dp(48)));
    }

    String normalizeSearch(String s) {
        if (s == null) return "";

        return s.toLowerCase()
            .replaceAll("[^a-z0-9 ]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }

    int levenshtein(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++)
            d[i][0] = i;

        for (int j = 0; j <= b.length(); j++)
            d[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {

                int cost =
                    a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;

                d[i][j] = Math.min(
                    Math.min(
                        d[i - 1][j] + 1,
                        d[i][j - 1] + 1
                    ),
                    d[i - 1][j - 1] + cost
                );
            }
        }

        return d[a.length()][b.length()];
    }

    boolean movieMatches(String title, String query) {

        String t = normalizeSearch(title);
        String q = normalizeSearch(query);

        if (q.isEmpty()) return true;

        // Exact / partial match
        if (t.contains(q)) return true;

        String[] words = t.split(" ");

        // Single-word typo matching
        for (String qw : q.split(" ")) {

            if (qw.length() < 3) continue;

            for (String tw : words) {

                int distance = levenshtein(qw, tw);

                int allowed =
                    qw.length() <= 4 ? 1 :
                    qw.length() <= 7 ? 2 : 3;

                if (distance <= allowed)
                    return true;
            }
        }

        // Whole title typo matching
        if (q.length() >= 4) {

            int distance = levenshtein(q, t);

            int allowed =
                q.length() <= 6 ? 1 :
                q.length() <= 12 ? 2 : 3;

            if (distance <= allowed)
                return true;
        }

        return false;
    }

    String[] getSearchResults(String query) {

        ArrayList<String> results = new ArrayList<>();

        String q = normalizeSearch(query);

        if (q.isEmpty()) {
            return getFirebaseMovieTitles();
        }

        for (String title : getFirebaseMovieTitles()) {

            if (movieMatches(title, q)) {
                results.add(title);
            }
        }

        return results.toArray(new String[0]);
    }

    void showSearch() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(10,8,10,20);

        topBar(c,true);

        EditText input = new EditText(this);

        input.setSingleLine(true);
        input.setHint("Movies, Shows, People...");
        input.setHintTextColor(GRAY);
        input.setTextColor(WHITE);
        input.setTextSize(14);
        input.setPadding(15,0,15,0);
        input.setBackground(bg(PANEL2,10));

        c.addView(
            input,
            new LinearLayout.LayoutParams(-1,dp(65))
        );

        c.addView(section("Top Searches"));

        String[] topMovies = getFirebaseMovieTitles();

        if (topMovies.length > 0) {
            posterGrid(c, topMovies);
        } else {
            c.addView(
                center(
                    "No movies available",
                    15,GRAY,false
                ),
                new LinearLayout.LayoutParams(-1,dp(100))
            );
        }

        input.setOnEditorActionListener(
            (v,a,e) -> {
                showSearchResults(input.getText().toString());
                return true;
            }
        );

        page.addView(c);
    }

    void showSearchResults(String query) {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(10,8,10,20);

        topBar(c,true);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(query);
        input.setTextColor(WHITE);
        input.setTextSize(14);
        input.setBackground(bg(PANEL2,10));

        c.addView(input,
            new LinearLayout.LayoutParams(-1,dp(65)));

        c.addView(section(
            query.length()==0 ?
            "Search Results" :
            "Results for \""+query+"\""
        ));

        String[] results = getSearchResults(query);

        if (results.length > 0) {

            input.setOnEditorActionListener(
            (v,a,e) -> {
                showSearchResults(input.getText().toString());
                return true;
            }
        );

        posterGrid(c,results);

        } else {

            c.addView(
                center(
                    "No results found",
                    15,GRAY,false
                ),
                new LinearLayout.LayoutParams(
                    -1,dp(100)
                )
            );
        }

        ScrollView s = new ScrollView(this);
        s.addView(c);

        page.addView(s);
    }

    void posterGrid(LinearLayout c,String[] names) {

        for(int i=0;i<names.length;i+=3) {

            LinearLayout row = new LinearLayout(this);

            for(int j=i;j<i+3 && j<names.length;j++) {

                String movieTitle = names[j];
                HashMap<String,Object> movie =
                    findFirebaseMovie(movieTitle);

                String posterUrl =
                    movieValue(movie,"posterUrl","");

                LinearLayout card =
                    new LinearLayout(this);

                card.setOrientation(LinearLayout.VERTICAL);

                ImageView image =
                    posterImage(movieTitle,posterUrl);

                card.addView(
                    image,
                    new LinearLayout.LayoutParams(-1,dp(185))
                );

                TextView label =
                    posterLabel(movieTitle);

                card.addView(
                    label,
                    new LinearLayout.LayoutParams(-1,dp(40))
                );

                card.setOnClickListener(
                    v -> { screenHistory.add(currentScreen); currentScreen = "details"; showDetails(movieTitle); }
                );

                LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(0,dp(230),1);

                lp.setMargins(0,0,7,7);

                row.addView(card,lp);
            }

            c.addView(row);
        }
    }

    void showDetails(String title) {

        base();

        HashMap<String,Object> movie =
            findFirebaseMovie(title);

        String posterUrl =
            movieValue(movie,"posterUrl","");

        String backdropUrl =
            movieValue(movie,"backdropUrl","");

        String trailerUrl =
            movieValue(movie,"trailerUrl","");

        String videoUrl =
            movieValue(movie,"videoUrl","");

        String genre =
            movieValue(movie,"genre","Other");

        String year =
            movieValue(movie,"year","");

        String language =
            movieValue(movie,"language","");

        String duration =
            movieValue(movie,"duration","");

        String rating =
            movieValue(movie,"rating","");

        String description =
            movieValue(movie,"description",
                "No description available.");

        ScrollView scroll = new ScrollView(this);

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(10,5,10,25);

        topBar(c,true);

        // BACKDROP
        if(!backdropUrl.isEmpty()) {

            ImageView backdrop =
                posterImage(title,backdropUrl);

            c.addView(
                backdrop,
                new LinearLayout.LayoutParams(-1,260)
            );

        } else if(!posterUrl.isEmpty()) {

            ImageView poster =
                posterImage(title,posterUrl);

            c.addView(
                poster,
                new LinearLayout.LayoutParams(-1,260)
            );
        }

        c.addView(
            txt(title,25,WHITE,true)
        );

        String meta = genre;

        if(!year.isEmpty())
            meta += " • " + year;

        if(!language.isEmpty())
            meta += " • " + language;

        if(!duration.isEmpty())
            meta += " • " + duration;

        if(!rating.isEmpty())
            meta += " • ⭐ " + rating;

        c.addView(
            txt(meta,12,GRAY,false)
        );

        // PLAY BUTTON
        TextView play =
            button("▶  Play");

        LinearLayout.LayoutParams pp =
            new LinearLayout.LayoutParams(-1,dp(48));

        pp.setMargins(0,14,0,8);

        c.addView(play,pp);

        play.setOnClickListener(v -> {

            if(!videoUrl.isEmpty() && !trailerUrl.isEmpty()) {

                final String[] options = {
                    "▶  Watch Movie",
                    "▶  Watch Trailer"
                };

                new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setItems(options,(dialog,which) -> {

                        if(which == 0) {
                            openUrl(videoUrl);
                        } else {
                            openUrl(trailerUrl);
                        }

                    })
                    .show();

            } else if(!videoUrl.isEmpty()) {

                openUrl(videoUrl);

            } else if(!trailerUrl.isEmpty()) {

                openUrl(trailerUrl);

            } else {

                Toast.makeText(
                    this,
                    "Movie and trailer are not available",
                    Toast.LENGTH_SHORT
                ).show();
            }
        });

        // MY LIST
        TextView add =
            center(
                isInMyList(title)
                    ? "✓  Remove from My List"
                    : "＋  Add to My List",
                13,WHITE,true
            );

        add.setBackground(bg(PANEL2,10));

        add.setOnClickListener(v -> {
            toggleMyList(title);

            add.setText(
                isInMyList(title)
                    ? "✓  Remove from My List"
                    : "＋  Add to My List"
            );
        });

        c.addView(
            add,
            new LinearLayout.LayoutParams(-1,dp(48))
        );

        // ABOUT
        c.addView(section("About"));

        c.addView(
            txt(description,13,GRAY,false)
        );

        // MORE LIKE THIS
        c.addView(section("More Like This"));

        ArrayList<String> related =
            new ArrayList<>();

        for(HashMap<String,Object> m : firebaseMovies) {

            String otherTitle =
                movieValue(m,"title","");

            if(otherTitle.isEmpty())
                continue;

            if(otherTitle.equalsIgnoreCase(title))
                continue;

            String otherGenre =
                movieValue(m,"genre","");

            if(
                !genre.equalsIgnoreCase("Other") &&
                !otherGenre.isEmpty() &&
                otherGenre.equalsIgnoreCase(genre)
            ) {
                related.add(otherTitle);
            }
        }

        // যদি একই genre-এর movie না থাকে,
        // অন্য Firebase movie দেখাবে
        if(related.isEmpty()) {

            for(HashMap<String,Object> m : firebaseMovies) {

                String otherTitle =
                    movieValue(m,"title","");

                if(otherTitle.isEmpty())
                    continue;

                if(otherTitle.equalsIgnoreCase(title))
                    continue;

                if(!related.contains(otherTitle))
                    related.add(otherTitle);

                if(related.size() >= 6)
                    break;
            }
        }

        if(!related.isEmpty()) {

            posterRow(
                c,
                related.toArray(new String[0])
            );

        } else {

            c.addView(
                txt(
                    "No other movies available yet.",
                    13,GRAY,false
                )
            );
        }

        scroll.addView(c);

        page.addView(scroll);
    }



    // =========================================================
    // CINEVA NETFLIX STYLE VIDEO PLAYER
    // =========================================================

    void playVideo(String title, String url, boolean trailer) {

        if (!trailer) {
            addWatchHistory(title);
        }

        screenHistory.add(currentScreen);
        currentScreen = "player";

        showCinevaPlayer(
            Uri.parse(url),
            "",
            trailer ? "Trailer" : title,
            false
        );
    }

    void playLocalVideo(Uri uri, String mimeType) {

        screenHistory.add(currentScreen);
        currentScreen = "local_video";

        showCinevaPlayer(
            uri,
            mimeType,
            "Local Video",
            true
        );
    }

    void showCinevaPlayer(
        Uri uri,
        String mimeType,
        String videoTitle,
        boolean localVideo
    ) {
        base();

        final boolean[] locked = {false};
        final boolean[] fullscreen = {false};

        // =====================================================
        // ROOT
        // =====================================================

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        // =====================================================
        // VIDEO
        // =====================================================

        androidx.media3.exoplayer.trackselection.DefaultTrackSelector
            trackSelector =
            new androidx.media3.exoplayer.trackselection.DefaultTrackSelector(this);

        PlayerView playerView = new PlayerView(this);

        playerView.setUseController(false);
        playerView.setShowBuffering(
            PlayerView.SHOW_BUFFERING_WHEN_PLAYING
        );
        playerView.setKeepScreenOn(true);
        playerView.setResizeMode(
            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
        );

        root.addView(
            playerView,
            new FrameLayout.LayoutParams(
                -1,
                -1
            )
        );

        // =====================================================
        // TOP BAR
        // =====================================================

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(5), dp(3), dp(5), dp(3));
        top.setBackgroundColor(
            Color.argb(205, 0, 0, 0)
        );

        FrameLayout.LayoutParams topLp =
            new FrameLayout.LayoutParams(
                -1,
                dp(58),
                Gravity.TOP
            );

        TextView back =
            center("‹", 34, WHITE, true);

        top.addView(
            back,
            new LinearLayout.LayoutParams(
                dp(45),
                dp(55)
            )
        );

        back.setOnClickListener(
            v -> goBackHistory()
        );

        // =====================================================
        // TITLE + EPISODE
        // =====================================================

        LinearLayout titleBox =
            new LinearLayout(this);

        titleBox.setOrientation(
            LinearLayout.VERTICAL
        );
        titleBox.setGravity(
            Gravity.CENTER_VERTICAL
        );

        TextView title =
            txt(videoTitle, 15, WHITE, true);

        title.setSingleLine(true);
        title.setEllipsize(
            android.text.TextUtils.TruncateAt.END
        );

        titleBox.addView(
            title,
            new LinearLayout.LayoutParams(
                -1,
                dp(28)
            )
        );

        // Show S01 E01 ONLY when title contains episode/season info
        String episodeText = "";

        java.util.regex.Matcher epMatcher =
            java.util.regex.Pattern
                .compile(
                    "(?i)(S\\d{1,2}\\s*E\\d{1,3})"
                )
                .matcher(videoTitle);

        if (epMatcher.find()) {
            episodeText =
                epMatcher.group(1).toUpperCase();
        }

        if (!episodeText.isEmpty()) {

            TextView episode =
                txt(
                    episodeText,
                    11,
                    GRAY,
                    false
                );

            titleBox.addView(
                episode,
                new LinearLayout.LayoutParams(
                    -1,
                    dp(20)
                )
            );
        }

        top.addView(
            titleBox,
            new LinearLayout.LayoutParams(
                0,
                dp(55),
                1
            )
        );

        // HD badge
        TextView hd =
            center("HD", 11, WHITE, true);

        hd.setBackground(
            bg(Color.rgb(35,35,35), dp(5))
        );

        top.addView(
            hd,
            new LinearLayout.LayoutParams(
                dp(40),
                dp(28)
            )
        );

        // Cast
        TextView cast =
            center("⏏", 21, WHITE, true);

        top.addView(
            cast,
            new LinearLayout.LayoutParams(
                dp(42),
                dp(55)
            )
        );

        cast.setOnClickListener(
            v -> Toast.makeText(
                MainActivity.this,
                "Cast to TV",
                Toast.LENGTH_SHORT
            ).show()
        );

        // Help
        TextView help =
            center("?", 19, WHITE, true);

        top.addView(
            help,
            new LinearLayout.LayoutParams(
                dp(40),
                dp(55)
            )
        );

        help.setOnClickListener(
            v -> new AlertDialog.Builder(this)
                .setTitle("Player Help")
                .setMessage(
                    "Tap the screen to show or hide controls.\n\n" +
                    "−10 / +10 skips 10 seconds.\n\n" +
                    "Use 🔒 Lock to prevent accidental touches.\n\n" +
                    "Use Fit, Subtitles, Audio, Speed and Quality " +
                    "from the bottom controls."
                )
                .setPositiveButton("OK", null)
                .show()
        );

        // Settings
        TextView settings =
            center("⚙", 21, WHITE, true);

        top.addView(
            settings,
            new LinearLayout.LayoutParams(
                dp(42),
                dp(55)
            )
        );

        root.addView(top, topLp);

        // =====================================================
        // CENTER CONTROLS
        // =====================================================

        LinearLayout centerControls =
            new LinearLayout(this);

        centerControls.setOrientation(
            LinearLayout.HORIZONTAL
        );
        centerControls.setGravity(
            Gravity.CENTER
        );

        FrameLayout.LayoutParams centerLp =
            new FrameLayout.LayoutParams(
                -1,
                dp(90),
                Gravity.CENTER
            );

        TextView rewind =
            center("↶\n10", 15, WHITE, true);

        TextView playPause =
            center("▶", 34, WHITE, true);

        TextView forward =
            center("10\n↷", 15, WHITE, true);

        centerControls.addView(
            rewind,
            new LinearLayout.LayoutParams(
                dp(75),
                dp(80)
            )
        );

        centerControls.addView(
            playPause,
            new LinearLayout.LayoutParams(
                dp(95),
                dp(85)
            )
        );

        centerControls.addView(
            forward,
            new LinearLayout.LayoutParams(
                dp(75),
                dp(80)
            )
        );

        root.addView(
            centerControls,
            centerLp
        );

        // =====================================================
        // LOCK BUTTON
        // =====================================================

        TextView lock =
            center(
                "🔒\nTap to Lock",
                11,
                WHITE,
                true
            );

        lock.setBackground(
            bg(
                Color.argb(150,0,0,0),
                dp(8)
            )
        );

        FrameLayout.LayoutParams lockLp =
            new FrameLayout.LayoutParams(
                dp(105),
                dp(60),
                Gravity.CENTER_VERTICAL | Gravity.LEFT
            );

        lockLp.setMargins(
            dp(12), 0, 0, 0
        );

        root.addView(lock, lockLp);

        // =====================================================
        // BOTTOM AREA
        // =====================================================

        LinearLayout bottom =
            new LinearLayout(this);

        bottom.setOrientation(
            LinearLayout.VERTICAL
        );
        bottom.setPadding(
            dp(12),
            dp(5),
            dp(12),
            dp(8)
        );

        bottom.setBackgroundColor(
            Color.argb(220,0,0,0)
        );

        FrameLayout.LayoutParams bottomLp =
            new FrameLayout.LayoutParams(
                -1,
                dp(125),
                Gravity.BOTTOM
            );

        // =====================================================
        // TIMELINE
        // =====================================================

        SeekBar seek =
            new SeekBar(this);

        seek.setMax(1000);

        bottom.addView(
            seek,
            new LinearLayout.LayoutParams(
                -1,
                dp(30)
            )
        );

        LinearLayout timeRow =
            new LinearLayout(this);

        timeRow.setGravity(
            Gravity.CENTER_VERTICAL
        );

        TextView currentTime =
            txt("00:00", 11, WHITE, false);

        TextView totalTime =
            txt("/ 00:00", 11, GRAY, false);

        timeRow.addView(
            currentTime,
            new LinearLayout.LayoutParams(
                dp(52),
                dp(25)
            )
        );

        timeRow.addView(
            totalTime,
            new LinearLayout.LayoutParams(
                0,
                dp(25),
                1
            )
        );

        bottom.addView(timeRow);

        // =====================================================
        // LOWER CONTROLS
        // =====================================================

        LinearLayout controls =
            new LinearLayout(this);

        controls.setGravity(
            Gravity.CENTER_VERTICAL
        );

        TextView bottomPlay =
            center("▶", 19, WHITE, true);

        TextView next =
            center("Next Episode ▶", 11, WHITE, true);

        TextView speedUp =
            center("Speed up", 11, WHITE, true);

        TextView fit =
            center("Fit", 11, WHITE, true);

        TextView subtitles =
            center("CC", 12, WHITE, true);

        TextView audio =
            center("Audio", 11, WHITE, true);

        TextView speed =
            center("1×", 12, WHITE, true);

        TextView quality =
            center("1080p", 11, WHITE, true);

        TextView[] buttons = {
            bottomPlay,
            next,
            speedUp,
            fit,
            subtitles,
            audio,
            speed,
            quality
        };

        int[] widths = {
            42, 92, 68, 45, 45, 55, 42, 58
        };

        for (int i = 0; i < buttons.length; i++) {

            controls.addView(
                buttons[i],
                new LinearLayout.LayoutParams(
                    dp(widths[i]),
                    dp(42)
                )
            );
        }

        bottom.addView(controls);

        root.addView(bottom, bottomLp);

        // =====================================================
        // PLAYER
        // =====================================================

        ExoPlayer player =
            new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();

        playerView.setPlayer(player);

        player.addListener(
            new androidx.media3.common.Player.Listener() {

                @Override
                public void onIsPlayingChanged(
                    boolean isPlaying
                ) {
                    String icon =
                        isPlaying ? "Ⅱ" : "▶";

                    playPause.setText(icon);
                    bottomPlay.setText(icon);
                }
            }
        );

        MediaItem.Builder builder =
            new MediaItem.Builder()
                .setUri(uri);

        if (
            mimeType != null &&
            !mimeType.isEmpty()
        ) {
            builder.setMimeType(mimeType);
        }

        player.setMediaItem(
            builder.build()
        );

        player.prepare();
        player.play();

        playerView.setTag(player);

        // =====================================================
        // PLAY / PAUSE
        // =====================================================

        View.OnClickListener toggle =
            v -> {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
            };

        playPause.setOnClickListener(toggle);
        bottomPlay.setOnClickListener(toggle);

        // =====================================================
        // SEEK
        // =====================================================

        rewind.setOnClickListener(
            v -> seekPlayer(player, -10000)
        );

        forward.setOnClickListener(
            v -> seekPlayer(player, 10000)
        );

        // =====================================================
        // SEEK BAR
        // =====================================================

        seek.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {

                public void onProgressChanged(
                    SeekBar bar,
                    int progress,
                    boolean fromUser
                ) {
                    if (fromUser) {

                        long duration =
                            player.getDuration();

                        if (duration > 0) {

                            long position =
                                duration * progress / 1000;

                            currentTime.setText(
                                formatPlayerTime(position)
                            );
                        }
                    }
                }

                public void onStartTrackingTouch(
                    SeekBar bar
                ) {}

                public void onStopTrackingTouch(
                    SeekBar bar
                ) {
                    long duration =
                        player.getDuration();

                    if (duration > 0) {

                        player.seekTo(
                            duration *
                            bar.getProgress() / 1000
                        );
                    }
                }
            }
        );

        // =====================================================
        // UPDATE TIMELINE
        // =====================================================

        final android.os.Handler handler =
            new android.os.Handler(
                android.os.Looper.getMainLooper()
            );

        final Runnable updater =
            new Runnable() {

                @Override
                public void run() {

                    long duration =
                        player.getDuration();

                    long position =
                        player.getCurrentPosition();

                    if (duration > 0) {

                        seek.setProgress(
                            (int)(
                                position * 1000 / duration
                            )
                        );

                        currentTime.setText(
                            formatPlayerTime(position)
                        );

                        totalTime.setText(
                            "/ " +
                            formatPlayerTime(duration)
                        );
                    }

                    handler.postDelayed(
                        this,
                        500
                    );
                }
            };

        handler.post(updater);

        // =====================================================
        // SPEED UP
        // =====================================================

        speedUp.setOnClickListener(
            v -> {
                float now =
                    player.getPlaybackParameters()
                        .speed;

                float nextSpeed;

                if (now < 1.25f)
                    nextSpeed = 1.25f;
                else if (now < 1.5f)
                    nextSpeed = 1.5f;
                else if (now < 2.0f)
                    nextSpeed = 2.0f;
                else
                    nextSpeed = 1.0f;

                player.setPlaybackSpeed(nextSpeed);

                speed.setText(
                    String.format(
                        java.util.Locale.US,
                        "%.2gx",
                        nextSpeed
                    )
                );
            }
        );

        // =====================================================
        // SPEED SELECTOR
        // =====================================================

        speed.setOnClickListener(
            v -> showPlaybackSpeedMenu(player)
        );

        // =====================================================
        // ASPECT RATIO
        // =====================================================

        fit.setOnClickListener(
            v -> showAspectRatioMenu(playerView)
        );

        // =====================================================
        // QUALITY
        // =====================================================

        quality.setOnClickListener(
            v -> {
                try {

                    new androidx.media3.ui.TrackSelectionDialogBuilder(
                        MainActivity.this,
                        "Video Quality",
                        player,
                        androidx.media3.common.C.TRACK_TYPE_VIDEO
                    ).build().show();

                } catch (Exception e) {

                    Toast.makeText(
                        MainActivity.this,
                        "Video quality is not available",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        );

        // =====================================================
        // AUDIO
        // =====================================================

        audio.setOnClickListener(
            v -> {
                try {

                    new androidx.media3.ui.TrackSelectionDialogBuilder(
                        MainActivity.this,
                        "Audio / Language",
                        player,
                        androidx.media3.common.C.TRACK_TYPE_AUDIO
                    ).build().show();

                } catch (Exception e) {

                    Toast.makeText(
                        MainActivity.this,
                        "No alternate audio available",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        );

        // =====================================================
        // SUBTITLES
        // =====================================================

        subtitles.setOnClickListener(
            v -> {
                try {

                    new androidx.media3.ui.TrackSelectionDialogBuilder(
                        MainActivity.this,
                        "Subtitles / Captions",
                        player,
                        androidx.media3.common.C.TRACK_TYPE_TEXT
                    ).build().show();

                } catch (Exception e) {

                    Toast.makeText(
                        MainActivity.this,
                        "No subtitles available",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        );

        // =====================================================
        // NEXT EPISODE
        // =====================================================

        next.setOnClickListener(
            v -> Toast.makeText(
                MainActivity.this,
                "Next Episode",
                Toast.LENGTH_SHORT
            ).show()
        );

        // =====================================================
        // LOCK
        // =====================================================

        lock.setOnClickListener(
            v -> {

                locked[0] = !locked[0];

                if (locked[0]) {

                    lock.setText(
                        "🔓\nUnlock"
                    );

                    top.setVisibility(
                        View.GONE
                    );

                    centerControls.setVisibility(
                        View.GONE
                    );

                    bottom.setVisibility(
                        View.GONE
                    );

                    Toast.makeText(
                        MainActivity.this,
                        "🔒 Player locked",
                        Toast.LENGTH_SHORT
                    ).show();

                } else {

                    lock.setText(
                        "🔒\nTap to Lock"
                    );

                    top.setVisibility(
                        View.VISIBLE
                    );

                    centerControls.setVisibility(
                        View.VISIBLE
                    );

                    bottom.setVisibility(
                        View.VISIBLE
                    );

                    Toast.makeText(
                        MainActivity.this,
                        "🔓 Player unlocked",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        );

        // =====================================================
        // FULLSCREEN
        // =====================================================

        settings.setOnClickListener(
            v -> {

                final String[] options = {
                    "Fullscreen",
                    "Fit / Fill / Zoom",
                    "Playback Speed",
                    "Video Quality",
                    "Audio / Language",
                    "Subtitles / Captions",
                    "Screen Lock"
                };

                new AlertDialog.Builder(this)
                    .setTitle("Player Settings")
                    .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                fullscreen[0] =
                                    !fullscreen[0];

                                if (fullscreen[0]) {

                                    getWindow().setFlags(
                                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                                    );

                                    setRequestedOrientation(
                                        android.content.pm.ActivityInfo
                                            .SCREEN_ORIENTATION_LANDSCAPE
                                    );

                                } else {

                                    getWindow().clearFlags(
                                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                                    );

                                    setRequestedOrientation(
                                        android.content.pm.ActivityInfo
                                            .SCREEN_ORIENTATION_UNSPECIFIED
                                    );
                                }

                            } else if (which == 1) {

                                showAspectRatioMenu(
                                    playerView
                                );

                            } else if (which == 2) {

                                showPlaybackSpeedMenu(
                                    player
                                );

                            } else if (which == 3) {

                                quality.performClick();

                            } else if (which == 4) {

                                audio.performClick();

                            } else if (which == 5) {

                                subtitles.performClick();

                            } else {

                                lock.performClick();
                            }
                        }
                    )
                    .show();
            }
        );

        // =====================================================
        // TAP VIDEO = SHOW / HIDE CONTROLS
        // =====================================================

        playerView.setOnClickListener(
            v -> {

                if (locked[0])
                    return;

                int visibility =
                    top.getVisibility() == View.VISIBLE
                    ? View.GONE
                    : View.VISIBLE;

                top.setVisibility(visibility);
                centerControls.setVisibility(visibility);
                bottom.setVisibility(visibility);
                lock.setVisibility(visibility);
            }
        );

        page.addView(root);
    }

    String formatPlayerTime(long ms) {

        if (ms < 0)
            ms = 0;

        long totalSeconds =
            ms / 1000;

        long hours =
            totalSeconds / 3600;

        long minutes =
            (totalSeconds % 3600) / 60;

        long seconds =
            totalSeconds % 60;

        if (hours > 0) {

            return String.format(
                java.util.Locale.US,
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            );

        }

        return String.format(
            java.util.Locale.US,
            "%02d:%02d",
            minutes,
            seconds
        );
    }

    // =========================================================
    // SEEK
    // =========================================================

    void seekPlayer(
        ExoPlayer player,
        long amount
    ) {
        long position = player.getCurrentPosition();
        long duration = player.getDuration();

        long target = position + amount;

        if (target < 0) {
            target = 0;
        }

        if (duration > 0 && target > duration) {
            target = duration;
        }

        player.seekTo(target);
    }

    // =========================================================
    // PLAYBACK SPEED
    // =========================================================

    void showPlaybackSpeedMenu(
        ExoPlayer player
    ) {

        final String[] speeds = {
            "0.5×",
            "0.75×",
            "1.0× Normal",
            "1.25×",
            "1.5×",
            "2.0×"
        };

        final float[] values = {
            0.5f,
            0.75f,
            1.0f,
            1.25f,
            1.5f,
            2.0f
        };

        new AlertDialog.Builder(this)
            .setTitle("Playback Speed")
            .setItems(
                speeds,
                (dialog, which) -> {

                    player.setPlaybackSpeed(
                        values[which]
                    );

                    Toast.makeText(
                        MainActivity.this,
                        "Speed: " + speeds[which],
                        Toast.LENGTH_SHORT
                    ).show();
                }
            )
            .show();
    }

    // =========================================================
    // ASPECT RATIO
    // =========================================================

    void showAspectRatioMenu(
        PlayerView playerView
    ) {

        final String[] modes = {
            "Fit",
            "Fill",
            "Zoom"
        };

        new AlertDialog.Builder(this)
            .setTitle("Aspect Ratio")
            .setItems(
                modes,
                (dialog, which) -> {

                    if (which == 0) {

                        playerView.setResizeMode(
                            androidx.media3.ui.AspectRatioFrameLayout
                                .RESIZE_MODE_FIT
                        );

                    } else if (which == 1) {

                        playerView.setResizeMode(
                            androidx.media3.ui.AspectRatioFrameLayout
                                .RESIZE_MODE_FILL
                        );

                    } else {

                        playerView.setResizeMode(
                            androidx.media3.ui.AspectRatioFrameLayout
                                .RESIZE_MODE_ZOOM
                        );
                    }
                }
            )
            .show();
    }

    void openLocalVideo() {

        Intent intent =
            new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent,9001);
    }

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
    ) {
        super.onActivityResult(
            requestCode,resultCode,data
        );

if (requestCode == GOOGLE_LOGIN_REQUEST) {

            try {

                com.google.android.gms.tasks.Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

                GoogleSignInAccount account =
                    task.getResult(ApiException.class);

                if (account == null ||
                    account.getIdToken() == null) {

                    throw new Exception(
                        "Google account information missing"
                    );
                }

                com.google.firebase.auth.AuthCredential credential =
                    GoogleAuthProvider.getCredential(
                        account.getIdToken(),
                        null
                    );

                auth.signInWithCredential(credential)
                    .addOnSuccessListener(result -> {

                        screenHistory.clear();
                        currentScreen = "home";

                        showHome();

                        Toast.makeText(
                            this,
                            "Login successful",
                            Toast.LENGTH_SHORT
                        ).show();

                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                            this,
                            "Firebase login failed",
                            Toast.LENGTH_LONG
                        ).show();
                    });

            } catch(Exception e) {

                Toast.makeText(
                    this,
                    "Google login cancelled or failed",
                    Toast.LENGTH_LONG
                ).show();
            }
        }


        if(requestCode == 9001 &&
           resultCode == RESULT_OK &&
           data != null &&
           data.getData() != null) {

            Uri uri = data.getData();

            try {
                getContentResolver()
                    .takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
            } catch(Exception ignored) {}

            playLocalVideo(uri);
        }
    }

    void playLocalVideo(Uri uri) {
        try {
            String name = "Local Video";

            if (uri != null) {
                android.database.Cursor cursor = null;

                try {
                    cursor = getContentResolver().query(
                        uri,
                        new String[]{
                            MediaStore.Video.Media.DISPLAY_NAME
                        },
                        null,
                        null,
                        null
                    );

                    if (cursor != null && cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(
                            MediaStore.Video.Media.DISPLAY_NAME
                        );

                        if (index >= 0) {
                            String found = cursor.getString(index);

                            if (found != null && !found.trim().isEmpty()) {
                                name = found;
                            }
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    if (cursor != null) cursor.close();
                }

                addWatchHistory(
                    "LOCAL_VIDEO::" + name + "::" + uri.toString()
                );
            } else {
                addWatchHistory("LOCAL_VIDEO::Local Video::");
            }

        } catch (Exception ignored) {}

        playLocalVideo(uri, "");
    }



    @Override
    public void onBackPressed() {
        goBackHistory();
    }

    void showPlayer() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackgroundColor(Color.BLACK);

        TextView top =
            center("‹    CINEVA                         ⚙",
                18,WHITE,true);

        c.addView(top,
            new LinearLayout.LayoutParams(-1,dp(55)));

        TextView video =
            center(
                "▶\n\nVIDEO PLAYER",
                22,WHITE,true
            );

        video.setBackgroundColor(Color.BLACK);

        c.addView(
            video,
            new LinearLayout.LayoutParams(-1,dp(340))
        );

        SeekBar seek = new SeekBar(this);
        c.addView(seek,
            new LinearLayout.LayoutParams(-1,45));

        LinearLayout controls =
            new LinearLayout(this);

        controls.setGravity(Gravity.CENTER);

        String[] cs = {"◀◀","▶","▶▶","▣","⛶"};

        for(String x:cs) {

            TextView b = center(x,18,WHITE,true);

            controls.addView(
                b,
                new LinearLayout.LayoutParams(dp(60),dp(55))
            );
        }

        c.addView(controls);

        page.addView(c);
    }

    void showDownloads() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(10,5,10,20);

        topBar(c,true);

        // =========================
        // CINEVA DOWNLOADS
        // =========================
        c.addView(section("CINEVA Downloads"));

        c.addView(
            center(
                "No downloaded movies",
                14,GRAY,false
            ),
            new LinearLayout.LayoutParams(-1,dp(80))
        );

        // =========================
        // LOCAL VIDEOS
        // =========================
        c.addView(section("Local Videos"));

        TextView access =
            button("📱  Allow access to all videos");

        LinearLayout.LayoutParams accessLp =
            new LinearLayout.LayoutParams(-1,dp(50));

        accessLp.setMargins(0,5,0,10);

        c.addView(access,accessLp);

        access.setOnClickListener(
            v -> requestVideoPermission()
        );

        if(hasVideoPermission()) {

            access.setText(
                "✓  Video access allowed"
            );

            showAllLocalVideos(c);

        } else {

            c.addView(
                center(
                    "Allow once to see all accessible videos on your phone.",
                    13,GRAY,false
                ),
                new LinearLayout.LayoutParams(-1,dp(70))
            );
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);

        scroll.addView(c,
            new ScrollView.LayoutParams(
                -1,
                -2
            )
        );

        page.addView(scroll);
    }

    boolean hasVideoPermission() {

        if(Build.VERSION.SDK_INT >= 33) {

            return checkSelfPermission(
                "android.permission.READ_MEDIA_VIDEO"
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED;

        } else {

            return checkSelfPermission(
                "android.permission.READ_EXTERNAL_STORAGE"
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
    }

    void requestVideoPermission() {

        if(Build.VERSION.SDK_INT >= 33) {

            requestPermissions(
                new String[]{
                    "android.permission.READ_MEDIA_VIDEO"
                },
                9200
            );

        } else {

            requestPermissions(
                new String[]{
                    "android.permission.READ_EXTERNAL_STORAGE"
                },
                9200
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {

        super.onRequestPermissionsResult(
            requestCode,permissions,grantResults
        );

        if(requestCode == 9200) {

            showDownloads();
        }
    }

    void showAllLocalVideos(LinearLayout parent) {

        parent.addView(
            section("All videos on your phone")
        );

        TextView loading =
            center(
                "Loading videos…",
                13,GRAY,false
            );

        parent.addView(
            loading,
            new LinearLayout.LayoutParams(
                -1,dp(70)
            )
        );

        // IMPORTANT:
        // MediaStore query runs in background so the
        // Downloads screen opens immediately.
        new Thread(() -> {

            ArrayList<LocalVideoItem> videos =
                new ArrayList<>();

            String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION
            };

            android.net.Uri collection;

            if(Build.VERSION.SDK_INT >= 29) {

                collection =
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    );

            } else {

                collection =
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            }

            try {

                android.database.Cursor cursor =
                    getContentResolver().query(
                        collection,
                        projection,
                        null,
                        null,
                        MediaStore.Video.Media.DATE_ADDED + " DESC"
                    );

                if(cursor != null) {

                    int idIndex =
                        cursor.getColumnIndex(
                            MediaStore.Video.Media._ID
                        );

                    int nameIndex =
                        cursor.getColumnIndex(
                            MediaStore.Video.Media.DISPLAY_NAME
                        );

                    int mimeIndex =
                        cursor.getColumnIndex(
                            MediaStore.Video.Media.MIME_TYPE
                        );

                    int sizeIndex =
                        cursor.getColumnIndex(
                            MediaStore.Video.Media.SIZE
                        );

                    int durationIndex =
                        cursor.getColumnIndex(
                            MediaStore.Video.Media.DURATION
                        );

                    while(cursor.moveToNext()) {

                        long id =
                            cursor.getLong(idIndex);

                        String name =
                            cursor.getString(nameIndex);

                        String mimeType = "";

                        if(mimeIndex >= 0 &&
                           !cursor.isNull(mimeIndex)) {

                            mimeType =
                                cursor.getString(mimeIndex);
                        }

                        long size =
                            sizeIndex >= 0 &&
                            !cursor.isNull(sizeIndex)
                            ? cursor.getLong(sizeIndex)
                            : 0;

                        long duration =
                            durationIndex >= 0 &&
                            !cursor.isNull(durationIndex)
                            ? cursor.getLong(durationIndex)
                            : 0;

                        Uri videoUri =
                            ContentUris.withAppendedId(
                                collection,id
                            );

                        videos.add(
                            new LocalVideoItem(
                                id,
                                name,
                                mimeType,
                                size,
                                duration,
                                videoUri
                            )
                        );
                    }

                    cursor.close();
                }

            } catch(Exception ignored) {}

            runOnUiThread(() -> {

                if(loading.getParent() != null) {
                    parent.removeView(loading);
                }

                if(videos.isEmpty()) {

                    parent.addView(
                        center(
                            "No videos found",
                            14,GRAY,false
                        ),
                        new LinearLayout.LayoutParams(
                            -1,dp(100)
                        )
                    );

                    return;
                }

                for(LocalVideoItem item : videos) {

                    addLocalVideoRow(parent,item);
                }
            });

        }).start();
    }

    static class LocalVideoItem {

        long id;
        String name;
        String mimeType;
        long size;
        long duration;
        Uri uri;

        LocalVideoItem(
            long id,
            String name,
            String mimeType,
            long size,
            long duration,
            Uri uri
        ) {
            this.id = id;
            this.name = name;
            this.mimeType = mimeType;
            this.size = size;
            this.duration = duration;
            this.uri = uri;
        }
    }

    void addLocalVideoRow(
        LinearLayout parent,
        LocalVideoItem item
    ) {

        LinearLayout row =
            new LinearLayout(this);

        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(10,5,10,5);
        row.setBackground(bg(PANEL2,10));

        ImageView thumbnail =
            new ImageView(this);

        thumbnail.setScaleType(
            ImageView.ScaleType.CENTER_CROP
        );

        thumbnail.setBackground(
            bg(PANEL,8)
        );

        thumbnail.setImageResource(
            android.R.drawable.ic_media_play
        );

        row.addView(
            thumbnail,
            new LinearLayout.LayoutParams(
                dp(100),dp(65)
            )
        );

        String info =
            item.name +
            "\n" +
            formatVideoDuration(item.duration) +
            " • " +
            formatVideoSize(item.size);

        TextView title =
            txt(info,13,WHITE,true);

        row.addView(
            title,
            new LinearLayout.LayoutParams(
                0,dp(65),1
            )
        );

        LinearLayout.LayoutParams rowLp =
            new LinearLayout.LayoutParams(
                -1,dp(75)
            );

        rowLp.setMargins(0,4,0,4);

        parent.addView(row,rowLp);

        final Uri finalUri = item.uri;
        final String finalMime = item.mimeType;

        row.setOnClickListener(
            v -> playLocalVideo(
                finalUri,
                finalMime
            )
        );

        // Thumbnail also loads away from UI thread.
        new Thread(() -> {

            try {

                Bitmap thumb =
                    MediaStore.Video.Thumbnails.getThumbnail(
                        getContentResolver(),
                        item.id,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null
                    );

                if(thumb != null) {

                    runOnUiThread(() -> {

                        if(thumbnail.getParent() != null) {
                            thumbnail.setImageBitmap(thumb);
                        }

                    });
                }

            } catch(Exception ignored) {}
        }).start();
    }

    String formatVideoSize(long bytes) {

        if(bytes <= 0)
            return "Unknown size";

        double mb =
            bytes / (1024.0 * 1024.0);

        if(mb < 1024)
            return String.format(
                Locale.US,"%.1f MB",mb
            );

        return String.format(
            Locale.US,"%.1f GB",mb / 1024.0
        );
    }

    String formatVideoDuration(long ms) {

        if(ms <= 0)
            return "00:00";

        long totalSeconds =
            ms / 1000;

        long hours =
            totalSeconds / 3600;

        long minutes =
            (totalSeconds % 3600) / 60;

        long seconds =
            totalSeconds % 60;

        if(hours > 0) {

            return String.format(
                Locale.US,
                "%02d:%02d:%02d",
                hours,minutes,seconds
            );

        }

        return String.format(
            Locale.US,
            "%02d:%02d",
            minutes,seconds
        );
    }

    void showComingSoon() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(10,5,10,20);

        topBar(c,true);

        c.addView(section("Coming Soon"));

        String[] coming = getFirebaseMovieTitles();

        if (coming.length == 0) {

            c.addView(
                center(
                    "No upcoming movies",
                    14,GRAY,false
                ),
                new LinearLayout.LayoutParams(-1,dp(100))
            );

        }

        for(String x:coming) {

            LinearLayout row =
                new LinearLayout(this);

            TextView image =
                center("▶",22,WHITE,true);

            image.setBackground(bg(PANEL2,8));

            row.addView(image,
                new LinearLayout.LayoutParams(dp(115),dp(75)));

            TextView info =
                txt(
                    "  "+x+"\n  Coming soon\n  🔔 Remind Me",
                    12,WHITE,false
                );

            row.addView(info,
                new LinearLayout.LayoutParams(
                    0,dp(75),1
                ));

            c.addView(row);

            c.addView(
                new Space(this),
                new LinearLayout.LayoutParams(-1,dp(8))
            );
        }

        page.addView(c);
    }

    void showMyList() {
        base();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(
            dp(14), dp(8), dp(14), dp(35)
        );

        // =========================
        // TOP BAR
        // =========================
        topBar(c, false);

        // =========================
        // ACCOUNT & SETTINGS
        // =========================
        TextView account =
            txt("⚙  Account & Settings   ›",
                14, WHITE, true);

        account.setGravity(Gravity.CENTER_VERTICAL);
        account.setPadding(
            dp(16), 0, dp(12), 0
        );
        account.setBackground(
            bg(PANEL2, dp(12))
        );

        LinearLayout.LayoutParams accountLp =
            new LinearLayout.LayoutParams(
                -1, dp(58)
            );

        accountLp.setMargins(
            0, dp(4), 0, dp(14)
        );

        c.addView(account, accountLp);

        account.setOnClickListener(
            v -> navigateTopLevel("settings")
        );

        // =========================
        // COINS HEADER
        // =========================
        LinearLayout coinCard =
            new LinearLayout(this);

        coinCard.setOrientation(
            LinearLayout.HORIZONTAL
        );
        coinCard.setGravity(
            Gravity.CENTER_VERTICAL
        );
        coinCard.setPadding(
            dp(16), dp(12), dp(16), dp(12)
        );
        coinCard.setBackground(
            bg(PANEL2, dp(14))
        );

        LinearLayout coinLeft =
            new LinearLayout(this);

        coinLeft.setOrientation(
            LinearLayout.VERTICAL
        );

        coinLeft.addView(
            txt("🪙  Cineva Coins",
                13, GRAY, true),
            new LinearLayout.LayoutParams(
                -1, dp(24)
            )
        );

        int coins =
            cinevaPrefs.getInt(
                "cineva_coins", 0
            );

        coinLeft.addView(
            txt(coins + " Coins",
                24, WHITE, true),
            new LinearLayout.LayoutParams(
                -1, dp(36)
            )
        );

        coinCard.addView(
            coinLeft,
            new LinearLayout.LayoutParams(
                0, dp(62), 1
            )
        );

        TextView coinBadge =
            center("EARN MORE", 10, WHITE, true);

        coinBadge.setBackground(
            bg(BLUE, dp(8))
        );

        coinCard.addView(
            coinBadge,
            new LinearLayout.LayoutParams(
                dp(95), dp(38)
            )
        );

        LinearLayout.LayoutParams coinLp =
            new LinearLayout.LayoutParams(
                -1, dp(86)
            );

        coinLp.setMargins(
            0, 0, 0, dp(15)
        );

        c.addView(coinCard, coinLp);

        // =========================
        // DAILY TASKS
        // =========================
        c.addView(section("Daily Tasks"));

        int today =
            java.util.Calendar.getInstance()
                .get(java.util.Calendar.DAY_OF_YEAR);

        int savedDay =
            cinevaPrefs.getInt(
                "task_day", -1
            );

        if (savedDay != today) {
            cinevaPrefs.edit()
                .putInt("task_day", today)
                .putBoolean(
                    "task_watch_done", false
                )
                .putBoolean(
                    "task_list_done", false
                )
                .apply();
        }

        boolean watchDone =
            cinevaPrefs.getBoolean(
                "task_watch_done", false
            );

        boolean listDone =
            cinevaPrefs.getBoolean(
                "task_list_done", false
            );

        // WATCH TASK
        LinearLayout watchTask =
            new LinearLayout(this);

        watchTask.setOrientation(
            LinearLayout.HORIZONTAL
        );
        watchTask.setGravity(
            Gravity.CENTER_VERTICAL
        );
        watchTask.setPadding(
            dp(14), dp(7), dp(12), dp(7)
        );
        watchTask.setBackground(
            bg(PANEL, dp(11))
        );

        watchTask.addView(
            center("▶", 20, WHITE, true),
            new LinearLayout.LayoutParams(
                dp(42), dp(50)
            )
        );

        LinearLayout wt =
            new LinearLayout(this);

        wt.setOrientation(
            LinearLayout.VERTICAL
        );

        wt.addView(
            txt("Watch a movie",
                13, WHITE, true),
            new LinearLayout.LayoutParams(
                -1, dp(25)
            )
        );

        wt.addView(
            txt("Complete once today",
                10, GRAY, false),
            new LinearLayout.LayoutParams(
                -1, dp(20)
            )
        );

        watchTask.addView(
            wt,
            new LinearLayout.LayoutParams(
                0, dp(52), 1
            )
        );

        TextView watchReward =
            center(
                watchDone ? "✓" : "+10",
                12,
                watchDone ? BLUE : WHITE,
                true
            );

        watchReward.setBackground(
            bg(
                watchDone ? PANEL2 : BLUE,
                dp(8)
            )
        );

        watchTask.addView(
            watchReward,
            new LinearLayout.LayoutParams(
                dp(58), dp(38)
            )
        );

        c.addView(
            watchTask,
            new LinearLayout.LayoutParams(
                -1, dp(64)
            )
        );

        if (!watchDone) {
            watchTask.setOnClickListener(v -> {

                cinevaPrefs.edit()
                    .putBoolean(
                        "task_watch_done", true
                    )
                    .putInt(
                        "cineva_coins",
                        cinevaPrefs.getInt(
                            "cineva_coins", 0
                        ) + 10
                    )
                    .apply();

                Toast.makeText(
                    this,
                    "+10 Coins earned!",
                    Toast.LENGTH_SHORT
                ).show();

                showMyList();
            });
        }

        // LIST TASK
        LinearLayout listTask =
            new LinearLayout(this);

        listTask.setOrientation(
            LinearLayout.HORIZONTAL
        );
        listTask.setGravity(
            Gravity.CENTER_VERTICAL
        );
        listTask.setPadding(
            dp(14), dp(7), dp(12), dp(7)
        );
        listTask.setBackground(
            bg(PANEL, dp(11))
        );

        listTask.addView(
            center("＋", 23, WHITE, true),
            new LinearLayout.LayoutParams(
                dp(42), dp(50)
            )
        );

        LinearLayout lt =
            new LinearLayout(this);

        lt.setOrientation(
            LinearLayout.VERTICAL
        );

        lt.addView(
            txt("Add a movie to My List",
                13, WHITE, true),
            new LinearLayout.LayoutParams(
                -1, dp(25)
            )
        );

        lt.addView(
            txt("Complete once today",
                10, GRAY, false),
            new LinearLayout.LayoutParams(
                -1, dp(20)
            )
        );

        listTask.addView(
            lt,
            new LinearLayout.LayoutParams(
                0, dp(52), 1
            )
        );

        TextView listReward =
            center(
                listDone ? "✓" : "+5",
                12,
                listDone ? BLUE : WHITE,
                true
            );

        listReward.setBackground(
            bg(
                listDone ? PANEL2 : BLUE,
                dp(8)
            )
        );

        listTask.addView(
            listReward,
            new LinearLayout.LayoutParams(
                dp(58), dp(38)
            )
        );

        LinearLayout.LayoutParams listTaskLp =
            new LinearLayout.LayoutParams(
                -1, dp(64)
            );

        listTaskLp.setMargins(
            0, dp(5), 0, dp(15)
        );

        c.addView(listTask, listTaskLp);

        if (!listDone) {
            listTask.setOnClickListener(v -> {

                if (!myList.isEmpty()) {

                    cinevaPrefs.edit()
                        .putBoolean(
                            "task_list_done", true
                        )
                        .putInt(
                            "cineva_coins",
                            cinevaPrefs.getInt(
                                "cineva_coins", 0
                            ) + 5
                        )
                        .apply();

                    Toast.makeText(
                        this,
                        "+5 Coins earned!",
                        Toast.LENGTH_SHORT
                    ).show();

                    showMyList();

                } else {

                    Toast.makeText(
                        this,
                        "Add a movie to My List first.",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        // =========================
        // PRO
        // =========================
        c.addView(section("Cineva Pro"));

        boolean pro =
            cinevaPrefs.getBoolean(
                "cineva_pro", false
            );

        LinearLayout proCard =
            new LinearLayout(this);

        proCard.setOrientation(
            LinearLayout.HORIZONTAL
        );
        proCard.setGravity(
            Gravity.CENTER_VERTICAL
        );
        proCard.setPadding(
            dp(16), dp(12), dp(12), dp(12)
        );
        proCard.setBackground(
            bg(PANEL2, dp(13))
        );

        LinearLayout proText =
            new LinearLayout(this);

        proText.setOrientation(
            LinearLayout.VERTICAL
        );

        proText.addView(
            txt(
                pro
                    ? "👑  Cineva Pro Active"
                    : "👑  Cineva Pro",
                14, WHITE, true
            ),
            new LinearLayout.LayoutParams(
                -1, dp(27)
            )
        );

        proText.addView(
            txt(
                "No ads • Premium features",
                10, GRAY, false
            ),
            new LinearLayout.LayoutParams(
                -1, dp(22)
            )
        );

        proCard.addView(
            proText,
            new LinearLayout.LayoutParams(
                0, dp(55), 1
            )
        );

        TextView proButton =
            center(
                pro
                    ? "✓ Active"
                    : "Get Pro",
                11, WHITE, true
            );

        proButton.setBackground(
            bg(BLUE, dp(8))
        );

        proCard.addView(
            proButton,
            new LinearLayout.LayoutParams(
                dp(82), dp(38)
            )
        );

        LinearLayout.LayoutParams proLp =
            new LinearLayout.LayoutParams(
                -1, dp(80)
            );

        proLp.setMargins(
            0, 0, 0, dp(15)
        );

        c.addView(proCard, proLp);

        proButton.setOnClickListener(v -> {

            Toast.makeText(
                this,
                pro
                    ? "Cineva Pro is already active."
                    : "Pro subscription will be available soon.",
                Toast.LENGTH_SHORT
            ).show();
        });

        // =========================
        // MY LIST
        // =========================
        c.addView(section("My List"));

        if (myList.isEmpty()) {

            TextView empty =
                center(
                    "No movies in My List yet.\n" +
                    "Open a movie and tap + My List.",
                    13, GRAY, false
                );

            empty.setGravity(
                Gravity.CENTER
            );
            empty.setBackground(
                bg(PANEL, dp(11))
            );

            c.addView(
                empty,
                new LinearLayout.LayoutParams(
                    -1, dp(88)
                )
            );

        } else {

            posterRow(
                c,
                myList.toArray(new String[0])
            );
        }

        // =========================
        // WATCH HISTORY
        // =========================
        c.addView(section("Watch History"));

        if (watchHistory.isEmpty()) {

            TextView emptyHistory =
                center(
                    "No watch history yet.",
                    13, GRAY, false
                );

            emptyHistory.setGravity(
                Gravity.CENTER
            );

            emptyHistory.setBackground(
                bg(PANEL, dp(11))
            );

            c.addView(
                emptyHistory,
                new LinearLayout.LayoutParams(
                    -1, dp(70)
                )
            );

        } else {

            for (String history : watchHistory) {

                final String historyValue =
                    history;

                final boolean isLocal =
                    historyValue.startsWith(
                        "LOCAL_VIDEO::"
                    ) ||
                    historyValue.startsWith(
                        "Local: "
                    );

                String displayTitle =
                    historyValue;

                String localUri = "";

                if (
                    historyValue.startsWith(
                        "LOCAL_VIDEO::"
                    )
                ) {

                    String[] parts =
                        historyValue.split("::", 3);

                    if (parts.length >= 2) {
                        displayTitle = parts[1];
                    }

                    if (parts.length >= 3) {
                        localUri = parts[2];
                    }

                } else if (
                    historyValue.startsWith("Local: ")
                ) {

                    displayTitle = "Local Video";

                    localUri =
                        historyValue
                            .substring(7)
                            .trim();
                }

                LinearLayout item =
                    new LinearLayout(this);

                item.setOrientation(
                    LinearLayout.HORIZONTAL
                );
                item.setGravity(
                    Gravity.CENTER_VERTICAL
                );
                item.setPadding(
                    dp(12), dp(7), dp(10), dp(7)
                );
                item.setBackground(
                    bg(PANEL, dp(10))
                );

                TextView icon =
                    center(
                        isLocal ? "📱" : "▶",
                        20, WHITE, true
                    );

                item.addView(
                    icon,
                    new LinearLayout.LayoutParams(
                        dp(42), dp(45)
                    )
                );

                LinearLayout info =
                    new LinearLayout(this);

                info.setOrientation(
                    LinearLayout.VERTICAL
                );

                info.addView(
                    txt(
                        displayTitle,
                        13, WHITE, true
                    ),
                    new LinearLayout.LayoutParams(
                        -1, dp(25)
                    )
                );

                info.addView(
                    txt(
                        isLocal
                            ? "Local video • Tap to play"
                            : "Watched • Tap to open",
                        10, GRAY, false
                    ),
                    new LinearLayout.LayoutParams(
                        -1, dp(20)
                    )
                );

                item.addView(
                    info,
                    new LinearLayout.LayoutParams(
                        0, dp(48), 1
                    )
                );

                item.addView(
                    center("›", 23, GRAY, false),
                    new LinearLayout.LayoutParams(
                        dp(28), dp(45)
                    )
                );

                LinearLayout.LayoutParams itemLp =
                    new LinearLayout.LayoutParams(
                        -1, dp(62)
                    );

                itemLp.setMargins(
                    0, dp(3), 0, dp(3)
                );

                c.addView(item, itemLp);

                if (isLocal) {

                    final String playableUri =
                        localUri;

                    item.setOnClickListener(v -> {

                        if (!playableUri.isEmpty()) {

                            try {

                                playLocalVideo(
                                    Uri.parse(
                                        playableUri
                                    )
                                );

                            } catch (Exception e) {

                                Toast.makeText(
                                    this,
                                    "Video is no longer available",
                                    Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    });

                } else {

                    final String onlineTitle =
                        displayTitle;

                    item.setOnClickListener(v -> {

                        screenHistory.add(
                            currentScreen
                        );

                        currentScreen =
                            "details";

                        showDetails(
                            onlineTitle
                        );
                    });
                }
            }
        }

        scroll.addView(c);
        page.addView(scroll);
    }

    void showProfiles() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.setPadding(20,20,20,20);

        c.addView(
            center("Who's Watching?",25,WHITE,true),
            new LinearLayout.LayoutParams(-1,dp(60))
        );

        c.addView(
            center("cineva",34,BLUE,true),
            new LinearLayout.LayoutParams(-1,dp(90))
        );

        LinearLayout row = new LinearLayout(this);

        String[] p = {
            "Ava","Sam","Leo","Kids"
        };

        for(String x:p) {

            LinearLayout item =
                new LinearLayout(this);

            item.setOrientation(LinearLayout.VERTICAL);

            TextView face =
                center("●",42,BLUE,true);

            item.addView(face,
                new LinearLayout.LayoutParams(dp(90),dp(65)));

            item.addView(
                center(x,12,WHITE,false),
                new LinearLayout.LayoutParams(dp(90),dp(35))
            );

            row.addView(item);
        }

        c.addView(row);

        TextView settings =
            center("⚙  Account & Settings",
                14,WHITE,true);

        settings.setBackground(bg(PANEL2,12));

        LinearLayout.LayoutParams slp =
            new LinearLayout.LayoutParams(-1,dp(55));

        slp.setMargins(20,30,20,0);

        c.addView(settings,slp);

        settings.setOnClickListener(
            v -> navigateTopLevel("settings")
        );

        page.addView(c);
    }

    void showSettings() {

        base();

        ScrollView scroll = new ScrollView(this);
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15),dp(5),dp(15),dp(25));

        TextView back =
            txt("‹   Settings",20,WHITE,true);

        back.setPadding(0,0,0,10);
        back.setOnClickListener(v -> goBackHistory());
        c.addView(back);

        setting(c,"Account");

        settingItem(c,"Profile");
        settingItem(c,"Subscription");
        settingItem(c,"Coins & Daily Tasks");

        TextView logout =
            center(
                "Log Out",
                14,
                Color.rgb(255,80,80),
                true
            );

        logout.setBackground(bg(PANEL2,10));

        LinearLayout.LayoutParams logoutLp =
            new LinearLayout.LayoutParams(-1,dp(52));

        logoutLp.setMargins(0,10,0,5);
        c.addView(logout,logoutLp);

        logout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel",null)
                .setPositiveButton(
                    "Log Out",
                    (dialog,which) -> logoutUser()
                )
                .show();
        });

        setting(c,"App Settings");
        settingItem(c,"App Language");
        settingItem(c,"Video Quality");
        settingItem(c,"Downloads");

        setting(c,"Parental Controls");
        settingItem(c,"Parental Controls");
        settingItem(c,"Help & Support");

        setting(c,"About Cineva");
        settingItem(c,"About Cineva");
        settingItem(c,"Privacy");
        settingItem(c,"Terms");

        scroll.addView(c);
        page.addView(scroll);
    }

    void setting(LinearLayout c,String s) {

        TextView t =
            txt(s,15,WHITE,true);

        t.setPadding(0,20,0,10);

        c.addView(t);
    }

    void settingItem(LinearLayout c,String s) {

        TextView t =
            txt(
                s + "                                      ›",
                13,
                GRAY,
                false
            );

        t.setPadding(dp(10),dp(14),dp(5),dp(14));
        t.setBackground(bg(PANEL,7));

        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(-1,dp(48));

        lp.setMargins(0,dp(2),0,dp(2));

        c.addView(t,lp);

        t.setOnClickListener(v -> {

            if("Profile".equals(s)) {

                screenHistory.add(currentScreen);
                currentScreen = "profile";
                showProfile();

            } else if("Subscription".equals(s)) {

                screenHistory.add(currentScreen);
                currentScreen = "subscription";
                showSubscription();

            } else if("Coins & Daily Tasks".equals(s)) {

                screenHistory.add(currentScreen);
                currentScreen = "tasks";
                showCoinsTasks();

            } else if("Downloads".equals(s)) {

                screenHistory.add(currentScreen);
                currentScreen = "downloads";
                showDownloads();

            } else if("App Language".equals(s)) {

                final String[] languages = {
                    "English",
                    "বাংলা",
                    "हिन्दी",
                    "नेपाली"
                };

                int selected =
                    cinevaPrefs.getInt("app_language_index",0);

                new AlertDialog.Builder(this)
                    .setTitle("App Language")
                    .setSingleChoiceItems(
                        languages,
                        selected,
                        (dialog,which) -> {

                            cinevaPrefs.edit()
                                .putInt("app_language_index",which)
                                .apply();

                            Toast.makeText(
                                this,
                                languages[which] +
                                " selected",
                                Toast.LENGTH_SHORT
                            ).show();

                            dialog.dismiss();
                        }
                    )
                    .setNegativeButton("Cancel",null)
                    .show();

            } else if("Video Quality".equals(s)) {

                final String[] quality = {
                    "Auto",
                    "360p",
                    "480p",
                    "720p",
                    "1080p"
                };

                int selected =
                    cinevaPrefs.getInt("video_quality_index",0);

                new AlertDialog.Builder(this)
                    .setTitle("Video Quality")
                    .setSingleChoiceItems(
                        quality,
                        selected,
                        (dialog,which) -> {

                            cinevaPrefs.edit()
                                .putInt("video_quality_index",which)
                                .apply();

                            Toast.makeText(
                                this,
                                "Video quality: " +
                                quality[which],
                                Toast.LENGTH_SHORT
                            ).show();

                            dialog.dismiss();
                        }
                    )
                    .setNegativeButton("Cancel",null)
                    .show();

            } else if("Parental Controls".equals(s)) {

                screenHistory.add(currentScreen);
                currentScreen = "parental";
                showParentalControls();

            } else if("Help & Support".equals(s)) {

                screenHistory.add(currentScreen);
                currentScreen = "help";
                showHelpSupport();

            } else if("About Cineva".equals(s)) {

                screenHistory.add(currentScreen);
                currentScreen = "about";
                showAboutCineva();

            } else if("Privacy".equals(s)) {

                screenHistory.add(currentScreen);
                currentScreen = "privacy";
                showPrivacy();

            } else if("Terms".equals(s)) {

                screenHistory.add(currentScreen);
                currentScreen = "terms";
                showTerms();

            } else {

                Toast.makeText(
                    this,
                    s + " — coming soon",
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    void showParentalControls() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15),dp(10),dp(15),dp(25));

        TextView back =
            txt("‹   Parental Controls",20,WHITE,true);

        back.setOnClickListener(v -> goBackHistory());

        c.addView(back);

        c.addView(section("Parental Controls"));

        c.addView(
            txt(
                "Control access to mature content.",
                13,GRAY,false
            )
        );

        Switch lock = new Switch(this);
        lock.setText("Restricted Mode");
        lock.setTextColor(WHITE);
        lock.setTextSize(14);

        boolean enabled =
            cinevaPrefs.getBoolean("parental_restricted",false);

        lock.setChecked(enabled);

        lock.setOnCheckedChangeListener((button,isChecked) ->
            cinevaPrefs.edit()
                .putBoolean("parental_restricted",isChecked)
                .apply()
        );

        c.addView(
            lock,
            new LinearLayout.LayoutParams(-1,dp(60))
        );

        page.addView(c);
    }

    void showHelpSupport() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15),dp(10),dp(15),dp(25));

        TextView back =
            txt("‹   Help & Support",20,WHITE,true);

        back.setOnClickListener(v -> goBackHistory());
        c.addView(back);

        c.addView(section("Help & Support"));

        c.addView(
            txt(
                "Need help with Cineva?",
                18,WHITE,true
            )
        );

        c.addView(
            txt(
                "For account, playback, downloads and other app-related problems, please check your settings and try again.",
                13,GRAY,false
            )
        );

        TextView faq =
            center("Frequently Asked Questions",14,WHITE,true);

        faq.setBackground(bg(PANEL2,dp(9)));

        faq.setOnClickListener(v ->
            Toast.makeText(
                this,
                "FAQ section will be available soon.",
                Toast.LENGTH_SHORT
            ).show()
        );

        c.addView(
            faq,
            new LinearLayout.LayoutParams(-1,dp(52))
        );

        TextView contact =
            center("Contact Cineva Support",14,WHITE,true);

        contact.setBackground(bg(PANEL2,dp(9)));

        contact.setOnClickListener(v ->
            Toast.makeText(
                this,
                "Support contact will be available soon.",
                Toast.LENGTH_SHORT
            ).show()
        );

        c.addView(
            contact,
            new LinearLayout.LayoutParams(-1,dp(52))
        );

        page.addView(c);
    }

    void showAboutCineva() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15),dp(10),dp(15),dp(25));

        TextView back =
            txt("‹   About Cineva",20,WHITE,true);

        back.setOnClickListener(v -> goBackHistory());
        c.addView(back);

        c.addView(
            center("CINEVA",30,BLUE,true),
            new LinearLayout.LayoutParams(-1,dp(70))
        );

        c.addView(
            txt(
                "Cineva is your personal movie and video experience.",
                14,WHITE,true
            )
        );

        c.addView(
            txt(
                "Version 1.0",
                12,GRAY,false
            )
        );

        c.addView(
            txt(
                "\\nMovies • Videos • Downloads • My List",
                13,GRAY,false
            )
        );

        page.addView(c);
    }

    void showPrivacy() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15),dp(10),dp(15),dp(25));

        TextView back =
            txt("‹   Privacy",20,WHITE,true);

        back.setOnClickListener(v -> goBackHistory());
        c.addView(back);

        c.addView(section("Privacy"));

        c.addView(
            txt(
                "Cineva respects your privacy.",
                16,WHITE,true
            )
        );

        c.addView(
            txt(
                "Your local My List, watch history and app preferences are stored locally on your device.",
                13,GRAY,false
            )
        );

        c.addView(
            txt(
                "\\nFirebase account and movie data may be used by the app's online features.",
                13,GRAY,false
            )
        );

        page.addView(c);
    }

    void showTerms() {

        base();

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15),dp(10),dp(15),dp(25));

        TextView back =
            txt("‹   Terms",20,WHITE,true);

        back.setOnClickListener(v -> goBackHistory());
        c.addView(back);

        c.addView(section("Terms of Use"));

        c.addView(
            txt(
                "By using Cineva, you agree to use the application responsibly and follow applicable laws.",
                13,GRAY,false
            )
        );

        c.addView(
            txt(
                "\\nContent availability may depend on the movie data and services connected to the application.",
                13,GRAY,false
            )
        );

        page.addView(c);
    }

    void openUrl(String url) {

        try {
            Intent i =
                new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                );

            startActivity(i);

        } catch(Exception e) {

            Toast.makeText(
                this,
                "Unable to open link",
                Toast.LENGTH_SHORT
            ).show();
        }
    }
}
