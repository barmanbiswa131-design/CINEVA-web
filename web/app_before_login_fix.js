(function () {
    "use strict";

    let cinevaHistory = [];
    let currentCinevaPage = "home";
    let settingsChildOpen = false;

    function getPages() {
        return document.querySelectorAll(".page");
    }

    function activatePage(page) {
        getPages().forEach(function (p) {
            p.classList.remove("active");
        });

        const target = document.getElementById(page);

        if (target) {
            target.classList.add("active");
            window.scrollTo(0, 0);
        }

        currentCinevaPage = page;
    }

    window.showPage = function (page, pushHistory) {
        if (!page) return;

        if (pushHistory === undefined) {
            pushHistory = true;
        }

        if (page === currentCinevaPage && !settingsChildOpen) {
            return;
        }

        if (pushHistory && currentCinevaPage !== page) {
            cinevaHistory.push(currentCinevaPage);
        }

        settingsChildOpen = false;

        const dynamic = document.getElementById("cinevaDynamicPage");
        if (dynamic) {
            dynamic.remove();
        }

        activatePage(page);
    };

    window.goCinevaBack = function () {
        const dynamic = document.getElementById("cinevaDynamicPage");

        if (dynamic) {
            dynamic.remove();
            settingsChildOpen = false;
            activatePage("settings");
            return;
        }

        if (cinevaHistory.length > 0) {
            const previous = cinevaHistory.pop();
            activatePage(previous);
            return;
        }

        activatePage("home");
    };

    /* Browser back */
    window.addEventListener("popstate", function () {
        window.goCinevaBack();
    });

    /* =========================
       SEARCH
       ========================= */

    window.searchMovies = function () {
        let old = document.getElementById("cinevaSearchBox");

        if (old) {
            old.remove();
            return;
        }

        const overlay = document.createElement("div");
        overlay.id = "cinevaSearchBox";

        overlay.innerHTML = `
            <div class="cineva-search-panel">
                <div class="cineva-search-top">
                    <button id="cinevaSearchBack">‹</button>
                    <input
                        id="cinevaSearchInput"
                        type="search"
                        placeholder="Search movies & series..."
                        autocomplete="off"
                    >
                    <button id="cinevaSearchClose">×</button>
                </div>

                <div id="cinevaSearchResults">
                    <div class="search-empty">
                        Search Cineva
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);

        const input = document.getElementById("cinevaSearchInput");
        const results = document.getElementById("cinevaSearchResults");

        function searchNow() {
            const q = input.value.trim().toLowerCase();

            if (!q) {
                results.innerHTML = `
                    <div class="search-empty">
                        Type a movie or series name
                    </div>
                `;
                return;
            }

            const cards = document.querySelectorAll(
                ".movie-card, .series-card, .card, [data-title]"
            );

            let found = [];

            cards.forEach(function (card) {
                const text = (
                    card.innerText ||
                    card.textContent ||
                    ""
                ).toLowerCase();

                const dataTitle = (
                    card.getAttribute("data-title") || ""
                ).toLowerCase();

                if (
                    text.includes(q) ||
                    dataTitle.includes(q)
                ) {
                    found.push(card);
                }
            });

            if (found.length === 0) {
                results.innerHTML = `
                    <div class="search-empty">
                        No results found for
                        <strong>${escapeHTML(input.value)}</strong>
                    </div>
                `;
                return;
            }

            results.innerHTML = "";

            found.forEach(function (card) {
                const clone = card.cloneNode(true);

                clone.addEventListener("click", function () {
                    const original = card;

                    if (original) {
                        original.click();
                    }

                    closeSearch();
                });

                results.appendChild(clone);
            });
        }

        input.addEventListener("input", searchNow);

        document.getElementById("cinevaSearchBack")
            .onclick = closeSearch;

        document.getElementById("cinevaSearchClose")
            .onclick = closeSearch;

        setTimeout(function () {
            input.focus();
        }, 50);
    };

    function closeSearch() {
        const box = document.getElementById("cinevaSearchBox");

        if (box) {
            box.remove();
        }
    }

    function escapeHTML(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    /* =========================
       PLAYER DEMO
       ========================= */

    window.playDemo = function () {
        const title = document.getElementById("playerTitle");

        if (title) {
            title.textContent =
                "One Piece [Hindi] • S01 E01";
        }

        const player = document.getElementById("player");

        if (player) {
            player.classList.remove("hidden");
        }
    };

    window.openSeries = function () {
        window.showPage("series");
    };

    window.showDetails = function () {
        cinevaMessage(
            "Movie Details",
            "Movie details will be connected to the Cineva database."
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
                button.textContent.trim() === "▶"
                    ? "Ⅱ"
                    : "▶";
        });
    };

    window.skip = function (seconds) {
        cinevaMessage(
            seconds > 0 ? "Forward" : "Rewind",
            seconds > 0
                ? "+10 seconds"
                : "-10 seconds"
        );
    };

    /* =========================
       ACCOUNT
       ========================= */

    window.hideLogin = function () {
        const login = document.getElementById("loginScreen");
        const app = document.getElementById("app");
        const header = document.getElementById("mainHeader");

        if (login) login.style.display = "none";
        if (app) app.style.display = "";
        if (header) header.style.display = "";
    };

    window.logoutCineva = function () {
        localStorage.removeItem("cineva_guest");
        localStorage.removeItem("cineva_user");
        localStorage.removeItem("cineva_name");

        const dynamic = document.getElementById(
            "cinevaDynamicPage"
        );

        if (dynamic) {
            dynamic.remove();
        }

        cinevaHistory = [];
        currentCinevaPage = "home";
        settingsChildOpen = false;

        const app = document.getElementById("app");
        const header = document.getElementById("mainHeader");
        const login = document.getElementById("loginScreen");

        if (app) app.style.display = "none";
        if (header) header.style.display = "none";
        if (login) login.style.display = "flex";
    };

    window.openAccountSettings = function () {
        window.showPage("settings");
    };

    /* =========================
       SETTINGS
       ========================= */

    window.openProfile = function () {
        cinevaPage(
            "Profile",
            `
            <div class="profile-card">
                <div class="profile-avatar">C</div>
                <h2>
                    ${escapeHTML(
                        localStorage.getItem("cineva_name") ||
                        "Cineva User"
                    )}
                </h2>
                <p>Cineva Member</p>
            </div>
            `
        );
    };

    window.openSubscription = function () {
        cinevaPage(
            "Subscription",
            `
            <div class="settings-info">
                <h3>Cineva Subscription</h3>
                <p>Your Cineva subscription information will appear here.</p>
            </div>
            `
        );
    };

    window.openCoins = function () {
        cinevaPage(
            "Coins & Daily Tasks",
            `
            <div class="settings-info">
                <h3>Coins</h3>
                <p>Your Cineva coins and daily tasks will appear here.</p>
            </div>
            `
        );
    };

    window.openDownloads = function () {
        window.showPage("downloads");
    };

    window.openHelp = function () {
        cinevaPage(
            "Help & Support",
            `
            <div class="settings-info">
                <h3>Help & Support</h3>
                <p>Cineva Help & Support is ready.</p>
            </div>
            `
        );
    };

    window.openPrivacy = function () {
        cinevaPage(
            "Privacy",
            `
            <div class="settings-info">
                <h3>Privacy</h3>
                <p>Your Cineva privacy information will appear here.</p>
            </div>
            `
        );
    };

    window.openTerms = function () {
        cinevaPage(
            "Terms of Use",
            `
            <div class="settings-info">
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
            "Choose language:\\n\\n" +
            "1. English\\n" +
            "2. বাংলা\\n" +
            "3. Hindi\\n" +
            "4. Nepali"
        );

        const index = parseInt(choice, 10) - 1;

        if (
            index >= 0 &&
            index < languages.length
        ) {
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
        cinevaPage(
            "Video Quality",
            `
            <div class="settings-info">
                <button class="quality-choice">Auto</button>
                <button class="quality-choice">1080p</button>
                <button class="quality-choice">720p</button>
                <button class="quality-choice">480p</button>
            </div>
            `
        );
    };

    window.openParentalControls = function () {
        cinevaPage(
            "Parental Controls",
            `
            <div class="settings-info">
                <h3>Parental Controls</h3>
                <p>Parental control settings will appear here.</p>
            </div>
            `
        );
    };

    function cinevaMessage(title, message) {
        alert(title + "\\n\\n" + message);
    }

    window.cinevaMessage = cinevaMessage;

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

        settingsChildOpen = true;

        const page = document.createElement("section");

        page.id = "cinevaDynamicPage";
        page.className = "page active";

        page.innerHTML = `
            <div class="dynamic-settings">

                <div class="dynamic-header">

                    <button
                        type="button"
                        id="dynamicBackButton"
                        aria-label="Back"
                    >
                        ‹
                    </button>

                    <h2>${escapeHTML(title)}</h2>

                </div>

                <div class="dynamic-content">
                    ${content}
                </div>

            </div>
        `;

        app.appendChild(page);

        getPages().forEach(function (p) {
            if (p !== page) {
                p.classList.remove("active");
            }
        });

        const back =
            document.getElementById(
                "dynamicBackButton"
            );

        if (back) {
            back.onclick = function () {
                page.remove();
                settingsChildOpen = false;
                activatePage("settings");
            };
        }

        window.scrollTo(0, 0);
    };

    window.closeCinevaPage = function () {
        const page = document.getElementById(
            "cinevaDynamicPage"
        );

        if (page) {
            page.remove();
        }

        settingsChildOpen = false;
        activatePage("settings");
    };

    /* =========================
       REMOVE OLD AUTO CONNECT
       ========================= */

    window.connectCinevaSettings = function () {
        /* Intentionally empty.
           HTML onclick handlers are now used directly.
           This prevents old handlers from overwriting
           new settings behaviour. */
    };

    /* =========================
       INITIAL STATE
       ========================= */

    document.addEventListener("DOMContentLoaded", function () {
        const dynamic =
            document.getElementById(
                "cinevaDynamicPage"
            );

        if (dynamic) {
            dynamic.remove();
        }

        const active =
            document.querySelector(".page.active");

        if (active) {
            currentCinevaPage = active.id;
        } else {
            activatePage("home");
        }
    });

})();
