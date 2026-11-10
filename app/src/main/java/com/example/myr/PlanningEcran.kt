package com.example.myr


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val PlanningAccent = Color(0xFFE57373)


// TODO : à revoir


/**
 * Définit les moments de la journée pour lesquels on peut assigner un repas.
 */
enum class Creneau(val label: String, val court: String, val heure: String) {
    PetitDej("Petit-déjeuner", "Matin", "8h00"),
    Dejeuner("Déjeuner", "Midi", "12h30"),
    Diner("Dîner", "Soir", "19h30")
}


/**
 * Définit les différents niveaux de visualisation du calendrier.
 */
enum class VuePlanning(val label: String) {
    Jour("Jour"), Semaine("Semaine"), Mois("Mois")
}


/**
 * Représente la donnée pure d'une recette dans le contexte du planning.
 *
 * @property platBatchId Si non null, indique que cette recette n'est pas à cuisiner
 * de zéro, mais est piochée dans les restes du stock (Batch Cooking).
 */
data class RecettePlan(
    val nom: String,
    val duree: String,
    val couleur: Color,
    val imageRes: Int,
    val platBatchId: Long? = null
) {
    val estBatch get() = platBatchId != null
}


/**
 * L'association d'une recette et du nombre de personnes prévu pour un créneau donné.
 */
data class RepasPlanifie(
    val recette: RecettePlan,
    val portions: Int = 4
)


/**
 * Modèle interne (State) gardant en mémoire quelle case de la grille a été cliquée
 * pour ouvrir la boîte de dialogue d'ajout de recette.
 */
private data class Cellule(val dateMs: Long, val creneau: Creneau)

val recettesPlanDemo = listOf(
    RecettePlan("Mexican Egg Mix", "15 min", Color(0xFFE0EFFF), android.R.drawable.ic_menu_gallery),
    RecettePlan("Italian Baked", "20 min", Color(0xFFFFF2D0), android.R.drawable.ic_menu_gallery),
    RecettePlan("Crêpes orange et miel", "35 min", Color(0xFFFFE3E0), android.R.drawable.ic_menu_gallery)
)



/** Convertisseur utilitaire transformant un objet [PlatBatch] (du stock) en [RecettePlan] */
private fun PlatBatch.versRecette() =
    RecettePlan(nom, "Restes", stockage.couleur, android.R.drawable.ic_menu_gallery, id)


private val fmtCle = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val fmtJourLong = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)
private val fmtCourt = SimpleDateFormat("d MMM", Locale.FRANCE)
private val fmtMois = SimpleDateFormat("MMMM", Locale.FRANCE)
private val fmtJourCourt = SimpleDateFormat("EEE", Locale.FRANCE)

private fun String.cap() = replaceFirstChar { it.uppercase() }


/**
 * Initialise un objet [Calendar] standardisé pour la France.
 * Indispensable pour éviter que la semaine ne commence le dimanche.
 */
private fun cal(ms: Long): Calendar = Calendar.getInstance(Locale.FRANCE).apply {
    firstDayOfWeek = Calendar.MONDAY
    minimalDaysInFirstWeek = 4
    timeInMillis = ms
}


/**
 * Le format de clé "yyyy-MM-dd" permet d'utiliser une Map classique
 * pour retrouver instantanément un repas sans avoir à faire de boucle complexe.
 */
private fun cleJour(date: Calendar): String = fmtCle.format(date.time)


/** Construit l'identifiant unique d'une case du planning (ex: "2024-11-20|Diner") */
private fun cleRepas(date: Calendar, creneau: Creneau): String = "${cleJour(date)}|${creneau.name}"


/** Retourne une liste de 7 calendriers correspondant à la semaine du timestamp fourni. */
private fun jourDeLaSemaine(ms: Long): List<Calendar> {
    val lundi = cal(ms).apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
    return List(7) { i -> (lundi.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) } }
}


/**
 * Calcule l'intégralité des dates nécessaires pour remplir la grille du mois.
 * S'il y a un décalage, il inclut les derniers jours du mois précédent et les premiers
 * jours du mois suivant pour garantir des lignes complètes de 7 jours (lundi à dimanche).
 */
private fun grilleDuMois(ms: Long): List<Calendar> {
    val premier = cal(ms).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val nbJours = premier.getActualMaximum(Calendar.DAY_OF_MONTH)
    val decalage = (premier.get(Calendar.DAY_OF_WEEK) + 5) % 7   // lundi = 0
    val nbSemaines = (decalage + nbJours + 6) / 7
    val debut = jourDeLaSemaine(premier.timeInMillis).first()
    return List(nbSemaines * 7) { i -> (debut.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) } }
}


/** Modifie le timestamp actuel en y ajoutant/retirant une unité (Jour, Semaine, Mois) */
private fun deplacer(ms: Long, vue: VuePlanning, sens: Int): Long = cal(ms).apply {
    when (vue) {
        VuePlanning.Jour -> add(Calendar.DAY_OF_YEAR, sens)
        VuePlanning.Semaine -> add(Calendar.WEEK_OF_YEAR, sens)
        VuePlanning.Mois -> add(Calendar.MONTH, sens)
    }
}.timeInMillis


/** Génère les deux lignes de texte (titre + sous-titre) affichées en haut de l'écran */
private fun libelles(vue: VuePlanning, ms: Long): Pair<String, String> {
    val c = cal(ms)
    val annee = c.get(Calendar.YEAR).toString()
    return when (vue) {
        VuePlanning.Jour -> fmtJourLong.format(c.time).cap() to annee
        VuePlanning.Semaine -> {
            val j = jourDeLaSemaine(ms)
            "${fmtCourt.format(j.first().time)} – ${fmtCourt.format(j.last().time)}" to
                    "Semaine ${c.get(Calendar.WEEK_OF_YEAR)} · $annee"
        }
        VuePlanning.Mois -> fmtMois.format(c.time).cap() to annee
    }
}



/**
 * Écran **Stateful** gérant le calendrier des repas.
 *
 * **Règle d'architecture métier :**
 * Cet écran possède une dépendance forte avec le stock (`PlatBatch`). Lorsqu'un plat du stock
 * est assigné au planning, l'écran calcule automatiquement si les portions suffisent.
 * Lorsque l'utilisateur coche un plat du batch comme "Mangé", les portions sont
 * physiquement déduites du stock (ou le plat est supprimé s'il est vide).
 *
 * @param stock La liste centralisée des plats (doit être la même instance que dans `StockBatchEcran`).
 * @param onGenererCourses Déclenché par la carte "Récapitulatif" pour créer la liste de courses.
 * @param onOuvrirStock Déclenché par le bandeau d'alerte pour consulter le frigo complet.
 */
@Composable
@Preview
fun PlanningEcran(
    stock: SnapshotStateList<PlatBatch> = remember { platsBatchDemo() },
    onGenererCourses: () -> Unit = {},
    onOuvrirStock: () -> Unit = {}
) {
    val planning = remember { mutableStateMapOf<String, RepasPlanifie>() }
    var vue by remember { mutableStateOf(VuePlanning.Semaine) }
    var dateRef by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var cellule by remember { mutableStateOf<Cellule?>(null) }

    val aujourdhui = cleJour(cal(System.currentTimeMillis()))
    val (titre, sousTitre) = libelles(vue, dateRef)

    // Jours de la période affichée
    val joursPeriode = when (vue) {
        VuePlanning.Jour -> listOf(cal(dateRef))
        VuePlanning.Semaine -> jourDeLaSemaine(dateRef)
        VuePlanning.Mois -> grilleDuMois(dateRef)
            .filter { it.get(Calendar.MONTH) == cal(dateRef).get(Calendar.MONTH) }
    }
    val repasPeriode = joursPeriode.flatMap { j -> Creneau.entries.mapNotNull { planning[cleRepas(j, it)] } }

    // Plats du stock à consommer rapidement (périmés ou dans les 2 jours)
    val urgents = stock.filter { it.joursRestants() <= 2 }.sortedBy { it.joursRestants() }

    // Plats qui se périment, regroupés par jour (clé yyyy-MM-dd) pour les marqueurs du calendrier
    val expirations = stock.groupBy { cleJour(cal(it.dateExpirationMs())) }

    // Portions encore disponibles pour un plat du stock
    fun disponibles(platId: Long, sauf: String?): Int {
        val plat = stock.firstOrNull { it.id == platId } ?: return 0
        return plat.portionsRestantes - portionsReservees(planning, platId, sauf)
    }

    // Change le nombre de personnes
    fun changerPortions(cle: String, n: Int) {
        val r = planning[cle] ?: return
        val max = r.recette.platBatchId?.let { disponibles(it, cle) }
        planning[cle] = r.copy(portions = if (max != null) n.coerceAtMost(max).coerceAtLeast(1) else n)
    }

    // "Mangé" : retire les portions du stock et libère le créneau
    fun marquerMange(cle: String) {
        val r = planning[cle] ?: return
        val id = r.recette.platBatchId
        if (id != null) {
            val i = stock.indexOfFirst { it.id == id }
            if (i >= 0) {
                val reste = stock[i].portionsRestantes - r.portions
                if (reste <= 0) stock.removeAt(i) else stock[i] = stock[i].copy(portionsRestantes = reste)
            }
        }
        planning.remove(cle)
    }

    val appBackground = Color(0xFFF8F9FA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(titre, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sousTitre, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Aujourd'hui",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PlanningAccent,
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, PlanningAccent.copy(alpha = 0.5f), CircleShape)
                            .clickable { dateRef = System.currentTimeMillis() }
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BoutonNav(Icons.Default.ArrowBackIosNew, "Précédent") { dateRef = deplacer(dateRef, vue, -1) }
                BoutonNav(Icons.Default.ArrowForwardIos, "Suivant") { dateRef = deplacer(dateRef, vue, 1) }
            }
        }

        SelecteurVue(
            vueActuelle = vue,
            onChange = { vue = it },
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Rappel des restes à consommer
            if (urgents.isNotEmpty()) {
                item(key = "restes") {
                    BandeauRestes(urgents = urgents, onClick = onOuvrirStock)
                }
            }

            item(key = "vue_${vue.name}") {
                when (vue) {
                    VuePlanning.Jour -> VueJour(
                        date = cal(dateRef),
                        expirants = expirations[cleJour(cal(dateRef))].orEmpty(),
                        planning = planning,
                        onAjouter = { c -> cellule = Cellule(dateRef, c) },
                        onPortions = { c, n -> changerPortions(cleRepas(cal(dateRef), c), n) },
                        onRetirer = { c -> planning.remove(cleRepas(cal(dateRef), c)) },
                        onMange = { c -> marquerMange(cleRepas(cal(dateRef), c)) }
                    )

                    VuePlanning.Semaine -> VueSemaine(
                        jours = jourDeLaSemaine(dateRef),
                        aujourdhui = aujourdhui,
                        expirations = expirations,
                        planning = planning,
                        onJour = { d -> dateRef = d.timeInMillis; vue = VuePlanning.Jour },
                        onCellule = { d, c -> cellule = Cellule(d.timeInMillis, c) }
                    )

                    VuePlanning.Mois -> VueMois(
                        grille = grilleDuMois(dateRef),
                        moisRef = cal(dateRef).get(Calendar.MONTH),
                        aujourdhui = aujourdhui,
                        expirations = expirations,
                        planning = planning,
                        onJour = { d -> dateRef = d.timeInMillis; vue = VuePlanning.Jour }
                    )
                }
            }

            // Récapitulatif de la période affichée
            item(key = "recap") {
                val libelle = when (vue) {
                    VuePlanning.Jour -> "Ce jour"
                    VuePlanning.Semaine -> "Cette semaine"
                    VuePlanning.Mois -> "Ce mois-ci"
                }

                RecapCarte(
                    libelle = libelle,
                    nbRepas = repasPeriode.size,
                    nbPortions = repasPeriode.sumOf { it.portions },
                    nbAcuisiner = repasPeriode.count { !it.recette.estBatch },
                    onGenerer = onGenererCourses
                )
            }

            item { BottomBarSpacer() }
        }
    }

    // Fenêtre d'ajout / modification d'un repas
    cellule?.let { sel ->
        val date = cal(sel.dateMs)
        val cle = cleRepas(date, sel.creneau)
        DialogRepas(
            titre = "${sel.creneau.label} · ${fmtJourLong.format(date.time).cap()}",
            repas = planning[cle],
            dateRepasMs = sel.dateMs,
            stock = stock,
            disponibles = { id -> disponibles(id, cle) },
            onChoisir = { r, max ->
                val p = planning[cle]?.portions ?: 4
                val n = if (max != null) p.coerceAtMost(max).coerceAtLeast(1) else p
                planning[cle] = RepasPlanifie(r, n)
                cellule = null
            },
            onPortions = { n -> changerPortions(cle, n) },
            onRetirer = { planning.remove(cle); cellule = null },
            onFermer = { cellule = null }
        )
    }
}



/**
 * Alerte jaune listant les 3 plats du stock les plus urgents à consommer.
 */
@Composable
private fun BandeauRestes(urgents: List<PlatBatch>, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFFFF2D0))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Kitchen, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("À consommer vite", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        urgents.take(3).forEach { p ->
            val j = p.joursRestants()
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${p.nom} · ${p.portionsRestantes} port.", fontSize = 13.sp)
                Text(
                    text = (if (j < 0) "Périmé · " else "Avant ") + dateExpirationCourte(p),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = couleurUrgence(j)
                )
            }
        }
        if (urgents.size > 3) {
            Text("+ ${urgents.size - 3} autres", fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Voir tout le stock", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PlanningAccent)
    }
}


/** Alerte rouge affichée dans la vue "Jour" pour les plats qui expirent le jour même. */
@Composable
private fun AlerteExpiration(plats: List<PlatBatch>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFD32F2F).copy(alpha = 0.10f))
            .padding(14.dp)
    ) {
        Text(
            "⚠ Dernier jour pour consommer",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )
        plats.forEach { p ->
            Text(
                "${p.nom} · ${p.portionsRestantes} portion(s) · ${p.stockage.label}",
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}




/** Vue de détail avec liste des recettes complètes. */
@Composable
private fun VueJour(
    date: Calendar,
    expirants: List<PlatBatch>,
    planning: Map<String, RepasPlanifie>,
    onAjouter: (Creneau) -> Unit,
    onPortions: (Creneau, Int) -> Unit,
    onRetirer: (Creneau) -> Unit,
    onMange: (Creneau) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        if (expirants.isNotEmpty()) AlerteExpiration(expirants)

        Creneau.entries.forEach { creneau ->
            val repas = planning[cleRepas(date, creneau)]
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(creneau.label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(creneau.heure, fontSize = 13.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (repas != null) {
                    RepasCarte(
                        repas = repas,
                        onPortions = { n -> onPortions(creneau, n) },
                        onRetirer = { onRetirer(creneau) },
                        onMange = if (repas.recette.estBatch) ({ onMange(creneau) }) else null
                    )
                } else {
                    EmplacementVide(onClick = { onAjouter(creneau) })
                }
            }
        }
    }
}


/**
 * Vue hebdomadaire.
 *
 * La grille est "renversée" : les 7 jours sont
 * des lignes horizontales, et les repas (Matin/Midi/Soir) forment 3 colonnes.
 */
@Composable
private fun VueSemaine(
    jours: List<Calendar>,
    aujourdhui: String,
    expirations: Map<String, List<PlatBatch>>,
    planning: Map<String, RepasPlanifie>,
    onJour: (Calendar) -> Unit,
    onCellule: (Calendar, Creneau) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // En-tête des colonnes
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Spacer(modifier = Modifier.width(44.dp))
            Creneau.entries.forEach { c ->
                Text(
                    text = c.court,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        jours.forEach { date ->
            val estAujourdhui = cleJour(date) == aujourdhui

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Date : un clique ouvre la vue Jour
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onJour(date) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = fmtJourCourt.format(date.time).take(3).cap(),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(if (estAujourdhui) PlanningAccent else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.get(Calendar.DAY_OF_MONTH).toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (estAujourdhui) Color.White else Color.Black
                        )
                        // Point rouge : un plat du stock se périme ce jour-là
                        if (expirations.containsKey(cleJour(date))) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color(0xFFD32F2F), CircleShape)
                            )
                        }
                    }
                }

                Creneau.entries.forEach { c ->
                    CelluleSemaine(
                        repas = planning[cleRepas(date, c)],
                        modifier = Modifier.weight(1f),
                        onClick = { onCellule(date, c) }
                    )
                }
            }
        }
    }
}



/** Petite case colorée affichée dans la grille de la VueSemaine. */
@Composable
private fun CelluleSemaine(repas: RepasPlanifie?, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(repas?.recette?.couleur ?: Color.Black.copy(alpha = 0.04f))
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (repas != null) {
            Text(
                text = repas.recette.nom,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )

            if (repas.recette.estBatch) {
                Icon(
                    Icons.Default.Kitchen,
                    contentDescription = "Reste du batch cooking",
                    tint = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier.size(12.dp).align(Alignment.TopEnd)
                )
            }
        } else {
            Icon(
                Icons.Default.Add,
                contentDescription = "Ajouter",
                tint = Color.Black.copy(alpha = 0.2f),
                modifier = Modifier.size(16.dp).align(Alignment.Center)
            )
        }
    }
}

/** Vue globale du mois (façon calendrier mural). Utilise des petits points de couleur pour indiquer les repas. */
@Composable
private fun VueMois(
    grille: List<Calendar>,
    moisRef: Int,
    aujourdhui: String,
    expirations: Map<String, List<PlatBatch>>,
    planning: Map<String, RepasPlanifie>,
    onJour: (Calendar) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(horizontal = 6.dp, vertical = 12.dp)
    ) {
        Row {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { lettre ->
                Text(
                    text = lettre,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        grille.chunked(7).forEach { semaine ->
            Row {
                semaine.forEach { date ->
                    val dansLeMois = date.get(Calendar.MONTH) == moisRef
                    val estAujourdhui = cleJour(date) == aujourdhui

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onJour(date) }
                            .padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(if (estAujourdhui) PlanningAccent else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.get(Calendar.DAY_OF_MONTH).toString(),
                                fontSize = 14.sp,
                                fontWeight = if (estAujourdhui) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    estAujourdhui -> Color.White
                                    dansLeMois -> Color.Black
                                    else -> Color.Gray.copy(alpha = 0.5f)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Un point par repas planifié ce jour-là
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Creneau.entries.forEach { c ->
                                planning[cleRepas(date, c)]?.let { repas ->
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(lerp(repas.recette.couleur, Color.Black, 0.25f), CircleShape)
                                    )
                                }
                            }
                            // Point rouge : un plat se périme ce jour-là
                            if (expirations.containsKey(cleJour(date))) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFFD32F2F), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}




@Composable
private fun BoutonNav(icone: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(Color.Black.copy(alpha = 0.06f), CircleShape)
    ) {
        Icon(icone, contentDescription = description, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
    }
}


/** Bouton segmenté (Switch) gérant le State Hoisting pour changer la vue du calendrier. */
@Composable
private fun SelecteurVue(
    vueActuelle: VuePlanning,
    onChange: (VuePlanning) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), CircleShape)
            .padding(4.dp)
    ) {
        VuePlanning.entries.forEach { v ->
            val actif = v == vueActuelle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (actif) Color.Black else Color.Transparent)
                    .clickable { onChange(v) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = v.label,
                    color = if (actif) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SelecteurPortions(portions: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.background(Color.Black.copy(alpha = 0.06f), CircleShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (portions > 1) onChange(portions - 1) },
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Diminuer", tint = Color.Black, modifier = Modifier.size(16.dp))
        }
        Text("$portions pers.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        IconButton(
            onClick = { onChange(portions + 1) },
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Augmenter", tint = Color.Black, modifier = Modifier.size(16.dp))
        }
    }
}


/**
 * Grande carte affichant les détails de la recette choisie, utilisée dans la vue Jour
 * et dans la boîte de dialogue.
 *
 * @param onMange Apparaît uniquement pour un plat du stock, permettant d'officialiser
 * le fait qu'il a été consommé, déduisant sa quantité du stock central.
 */
@Composable
fun RepasCarte(
    repas: RepasPlanifie,
    onPortions: (Int) -> Unit,
    onRetirer: () -> Unit,
    onMange: (() -> Unit)? = null      // non null uniquement pour les repas issus du batch
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(repas.recette.couleur)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = repas.recette.imageRes),
            contentDescription = repas.recette.nom,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(repas.recette.nom, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (repas.recette.estBatch) Icons.Default.Kitchen else Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (repas.recette.estBatch) "Restes du batch cooking" else repas.recette.duree,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SelecteurPortions(portions = repas.portions, onChange = onPortions)

            if (onMange != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Mangé ✓ (retirer du stock)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black)
                        .clickable(onClick = onMange)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }

        IconButton(onClick = onRetirer, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Retirer ce repas",
                tint = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}



/** Placeholder appelant l'ajout d'une recette sur un créneau. */
@Composable
fun EmplacementVide(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Ajouter une recette", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        }
    }
}



/** Carte de statistiques de la semaine */
@Composable
private fun RecapCarte(
    libelle: String,
    nbRepas: Int,
    nbPortions: Int,
    nbAcuisiner: Int,
    onGenerer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Text(libelle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("$nbRepas repas planifiés · $nbPortions portions", fontSize = 13.sp, color = Color.Gray)
        if (nbRepas > nbAcuisiner) {
            Text(
                "${nbRepas - nbAcuisiner} déjà cuisinés (restes), ${nbAcuisiner} à cuisiner",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        val actif = nbAcuisiner > 0
        Text(
            text = "Générer la liste de courses",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(PlanningAccent.copy(alpha = if (actif) 1f else 0.35f))
                .clickable(enabled = actif, onClick = onGenerer)
                .padding(vertical = 14.dp)
        )
    }
}




/**
 * Modale (Dialog) permettant de choisir quoi manger sur un créneau.
 *
 * Mixe la liste des recettes "Neuves" (à cuisiner) et des restes du frigo (PlatBatch).
 * Si un reste est sélectionné, empêche l'utilisateur d'ajouter plus de portions que
 * ce qu'il n'y a physiquement en stock.
 */
@Composable
private fun DialogRepas(
    titre: String,
    repas: RepasPlanifie?,
    dateRepasMs: Long,
    stock: List<PlatBatch>,
    disponibles: (Long) -> Int,
    onChoisir: (RecettePlan, Int?) -> Unit,   // 2e paramètre : plafond de portions (plats du stock)
    onPortions: (Int) -> Unit,
    onRetirer: () -> Unit,
    onFermer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFermer,
        containerColor = Color.White,
        title = { Text(titre, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Restes du batch cooking, les plus urgents à cette date en premier
                if (stock.isNotEmpty()) {
                    Text("Restes du batch cooking", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    stock.sortedBy { it.joursRestants(dateRepasMs) }.forEach { p ->
                        val dispo = disponibles(p.id)
                        val jours = p.joursRestants(dateRepasMs)
                        val choisie = repas?.recette?.platBatchId == p.id
                        val utilisable = dispo > 0

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(p.stockage.couleur.copy(alpha = if (utilisable) 1f else 0.4f))
                                .then(
                                    if (choisie) Modifier.border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                                    else Modifier
                                )
                                .clickable(enabled = utilisable) { onChoisir(p.versRecette(), dispo) }
                                .padding(14.dp)
                        ) {
                            Text(p.nom, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            val delai = when {
                                jours < 0 -> "périmé à cette date (limite ${dateExpirationCourte(p)})"
                                jours == 0 -> "dernier jour possible"
                                else -> "limite ${dateExpirationCourte(p)}"
                            }
                            Text(
                                text = "${p.stockage.label} · $dispo portion(s) dispo · $delai",
                                fontSize = 12.sp,
                                color = if (jours <= 1) couleurUrgence(jours) else Color.DarkGray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Recettes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                // Choix de la recette
                recettesPlanDemo.forEach { r ->
                    val choisie = repas?.recette?.nom == r.nom
                    Text(
                        text = r.nom,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(r.couleur)
                            .then(
                                if (choisie) Modifier.border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                                else Modifier
                            )
                            .clickable { onChoisir(r, null) }
                            .padding(14.dp)
                    )
                }

                if (repas != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nombre de personnes", fontSize = 14.sp, color = Color.Gray)
                        SelecteurPortions(portions = repas.portions, onChange = onPortions)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onFermer) { Text("OK", color = Color.Black) } },
        dismissButton = {
            if (repas != null) {
                TextButton(onClick = onRetirer) { Text("Retirer", color = PlanningAccent) }
            }
        }
    )
}