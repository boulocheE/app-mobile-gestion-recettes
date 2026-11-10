package com.example.myr

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val StockAccent = Color(0xFFE57373)
private const val UN_JOUR_MS = 24L * 60 * 60 * 1000


/**
 * Définit les différents états de conservation possibles pour un plat cuisiné.
 *
 * La `dureeJours` est réinitialisée à chaque fois qu'un plat change d'état
 * (ex: passer du Congélateur au Frigo réinitialise le délai à 3 jours).
 *
 * @property dureeJours Durée de conservation conseillée à partir du moment où le plat entre dans cet état.
 */
enum class Stockage(val label: String, val dureeJours: Int, val couleur: Color) {
    Frigo("Frigo", 3, Color(0xFFE0EFFF)),
    Congelateur("Congélateur", 90, Color(0xFFE6E0FF)),
    Entame("Entamé", 2, Color(0xFFFFF2D0))
}



/**
 * Modèle de données représentant un plat préparé à l'avance (Batch Cooking).
 *
 * @property portionsTotal Le nombre de portions cuisinées initialement.
 * @property portionsRestantes Le nombre de portions physiquement présentes actuellement.
 * @property dateStockageMs Date (timestamp) de la dernière modification d'état (cuisson, congélation).
 */
data class PlatBatch(
    val id: Long,
    val nom: String,
    val portionsTotal: Int,
    val portionsRestantes: Int,
    val stockage: Stockage,
    val dateStockageMs: Long
) {

    /** Calcule la date limite de consommation absolue en fonction de l'état actuel. */
    fun dateExpirationMs(): Long = dateStockageMs + stockage.dureeJours * UN_JOUR_MS

    /**
     * Calcule le nombre de jours calendaires restants avant péremption.
     * @return > 0 (bon), 0 (à consommer aujourd'hui), < 0 (périmé).
     */
    fun joursRestants(maintenant: Long = System.currentTimeMillis()): Int =
        joursEntre(maintenant, dateExpirationMs())
}

private fun debutJour(ms: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ms
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun joursEntre(deMs: Long, aMs: Long): Int =
    Math.round((debutJour(aMs) - debutJour(deMs)) / UN_JOUR_MS.toDouble()).toInt()

private val fmtDateExp = SimpleDateFormat("EEE d MMM", Locale.FRANCE)

/** Formate la date de péremption de manière compacte (ex: "jeu. 31 oct."). */
fun dateExpirationCourte(p: PlatBatch): String = fmtDateExp.format(Date(p.dateExpirationMs()))

/** Renvoie une couleur d'alerte (Rouge, Orange, Vert) selon l'urgence de consommation. */
fun couleurUrgence(jours: Int) = when {
    jours < 0 -> Color(0xFFD32F2F)
    jours <= 1 -> Color(0xFFEF6C00)
    else -> Color(0xFF388E3C)
}

/** Formate un texte lisible informant du délai restant avant péremption. */
fun texteUrgence(jours: Int) = when {
    jours < 0 -> "Périmé depuis ${-jours} j"
    jours == 0 -> "À manger aujourd'hui"
    jours == 1 -> "Encore 1 jour"
    else -> "Encore $jours jours"
}





fun platsBatchDemo() = mutableStateListOf(
    PlatBatch(1, "Chili con carne", 6, 4, Stockage.Frigo, System.currentTimeMillis() - 2 * UN_JOUR_MS),
    PlatBatch(2, "Soupe de potiron", 8, 8, Stockage.Congelateur, System.currentTimeMillis() - 10 * UN_JOUR_MS),
    PlatBatch(3, "Gratin de pâtes", 4, 2, Stockage.Entame, System.currentTimeMillis() - UN_JOUR_MS)
)


/**
 * Vérifie combien de portions d'un plat spécifique sont déjà prévues dans le planning.
 *
 * @param planning Le calendrier actuel des repas.
 * @param platId L'identifiant du plat stocké.
 * @param sauf Une clé de créneau à exclure du calcul
 */
fun portionsReservees(planning: Map<String, RepasPlanifie>, platId: Long, sauf: String? = null): Int =
    planning.filter { it.key != sauf }.values
        .filter { it.recette.platBatchId == platId }
        .sumOf { it.portions }



/**
 * Écran **Stateful** listant le stock physique des plats cuisinés à l'avance (Batch Cooking).
 *
 * @param plats La liste des plats en stock.
 * @param planning Le planning des repas, passé ici en lecture seule pour pouvoir indiquer
 * à l'utilisateur combien de portions d'un plat sont déjà réservées.
 */
@Composable
@Preview
fun StockBatchEcran(
    plats: SnapshotStateList<PlatBatch> = remember { platsBatchDemo() },
    planning: Map<String, RepasPlanifie> = emptyMap()
) {
    var filtre by remember { mutableStateOf<Stockage?>(null) }
    var ajout by remember { mutableStateOf(false) }

    // Les plats les plus urgents en premier
    val affiches = plats
        .filter { filtre == null || it.stockage == filtre }
        .sortedBy { it.joursRestants() }

    val totalPortions = plats.sumOf { it.portionsRestantes }
    val aConsommerVite = plats.count { it.joursRestants() <= 1 }

    fun maj(id: Long, f: (PlatBatch) -> PlatBatch) {
        val i = plats.indexOfFirst { it.id == id }
        if (i >= 0) plats[i] = f(plats[i])
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
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
                Text("Batch cooking", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$totalPortions portions · $aConsommerVite à manger vite",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            IconButton(
                onClick = { ajout = true },
                modifier = Modifier.size(40.dp).background(StockAccent, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un plat", tint = Color.White)
            }
        }

        // Filtres par lieu de stockage
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Pastille("Tous", filtre == null) { filtre = null }
            Stockage.entries.forEach { s ->
                Pastille("${s.label} (${plats.count { it.stockage == s }})", filtre == s) { filtre = s }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (affiches.isEmpty()) {
                item {
                    Text(
                        "Rien ici pour le moment.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            }
            items(affiches, key = { it.id }) { plat ->
                PlatCarte(
                    plat = plat,
                    reservees = portionsReservees(planning, plat.id),
                    onManger = {
                        if (plat.portionsRestantes <= 1) plats.removeAll { it.id == plat.id }
                        else maj(plat.id) { it.copy(portionsRestantes = it.portionsRestantes - 1) }
                    },
                    onDeplacer = { s ->
                        maj(plat.id) { it.copy(stockage = s, dateStockageMs = System.currentTimeMillis()) }
                    },
                    onJeter = { plats.removeAll { it.id == plat.id } }
                )
            }
            item { BottomBarSpacer() }
        }
    }

    if (ajout) {
        DialogAjoutBatch(
            onAjouter = { nom, portions, stockage ->
                plats.add(
                    PlatBatch(
                        id = System.currentTimeMillis(),
                        nom = nom,
                        portionsTotal = portions,
                        portionsRestantes = portions,
                        stockage = stockage,
                        dateStockageMs = System.currentTimeMillis()
                    )
                )
                ajout = false
            },
            onFermer = { ajout = false }
        )
    }
}



/** Filtre sous forme de pastille cliquable. */
@Composable
private fun Pastille(texte: String, actif: Boolean, onClick: () -> Unit) {
    Text(
        text = texte,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (actif) Color.White else Color.Gray,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (actif) Color.Black else Color.Black.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}


/**
 * Carte interactive affichant l'état d'un plat cuisiné.
 *
 * Permet de consommer physiquement une portion ("Manger"), de jeter le plat,
 * ou de modifier son état de stockage via les boutons de déplacement.
 */
@Composable
private fun PlatCarte(
    plat: PlatBatch,
    reservees: Int,
    onManger: () -> Unit,
    onDeplacer: (Stockage) -> Unit,
    onJeter: () -> Unit
) {
    val jours = plat.joursRestants()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(plat.stockage.couleur)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(plat.nom, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${plat.stockage.label} · ${plat.portionsRestantes}/${plat.portionsTotal} portions" +
                            if (reservees > 0) " · $reservees au planning" else "",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "${texteUrgence(jours)} · limite : ${dateExpirationCourte(plat)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = couleurUrgence(jours)
                )
            }
            IconButton(onClick = onJeter, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Jeter ce plat",
                    tint = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Manger 1 portion",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable(onClick = onManger)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            // Déplacer vers un autre état
            Stockage.entries.filter { it != plat.stockage }.forEach { s ->
                Text(
                    "→ ${s.label}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                        .clickable { onDeplacer(s) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
    }
}



/** Formulaire de création d'un nouveau plat cuisiné (Batch Cooking). */
@Composable
private fun DialogAjoutBatch(
    onAjouter: (String, Int, Stockage) -> Unit,
    onFermer: () -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var portions by remember { mutableIntStateOf(4) }
    var stockage by remember { mutableStateOf(Stockage.Frigo) }

    AlertDialog(
        onDismissRequest = onFermer,
        containerColor = Color.White,
        title = { Text("Nouveau plat", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom du plat") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Portions", fontSize = 14.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.06f), CircleShape),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (portions > 1) portions-- }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Remove, "Diminuer", modifier = Modifier.size(16.dp))
                        }
                        Text("$portions", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { portions++ }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Add, "Augmenter", modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Stockage.entries.forEach { s ->
                        val actif = s == stockage
                        Text(
                            s.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(s.couleur)
                                .then(if (actif) Modifier.border(2.dp, Color.Black, CircleShape) else Modifier)
                                .clickable { stockage = s }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAjouter(nom.trim(), portions, stockage) },
                enabled = nom.isNotBlank()
            ) { Text("Ajouter", color = if (nom.isNotBlank()) Color.Black else Color.Gray) }
        },
        dismissButton = { TextButton(onClick = onFermer) { Text("Annuler", color = StockAccent) } }
    )
}