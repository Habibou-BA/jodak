// Interactions légères : menu mobile + révélation échelonnée au chargement.
document.addEventListener("DOMContentLoaded", () => {
    const nav = document.querySelector(".nav");
    const toggle = document.querySelector(".nav-toggle");
    if (nav && toggle) {
        toggle.addEventListener("click", () => {
            const open = nav.classList.toggle("open");
            toggle.setAttribute("aria-expanded", String(open));
        });
    }

    document.querySelectorAll("[data-reveal]").forEach((el, index) => {
        el.style.animationDelay = `${Math.min(index, 12) * 60}ms`;
    });

    initCountdown();
});

// Compte à rebours vers l'ouverture des Jeux (élément #countdown[data-target]).
function initCountdown() {
    const root = document.getElementById("countdown");
    if (!root) {
        return;
    }
    const target = new Date(root.dataset.target).getTime();
    const cells = {
        days: root.querySelector('[data-cd="days"]'),
        hours: root.querySelector('[data-cd="hours"]'),
        mins: root.querySelector('[data-cd="mins"]'),
        secs: root.querySelector('[data-cd="secs"]'),
    };
    const pad = (n) => String(n).padStart(2, "0");

    const tick = () => {
        const diff = target - Date.now();
        if (diff <= 0) {
            root.classList.add("is-live");
            Object.values(cells).forEach((c) => c && (c.textContent = "00"));
            clearInterval(timer);
            return;
        }
        const s = Math.floor(diff / 1000);
        if (cells.days) cells.days.textContent = String(Math.floor(s / 86400));
        if (cells.hours) cells.hours.textContent = pad(Math.floor((s % 86400) / 3600));
        if (cells.mins) cells.mins.textContent = pad(Math.floor((s % 3600) / 60));
        if (cells.secs) cells.secs.textContent = pad(s % 60);
    };

    tick();
    const timer = setInterval(tick, 1000);
}
