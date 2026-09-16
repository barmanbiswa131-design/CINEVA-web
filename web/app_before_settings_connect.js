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
