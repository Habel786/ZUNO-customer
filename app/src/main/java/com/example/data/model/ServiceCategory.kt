package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class ServiceCategory(
    val id: String,
    val name: String,
    val description: String,
    val defaultRate: Double,
    val iconName: String,
    val popular: Boolean = false
) {
    companion object {
        val ALL_SERVICES = listOf(
            ServiceCategory(
                id = "general_labour",
                name = "General Labour",
                description = "Loading, unloading, lifting, shifting & general site work",
                defaultRate = 500.0,
                iconName = "Handyman",
                popular = true
            ),
            ServiceCategory(
                id = "carpenter",
                name = "Carpenter",
                description = "Furniture repairs, woodwork, fittings & modular assembly",
                defaultRate = 650.0,
                iconName = "Construction",
                popular = true
            ),
            ServiceCategory(
                id = "plumber",
                name = "Plumber",
                description = "Pipe fitting, leakage fixes, tap installation & drainage",
                defaultRate = 600.0,
                iconName = "Plumbing",
                popular = true
            ),
            ServiceCategory(
                id = "electrician",
                name = "Electrician",
                description = "Wiring, switches, fan/appliance installations & short circuits",
                defaultRate = 600.0,
                iconName = "Bolt",
                popular = true
            ),
            ServiceCategory(
                id = "painter",
                name = "Painter",
                description = "Interior/exterior wall painting, waterproofing & touch-ups",
                defaultRate = 700.0,
                iconName = "FormatPaint",
                popular = false
            ),
            ServiceCategory(
                id = "mason",
                name = "Mason",
                description = "Brickwork, tile laying, plastering & cement construction",
                defaultRate = 750.0,
                iconName = "HomeRepairService",
                popular = false
            ),
            ServiceCategory(
                id = "cleaner",
                name = "Cleaner",
                description = "Deep home cleaning, sofa/carpet wash & post-construction clean",
                defaultRate = 450.0,
                iconName = "CleaningServices",
                popular = true
            ),
            ServiceCategory(
                id = "driver",
                name = "Driver",
                description = "Commercial/private car, van, tempo & emergency driver services",
                defaultRate = 550.0,
                iconName = "DirectionsCar",
                popular = false
            )
        )

        fun findByName(name: String): ServiceCategory? {
            return ALL_SERVICES.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}
