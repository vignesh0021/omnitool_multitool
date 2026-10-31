package com.omnitool.app.data.models

data class ChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val isCrucial: Boolean = false,
    var isChecked: Boolean = false
)

data class WiringHeightStandard(
    val country: String,
    val flagEmoji: String,
    val switchHeight: String,
    val socketHeight: String,
    val notes: String
)

data class WallMaterialTip(
    val material: String,
    val detectionDepth: String,
    val interferenceLevel: String,
    val advice: String
)

object SafetyGuideRepository {

    fun getDefaultChecklist(): List<ChecklistItem> = listOf(
        ChecklistItem(
            id = "power_off",
            title = "Kill Power at Breaker Box",
            description = "Shut off electrical breakers for the room before drilling into any wall with suspected wiring.",
            isCrucial = true
        ),
        ChecklistItem(
            id = "tap_test",
            title = "Perform Acoustic Tap Test",
            description = "Knock on the wall: hollow sound indicates cavity between studs, solid thud indicates stud or timber."
        ),
        ChecklistItem(
            id = "align_outlets",
            title = "Check Above & Below Outlets",
            description = "Electrical wiring almost always runs vertically up or down from light switches and wall sockets.",
            isCrucial = true
        ),
        ChecklistItem(
            id = "water_mains",
            title = "Identify Water Supply Lines",
            description = "Bathrooms and kitchens have copper or PEX pipes running behind sinks, showers, and toilets."
        ),
        ChecklistItem(
            id = "anchor_depth",
            title = "Check Screw / Drill Bit Length",
            description = "Standard drywall is 1/2\" (12.7mm) thick. Keep pilot holes shallow (under 1\" / 25mm) unless targeting a confirmed wooden stud."
        )
    )

    fun getWiringStandards(): List<WiringHeightStandard> = listOf(
        WiringHeightStandard(
            country = "USA & Canada (NEC)",
            flagEmoji = "🇺🇸 🇨🇦",
            switchHeight = "48 inches (120 cm) AFF",
            socketHeight = "12 - 16 inches (30 - 40 cm) AFF",
            notes = "Standard stud spacing is 16\" on center (sometimes 24\"). Wires usually run horizontally at 18-24\" height through drilled stud holes."
        ),
        WiringHeightStandard(
            country = "United Kingdom (BS 7671)",
            flagEmoji = "🇬🇧",
            switchHeight = "450mm - 1200mm (Building Regs Part M)",
            socketHeight = "450mm minimum from finished floor",
            notes = "Wires run in safe electrical zones (direct horizontal & vertical lines from visible accessories and within 150mm of corners/ceiling)."
        ),
        WiringHeightStandard(
            country = "Germany & EU (DIN 18015)",
            flagEmoji = "🇩🇪 🇪🇺",
            switchHeight = "105 cm or 115 cm AFF",
            socketHeight = "30 cm AFF (living areas), 115 cm (worktops)",
            notes = "Strict installation zones: 15 cm and 30 cm below ceiling or above finished floor. Vertical conduits run 15 cm from doorframes or corners."
        ),
        WiringHeightStandard(
            country = "Australia & New Zealand (AS/NZS 3000)",
            flagEmoji = "🇦🇺 🇳🇿",
            switchHeight = "900mm - 1100mm AFF",
            socketHeight = "300mm - 400mm AFF",
            notes = "Circuits are typically protected by RCDs, but always isolate circuit before drilling. Stud framing commonly 450mm or 600mm centers."
        )
    )

    fun getWallMaterialTips(): List<WallMaterialTip> = listOf(
        WallMaterialTip(
            material = "Drywall / Gypsum Board",
            detectionDepth = "Up to 5 - 8 cm (2 - 3.2 in)",
            interferenceLevel = "Low",
            advice = "Ideal for magnetic stud finding. Screws and metal drywall corners give clear localized signal spikes."
        ),
        WallMaterialTip(
            material = "Plaster & Lath",
            detectionDepth = "Up to 3 - 5 cm (1.2 - 2 in)",
            interferenceLevel = "Medium",
            advice = "Lath nails provide periodic readings. Look for consistent vertical lines of spikes indicating stud layout."
        ),
        WallMaterialTip(
            material = "Concrete & Brick",
            detectionDepth = "Up to 2 - 4 cm (0.8 - 1.5 in)",
            interferenceLevel = "High (Rebar Presence)",
            advice = "Reinforcing rebar meshes create broad magnetic fields. Zero out baseline against the wall before scanning for live AC wires."
        ),
        WallMaterialTip(
            material = "Wood Paneling",
            detectionDepth = "Up to 4 - 6 cm (1.5 - 2.4 in)",
            interferenceLevel = "Low",
            advice = "Wood causes zero magnetic interference; magnetic spikes directly reflect nails, screws, and pipes behind the wood."
        )
    )
}
