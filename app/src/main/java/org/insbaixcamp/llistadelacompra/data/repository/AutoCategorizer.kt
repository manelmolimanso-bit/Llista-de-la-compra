package org.insbaixcamp.llistadelacompra.data.repository

import org.insbaixcamp.llistadelacompra.data.model.Category
import org.insbaixcamp.llistadelacompra.data.model.PredefinedProduct

object AutoCategorizer {

    val DEFAULT_CATEGORIES = listOf(
        Category(id = "lactics", name = "Làctics i Ous", colorHex = "#FFC107", iconName = "egg"),
        Category(id = "fruita_verdura", name = "Fruita i Verdura", colorHex = "#4CAF50", iconName = "eco"),
        Category(id = "carn_peix", name = "Carn i Peix", colorHex = "#F44336", iconName = "restaurant"),
        Category(id = "pa_reposteria", name = "Pa i Reposteria", colorHex = "#FF9800", iconName = "bakery_dining"),
        Category(id = "begudes", name = "Begudes", colorHex = "#2196F3", iconName = "local_drink"),
        Category(id = "neteja_llar", name = "Neteja i Llar", colorHex = "#00BCD4", iconName = "cleaning_services"),
        Category(id = "altres", name = "Altres", colorHex = "#9E9E9E", iconName = "category")
    )

    val PREDEFINED_PRODUCTS = listOf(
        PredefinedProduct("Llet", "Làctics i Ous", "Bonpreu"),
        PredefinedProduct("Formatge", "Làctics i Ous", "Mercadona"),
        PredefinedProduct("Iogurt", "Làctics i Ous"),
        PredefinedProduct("Ous", "Làctics i Ous"),
        PredefinedProduct("Mantega", "Làctics i Ous"),
        PredefinedProduct("Poma", "Fruita i Verdura", "Km0"),
        PredefinedProduct("Plàtan", "Fruita i Verdura"),
        PredefinedProduct("Tomàquet", "Fruita i Verdura"),
        PredefinedProduct("Patates", "Fruita i Verdura"),
        PredefinedProduct("Ceba", "Fruita i Verdura"),
        PredefinedProduct("Pollastre", "Carn i Peix"),
        PredefinedProduct("Vedella", "Carn i Peix"),
        PredefinedProduct("Hamburguesa", "Carn i Peix"),
        PredefinedProduct("Salmó", "Carn i Peix"),
        PredefinedProduct("Lluç", "Carn i Peix"),
        PredefinedProduct("Pa de pagès", "Pa i Reposteria"),
        PredefinedProduct("Croissant", "Pa i Reposteria"),
        PredefinedProduct("Galetes", "Pa i Reposteria"),
        PredefinedProduct("Aigua", "Begudes"),
        PredefinedProduct("Suc d'taronja", "Begudes"),
        PredefinedProduct("Cervesa", "Begudes"),
        PredefinedProduct("Vi", "Begudes"),
        PredefinedProduct("Detergent", "Neteja i Llar"),
        PredefinedProduct("Papel higiènic", "Neteja i Llar"),
        PredefinedProduct("Sabó de mans", "Neteja i Llar"),
        PredefinedProduct("Xampú", "Neteja i Llar")
    )

    private val KEYWORD_TO_CATEGORY_NAME = mapOf(
        // Làctics i Ous
        "llet" to "Làctics i Ous", "leche" to "Làctics i Ous",
        "formatge" to "Làctics i Ous", "queso" to "Làctics i Ous",
        "iogurt" to "Làctics i Ous", "yogur" to "Làctics i Ous",
        "ous" to "Làctics i Ous", "huevo" to "Làctics i Ous", "huevos" to "Làctics i Ous",
        "mantega" to "Làctics i Ous", "mantequilla" to "Làctics i Ous",
        "nata" to "Làctics i Ous",

        // Fruita i Verdura
        "poma" to "Fruita i Verdura", "manzana" to "Fruita i Verdura",
        "plàtan" to "Fruita i Verdura", "platan" to "Fruita i Verdura", "platano" to "Fruita i Verdura",
        "taronja" to "Fruita i Verdura", "naranja" to "Fruita i Verdura",
        "tomàquet" to "Fruita i Verdura", "tomaquet" to "Fruita i Verdura", "tomate" to "Fruita i Verdura",
        "patata" to "Fruita i Verdura", "patates" to "Fruita i Verdura",
        "ceba" to "Fruita i Verdura", "cebolla" to "Fruita i Verdura",
        "enciam" to "Fruita i Verdura", "lechuga" to "Fruita i Verdura",
        "llimona" to "Fruita i Verdura", "limon" to "Fruita i Verdura",
        "maduixa" to "Fruita i Verdura", "fresa" to "Fruita i Verdura",
        "alvocat" to "Fruita i Verdura", "aguacate" to "Fruita i Verdura",

        // Carn i Peix
        "pollastre" to "Carn i Peix", "pollo" to "Carn i Peix",
        "vedella" to "Carn i Peix", "ternera" to "Carn i Peix",
        "porc" to "Carn i Peix", "cerdo" to "Carn i Peix",
        "hamburguesa" to "Carn i Peix",
        "salsitxes" to "Carn i Peix", "salchichas" to "Carn i Peix",
        "salmó" to "Carn i Peix", "salmon" to "Carn i Peix",
        "lluç" to "Carn i Peix", "merluza" to "Carn i Peix",
        "tonyina" to "Carn i Peix", "atun" to "Carn i Peix",
        "gamba" to "Carn i Peix", "gambes" to "Carn i Peix", "gambas" to "Carn i Peix",
        "bacallà" to "Carn i Peix", "bacalao" to "Carn i Peix",

        // Pa i Reposteria
        "pa" to "Pa i Reposteria", "pan" to "Pa i Reposteria",
        "croissant" to "Pa i Reposteria",
        "magdalenes" to "Pa i Reposteria", "magdalenas" to "Pa i Reposteria",
        "galetes" to "Pa i Reposteria", "galletas" to "Pa i Reposteria",
        "farina" to "Pa i Reposteria", "harina" to "Pa i Reposteria",

        // Begudes
        "aigua" to "Begudes", "agua" to "Begudes",
        "suc" to "Begudes", "zumo" to "Begudes",
        "cervesa" to "Begudes", "cerveza" to "Begudes",
        "vi" to "Begudes", "vino" to "Begudes",
        "refresc" to "Begudes", "refresco" to "Begudes",
        "coca" to "Begudes", "cocacola" to "Begudes",
        "cafè" to "Begudes", "cafe" to "Begudes",
        "te" to "Begudes", "té" to "Begudes",

        // Neteja i Llar
        "detergent" to "Neteja i Llar", "detergente" to "Neteja i Llar",
        "sabó" to "Neteja i Llar", "sabo" to "Neteja i Llar", "jabon" to "Neteja i Llar",
        "papel" to "Neteja i Llar", "paper" to "Neteja i Llar",
        "xampú" to "Neteja i Llar", "xampu" to "Neteja i Llar", "champu" to "Neteja i Llar",
        "lleixiu" to "Neteja i Llar", "lejia" to "Neteja i Llar",
        "fregall" to "Neteja i Llar", "estropajo" to "Neteja i Llar"
    )

    /**
     * Tries to find the matching category ID from list categories given a typed product name.
     */
    fun findSuggestedCategory(productName: String, availableCategories: List<Category>): Category? {
        val cleanInput = productName.trim().lowercase()
        if (cleanInput.isEmpty()) return null

        // 1. Direct keyword lookup
        for ((keyword, targetCategoryName) in KEYWORD_TO_CATEGORY_NAME) {
            if (cleanInput.contains(keyword)) {
                val match = availableCategories.firstOrNull { it.name.equals(targetCategoryName, ignoreCase = true) }
                if (match != null) return match
            }
        }

        return null
    }
}
