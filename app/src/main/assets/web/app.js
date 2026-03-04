const routes = ["home", "ingest", "ocr", "library", "qa", "typeset", "settings"];
const view = document.getElementById("view");
const toTop = document.getElementById("toTop");
const today = document.getElementById("today");

const presetThemes = {
  sky: { bg: "#f4f7ff", bg2: "#edf3ff", line: "#dbe6ff", brand: "#2f61ff", brand2: "#2a4fc2" },
  mint: { bg: "#f2faf7", bg2: "#e7f6f1", line: "#cfe8df", brand: "#1f8f72", brand2: "#176f59" },
  sand: { bg: "#fdf8f1", bg2: "#f8efe2", line: "#eadcca", brand: "#b3752c", brand2: "#8f5a1d" }
};

function safeRoute(route) {
  return routes.includes(route) ? route : "home";
}

function currentRoute() {
  const hash = location.hash.replace("#", "").trim();
  const persisted = localStorage.getItem("nb_route") || "home";
  return safeRoute(hash || persisted);
}

function setVars(theme) {
  document.documentElement.style.setProperty("--bg", theme.bg);
  document.documentElement.style.setProperty("--bg-2", theme.bg2);
  document.documentElement.style.setProperty("--line", theme.line);
  document.documentElement.style.setProperty("--brand", theme.brand);
  document.documentElement.style.setProperty("--brand-2", theme.brand2);
}

function applySavedTheme() {
  const key = localStorage.getItem("nb_theme") || "sky";
  setVars(presetThemes[key] || presetThemes.sky);
}

function animateCounters() {
  const counters = view.querySelectorAll("[data-count]");
  counters.forEach((el) => {
    const target = Number(el.getAttribute("data-count") || "0");
    const suffix = el.getAttribute("data-suffix") || "";
    const start = performance.now();
    const duration = 700;

    function tick(now) {
      const p = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - p, 3);
      const value = Math.round(target * eased);
      el.textContent = `${value}${suffix}`;
      if (p < 1) requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
  });
}

function wireSettings() {
  const paletteBtns = view.querySelectorAll(".theme-btn[data-theme]");
  const accentPicker = view.querySelector("#accentPicker");
  const headingScale = view.querySelector("#headingScale");

  paletteBtns.forEach((btn) => {
    btn.onclick = () => {
      const key = btn.getAttribute("data-theme");
      const theme = presetThemes[key];
      if (!theme) return;
      localStorage.setItem("nb_theme", key);
      setVars(theme);
      if (accentPicker) accentPicker.value = theme.brand;
    };
  });

  if (accentPicker) {
    accentPicker.oninput = () => {
      document.documentElement.style.setProperty("--brand", accentPicker.value);
      document.documentElement.style.setProperty("--brand-2", accentPicker.value);
    };
  }

  if (headingScale) {
    headingScale.oninput = () => {
      document.documentElement.style.setProperty("--heading", `${headingScale.value}px`);
      localStorage.setItem("nb_heading", headingScale.value);
    };

    const saved = localStorage.getItem("nb_heading");
    if (saved) {
      headingScale.value = saved;
      document.documentElement.style.setProperty("--heading", `${saved}px`);
    }
  }
}

function bindRouteButtons() {
  document.querySelectorAll("[data-route]").forEach((el) => {
    el.onclick = () => go(el.dataset.route);
  });
}

function render(route) {
  const tpl = document.getElementById(`tpl-${route}`);
  if (!tpl) return;

  view.classList.add("route-leave");
  window.setTimeout(() => {
    view.innerHTML = "";
    view.classList.remove("route-leave");
    view.classList.add("route-enter");
    view.appendChild(tpl.content.cloneNode(true));

    document.querySelectorAll(".tab").forEach((tab) => {
      tab.classList.toggle("active", tab.dataset.route === route);
    });

    localStorage.setItem("nb_route", route);
    bindRouteButtons();
    if (route === "home") animateCounters();
    if (route === "settings") wireSettings();
    window.scrollTo({ top: 0, behavior: "smooth" });

    window.setTimeout(() => view.classList.remove("route-enter"), 320);
  }, 140);
}

function go(route) {
  const next = safeRoute(route);
  if (location.hash.replace("#", "") === next) {
    render(next);
    return;
  }
  location.hash = next;
}

function bindScrollActions() {
  window.addEventListener("scroll", () => {
    const y = window.scrollY || document.documentElement.scrollTop;
    toTop.classList.toggle("show", y > 360);
  });

  toTop.onclick = () => window.scrollTo({ top: 0, behavior: "smooth" });
}

function renderDate() {
  const d = new Date();
  today.textContent = d.toLocaleDateString("zh-CN", {
    month: "short",
    day: "numeric",
    weekday: "short"
  });
}

window.addEventListener("hashchange", () => render(currentRoute()));
window.addEventListener("DOMContentLoaded", () => {
  applySavedTheme();
  renderDate();
  bindRouteButtons();
  bindScrollActions();

  if (!location.hash) {
    location.hash = currentRoute();
  } else {
    render(currentRoute());
  }
});