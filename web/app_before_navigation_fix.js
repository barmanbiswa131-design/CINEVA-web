(function () {
    "use strict";

    let currentPage = "home";
    let pageStack = [];

    function allPages() {
        return document.querySelectorAll(".page");
    }

    window.showPage = function (page, pushHistory = true) {
        const target = document.getElementById(page);
        if (!target) return;

        if (pushHistory && currentPage !== page) {
            pageStack.push(currentPage);
            history.pushState({ page: page }, "", "#" + page);
        }

        allPages().forEach(function (p) {
            p.classList.remove("active");
        });

        target.classList.add("active");
        currentPage = page;

        window.scrollTo(0, 0);
    };

    window.goBackCineva = function () {
        if (pageStack.length > 0) {
            const previous = pageStack.pop();

            allPages().forEach(function (p) {
                p.classList.remove("active");
            });

            const target = document.getElementById(previous);

            if (target) {
                target.classList.add("active");
                currentPage = previous;
                window.scrollTo(0, 0);
            }
        } else {
            showPage("home", false);
        }
    };

    window.onpopstate = function () {
        const hash = location.hash.replace("#", "");

        if (hash) {
            showPage(hash, false);
        } else {
            showPage("home", false);
        }
    };

    window.playDemo = function () {
        const title = document.getElementById("playerTitle");

        if (title) {
            title.textContent = "One Piece [Hindi] • S01 E01";
        }

        const player = document.getElementById("player");

        if (player) {
            player.classList.remove("hidden");
        }
    };

    window.openSeries = function () {
        showPage("series");
    };

    window.showDetails = function () {
        cinevaMessage(
            "Movie Details",
            "Movie and series information will appear here."
        );
    };

    window.closePlayer = function () {
        const player = document.getElementById("player");

        if (player) {
            player.classList.add("hidden");
        }
    };

    window.togglePlay = function () {
        const buttons = document.querySelectorAll(
            ".play-button, .player-bottom > button:first-child"
        );

        buttons.forEach(function (button) {
            button.textContent =
                button.textContent.trim() === "▶" ? "Ⅱ" : "▶";
        });
    };

    window.skip = function (seconds) {
        cinevaMessage(
            seconds > 0 ? "Forward" : "Rewind",
            seconds > 0 ? "+10 seconds" : "-10 seconds"
        );
    };

    /* =========================
       REAL CINEVA SEARCH
       ========================= */

    window.searchMovies = function () {
        let query = prompt("Search Cineva");

        if (!query || !query.trim()) return;

        query = query.trim().toLowerCase();

        const items = document.querySelectorAll(
            ".movie-card, .series-card, .content-card, [data-title]"
        );

        let found = [];

        items.forEach(function (item) {
            const text = (
                item.getAttribute("data-title") ||
                item.innerText ||
                item.textContent ||
                ""
            ).toLowerCase();

            if (text.includes(query)) {
                item.style.display = "";
                found.push(item);
            } else {
                item.style.display = "none";
            }
        });

        let old = document.getElementById("cinevaSearchResult");
        if (old) old.remove();

        const result = document.createElement("div");
        result.id = "cinevaSearchResult";

        result.style.padding = "20px";
        result.style.color = "white";
        result.style.background = "#080808";

        if (found.length > 0) {
            result.innerHTML =
                "<h2>Search results</h2>" +
                "<p>" + found.length +
                " result(s) found for \"" +
                escapeHtml(query) +
                "\"</p>";
        } else {
            result.innerHTML =
                "<h2>No results</h2>" +
                "<p>Nothing found for \"" +
                escapeHtml(query) +
                "\"</p>";
        }

        const app = document.getElementById("app");

        if (app) {
            app.prepend(result);
        }
    };

    window.clearCinevaSearch = function () {
        document
            .querySelectorAll(
                ".movie-card, .series-card, .content-card, [data-title]"
            )
            .forEach(function (item) {
                item.style.display = "";
            });

        const result = document.getElementById("cinevaSearchResult");

        if (result) {
            result.remove();
        }
    };

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    /* =========================
       LOGIN
       ========================= */

    window.hideLogin = function () {
        const login = document.getElementById("loginScreen");
        const app = document.getElementById("app");
        const header = document.getElementById("mainHeader");

        if (login) login.style.display = "none";
        if (app) app.style.display = "";
        if (header) header.style.display = "";
    };

    window.loginWithGoogle = function () {
        alert("Google Login will be connected with Firebase.");
    };

    window.loginWithEmail = function () {
        const emailElement =
            document.getElementById("loginEmail");

        const passwordElement =
            document.getElementById("loginPassword");

        const email =
            emailElement ? emailElement.value.trim() : "";

        const password =
            passwordElement ? passwordElement.value : "";

        if (!email || !password) {
            alert("Please enter email and password.");
            return;
        }

        alert("Email login will be connected with Firebase.");
    };

    /* =========================
       ACCOUNT
       ========================= */

    window.cinevaMessage = function (title, message) {
        alert(title + "\n\n" + message);
    };

    window.logoutCineva = function () {
        localStorage.removeItem("cineva_guest");
        localStorage.removeItem("cineva_user");
        localStorage.removeItem("cineva_name");

        const app = document.getElementById("app");
        const header = document.getElementById("mainHeader");
        const login = document.getElementById("loginScreen");

        if (app) app.style.display = "none";
        if (header) header.style.display = "none";

        if (login) {
            login.style.display = "flex";
        }
    };

    /* =========================
       SETTINGS
       ========================= */

    window.openAccountSettings = function () {
        showPage("settings");
    };

    window.accountMenu = function () {
        showPage("settings");
    };

    window.openProfile = function () {
        cinevaPage(
            "Profile",
            `
            <div class="profile-card">
                <div class="profile-avatar">C</div>
                <h2>
                    ${escapeHtml(
                        localStorage.getItem("cineva_name") ||
                        "Cineva User"
                    )}
                </h2>
                <p>Cineva Member</p>
            </div>

            <button class="settings-action"
                onclick="closeCinevaPage()">
                Back to Settings
            </button>
            `
        );
    };

    window.openSubscription = function () {
        cinevaPage(
            "Subscription",
            `
            <div class="settings-section">
                <h3>Cineva Subscription</h3>
                <p>Subscription plans will appear here.</p>
            </div>
            `
        );
    };

    window.openCoins = function () {
        cinevaPage(
            "Coins & Daily Tasks",
            `
            <div class="settings-section">
                <h3>Coins</h3>
                <p>Your Cineva coins and daily tasks will appear here.</p>
            </div>
            `
        );
    };

    window.openDownloads = function () {
        showPage("downloads");
    };

    window.openHelp = function () {
        cinevaMessage(
            "Help & Support",
            "Cineva Help & Support is ready."
        );
    };

    window.openPrivacy = function () {
        cinevaPage(
            "Privacy",
            `
            <div class="settings-section">
                <h3>Privacy</h3>
                <p>Your Cineva account and viewing activity stay under your control.</p>
            </div>
            `
        );
    };

    window.openTerms = function () {
        cinevaPage(
            "Terms",
            `
            <div class="settings-section">
                <h3>Terms of Use</h3>
                <p>Cineva Terms of Use will appear here.</p>
            </div>
            `
        );
    };

    window.changeLanguage = function () {
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

        const index = parseInt(choice, 10) - 1;

        if (index >= 0 && index < languages.length) {
            localStorage.setItem(
                "cineva_language",
                languages[index]
            );

            cinevaMessage(
                "Language",
                languages[index] + " selected."
            );
        }
    };

    window.openVideoQuality = function () {
        const qualities = [
            "Auto",
            "1080p",
            "720p",
            "480p",
            "360p"
        ];

        const choice = prompt(
            "Choose video quality:\n\n" +
            "1. Auto\n" +
            "2. 1080p\n" +
            "3. 720p\n" +
            "4. 480p\n" +
            "5. 360p"
        );

        const index = parseInt(choice, 10) - 1;

        if (index >= 0 && index < qualities.length) {
            localStorage.setItem(
                "cineva_quality",
                qualities[index]
            );

            cinevaMessage(
                "Video Quality",
                qualities[index] + " selected."
            );
        }
    };

    /* =========================
       DYNAMIC SETTINGS PAGE
       ========================= */

    window.cinevaPage = function (title, content) {
        const app = document.getElementById("app");

        if (!app) return;

        const old = document.getElementById(
            "cinevaDynamicPage"
        );

        if (old) {
            old.remove();
        }

        allPages().forEach(function (p) {
            p.classList.remove("active");
        });

        const page = document.createElement("section");

        page.id = "cinevaDynamicPage";
        page.className = "page active";

        page.innerHTML = `
            <div class="dynamic-settings">

                <div class="dynamic-header">
                    <button onclick="closeCinevaPage()">‹</button>
                    <h2>${escapeHtml(title)}</h2>
                </div>

                ${content}

            </div>
        `;

        app.appendChild(page);

        currentPage = "cinevaDynamicPage";

        window.scrollTo(0, 0);
    };

    window.closeCinevaPage = function () {
        const page =
            document.getElementById("cinevaDynamicPage");

        if (page) {
            page.remove();
        }

        showPage("settings", false);
    };

    /* =========================
       CONNECT SETTINGS BUTTONS
       ========================= */

    window.connectCinevaSettings = function () {
        document
            .querySelectorAll(
                "button, .setting-item, .settings-item, .setting-row, .settings-row"
            )
            .forEach(function (el) {

                const text =
                    (el.innerText ||
                     el.textContent ||
                     "")
                    .trim()
                    .toLowerCase();

                if (
                    text.includes("logout") ||
                    text.includes("log out")
                ) {
                    el.onclick = function (event) {
                        event.preventDefault();

                        if (
                            confirm(
                                "Are you sure you want to log out of Cineva?"
                            )
                        ) {
                            logoutCineva();
                        }
                    };
                }
            });
    };

    /* =========================
       STARTUP
       ========================= */

    document.addEventListener(
        "DOMContentLoaded",
        function () {

            connectCinevaSettings();

            const hash =
                location.hash.replace("#", "");

            if (hash) {
                showPage(hash, false);
            } else {
                showPage("home", false);
            }
        }
    );

})();
