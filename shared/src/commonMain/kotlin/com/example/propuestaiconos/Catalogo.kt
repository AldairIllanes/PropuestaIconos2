package com.example.propuestaiconos

/**
 * GOW-GO-26-002 — Catálogo de propuestas de PARADEROS (KMP).
 *
 * Fuente única de verdad: paradas de Arequipa, propuestas de íconos (SVG)
 * y su serialización a JSON para inyectarla en la demo del WebView.
 * Vive en commonMain del módulo `shared`: Android (WebView) y iOS (WKWebView futuro).
 */
data class Centro(val lng: Double, val lat: Double)

data class Parada(
    val id: String,
    val lng: Double,
    val lat: Double,
    val nombre: String,
    /** "activo" | "inactivo" */
    val estado: String,
)

data class Propuesta(
    val id: String,
    val nombre: String,
    val svgActivo: String,
    val svgInactivo: String,
)

object Catalogo {

    val centro = Centro(lng = -71.5375, lat = -16.409)

    /** Puntos cerca de vías reconocibles del centro (aprox.). No son GTFS. */
    val paradas = listOf(
        Parada("p1", -71.5368, -16.3988, "Plaza de Armas", "activo"),
        Parada("p2", -71.5355, -16.4012, "Portal de Flores", "activo"),
        Parada("p3", -71.5338, -16.4045, "San Francisco", "activo"),
        Parada("p4", -71.5392, -16.4068, "Mercado San Camilo", "activo"),
        Parada("p5", -71.5415, -16.4030, "Puente Grau", "inactivo"),
        Parada("p6", -71.5448, -16.4015, "Yanahuara", "activo"),
        Parada("p7", -71.5305, -16.4088, "Av. Parra", "activo"),
        Parada("p8", -71.5280, -16.4125, "Selva Alegre", "inactivo"),
        Parada("p9", -71.5420, -16.4105, "Av. Ejército", "activo"),
        Parada("p10", -71.5350, -16.4140, "Av. Independencia", "activo"),
    )

    /** Propuestas candidatas (versión flat del diseño). Agregar aquí nuevas variantes (D, E…). */
    val propuestas = listOf(
        Propuesta("A", "Señal", svgSenalActivo, svgSenalInactivo),
        Propuesta("B", "Marquesina", svgMarquesinaActivo, svgMarquesinaInactivo),
        Propuesta("C", "Punto+P", svgPuntoPActivo, svgPuntoPInactivo),
    )

    /** JSON (una sola línea) listo para `evaluateJavascript("window.initApp(...)")`. */
    fun toJson(): String = buildString {
        append("{\"centro\":{")
        append("\"lng\":").append(centro.lng).append(",")
        append("\"lat\":").append(centro.lat)
        append("},\"propuestas\":[")
        propuestas.forEachIndexed { i, p ->
            if (i > 0) append(',')
            append("{\"id\":").append(jsonStr(p.id))
            append(",\"nombre\":").append(jsonStr(p.nombre))
            append(",\"svgActivo\":").append(jsonStr(p.svgActivo))
            append(",\"svgInactivo\":").append(jsonStr(p.svgInactivo))
            append('}')
        }
        append("],\"stops\":[")
        paradas.forEachIndexed { i, s ->
            if (i > 0) append(',')
            append("{\"id\":").append(jsonStr(s.id))
            append(",\"lng\":").append(s.lng)
            append(",\"lat\":").append(s.lat)
            append(",\"nombre\":").append(jsonStr(s.nombre))
            append(",\"estado\":").append(jsonStr(s.estado))
            append('}')
        }
        append("]}")
    }

    private fun jsonStr(v: String): String = buildString {
        append('"')
        v.forEach { c ->
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '<' -> append("\\u003c") // seguridad al inyectar como JS
                '>' -> append("\\u003e")
                else -> append(c)
            }
        }
        append('"')
    }
}

// ---------------------------------------------------------------------------
// PROPUESTA A — SEÑAL: poste + placa con bus y P
// ---------------------------------------------------------------------------

private val svgSenalActivo = """
<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">
  <ellipse cx="48" cy="91" rx="14" ry="4" fill="#0F2A3D" opacity="0.18"/>
  <rect x="44" y="44" width="8" height="44" rx="3" fill="#8FA3B8"/>
  <rect x="36" y="85" width="24" height="6" rx="3" fill="#7C8FA6"/>
  <rect x="22" y="4" width="52" height="46" rx="8" fill="#1B9DE3"/>
  <rect x="32" y="12" width="32" height="17" rx="4" fill="#FFFFFF"/>
  <rect x="35" y="15" width="7" height="7" rx="1" fill="#1B9DE3"/>
  <rect x="44.5" y="15" width="7" height="7" rx="1" fill="#1B9DE3"/>
  <rect x="54" y="15" width="7" height="7" rx="1" fill="#1B9DE3"/>
  <circle cx="39" cy="30" r="2.5" fill="#0B3A57"/>
  <circle cx="57" cy="30" r="2.5" fill="#0B3A57"/>
  <text x="48" y="46" text-anchor="middle" font-size="17" font-weight="800" font-family="sans-serif" fill="#FFFFFF">P</text>
</svg>
""".trimIndent()

private val svgSenalInactivo = """
<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">
  <ellipse cx="48" cy="91" rx="14" ry="4" fill="#0F2A3D" opacity="0.14"/>
  <rect x="44" y="44" width="8" height="44" rx="3" fill="#C62828"/>
  <rect x="36" y="85" width="24" height="6" rx="3" fill="#8E1616"/>
  <rect x="22" y="4" width="52" height="46" rx="8" fill="#C62828"/>
  <rect x="32" y="12" width="32" height="17" rx="4" fill="#E2E8F0"/>
  <rect x="35" y="15" width="7" height="7" rx="1" fill="#C62828"/>
  <rect x="44.5" y="15" width="7" height="7" rx="1" fill="#C62828"/>
  <rect x="54" y="15" width="7" height="7" rx="1" fill="#C62828"/>
  <circle cx="39" cy="30" r="2.5" fill="#6B0F0F"/>
  <circle cx="57" cy="30" r="2.5" fill="#6B0F0F"/>
  <text x="48" y="46" text-anchor="middle" font-size="17" font-weight="800" font-family="sans-serif" fill="#FFFFFF">P</text>
</svg>
""".trimIndent()

// ---------------------------------------------------------------------------
// PROPUESTA B — MARQUESINA: refugio con banco y panel de información
// ---------------------------------------------------------------------------

private val svgMarquesinaActivo = """
<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">
  <ellipse cx="48" cy="91" rx="30" ry="4" fill="#0F2A3D" opacity="0.18"/>
  <path d="M8 30 L48 13 L88 30 L88 38 L8 38 Z" fill="#1B9DE3"/>
  <rect x="30" y="40" width="10" height="20" rx="2" fill="#0E7FB8"/>
  <rect x="14" y="38" width="7" height="48" fill="#8FA3B8"/>
  <rect x="75" y="38" width="7" height="48" fill="#8FA3B8"/>
  <rect x="30" y="62" width="38" height="6" rx="2" fill="#1B9DE3"/>
  <rect x="30" y="52" width="38" height="4" rx="2" fill="#0E7FB8"/>
  <rect x="32" y="68" width="5" height="18" fill="#8FA3B8"/>
  <rect x="61" y="68" width="5" height="18" fill="#8FA3B8"/>
  <rect x="10" y="85" width="15" height="5" rx="2" fill="#7C8FA6"/>
  <rect x="71" y="85" width="15" height="5" rx="2" fill="#7C8FA6"/>
</svg>
""".trimIndent()

private val svgMarquesinaInactivo = """
<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">
  <ellipse cx="48" cy="91" rx="30" ry="4" fill="#0F2A3D" opacity="0.14"/>
  <path d="M8 30 L48 13 L88 30 L88 38 L8 38 Z" fill="#C62828"/>
  <rect x="30" y="40" width="10" height="20" rx="2" fill="#8E1616"/>
  <rect x="14" y="38" width="7" height="48" fill="#C62828"/>
  <rect x="75" y="38" width="7" height="48" fill="#C62828"/>
  <rect x="30" y="62" width="38" height="6" rx="2" fill="#C62828"/>
  <rect x="30" y="52" width="38" height="4" rx="2" fill="#8E1616"/>
  <rect x="32" y="68" width="5" height="18" fill="#C62828"/>
  <rect x="61" y="68" width="5" height="18" fill="#C62828"/>
  <rect x="10" y="85" width="15" height="5" rx="2" fill="#8E1616"/>
  <rect x="71" y="85" width="15" height="5" rx="2" fill="#8E1616"/>
</svg>
""".trimIndent()

// ---------------------------------------------------------------------------
// PROPUESTA C — PUNTO + P: pin de mapa con P (legible al alejar)
// ---------------------------------------------------------------------------

private val svgPuntoPActivo = """
<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">
  <ellipse cx="48" cy="90" rx="12" ry="4" fill="#0F2A3D" opacity="0.18"/>
  <path d="M48 4 C30 4 15 18 15 36 C15 56 48 86 48 86 C48 86 81 56 81 36 C81 18 66 4 48 4 Z" fill="#1B9DE3"/>
  <circle cx="48" cy="35" r="19" fill="#0E3A54"/>
  <text x="48" y="44" text-anchor="middle" font-size="26" font-weight="800" font-family="sans-serif" fill="#FFFFFF">P</text>
</svg>
""".trimIndent()

private val svgPuntoPInactivo = """
<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">
  <ellipse cx="48" cy="90" rx="12" ry="4" fill="#0F2A3D" opacity="0.14"/>
  <path d="M48 4 C30 4 15 18 15 36 C15 56 48 86 48 86 C48 86 81 56 81 36 C81 18 66 4 48 4 Z" fill="#C62828"/>
  <circle cx="48" cy="35" r="19" fill="#6B0F0F"/>
  <text x="48" y="44" text-anchor="middle" font-size="26" font-weight="800" font-family="sans-serif" fill="#FFFFFF">P</text>
</svg>
""".trimIndent()
