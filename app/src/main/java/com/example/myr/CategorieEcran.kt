package com.example.myr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



/**
 * Modèle de données représentant une catégorie de recettes (ex: Fruits, Meat).
 *
 * @property nom Le nom affiché de la catégorie.
 * @property nbRecettes Le nombre de recettes disponibles dans cette catégorie.
 * @property couleur La couleur de fond spécifique à cette catégorie (souvent pastel).
 * @property imageRes L'identifiant de la ressource image (Drawable) illustrant la catégorie.
 */
data class Categorie(
    val nom: String,
    val nbRecettes: Int,
    val couleur: Color,
    val imageRes: Int
)


/**
 * Liste de données statique utilisée pour le mode Preview et le prototypage.
 */
val categoriesDemo = listOf(
    Categorie("Fruits", 24, Color(0xFFFFD9BD), android.R.drawable.ic_menu_gallery),
    Categorie("Meat", 18, Color(0xFFD5E8C8), android.R.drawable.ic_menu_gallery),
    Categorie("Sushi", 12, Color(0xFFFFF0D0), android.R.drawable.ic_menu_gallery),
    Categorie("Fries", 9, Color(0xFFF9C6CF), android.R.drawable.ic_menu_gallery),
    Categorie("Desserts", 31, Color(0xFFFFF59D), android.R.drawable.ic_menu_gallery)
)



/**
 * Écran permettant d'explorer l'ensemble des catégories de recettes disponibles.
 *
 * **Architecture :**
 * Cet écran gère en interne la logique de la barre de recherche (Stateful pour le `query`),
 * mais délègue les actions de navigation (State Hoisting) via les callbacks `onOuvrirBibliotheque`
 * et `onCategorieClick`.
 *
 * **Structure visuelle :**
 * 1. Un en-tête avec un titre centré et un accès rapide à la bibliothèque (icône dossier).
 * 2. Une barre de recherche interactive accompagnée d'un bouton de filtrage rouge.
 * 3. Une [LazyColumn] listant les catégories sous forme de grandes cartes colorées.
 *
 * @param categories La liste des catégories à afficher (utilise [categoriesDemo] par défaut).
 * @param onOuvrirBibliotheque Déclenché lors du clic sur l'icône de dossier en haut à droite.
 * @param onCategorieClick Déclenché lors du clic sur l'une des cartes de la liste.
 */
@Composable
@Preview
fun CategoriesEcran(
    categories: List<Categorie> = categoriesDemo,
    onOuvrirBibliotheque: () -> Unit = {},
    onCategorieClick: (Categorie) -> Unit = {}
) {
    val appBackground = Color(0xFFF8F9FA)
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackground)
            .statusBarsPadding()
    ) {
        // En-tête : retour à gauche, titre centré, bibliothèque à droite
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Catégories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onOuvrirBibliotheque,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.06f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = "Ma bibliothèque",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                placeholder = { Text("Search recipes", fontSize = 14.sp, color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Rechercher", tint = Color.Gray)
                },
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.06f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.06f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFE57373))
                    .clickable { /* ouvrir les filtres */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filtres",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        // Liste des catégories
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { categorie ->
                CategorieLigne(categorie = categorie, onClick = { onCategorieClick(categorie) })
            }
            item { BottomBarSpacer() }
        }
    }
}


/**
 * Composant Stateless affichant une ligne individuelle dans la liste des catégories.
 *
 * Se présente sous la forme d'une grande carte aux bords très arrondis (24.dp).
 * La couleur de fond est dynamiquement attribuée par le modèle [Categorie].
 *
 * @param categorie L'objet contenant les données à afficher (nom, compte, image, couleur).
 * @param onClick L'action à exécuter lorsque la carte entière est pressée.
 */
@Composable
fun CategorieLigne(categorie: Categorie, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(categorie.couleur)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categorie.nom,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${categorie.nbRecettes} recettes",
                fontSize = 13.sp,
                color = Color.Black.copy(alpha = 0.55f)
            )
        }

        Image(
            painter = painterResource(id = categorie.imageRes),
            contentDescription = categorie.nom,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentScale = ContentScale.Crop
        )
    }
}