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
