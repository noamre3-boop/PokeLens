package com.noam.pokelens

data class Mon(val num: Int, val name: String, val types: List<String>)

object PokemonDb {
    // שורה = מספר בפוקידקס (לפי הסדר). פורמט: שם,טיפוס1[,טיפוס2]
    private const val RAW = """Bulbasaur,Grass,Poison
Ivysaur,Grass,Poison
Venusaur,Grass,Poison
Charmander,Fire
Charmeleon,Fire
Charizard,Fire,Flying
Squirtle,Water
Wartortle,Water
Blastoise,Water
Caterpie,Bug
Metapod,Bug
Butterfree,Bug,Flying
Weedle,Bug,Poison
Kakuna,Bug,Poison
Beedrill,Bug,Poison
Pidgey,Normal,Flying
Pidgeotto,Normal,Flying
Pidgeot,Normal,Flying
Rattata,Normal
Raticate,Normal
Spearow,Normal,Flying
Fearow,Normal,Flying
Ekans,Poison
Arbok,Poison
Pikachu,Electric
Raichu,Electric
Sandshrew,Ground
Sandslash,Ground
Nidoran♀,Poison
Nidorina,Poison
Nidoqueen,Poison,Ground
Nidoran♂,Poison
Nidorino,Poison
Nidoking,Poison,Ground
Clefairy,Fairy
Clefable,Fairy
Vulpix,Fire
Ninetales,Fire
Jigglypuff,Normal,Fairy
Wigglytuff,Normal,Fairy
Zubat,Poison,Flying
Golbat,Poison,Flying
Oddish,Grass,Poison
Gloom,Grass,Poison
Vileplume,Grass,Poison
Paras,Bug,Grass
Parasect,Bug,Grass
Venonat,Bug,Poison
Venomoth,Bug,Poison
Diglett,Ground
Dugtrio,Ground
Meowth,Normal
Persian,Normal
Psyduck,Water
Golduck,Water
Mankey,Fighting
Primeape,Fighting
Growlithe,Fire
Arcanine,Fire
Poliwag,Water
Poliwhirl,Water
Poliwrath,Water,Fighting
Abra,Psychic
Kadabra,Psychic
Alakazam,Psychic
Machop,Fighting
Machoke,Fighting
Machamp,Fighting
Bellsprout,Grass,Poison
Weepinbell,Grass,Poison
Victreebel,Grass,Poison
Tentacool,Water,Poison
Tentacruel,Water,Poison
Geodude,Rock,Ground
Graveler,Rock,Ground
Golem,Rock,Ground
Ponyta,Fire
Rapidash,Fire
Slowpoke,Water,Psychic
Slowbro,Water,Psychic
Magnemite,Electric,Steel
Magneton,Electric,Steel
Farfetch'd,Normal,Flying
Doduo,Normal,Flying
Dodrio,Normal,Flying
Seel,Water
Dewgong,Water,Ice
Grimer,Poison
Muk,Poison
Shellder,Water
Cloyster,Water,Ice
Gastly,Ghost,Poison
Haunter,Ghost,Poison
Gengar,Ghost,Poison
Onix,Rock,Ground
Drowzee,Psychic
Hypno,Psychic
Krabby,Water
Kingler,Water
Voltorb,Electric
Electrode,Electric
Exeggcute,Grass,Psychic
Exeggutor,Grass,Psychic
Cubone,Ground
Marowak,Ground
Hitmonlee,Fighting
Hitmonchan,Fighting
Lickitung,Normal
Koffing,Poison
Weezing,Poison
Rhyhorn,Ground,Rock
Rhydon,Ground,Rock
Chansey,Normal
Tangela,Grass
Kangaskhan,Normal
Horsea,Water
Seadra,Water
Goldeen,Water
Seaking,Water
Staryu,Water
Starmie,Water,Psychic
Mr. Mime,Psychic,Fairy
Scyther,Bug,Flying
Jynx,Ice,Psychic
Electabuzz,Electric
Magmar,Fire
Pinsir,Bug
Tauros,Normal
Magikarp,Water
Gyarados,Water,Flying
Lapras,Water,Ice
Ditto,Normal
Eevee,Normal
Vaporeon,Water
Jolteon,Electric
Flareon,Fire
Porygon,Normal
Omanyte,Rock,Water
Omastar,Rock,Water
Kabuto,Rock,Water
Kabutops,Rock,Water
Aerodactyl,Rock,Flying
Snorlax,Normal
Articuno,Ice,Flying
Zapdos,Electric,Flying
Moltres,Fire,Flying
Dratini,Dragon
Dragonair,Dragon
Dragonite,Dragon,Flying
Mewtwo,Psychic
Mew,Psychic
Chikorita,Grass
Bayleef,Grass
Meganium,Grass
Cyndaquil,Fire
Quilava,Fire
Typhlosion,Fire
Totodile,Water
Croconaw,Water
Feraligatr,Water
Sentret,Normal
Furret,Normal
Hoothoot,Normal,Flying
Noctowl,Normal,Flying
Ledyba,Bug,Flying
Ledian,Bug,Flying
Spinarak,Bug,Poison
Ariados,Bug,Poison
Crobat,Poison,Flying
Chinchou,Water,Electric
Lanturn,Water,Electric
Pichu,Electric
Cleffa,Fairy
Igglybuff,Normal,Fairy
Togepi,Fairy
Togetic,Fairy,Flying
Natu,Psychic,Flying
Xatu,Psychic,Flying
Mareep,Electric
Flaaffy,Electric
Ampharos,Electric
Bellossom,Grass
Marill,Water,Fairy
Azumarill,Water,Fairy
Sudowoodo,Rock
Politoed,Water
Hoppip,Grass,Flying
Skiploom,Grass,Flying
Jumpluff,Grass,Flying
Aipom,Normal
Sunkern,Grass
Sunflora,Grass
Yanma,Bug,Flying
Wooper,Water,Ground
Quagsire,Water,Ground
Espeon,Psychic
Umbreon,Dark
Murkrow,Dark,Flying
Slowking,Water,Psychic
Misdreavus,Ghost
Unown,Psychic
Wobbuffet,Psychic
Girafarig,Normal,Psychic
Pineco,Bug
Forretress,Bug,Steel
Dunsparce,Normal
Gligar,Ground,Flying
Steelix,Steel,Ground
Snubbull,Fairy
Granbull,Fairy
Qwilfish,Water,Poison
Scizor,Bug,Steel
Shuckle,Bug,Rock
Heracross,Bug,Fighting
Sneasel,Dark,Ice
Teddiursa,Normal
Ursaring,Normal
Slugma,Fire
Magcargo,Fire,Rock
Swinub,Ice,Ground
Piloswine,Ice,Ground
Corsola,Water,Rock
Remoraid,Water
Octillery,Water
Delibird,Ice,Flying
Mantine,Water,Flying
Skarmory,Steel,Flying
Houndour,Dark,Fire
Houndoom,Dark,Fire
Kingdra,Water,Dragon
Phanpy,Ground
Donphan,Ground
Porygon2,Normal
Stantler,Normal
Smeargle,Normal
Tyrogue,Fighting
Hitmontop,Fighting
Smoochum,Ice,Psychic
Elekid,Electric
Magby,Fire
Miltank,Normal
Blissey,Normal
Raikou,Electric
Entei,Fire
Suicune,Water
Larvitar,Rock,Ground
Pupitar,Rock,Ground
Tyranitar,Rock,Dark
Lugia,Psychic,Flying
Ho-Oh,Fire,Flying
Celebi,Psychic,Grass"""

    val all: List<Mon> by lazy {
        RAW.trim().lines().mapIndexed { i, line ->
            val p = line.trim().split(",")
            Mon(i + 1, p[0], p.drop(1))
        }
    }
    private val byKey: Map<String, Mon> by lazy { all.associateBy { norm(it.name) } }

    fun norm(s: String): String =
        s.replace("♀", "f").replace("♂", "m").lowercase().filter { it in 'a'..'z' || it in '0'..'9' }

    /** מחזיר פוקימון אם הטקסט (שורה מה-OCR) הוא שם של פוקימון, כולל סבילות לשגיאות OCR קטנות */
    fun match(text: String): Mon? {
        val candidates = mutableListOf(text)
        candidates += text.split(' ', '\t')
        for (c in candidates) {
            val n = norm(c)
            if (n.length < 3) continue
            byKey[n]?.let { return it }
        }
        for (c in candidates) {
            val n = norm(c)
            if (n.length < 5) continue
            val maxD = if (n.length >= 8) 2 else 1
            var best: Mon? = null
            var bestD = maxD + 1
            for ((k, m) in byKey) {
                if (kotlin.math.abs(k.length - n.length) > maxD) continue
                val d = lev(k, n, bestD)
                if (d < bestD) { bestD = d; best = m }
            }
            if (best != null) return best
        }
        return null
    }

    private fun lev(a: String, b: String, cap: Int): Int {
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            cur[0] = i
            var rowMin = cur[0]
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost)
                if (cur[j] < rowMin) rowMin = cur[j]
            }
            if (rowMin >= cap) return cap
            val t = prev; prev = cur; cur = t
        }
        return prev[b.length]
    }

    // ---------- טבלת יעילות טיפוסים (מכפילי Pokémon GO) ----------
    val TYPES = listOf("Normal","Fire","Water","Electric","Grass","Ice","Fighting","Poison","Ground",
        "Flying","Psychic","Bug","Rock","Ghost","Dragon","Dark","Steel","Fairy")

    val HE = mapOf(
        "Normal" to "⚪ רגיל", "Fire" to "🔥 אש", "Water" to "💧 מים", "Electric" to "⚡ חשמל",
        "Grass" to "🌿 דשא", "Ice" to "❄️ קרח", "Fighting" to "🥊 לחימה", "Poison" to "☠️ רעל",
        "Ground" to "⛰️ אדמה", "Flying" to "🪶 מעופף", "Psychic" to "🔮 על-חושי", "Bug" to "🐛 חרק",
        "Rock" to "🪨 סלע", "Ghost" to "👻 רוח", "Dragon" to "🐉 דרקון", "Dark" to "🌑 אופל",
        "Steel" to "⚙️ פלדה", "Fairy" to "🧚 פיה")

    // תוקף -> (חזק נגד, חלש נגד, ללא השפעה)
    private val CHART: Map<String, Triple<Set<String>, Set<String>, Set<String>>> = mapOf(
        "Normal" to Triple(setOf(), setOf("Rock","Steel"), setOf("Ghost")),
        "Fire" to Triple(setOf("Grass","Ice","Bug","Steel"), setOf("Fire","Water","Rock","Dragon"), setOf()),
        "Water" to Triple(setOf("Fire","Ground","Rock"), setOf("Water","Grass","Dragon"), setOf()),
        "Electric" to Triple(setOf("Water","Flying"), setOf("Electric","Grass","Dragon"), setOf("Ground")),
        "Grass" to Triple(setOf("Water","Ground","Rock"), setOf("Fire","Grass","Poison","Flying","Bug","Dragon","Steel"), setOf()),
        "Ice" to Triple(setOf("Grass","Ground","Flying","Dragon"), setOf("Fire","Water","Ice","Steel"), setOf()),
        "Fighting" to Triple(setOf("Normal","Ice","Rock","Dark","Steel"), setOf("Poison","Flying","Psychic","Bug","Fairy"), setOf("Ghost")),
        "Poison" to Triple(setOf("Grass","Fairy"), setOf("Poison","Ground","Rock","Ghost"), setOf("Steel")),
        "Ground" to Triple(setOf("Fire","Electric","Poison","Rock","Steel"), setOf("Grass","Bug"), setOf("Flying")),
        "Flying" to Triple(setOf("Grass","Fighting","Bug"), setOf("Electric","Rock","Steel"), setOf()),
        "Psychic" to Triple(setOf("Fighting","Poison"), setOf("Psychic","Steel"), setOf("Dark")),
        "Bug" to Triple(setOf("Grass","Psychic","Dark"), setOf("Fire","Fighting","Poison","Flying","Ghost","Steel","Fairy"), setOf()),
        "Rock" to Triple(setOf("Fire","Ice","Flying","Bug"), setOf("Fighting","Ground","Steel"), setOf()),
        "Ghost" to Triple(setOf("Psychic","Ghost"), setOf("Dark"), setOf("Normal")),
        "Dragon" to Triple(setOf("Dragon"), setOf("Steel"), setOf("Fairy")),
        "Dark" to Triple(setOf("Psychic","Ghost"), setOf("Fighting","Dark","Fairy"), setOf()),
        "Steel" to Triple(setOf("Ice","Rock","Fairy"), setOf("Fire","Water","Electric","Steel"), setOf()),
        "Fairy" to Triple(setOf("Fighting","Dragon","Dark"), setOf("Fire","Poison","Steel"), setOf())
    )

    fun multiplier(attack: String, defTypes: List<String>): Double {
        val (se, nve, imm) = CHART.getValue(attack)
        var m = 1.0
        for (d in defTypes) {
            m *= when (d) { in se -> 1.6; in nve -> 0.625; in imm -> 0.390625; else -> 1.0 }
        }
        return m
    }

    /** חולשות: רשימת (טיפוס תוקף, מכפיל) ממוינת מהחזק לחלש */
    fun weaknesses(mon: Mon): List<Pair<String, Double>> =
        TYPES.map { it to multiplier(it, mon.types) }.filter { it.second > 1.01 }.sortedByDescending { it.second }
}
