package com.example.myr

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CoursesAccent = Color(0xFFE57373)
private val FrigoAccent = Color(0xFF4CAF50)
private const val NON_ASSIGNE = "À assigner"




/**
 * Représente un lieu d'achat pour catégoriser la liste de courses.
 *
 * @property nom Nom du magasin (ex: "Carrefour").
 * @property couleur Couleur d'accentuation utilisée pour les badges visuels.
 */
data class Magasin(val nom: String, val couleur: Color)

val magasinsDemo = listOf(
    Magasin("Carrefour", Color(0xFFE0EFFF)),
    Magasin("Lidl", Color(0xFFFFF2D0)),
    Magasin("Marché", Color(0xFFD5E8C8))
)



/**
 * Modèle d'un article présent dans la liste de courses.
 *
 * @property id Identifiant unique.
 * @property nom Nom du produit à acheter.
 * @property quantite Quantité désirée.
 * @property rayon Catégorie du produit (ex: "Fruits & légumes") pour le tri.
 * @property magasin Nom du magasin assigné, ou null si non défini.
 * @property coche Indique si l'article a été mis dans le panier.
 */
data class ArticleCourse(
    val id: Int,
    val nom: String,
    val quantite: String,
    val rayon: String,
    val magasin: String? = null,
    val coche: Boolean = false
)


/**
 * Modèle d'un article stocké dans le réfrigérateur virtuel.
 *
 * @property id Identifiant unique.
 * @property nom Nom de l'aliment.
 * @property quantite Quantité restante (ex: "50 cl").
 * @property joursAvantPeremption Délai avant péremption (< 0 = périmé, 0-2 = urgent, > 2 = OK).
 */
data class ArticleFrigo(
    val id: Int,
    val nom: String,
    val quantite: String,
    val joursAvantPeremption: Int
)

val articlesFrigoDemo = listOf(
    ArticleFrigo(1, "Lait entier", "50 cl", 1),       // Bientôt périmé (Alerte)
    ArticleFrigo(2, "Beurre", "2 c. à soupe", 4),     // OK
    ArticleFrigo(3, "Poulet", "600 g", 0),          // Périme aujourd'hui / Urgent
    ArticleFrigo(4, "Œufs", "6", 10),                 // OK
    ArticleFrigo(5, "Salade", "1 pièce", -1)          // Périmé
)

val articlesDemo = listOf(
    ArticleCourse(1, "Oranges", "4", "Fruits & légumes", "Marché"),
    ArticleCourse(2, "Citrons", "2", "Fruits & légumes", "Marché"),
    ArticleCourse(3, "Farine", "1 tasse", "Épicerie", "Lidl"),
    ArticleCourse(4, "Miel", "1 pot", "Épicerie")
)

private val couleursRayon = mapOf(
    "Fruits & légumes" to Color(0xFFD5E8C8),
    "Viandes" to Color(0xFFFFE3E0),
    "Produits laitiers" to Color(0xFFE0EFFF),
    "Épicerie" to Color(0xFFFFF2D0),
    "Autres" to Color(0xFFEDEDED)
)


/** Sections principales de la gestion alimentaire. */
enum class SectionApp(val label: String) {
    Frigo("Mon Frigo"),
    Courses("Courses")
}


/** Modes d'affichage et de tri pour la liste de courses. */
enum class ModeCourses(val label: String) {
    UnMagasin("Un magasin"),
    ParMagasin("Par magasin")
}




/**
 * Écran principal orchestrant la gestion alimentaire (Frigo et Courses).
 *
 * Ce composant **Stateful** gère l'état global des listes de données (`articlesCourses`
 * et `articlesFrigo`). Il implémente la logique de transfert : lorsqu'un aliment
 * manque dans le frigo, il peut être transféré ou copié directement dans la liste des courses.
 *
 * @param magasins Liste des magasins disponibles pour l'affectation.
 */
@Composable
@Preview
fun GestionAlimentaireEcran(magasins: List<Magasin> = magasinsDemo) {
    var sectionActive by remember { mutableStateOf(SectionApp.Frigo) }
    val articlesCourses = remember { articlesDemo.toMutableStateList() }
    val articlesFrigo = remember { articlesFrigoDemo.toMutableStateList() }

    val appBackground = Color(0xFFF8F9FA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackground)
            .statusBarsPadding()
    ) {
        // Sélecteur de section (Frigo / Courses)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionApp.entries.forEach { section ->
                val actif = section == sectionActive
                val couleurActive = if (section == SectionApp.Frigo) FrigoAccent else CoursesAccent

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (actif) couleurActive else Color.Black.copy(alpha = 0.05f))
                        .clickable { sectionActive = section }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = section.label,
                        color = if (actif) Color.White else Color.Black.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Affichage dynamique selon la section choisie
        when (sectionActive) {
            SectionApp.Frigo -> {
                FrigoContenu(
                    articlesFrigo = articlesFrigo,
                    onAjouterAuxCourses = { articleFrigo ->
                        // Transférer ou ajouter l'article du frigo vers la liste de courses
                        if (articlesCourses.none { it.nom.equals(articleFrigo.nom, ignoreCase = true) }) {
                            articlesCourses.add(
                                ArticleCourse(
                                    id = (articlesCourses.maxOfOrNull { it.id } ?: 0) + 1,
                                    nom = articleFrigo.nom,
                                    quantite = articleFrigo.quantite,
                                    rayon = "Produits laitiers",
                                    magasin = null
                                )
                            )
                        }

                        articlesFrigo.remove(articleFrigo)
                    },
                    onSupprimerFrigo = { articlesFrigo.remove(it) }
                )
            }
            SectionApp.Courses -> {
                CoursesContenu(
                    articles = articlesCourses,
                    magasins = magasins
                )
            }
        }
    }
}



/**
 * Sous-écran **Stateful** gérant le contenu du réfrigérateur.
 *
 * Gère l'ajout rapide de nouveaux aliments avec leur délai de péremption.
 * Affiche dynamiquement une bannière d'alerte globale en haut de l'écran si un
 * ou plusieurs articles arrivent à expiration dans moins de 2 jours.
 *
 * @param articlesFrigo La liste mutable des articles actuels.
 * @param onAjouterAuxCourses Callback déclenché pour racheter un produit manquant.
 * @param onSupprimerFrigo Callback déclenché pour jeter un produit.
 */
@Composable
fun FrigoContenu(
    articlesFrigo: MutableList<ArticleFrigo>,
    onAjouterAuxCourses: (ArticleFrigo) -> Unit,
    onSupprimerFrigo: (ArticleFrigo) -> Unit
) {
    var nouvelArticleNom by remember { mutableStateOf("") }
    var nouvelArticleQte by remember { mutableStateOf("") }
    var nouvelArticlePeremption by remember { mutableStateOf("") }

    val articlesUrgents = articlesFrigo.count { it.joursAvantPeremption <= 2 }

    Column(modifier = Modifier.fillMaxSize()) {
        if (articlesUrgents > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFEBEE))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                Text(
                    text = "$articlesUrgents article(s) périment bientôt ou sont périmés !",
                    color = Color(0xFFC62828),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Formulaire d'ajout rapide dans le frigo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Ajouter un aliment au frigo", fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = nouvelArticleNom,
                    onValueChange = { nouvelArticleNom = it },
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    placeholder = { Text("Nom", fontSize = 13.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                TextField(
                    value = nouvelArticleQte,
                    onValueChange = { nouvelArticleQte = it },
                    modifier = Modifier.weight(1f).height(50.dp),
                    placeholder = { Text("Qté", fontSize = 13.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                TextField(
                    value = nouvelArticlePeremption,
                    onValueChange = { nouvelArticlePeremption = it },
                    modifier = Modifier.weight(1f).height(50.dp),
                    placeholder = { Text("Jours restants", fontSize = 11.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FrigoAccent)
                    .clickable {
                        if (nouvelArticleNom.isNotBlank()) {
                            val jours = nouvelArticlePeremption.toIntOrNull() ?: 5
                            articlesFrigo.add(
                                ArticleFrigo(
                                    id = (articlesFrigo.maxOfOrNull { it.id } ?: 0) + 1,
                                    nom = nouvelArticleNom.trim(),
                                    quantite = nouvelArticleQte.ifBlank { "1" },
                                    joursAvantPeremption = jours
                                )
                            )
                            nouvelArticleNom = ""
                            nouvelArticleQte = ""
                            nouvelArticlePeremption = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Ajouter au frigo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Liste des articles du frigo
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(articlesFrigo, key = { it.id }) { article ->
                FrigoLigne(
                    article = article,
                    onAcheterDeNouveau = { onAjouterAuxCourses(article) },
                    onSupprimer = { onSupprimerFrigo(article) }
                )
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}



/**
 * Ligne individuelle affichant un article du réfrigérateur.
 *
 * **Stateless** : L'interface réagit uniquement à la donnée.
 * Contient une logique d'affichage conditionnelle pour générer un badge coloré
 * (Vert, Orange, ou Rouge) informant de la péremption.
 *
 * @param article La donnée à afficher.
 * @param onAcheterDeNouveau Action déclenchée par le bouton "À racheter".
 * @param onSupprimer Action déclenchée par l'icône de suppression (poubelle).
 */
@Composable
fun FrigoLigne(
    article: ArticleFrigo,
    onAcheterDeNouveau: () -> Unit,
    onSupprimer: () -> Unit
) {
    val (couleurBadge, texteBadge) = when {
        article.joursAvantPeremption < 0 -> Pair(Color(0xFFD32F2F), "Périmé")
        article.joursAvantPeremption == 0 -> Pair(Color(0xFFE64A19), "Périme aujourd'hui")
        article.joursAvantPeremption <= 2 -> Pair(Color(0xFFF57C00), "Expire dans ${article.joursAvantPeremption}j")
        else -> Pair(Color(0xFF388E3C), "Bon (${article.joursAvantPeremption}j)")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.nom,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Quantité : ${article.quantite}", fontSize = 13.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(couleurBadge.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = texteBadge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = couleurBadge)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // Bouton pour basculer en courses
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CoursesAccent.copy(alpha = 0.15f))
                    .clickable(onClick = onAcheterDeNouveau)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text("À racheter", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoursesAccent)
            }

            IconButton(onClick = onSupprimer) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Gray)
            }
        }
    }
}




/**
 * Sous-écran **Stateful** gérant la liste de courses.
 *
 * Propose deux vues dynamiques :
 * - Mode "Un magasin" : Filtre la liste pour n'afficher que les produits assignés à
 *   un magasin spécifique, regroupés par rayons.
 * - Mode "Par magasin" : Affiche l'ensemble de la liste en regroupant visuellement
 *   les articles sous l'en-tête de leur magasin de destination respectif.
 *
 * Intègre une barre de progression de complétion des courses.
 *
 * @param articles La liste globale des articles à acheter.
 * @param magasins Référentiel des magasins pour le tri et le filtrage.
 */
@Composable
fun CoursesContenu(
    articles: MutableList<ArticleCourse>,
    magasins: List<Magasin>
) {
    var nouvelArticle by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(ModeCourses.UnMagasin) }
    var magasinChoisi by remember { mutableStateOf(magasins.first().nom) }

    val total = articles.size
    val pris = articles.count { it.coche }
    val progression = if (total == 0) 0f else pris.toFloat() / total

    val groupes: List<Pair<String, List<ArticleCourse>>> = when (mode) {
        ModeCourses.UnMagasin ->
            articles.groupBy { it.rayon }.toList()
        ModeCourses.ParMagasin ->
            articles.groupBy { it.magasin ?: NON_ASSIGNE }.toList()
                .sortedBy { if (it.first == NON_ASSIGNE) 0 else 1 }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Liste de courses", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            IconButton(
                onClick = { articles.removeAll { it.coche } },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.06f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Retirer les articles cochés",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sélecteur de mode
        SelecteurMode(
            modeActuel = mode,
            onModeChange = { mode = it },
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        if (mode == ModeCourses.UnMagasin) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                magasins.forEach { magasin ->
                    val actif = magasin.nom == magasinChoisi
                    Text(
                        text = magasin.nom,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (actif) Color.White else Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (actif) Color.Black else magasin.couleur)
                            .clickable { magasinChoisi = magasin.nom }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progression
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "$pris / $total articles",
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progression },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = CoursesAccent,
                trackColor = Color.Black.copy(alpha = 0.06f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ajout rapide
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = nouvelArticle,
                onValueChange = { nouvelArticle = it },
                modifier = Modifier.weight(1f).height(50.dp),
                placeholder = { Text("Ajouter un article", fontSize = 14.sp, color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.06f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.06f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            IconButton(
                onClick = {
                    if (nouvelArticle.isNotBlank()) {
                        articles.add(
                            ArticleCourse(
                                id = (articles.maxOfOrNull { it.id } ?: 0) + 1,
                                nom = nouvelArticle.trim(),
                                quantite = "",
                                rayon = "Autres",
                                magasin = if (mode == ModeCourses.UnMagasin) magasinChoisi else null
                            )
                        )
                        nouvelArticle = ""
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .background(CoursesAccent, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Liste
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            groupes.forEach { (titre, liste) ->
                item(key = "${mode.name}_$titre") {
                    Spacer(modifier = Modifier.height(4.dp))
                    val couleurTitre = when (mode) {
                        ModeCourses.UnMagasin -> couleursRayon[titre]
                        ModeCourses.ParMagasin -> magasins.find { it.nom == titre }?.couleur
                    } ?: Color(0xFFEDEDED)

                    Text(
                        text = titre,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .background(couleurTitre, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                items(liste, key = { it.id }) { article ->
                    ArticleLigne(
                        article = article,
                        magasins = magasins,
                        afficherMagasin = mode == ModeCourses.ParMagasin,
                        onToggle = {
                            val index = articles.indexOfFirst { it.id == article.id }
                            if (index >= 0) articles[index] = article.copy(coche = !article.coche)
                        },
                        onChangerMagasin = { nom ->
                            val index = articles.indexOfFirst { it.id == article.id }
                            if (index >= 0) articles[index] = article.copy(magasin = nom)
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}




/**
 * Switch visuel ("Segmented Control") pour basculer entre les modes de vue.
 *
 * Totalement **Stateless** (l'état est fourni par le parent).
 */
@Composable
fun SelecteurMode(
    modeActuel: ModeCourses,
    onModeChange: (ModeCourses) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0), CircleShape)
            .padding(4.dp)
    ) {
        ModeCourses.entries.forEach { mode ->
            val actif = mode == modeActuel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (actif) Color.Black else Color.Transparent)
                    .clickable { onModeChange(mode) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    color = if (actif) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}




/**
 * Ligne individuelle représentant un élément dans la liste de courses.
 *
 * Visuellement, applique un effet barré sur le texte lorsque
 * l'article est coché. Maintient un état interne (`menuOuvert`) uniquement pour gérer
 * l'affichage du menu déroulant d'affectation de magasin.
 *
 * @param article Les données de l'article à afficher.
 * @param onToggle Callback déclenché pour cocher/décocher l'article.
 * @param magasins Liste pour alimenter le DropdownMenu d'affectation.
 * @param afficherMagasin Booléen activant ou désactivant l'affichage du badge magasin à droite.
 * @param onChangerMagasin Callback déclenché lors d'une sélection dans le DropdownMenu.
 */
@Composable
fun ArticleLigne(
    article: ArticleCourse,
    onToggle: () -> Unit,
    magasins: List<Magasin> = emptyList(),
    afficherMagasin: Boolean = false,
    onChangerMagasin: (String) -> Unit = {}
) {
    var menuOuvert by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (article.coche) CoursesAccent else Color.Transparent)
                .border(
                    2.dp,
                    if (article.coche) CoursesAccent else Color.Black.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (article.coche) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.nom,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (article.coche) Color.Gray else Color.Black,
                textDecoration = if (article.coche) TextDecoration.LineThrough else TextDecoration.None
            )
            if (article.quantite.isNotEmpty()) {
                Text(text = article.quantite, fontSize = 13.sp, color = Color.Gray)
            }
        }

        if (afficherMagasin) {
            Box {
                val couleur = magasins.find { it.nom == article.magasin }?.couleur ?: Color(0xFFEDEDED)
                Text(
                    text = article.magasin ?: "Choisir",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(couleur)
                        .clickable { menuOuvert = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                DropdownMenu(
                    expanded = menuOuvert,
                    onDismissRequest = { menuOuvert = false }
                ) {
                    magasins.forEach { magasin ->
                        DropdownMenuItem(
                            text = { Text(magasin.nom) },
                            onClick = {
                                onChangerMagasin(magasin.nom)
                                menuOuvert = false
                            }
                        )
                    }
                }
            }
        }
    }
}