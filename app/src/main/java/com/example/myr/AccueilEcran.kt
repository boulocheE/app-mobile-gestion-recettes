package com.example.myr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue


/**
 * Écran d'accueil principal de l'application de recettes.
 *
 * Ce composant "Stateful" gère un défilement vertical global et orchestre trois sections majeures :
 * 1. Un en-tête contenant l'accès au profil utilisateur, une barre de recherche interactive et un bouton de filtrage.
 * 2. Une section "Top Recipes" présentant une liste de cartes de recettes défilables horizontalement.
 * 3. Une section de catégories (Fruits, Viandes, etc.) également défilable horizontalement.
 */
@Composable
@Preview
fun HomePage() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Fond gris très clair
            .padding(top = 40.dp)
            .verticalScroll(scrollState)
    ) {
        var query by remember { mutableStateOf("") }


        // EN-TÊTE : profil + recherche
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            // Icône de profil en haut à droite
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.06f), CircleShape)
                    .clip(CircleShape)
                    .clickable { /* TODO ouvrir le profil */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profil",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barre de recherche + bouton filtres
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
                    focusedIndicatorColor = Color.Transparent,   // supprime le trait du bas
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Bouton filtres
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

        Spacer(modifier = Modifier.height(32.dp))

        // SECTION : TOP RECETTES
        Text(
            text = "Top Recipes",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Défilement horizontal pour les cartes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecipeCardExactStyle(
                title = "Mexican Egg Mix",
                rating = "4.5",
                time = "15 min",
                description = "A gentle combination of healthy chicken veggies with a Mexican taste.",
                backgroundColor = Color(0xFFE0EFFF) // Bleu pastel
            )
            RecipeCardExactStyle(
                title = "Italian Baked",
                rating = "4.8",
                time = "20 min",
                description = "Italian baked spicy chicken green beans based together with cheese.",
                backgroundColor = Color(0xFFFFF2D0) // Jaune pastel
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // SECTION : CATÉGORIES
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp) // Espacement régulier
        ) {
            CategoryExactItemStyle(icon = "🍎", name = "Fruits", bgColor = Color(0xFFFFF0E6))
            CategoryExactItemStyle(icon = "🥩", name = "Meat", bgColor = Color(0xFFFFF0E6))
            CategoryExactItemStyle(icon = "🍣", name = "Sushi", bgColor = Color(0xFFFFF0E6))
            CategoryExactItemStyle(icon = "🍟", name = "Fries", bgColor = Color(0xFFFFF0E6))
        }


        BottomBarSpacer()
    }
}



/**
 * Carte de recette standard avec un design superposé (overlapping).
 *
 * Utilise un [Box] pour positionner l'image de la recette de manière à ce qu'elle dépasse
 * de la carte de fond, créant un effet de profondeur 3D. Intègre un badge interactif "Save"
 * en bas à droite.
 *
 * @param title Le nom principal de la recette affiché en gras.
 * @param rating La note de la recette sur 5 (ex: "4.5").
 * @param time La durée de préparation estimée (ex: "15 min").
 * @param description Une brève description. Le texte est automatiquement tronqué (Ellipsis)
 * s'il dépasse 3 lignes.
 * @param backgroundColor La couleur d'arrière-plan de la base de la carte.
 */
@Composable
fun RecipeCardExactStyle(
    title: String,
    rating: String,
    time: String,
    description: String,
    backgroundColor: Color
) {
    val imageSize  = 100.dp
    val saveHeight = 32.dp

    Box(
        modifier = Modifier.width(200.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Décalée vers le bas de la moitié de l'image
        Column(
            modifier = Modifier
                .padding(top = imageSize / 2, bottom = saveHeight / 2)
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(32.dp))
                .padding(start = 20.dp, end = 20.dp, top = imageSize / 2 + 16.dp, bottom = saveHeight / 2 + 12.dp)
        ) {
            // Titre
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(color = Color.Gray.copy(alpha = 0.06f), shape = RoundedCornerShape(25))
                        .padding(5.dp)) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = rating, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(color = Color.Gray.copy(alpha = 0.06f), shape = RoundedCornerShape(8.dp))
                        .padding(5.dp)) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Niveau", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = "Time", tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = time, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Image(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
            contentDescription = title,
            modifier = Modifier
                .size(imageSize)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp)
                .height(saveHeight)
                .background(Color.White, RoundedCornerShape(50))
                .border(1.dp, Color(0xFFE57373), RoundedCornerShape(50))
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Save",
                tint = Color(0xFFE57373),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Save",
                fontSize = 12.sp,
                color = Color(0xFFE57373),
                fontWeight = FontWeight.Bold
            )
        }
    }
}



/**
 * Élément visuel représentant une catégorie de repas.
 *
 * Utilise un design vertical arrondi avec une bulle blanche interne contenant l'icône,
 * suivie du nom de la catégorie en bas.
 *
 * @param icon L'icône représentative. Fournie ici sous forme de chaîne de caractères (emoji).
 * @param name Le libellé de la catégorie.
 * @param bgColor La couleur de fond de l'ensemble de la pilule.
 */
@Composable
fun CategoryExactItemStyle(icon: String, name: String, bgColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(bgColor, CircleShape)
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 25.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 32.sp)
        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = name,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
    }
}