/**
 * GOW-GO-26-002 — Solo propuestas de PARADEROS en Arequipa
 * Pregunta clave: ¿se reconoce el paradero al alejar / acercar el zoom?
 * Sin rutas inventadas ni vehículos.
 *
 * El catálogo (paradas + propuestas SVG) vive en el módulo KMP `shared`
 * y llega vía window.initApp(json) inyectado desde Kotlin.
 */

let AREQUIPA = { lng: -71.5375, lat: -16.409 };
let STOPS = [];
let FAMILIES = {};
let currentFamily = "A";
let map;
let started = false;

/** Recibe el catálogo del módulo compartido (KMP) y arranca la demo. */
window.initApp = function (data) {
  if (started) return;
  started = true;

  AREQUIPA = data.centro;
  STOPS = data.stops.map((s) => ({
    id: s.id,
    lng: s.lng,
    lat: s.lat,
    name: s.nombre,
    state: s.estado,
  }));

  FAMILIES = {};
  for (const p of data.propuestas) {
    FAMILIES[p.id] = {
      label: p.nombre,
      activo: () => svgImg(p.svgActivo),
      inactivo: () => svgImg(p.svgInactivo),
    };
  }
  currentFamily = data.propuestas[0].id;

  renderFamilias(data.propuestas);
  init();
};

/** Crea los botones A/B/C… a partir del catálogo (sin reinstalar para añadir propuestas). */
function renderFamilias(propuestas) {
  const cont = document.getElementById("familias");
  cont.innerHTML = "";
  for (const p of propuestas) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "fam" + (p.id === currentFamily ? " active" : "");
    btn.dataset.family = p.id;
    btn.textContent = `${p.id} · ${p.nombre}`;
    cont.appendChild(btn);
  }
}

function svgImg(svg) {
  const img = new Image(96, 96);
  img.src = "data:image/svg+xml;charset=utf-8," + encodeURIComponent(svg);
  return img;
}

/** Señal de parada y demás propuestas viven en el módulo KMP `shared` (Catalogo.kt). */

function stopsGeoJSON() {
  return {
    type: "FeatureCollection",
    features: STOPS.map((s) => ({
      type: "Feature",
      properties: {
        id: s.id,
        name: s.name,
        state: s.state,
        icon: `${currentFamily}-${s.state}`,
      },
      geometry: { type: "Point", coordinates: [s.lng, s.lat] },
    })),
  };
}

function iconSizeForZoom(z) {
  // Más grande cerca; más chico lejos, pero sin desaparecer
  if (z < 12) return 0.28;
  if (z < 13) return 0.36;
  if (z < 14) return 0.45;
  if (z < 15) return 0.55;
  return 0.65;
}

function updateZoomUi() {
  const z = map.getZoom();
  const banda = z < 13 ? "alejado" : z < 15.5 ? "ciudad" : "cercano";
  document.getElementById("zoomLabel").textContent = `Zoom ${z.toFixed(1)} · ${banda}`;
  if (map.getLayer("stops")) {
    map.setLayoutProperty("stops", "icon-size", iconSizeForZoom(z));
  }
}

function registerIcons() {
  const tasks = [];
  for (const fam of Object.keys(FAMILIES)) {
    for (const state of ["activo", "inactivo"]) {
      const id = `${fam}-${state}`;
      if (map.hasImage(id)) map.removeImage(id);
      const img = FAMILIES[fam][state]();
      const add = () => map.addImage(id, img, { pixelRatio: 2 });
      if (img.complete) add();
      else tasks.push(new Promise((res) => { img.onload = () => { add(); res(); }; }));
    }
  }
  return Promise.all(tasks);
}

function diag(...args) {
  console.log("[DIAG]", ...args);
}

function fitLayout() {
  const bar = document.querySelector(".bar");
  const mapEl = document.getElementById("map");
  const vh = window.innerHeight || document.documentElement.clientHeight;
  document.documentElement.style.height = vh + "px";
  document.body.style.height = vh + "px";
  if (bar && mapEl) {
    mapEl.style.height = Math.max(120, vh - bar.offsetHeight) + "px";
  }
  if (map && map.resize) map.resize();
}

async function init() {
  map = new maplibregl.Map({
    container: "map",
    style: {
      version: 8,
      sources: {
        osm: {
          type: "raster",
          tiles: ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
          tileSize: 256,
          attribution: "© OpenStreetMap",
        },
      },
      layers: [
        { id: "bg", type: "background", paint: { "background-color": "#10182b" } },
        { id: "osm", type: "raster", source: "osm" },
      ],
    },
    center: [AREQUIPA.lng, AREQUIPA.lat],
    zoom: 14,
    minZoom: 11,
    maxZoom: 18,
  });

  map.on("error", (e) => diag("map error:", e && e.error && e.error.message, e && e.error && e.error.url));

  map.addControl(new maplibregl.NavigationControl(), "top-right");
  await new Promise((r) => map.on("load", r));
  map.resize();
  await registerIcons();

  map.addSource("stops", { type: "geojson", data: stopsGeoJSON() });
  map.addLayer({
    id: "stops",
    type: "symbol",
    source: "stops",
    layout: {
      "icon-image": ["get", "icon"],
      "icon-size": iconSizeForZoom(map.getZoom()),
      "icon-anchor": "bottom",
      "icon-allow-overlap": true,
      "icon-ignore-placement": true,
    },
  });

  map.on("zoom", updateZoomUi);
  map.on("moveend", updateZoomUi);
  updateZoomUi();
  window.addEventListener("resize", fitLayout);
  window.addEventListener("orientationchange", () => setTimeout(fitLayout, 300));
  fitLayout();

  map.on("click", "stops", (e) => {
    const f = e.features[0];
    new maplibregl.Popup()
      .setLngLat(f.geometry.coordinates)
      .setHTML(`<strong>${f.properties.name}</strong><br/>${f.properties.state} · propuesta ${currentFamily}`)
      .addTo(map);
  });

  document.querySelectorAll(".fam").forEach((btn) => {
    btn.addEventListener("click", async () => {
      currentFamily = btn.dataset.family;
      document.querySelectorAll(".fam").forEach((b) => b.classList.toggle("active", b === btn));
      await registerIcons();
      map.getSource("stops").setData(stopsGeoJSON());
    });
  });

  document.getElementById("btnZoomOut").onclick = () => map.easeTo({ zoom: 12, duration: 600 });
  document.getElementById("btnZoomCity").onclick = () => map.easeTo({ zoom: 14, center: [AREQUIPA.lng, AREQUIPA.lat], duration: 600 });
  document.getElementById("btnZoomIn").onclick = () => map.easeTo({ zoom: 16.5, duration: 600 });
}

// Arranque: la app Android (KMP) llama a window.initApp(catálogo) al terminar de cargar la página.
setTimeout(() => {
  if (!started) {
    document.getElementById("map").innerHTML =
      '<div style="padding:1rem;color:#9aabbc;font:14px sans-serif">' +
      "Esperando el catálogo de la app (window.initApp).<br>" +
      "Esta demo se ejecuta en la app Android; el catálogo vive en el módulo KMP compartido.</div>";
  }
}, 4000);
