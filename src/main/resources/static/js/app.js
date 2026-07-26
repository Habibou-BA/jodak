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
});
