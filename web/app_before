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
                    <div class="search-empty">Search Cineva</div>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);

        const input = document.getElementById("cinevaSearchInput");
        const results = document.getElementById("cinevaSearchResults");

        async function searchNow() {
            const q = input.value.trim().toLowerCase();

            if (!q) {
                results.innerHTML =
                    '<div class="search-empty">Type a movie or series name</div>';
                return;
            }

            if (typeof firebase === "undefined" ||
                !firebase.apps ||
                !firebase.apps.length ||
                !firebase.firestore) {
                results.innerHTML =
                    '<div class="search-empty">Database is not connected.</div>';
                return;
            }

            results.innerHTML =
                '<div class="search-empty">Searching...</div>';

            try {
                const db = firebase.firestore();

                const [movieSnap, seriesSnap] = await Promise.all([
                    db.collection("movies").get(),
                    db.collection("series").get()
                ]);

                const movies = [];
                const series = [];

                movieSnap.forEach(doc => {
                    movies.push({ id: doc.id, type: "movie", ...doc.data() });
                });

                seriesSnap.forEach(doc => {
                    series.push({ id: doc.id, type: "series", ...doc.data() });
                });

                const all = movies.concat(series);

                const found = all.filter(item => {
                    const title = String(item.title || "").toLowerCase();
                    const genre = String(item.genre || "").toLowerCase();
                    const language = String(item.language || "").toLowerCase();

                    return title.includes(q) ||
                           genre.includes(q) ||
                           language.includes(q);
                });

                if (!found.length) {
                    results.innerHTML = `
                        <div class="search-empty">
                            No results found for
                            <strong>${escapeHTML(input.value)}</strong>
                        </div>
                    `;
                    return;
                }

                results.innerHTML = "";

                found.forEach(item => {
                    const card = document.createElement("article");
                    card.className = "movie-card";
                    card.setAttribute("data-title", item.title || "");

                    const poster = item.posterUrl || "";

                    card.innerHTML = `
                        <div class="poster"
                             style="background-image:url('${escapeHTML(poster)}')">
                        </div>

                        <b>${escapeHTML(item.title || "Untitled")}</b>

                        <small>
                            ${escapeHTML(
                                item.type === "series"
                                    ? "Series"
                                    : (item.language || "Movie")
                            )}
                            ${item.year ? " • " + escapeHTML(item.year) : ""}
                        </small>
                    `;

                    card.addEventListener("click", async function () {
                        closeSearch();

                        if (item.type === "series") {
                            window.showPage("series");
                        } else {
                            window.showFirebaseMovie(item);
                        }
                    });

                    results.appendChild(card);
                });

            } catch (error) {
                console.error("Cineva search error:", error);

                results.innerHTML = `
                    <div class="search-empty">
                        Search failed.<br>
                        Please try again.
                    </div>
                `;
            }
        }

        input.addEventListener("input", searchNow);

        document.getElementById("cinevaSearchBack").onclick = closeSearch;
        document.getElementById("cinevaSearchClose").onclick = closeSearch;

        setTimeout(function () {
            input.focus();
        }, 50);
    };

    window.showFirebaseMovie = function (movie) {
        const title = movie.title || "Untitled";
        const poster = movie.posterUrl || "";
        const video = movie.videoUrl || "";
        const trailer = movie.trailerUrl || "";
        const description = movie.description || "No description available.";

        const app = document.getElementById("app");
        if (!app) return;

        const old = document.getElementById("cinevaFirebaseDetails");
        if (old) old.remove();

        const page = document.createElement("section");
        page.id = "cinevaFirebaseDetails";
        page.className = "page active";

        page.innerHTML = `
            <div class="dynamic-settings">
                <div class="dynamic-header">
                    <button type="button" id="firebaseMovieBack">‹</button>
                    <h2>${escapeHTML(title)}</h2>
                </div>

                <div class="dynamic-content">
                    <div class="series-details">

                        ${
                            poster
                            ? `<img src="${escapeHTML(poster)}"
                                    style="width:100%;max-width:320px;border-radius:12px;display:block;margin-bottom:20px;">`
                            : ""
                        }

                        <span class="badge">
                            ${movie.type === "series" ? "SERIES" : "MOVIE"}
                        </span>

                        <h1>${escapeHTML(title)}</h1>

                        <p>${escapeHTML(description)}</p>

                        ${
                            movie.year
                            ? `<p>${escapeHTML(movie.year)}</p>`
                            : ""
                        }

                        <div style="display:flex;gap:10px;flex-wrap:wrap;margin-top:20px;">

                            ${
                                video
                                ? `<button class="primary"
                                     id="firebasePlayButton">
                                     ▶ Watch
                                   </button>`
                                : ""
                            }

                            ${
                                trailer
                                ? `<button class="secondary"
                                     id="firebaseTrailerButton">
                                     ▶ Trailer
                                   </button>`
                                : ""
                            }

                        </div>
                    </div>
                </div>
            </div>
        `;

        app.appendChild(page);

        document.querySelectorAll(".page").forEach(function(p) {
            if (p !== page) p.classList.remove("active");
        });

        const back = document.getElementById("firebaseMovieBack");

        if (back) {
            back.onclick = function() {
                page.remove();
                activatePage("home");
            };
        }

        const play = document.getElementById("firebasePlayButton");

        if (play) {
            play.onclick = function() {
                if (video) window.open(video, "_blank");
            };
        }

        const trailerButton =
            document.getElementById("firebaseTrailerButton");

        if (trailerButton) {
            trailerButton.onclick = function() {
                if (trailer) window.open(trailer, "_blank");
            };
        }

        window.scrollTo(0, 0);
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

/* =========================================
   CINEVA FIREBASE AUTH
   ========================================= */

(function () {
    "use strict";

    function cinevaFirebaseReady() {
        return typeof firebase !== "undefined" &&
               firebase.apps &&
               firebase.apps.length > 0;
    }

    window.loginWithEmail = async function () {
        const emailEl = document.getElementById("loginEmail");
        const passwordEl = document.getElementById("loginPassword");

        const email = emailEl ? emailEl.value.trim() : "";
        const password = passwordEl ? passwordEl.value : "";

        if (!email || !password) {
            alert("Please enter email and password.");
            return;
        }

        if (!cinevaFirebaseReady()) {
            alert("Firebase is not connected.");
            return;
        }

        try {
            const result =
                await firebase.auth().signInWithEmailAndPassword(
                    email,
                    password
                );

            const user = result.user;

            localStorage.setItem(
                "cineva_user",
                user.email || ""
            );

            localStorage.setItem(
                "cineva_name",
                user.displayName ||
                (user.email ? user.email.split("@")[0] : "Cineva User")
            );

            if (typeof window.hideLogin === "function") {
                window.hideLogin();
            }

            if (typeof window.showPage === "function") {
                window.showPage("home", false);
            }

        } catch (error) {
            if (error.code === "auth/invalid-credential" ||
                error.code === "auth/wrong-password" ||
                error.code === "auth/user-not-found") {

                alert("Email or password is incorrect.");

            } else if (error.code === "auth/invalid-email") {

                alert("Please enter a valid email address.");

            } else {

                alert("Login failed: " + error.message);
            }
        }
    };

    window.loginWithGoogle = async function () {

        if (!cinevaFirebaseReady()) {
            alert("Firebase is not connected.");
            return;
        }

        try {
            const provider =
                new firebase.auth.GoogleAuthProvider();

            const result =
                await firebase.auth().signInWithPopup(provider);

            const user = result.user;

            localStorage.setItem(
                "cineva_user",
                user.email || ""
            );

            localStorage.setItem(
                "cineva_name",
                user.displayName || "Cineva User"
            );

            if (typeof window.hideLogin === "function") {
                window.hideLogin();
            }

            if (typeof window.showPage === "function") {
                window.showPage("home", false);
            }

        } catch (error) {

            if (error.code !== "auth/popup-closed-by-user") {
                alert("Google login failed: " + error.message);
            }
        }
    };

    window.logoutCineva = async function () {

        try {
            if (cinevaFirebaseReady()) {
                await firebase.auth().signOut();
            }
        } catch (error) {
            console.log("Firebase logout:", error);
        }

        localStorage.removeItem("cineva_user");
        localStorage.removeItem("cineva_name");
        localStorage.removeItem("cineva_guest");

        const dynamic =
            document.getElementById("cinevaDynamicPage");

        if (dynamic) {
            dynamic.remove();
        }

        const app =
            document.getElementById("app");

        const header =
            document.getElementById("mainHeader");

        const login =
            document.getElementById("loginScreen");

        if (app) app.style.display = "none";
        if (header) header.style.display = "none";
        if (login) login.style.display = "flex";
    };

    document.addEventListener("DOMContentLoaded", function () {

        if (!cinevaFirebaseReady()) {
            console.log("Cineva Firebase not ready.");
            return;
        }

        firebase.auth().onAuthStateChanged(function (user) {

            if (user) {

                localStorage.setItem(
                    "cineva_user",
                    user.email || ""
                );

                localStorage.setItem(
                    "cineva_name",
                    user.displayName ||
                    (user.email
                        ? user.email.split("@")[0]
                        : "Cineva User")
                );

                if (typeof window.hideLogin === "function") {
                    window.hideLogin();
                }

            }
        });
    });

})();
