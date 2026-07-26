/* Console d'administration JODAK — consomme l'API /api/admin/** avec un JWT Bearer. */
(function () {
    "use strict";

    const TOKEN_KEY = "jodak_admin_token";
    const REFRESH_KEY = "jodak_admin_refresh";
    const LOGIN_URL = "/backoffice/login";

    const getToken = () => localStorage.getItem(TOKEN_KEY);
    const setTokens = (a, r) => {
        localStorage.setItem(TOKEN_KEY, a);
        if (r) localStorage.setItem(REFRESH_KEY, r);
    };
    const clearTokens = () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_KEY);
    };

    function jwtPayload(token) {
        try {
            return JSON.parse(atob(token.split(".")[1]));
        } catch (e) {
            return {};
        }
    }

    function esc(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    function toast(message, ok) {
        const el = document.querySelector("[data-toast]");
        if (!el) return;
        el.textContent = message;
        el.className = "toast show " + (ok ? "ok" : "err");
        setTimeout(() => (el.className = "toast"), 3200);
    }

    async function api(path, options) {
        options = options || {};
        const headers = options.headers || {};
        const token = getToken();
        if (token) headers["Authorization"] = "Bearer " + token;
        let body = options.body;
        if (body && !options.form) {
            headers["Content-Type"] = "application/json";
            body = JSON.stringify(body);
        }
        const res = await fetch(path, { method: options.method || "GET", headers, body });
        if (res.status === 401) {
            clearTokens();
            location.href = LOGIN_URL;
            throw new Error("Non authentifié");
        }
        return res;
    }

    /* ---------- Connexion ---------- */
    function initLogin() {
        const form = document.getElementById("login-form");
        if (!form) return;
        if (getToken()) {
            location.href = "/backoffice/dashboard";
            return;
        }
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const errorEl = form.querySelector("[data-login-error]");
            errorEl.textContent = "";
            const payload = { email: form.email.value.trim(), password: form.password.value };
            try {
                const res = await fetch("/api/admin/auth/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload),
                });
                if (res.ok) {
                    const data = await res.json();
                    setTokens(data.accessToken, data.refreshToken);
                    location.href = "/backoffice/dashboard";
                } else {
                    errorEl.textContent = "Identifiants invalides.";
                }
            } catch (err) {
                errorEl.textContent = "Connexion impossible.";
            }
        });
    }

    /* ---------- Coque protégée ---------- */
    function initShell() {
        if (!document.body.hasAttribute("data-protected")) return false;
        if (!getToken()) {
            location.href = LOGIN_URL;
            return true;
        }
        const idEl = document.querySelector("[data-admin-id]");
        if (idEl) {
            const p = jwtPayload(getToken());
            idEl.textContent = p.email || ("#" + (p.sub || ""));
        }
        const logout = document.querySelector("[data-logout]");
        if (logout) {
            logout.addEventListener("click", async () => {
                const refresh = localStorage.getItem(REFRESH_KEY);
                try {
                    if (refresh) {
                        await fetch("/api/admin/auth/logout", {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ refreshToken: refresh }),
                        });
                    }
                } catch (e) { /* ignore */ }
                clearTokens();
                location.href = LOGIN_URL;
            });
        }
        const toggle = document.querySelector(".menu-toggle");
        const sidebar = document.querySelector(".sidebar");
        if (toggle && sidebar) toggle.addEventListener("click", () => sidebar.classList.toggle("open"));
        return true;
    }

    /* ---------- Tableau de bord ---------- */
    async function initDashboard() {
        if (!document.querySelector("[data-stat]")) return;
        try {
            const res = await fetch("/api/v1/tableau-de-bord");
            if (!res.ok) return;
            const d = await res.json();
            document.querySelectorAll("[data-stat]").forEach((el) => {
                const key = el.getAttribute("data-stat");
                if (d[key] != null) el.textContent = d[key];
            });
        } catch (e) { /* ignore */ }
    }

    /* ---------- Imports ---------- */
    function initImports() {
        const form = document.getElementById("import-form");
        const tbody = document.querySelector("[data-jobs]");
        if (!form || !tbody) return;

        async function loadJobs() {
            try {
                const res = await api("/api/admin/imports?size=20&sort=createdAt,desc");
                const page = await res.json();
                const rows = (page.content || []);
                tbody.innerHTML = rows.length ? rows.map(renderJob).join("")
                    : '<tr><td colspan="9" class="muted">Aucun import.</td></tr>';
                if (rows.some((j) => j.status === "PENDING" || j.status === "RUNNING")) {
                    clearTimeout(loadJobs._t);
                    loadJobs._t = setTimeout(loadJobs, 1500);
                }
            } catch (e) { /* redirigé si 401 */ }
        }

        function renderJob(j) {
            const errLink = j.failedRows > 0
                ? ` · <a href="#" data-errors="${j.id}">${j.failedRows} erreur(s)</a>` : "";
            return `<tr>
                <td>${j.id}</td><td>${esc(j.jobType)}</td><td>${esc(j.mode)}</td>
                <td><span class="badge ${esc(j.status)}">${esc(j.status)}</span></td>
                <td><span class="progress"><span style="width:${j.progressPercent}%"></span></span> ${j.progressPercent}%</td>
                <td>${j.importedRows}</td><td>${j.skippedRows}</td>
                <td>${j.failedRows}${errLink}</td>
                <td>${j.status === "PENDING" || j.status === "RUNNING"
                    ? `<a href="#" data-cancel="${j.id}">Annuler</a>` : ""}</td>
            </tr>`;
        }

        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const data = new FormData(form);
            try {
                const res = await api("/api/admin/imports", { method: "POST", body: data, form: true });
                if (res.ok) {
                    toast("Import lancé.", true);
                    form.reset();
                    loadJobs();
                } else {
                    const err = await res.json().catch(() => ({}));
                    toast(err.detail || "Import refusé.", false);
                }
            } catch (e2) { /* ignore */ }
        });

        tbody.addEventListener("click", async (e) => {
            const cancel = e.target.getAttribute("data-cancel");
            const errors = e.target.getAttribute("data-errors");
            if (cancel) {
                e.preventDefault();
                await api("/api/admin/imports/" + cancel + "/cancel", { method: "POST" });
                toast("Annulation demandée.", true);
                loadJobs();
            } else if (errors) {
                e.preventDefault();
                const res = await api("/api/admin/imports/" + errors + "/errors?size=20");
                const page = await res.json();
                const lines = (page.content || [])
                    .map((x) => `Ligne ${x.rowNumber} [${x.columnName || "-"}] : ${x.message}`);
                alert(lines.join("\n") || "Aucune erreur.");
            }
        });

        loadJobs();
    }

    /* ---------- Maintenance ---------- */
    function initMaintenance() {
        const exportBtn = document.querySelector("[data-export]");
        const backupBtn = document.querySelector("[data-backup]");
        const resetForm = document.getElementById("reset-form");

        if (exportBtn) {
            exportBtn.addEventListener("click", async () => {
                try {
                    const res = await api("/api/admin/export");
                    if (!res.ok) { toast("Export impossible.", false); return; }
                    const blob = await res.blob();
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement("a");
                    a.href = url;
                    a.download = "export-jodak.zip";
                    a.click();
                    URL.revokeObjectURL(url);
                    toast("Export téléchargé.", true);
                } catch (e) { /* ignore */ }
            });
        }

        if (backupBtn) {
            const result = document.querySelector("[data-backup-result]");
            backupBtn.addEventListener("click", async () => {
                try {
                    const res = await api("/api/admin/backup", { method: "POST" });
                    if (!res.ok) { toast("Sauvegarde impossible.", false); return; }
                    const b = await res.json();
                    if (result) result.textContent = `Fichier ${b.fileName} · checksum ${b.checksum}`;
                    toast("Sauvegarde créée.", true);
                } catch (e) { /* ignore */ }
            });
        }

        if (resetForm) {
            const result = document.querySelector("[data-reset-result]");
            resetForm.addEventListener("submit", async (e) => {
                e.preventDefault();
                if (!confirm("Confirmer la réinitialisation ? Une sauvegarde sera créée, puis les "
                    + "données de compétition seront supprimées.")) return;
                const payload = {
                    password: resetForm.password.value,
                    confirmationPhrase: resetForm.confirmationPhrase.value,
                };
                try {
                    const res = await api("/api/admin/reset", { method: "POST", body: payload });
                    const data = await res.json().catch(() => ({}));
                    if (res.ok) {
                        if (result) result.textContent = "Réinitialisé. Sauvegarde : " + data.backupFileName;
                        toast("Plateforme réinitialisée.", true);
                        resetForm.reset();
                    } else {
                        toast(data.detail || "Réinitialisation refusée.", false);
                    }
                } catch (e2) { /* ignore */ }
            });
        }
    }

    /* ---------- Journal ---------- */
    async function initLogs() {
        const tbody = document.querySelector("[data-logs]");
        if (!tbody) return;
        try {
            const res = await api("/api/admin/logs?size=50");
            const page = await res.json();
            const rows = page.content || [];
            tbody.innerHTML = rows.length ? rows.map((l) => `<tr>
                <td>${esc((l.createdAt || "").replace("T", " ").slice(0, 19))}</td>
                <td>${esc(l.action)}</td>
                <td>${l.success ? "✅" : "❌"}</td>
                <td>${esc(l.adminId || "-")}</td>
                <td>${esc(l.ip || "-")}</td>
                <td>${esc(l.message || "")}</td></tr>`).join("")
                : '<tr><td colspan="6" class="muted">Aucune entrée.</td></tr>';
        } catch (e) { /* ignore */ }
    }

    document.addEventListener("DOMContentLoaded", () => {
        if (document.body.hasAttribute("data-login")) {
            initLogin();
            return;
        }
        if (!initShell()) return;
        initDashboard();
        initImports();
        initMaintenance();
        initLogs();
    });
})();
