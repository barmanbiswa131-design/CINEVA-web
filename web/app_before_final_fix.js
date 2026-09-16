function showPage(page) {
    document.querySelectorAll(".page").forEach(function(p) {
        p.classList.remove("active");
    });

    const target = document.getElementById(page);

    if (target) {
        target.classList.add("active");
        window.scrollTo(0, 0);
    }
}

function playDemo() {
    document.getElementById("playerTitle").textContent =
        "One Piece [Hindi] • S01 E01";

    document.getElementById("player").classList.remove("hidden");
}

function openSeries() {
    showPage("series");
}

function showDetails() {
    alert("Movie details will be connected to Cineva database.");
}

function closePlayer() {
    document.getElementById("player").classList.add("hidden");
}

function togglePlay() {
    const buttons = document.querySelectorAll(
        ".play-button, .player-bottom > button:first-child"
    );

    buttons.forEach(function(button) {
        button.textContent =
            button.textContent.trim() === "▶" ? "Ⅱ" : "▶";
    });
}

function skip(seconds) {
    alert(seconds > 0 ? "+10 seconds" : "-10 seconds");
}

function searchMovies() {
    const query = prompt("Search Cineva");

    if (query && query.trim()) {
        alert("Search will be connected to Cineva database.");
    }
}

function hideLogin() {
    const login = document.getElementById("loginScreen");
    const app = document.getElementById("app");
    const header = document.getElementById("mainHeader");

    if (login) login.style.display = "none";
    if (app) app.style.display = "";
    if (header) header.style.display = "";
}

function loginWithGoogle() {
    alert("Google Login will be connected with Firebase.");
}

function loginWithEmail() {
    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;

    if (!email || !password) {
        alert("Please enter email and password.");
        return;
    }

    alert("Email login will be connected with Firebase.");
}

function continueAsGuest() {
    localStorage.setItem("cineva_guest", "true");
    hideLogin();
}

function createAccount() {
    alert("Create Account will be connected with Firebase.");
}

function forgotPassword() {
    alert("Password reset will be connected with Firebase.");
}

document.addEventListener("DOMContentLoaded", function() {
    const guest = localStorage.getItem("cineva_guest");

    if (guest === "true") {
        hideLogin();
    }
});

/* =========================================
   CINEVA ACCOUNT + SETTINGS
   ========================================= */

function cinevaMessage(title, message) {
    alert(title + "\n\n" + message);
}

function logoutCineva() {
    localStorage.removeItem("cineva_guest");
    localStorage.removeItem("cineva_user");

    const login = document.getElementById("loginScreen");
    const app = document.getElementById("app");
    const header = document.getElementById("mainHeader");

    if (app) app.style.display = "none";
    if (header) header.style.display = "none";
    if (login) login.style.display = "flex";
}

function openAccountSettings() {
    showPage("settings");
}

function openProfile() {
    cinevaMessage(
        "Profile",
        "Your Cineva profile will appear here."
    );
}

function openSubscription() {
    cinevaMessage(
        "Subscription",
        "Cineva subscription plans will appear here."
    );
}

function openCoins() {
    cinevaMessage(
        "Coins & Daily Tasks",
        "Coins balance and daily tasks will appear here."
    );
}

function openDownloads() {
    showPage("downloads");
}

function openHelp() {
    cinevaMessage(
        "Help & Support",
        "Cineva Help & Support is ready."
    );
}

function openPrivacy() {
    cinevaMessage(
        "Privacy",
        "Cineva Privacy information will appear here."
    );
}

function openTerms() {
    cinevaMessage(
        "Terms",
        "Cineva Terms of Use will appear here."
    );
}

function changeLanguage() {
    const languages = [
        "English",
        "বাংলা",
        "Hindi",
        "Nepali"
    ];

    const choice = prompt(
        "Choose language:\n\n" +
        "1. English\n" +
        "2. বাংলা\n" +
        "3. Hindi\n" +
        "4. Nepali"
    );

    const index = parseInt(choice) - 1;

    if (index >= 0 && index < languages.length) {
        localStorage.setItem(
            "cineva_language",
            languages[index]
        );

        cinevaMessage(
            "Language",
            languages[index] +
            " selected."
        );
    }
}

function accountMenu() {
    showPage("settings");
}

/* Automatically connect buttons by text */
function connectCinevaSettings() {

    document.querySelectorAll("button, .setting-item, .settings-item, .setting-row, .settings-row").forEach(function(el) {

        const text = (el.innerText || el.textContent || "")
            .trim()
            .toLowerCase();

        if (text.includes("logout") ||
            text.includes("log out")) {

            el.onclick = function() {
                if (confirm("Are you sure you want to log out of Cineva?")) {
                    logoutCineva();
                }
            };

        } else if (text.includes("profile")) {

            el.onclick = openProfile;

        } else if (text.includes("subscription")) {

            el.onclick = openSubscription;

        } else if (text.includes("coins") ||
                   text.includes("daily tasks")) {

            el.onclick = openCoins;

        } else if (text.includes("downloads")) {

            el.onclick = openDownloads;

        } else if (text.includes("help") ||
                   text.includes("support")) {

            el.onclick = openHelp;

        } else if (text.includes("privacy")) {

            el.onclick = openPrivacy;

        } else if (text.includes("terms")) {

            el.onclick = openTerms;

        } else if (text.includes("language")) {

            el.onclick = changeLanguage;
        }
    });
}

document.addEventListener("DOMContentLoaded", function() {

    connectCinevaSettings();

    const guest =
        localStorage.getItem("cineva_guest");

    const user =
        localStorage.getItem("cineva_user");

    if (guest === "true" || user === "true") {
        hideLogin();
    }
});

/* =========================================
   CINEVA FULL SETTINGS PAGES
   ========================================= */

function cinevaPage(title, content) {
    const old = document.getElementById("cinevaDynamicPage");

    if (old) old.remove();

    const page = document.createElement("section");
    page.id = "cinevaDynamicPage";
    page.className = "page active";

    page.innerHTML = `
        <div class="dynamic-settings">
            <div class="dynamic-header">
                <button onclick="closeCinevaPage()">‹</button>
                <h2>${title}</h2>
            </div>
            ${content}
        </div>
    `;

    document.getElementById("app").appendChild(page);

    document.querySelectorAll(".page").forEach(function(p) {
        if (p !== page) p.classList.remove("active");
    });

    window.scrollTo(0, 0);
}

function closeCinevaPage() {
    const page = document.getElementById("cinevaDynamicPage");
    if (page) page.remove();

    showPage("settings");
}

/* PROFILE */

function openProfile() {
    const name =
        localStorage.getItem("cineva_name") || "Cineva User";

    cinevaPage("Profile", `
        <div class="profile-card">
            <div class="profile-avatar">C</div>
            <h2>${name}</h2>
            <p>Cineva Member</p>
        </div>

        <div class="setting-card">
            <label>Name</label>
            <input id="cinevaName" value="${name}">
            <button onclick="saveCinevaProfile()">Save Profile</button>
        </div>
    `);
}

function saveCinevaProfile() {
    const name =
        document.getElementById("cinevaName").value.trim();

    if (!name) {
        cinevaMessage("Profile", "Please enter your name.");
        return;
    }

    localStorage.setItem("cineva_name", name);

    cinevaMessage(
        "Profile Saved",
        "Your Cineva profile has been updated."
    );
}

/* SUBSCRIPTION */

function openSubscription() {
    cinevaPage("Subscription", `
        <div class="subscription-card">
            <div class="subscription-badge">CINEVA</div>
            <h2>Free Plan</h2>
            <p>Current subscription</p>
        </div>

        <div class="setting-card">
            <h3>Cineva Premium</h3>
            <p>Premium plans can be connected later.</p>
            <button onclick="cinevaMessage('Premium','Subscription system is ready for backend connection.')">
                View Plans
            </button>
        </div>
    `);
}

/* COINS */

function openCoins() {
    const coins =
        localStorage.getItem("cineva_coins") || "0";

    cinevaPage("Coins & Daily Tasks", `
        <div class="coin-big">
            <span>🪙</span>
            <strong>${coins}</strong>
            <small>Cineva Coins</small>
        </div>

        <div class="task-card">
            <div>
                <strong>Daily Login</strong>
                <p>Open Cineva today</p>
            </div>
            <button onclick="claimDailyCoin(this)">+10</button>
        </div>

        <div class="task-card">
            <div>
                <strong>Watch a Movie</strong>
                <p>Complete a movie watch task</p>
            </div>
            <button onclick="cinevaMessage('Task','Watch task will connect to playback history.')">
                +20
            </button>
        </div>
    `);
}

function claimDailyCoin(button) {
    let coins =
        parseInt(localStorage.getItem("cineva_coins") || "0");

    if (localStorage.getItem("cineva_daily_claimed") ===
        new Date().toDateString()) {

        cinevaMessage(
            "Daily Task",
            "Today's reward has already been claimed."
        );
        return;
    }

    coins += 10;

    localStorage.setItem(
        "cineva_coins",
        String(coins)
    );

    localStorage.setItem(
        "cineva_daily_claimed",
        new Date().toDateString()
    );

    button.textContent = "✓ Claimed";
    button.disabled = true;
}

/* LANGUAGE */

function changeLanguage() {
    cinevaPage("Language", `
        <div class="language-list">

            <button onclick="selectLanguage('English')">
                <span>English</span>
                <span>✓</span>
            </button>

            <button onclick="selectLanguage('বাংলা')">
                <span>বাংলা</span>
                <span>›</span>
            </button>

            <button onclick="selectLanguage('Hindi')">
                <span>Hindi</span>
                <span>›</span>
            </button>

            <button onclick="selectLanguage('Nepali')">
                <span>Nepali</span>
                <span>›</span>
            </button>

        </div>
    `);
}

function selectLanguage(language) {
    localStorage.setItem(
        "cineva_language",
        language
    );

    cinevaMessage(
        "Language Changed",
        "Cineva language: " + language
    );
}

/* DOWNLOADS */

function openDownloads() {
    cinevaPage("Downloads", `
        <div class="empty-state">
            <div class="empty-icon">↓</div>
            <h3>No Downloads</h3>
            <p>Movies and episodes downloaded for offline viewing will appear here.</p>
        </div>
    `);
}

/* VIDEO QUALITY */

function openVideoQuality() {
    cinevaPage("Video Quality", `
        <div class="quality-list">

            <button onclick="selectQuality('Auto')">
                <span>Auto</span>
                <small>Recommended</small>
            </button>

            <button onclick="selectQuality('1080p')">
                <span>1080p</span>
                <small>HD</small>
            </button>

            <button onclick="selectQuality('720p')">
                <span>720p</span>
                <small>HD</small>
            </button>

            <button onclick="selectQuality('480p')">
                <span>480p</span>
                <small>SD</small>
            </button>

        </div>
    `);
}

function selectQuality(value) {
    localStorage.setItem(
        "cineva_quality",
        value
    );

    cinevaMessage(
        "Video Quality",
        "Selected: " + value
    );
}

/* HELP */

function openHelp() {
    cinevaPage("Help & Support", `
        <div class="help-card">
            <h3>How can we help?</h3>

            <button onclick="cinevaMessage('Playback Help','Check your internet connection and video quality settings.')">
                ▶ Playback problems
            </button>

            <button onclick="cinevaMessage('Account Help','Account and login support will be connected to Cineva backend.')">
                👤 Account & Login
            </button>

            <button onclick="cinevaMessage('Downloads','Downloaded videos can be watched offline from Downloads.')">
                ↓ Downloads
            </button>

            <button onclick="cinevaMessage('Contact Support','Support contact will be connected later.')">
                ✉ Contact Support
            </button>
        </div>
    `);
}

/* PRIVACY */

function openPrivacy() {
    cinevaPage("Privacy", `
        <div class="legal-card">
            <h3>Your Privacy</h3>
            <p>
                Cineva is designed to keep your account,
                viewing activity and settings under your control.
            </p>

            <p>
                Detailed privacy controls will be connected
                when the Cineva backend is added.
            </p>
        </div>
    `);
}

/* TERMS */

function openTerms() {
    cinevaPage("Terms", `
        <div class="legal-card">
            <h3>Cineva Terms of Use</h3>
            <p>
                Cineva is a movie and series streaming interface.
            </p>

            <p>
                Content availability, accounts, subscriptions
                and downloads will depend on the Cineva service
                and its backend.
            </p>
        </div>
    `);
}

/* PARENTAL CONTROLS */

function openParentalControls() {
    cinevaPage("Parental Controls", `
        <div class="setting-card">
            <h3>Parental Controls</h3>
            <p>Control access to restricted content.</p>

            <label class="switch-row">
                <span>Restricted Mode</span>
                <input
                    type="checkbox"
                    id="parentalSwitch"
                    onchange="toggleParental(this)"
                >
            </label>
        </div>
    `);
}

function toggleParental(el) {
    localStorage.setItem(
        "cineva_parental",
        el.checked ? "true" : "false"
    );
}

/* LOGOUT */

function logoutCineva() {
    localStorage.removeItem("cineva_guest");
    localStorage.removeItem("cineva_user");

    const login =
        document.getElementById("loginScreen");

    const app =
        document.getElementById("app");

    const header =
        document.getElementById("mainHeader");

    if (app) app.style.display = "none";
    if (header) header.style.display = "none";
    if (login) login.style.display = "flex";
}
