package com.example.myr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * Modèle de données représentant un dossier de recettes partagé ou privé.
 *
 * @property nom Nom du dossier.
 * @property nbRecettes Nombre total de recettes contenues dans ce dossier.
 * @property nbMembres Nombre d'utilisateurs ayant accès à ce dossier.
 * @property couleur Couleur (thème) utilisée pour la carte.
 * @property imageRes Identifiant de la ressource image (Drawable) illustrant le dossier.
 */
data class Dossier(
    val nom: String,
    val nbRecettes: Int,
    val nbMembres: Int,
    val couleur: Color,
    val imageRes: Int
)

val dossiersDemo = listOf(
    Dossier("Fruity Family", 15, 12, Color(0xFFFFE3E0), android.R.drawable.ic_menu_gallery),
    Dossier("Crimson Dimsums", 10, 6, Color(0xFFE0EFFF), android.R.drawable.ic_menu_gallery),
    Dossier("Nutty Desserts", 58, 102, Color(0xFFFFF2D0), android.R.drawable.ic_menu_gallery)
)




/**
 * Écran **Stateless** affichant la liste des dossiers de l'utilisateur (Bibliothèque).
 *
 * Affiche une liste déroulante de grandes cartes colorées. L'écran gère l'espacement avec
 * la barre d'état système et implémente un State Hoisting complet : il ne conserve
 * aucun état interne, toutes les actions sont remontées à son parent.
 *
 * @param dossiers Liste des dossiers à afficher.
 * @param onBack Action de retour arrière (flèche en haut à gauche).
 * @param onNouveauDossier Action d'ajout d'un dossier (bouton "+" en haut à droite).
 * @param onDossierClick Action déclenchée lors de la sélection d'un dossier spécifique.
 */
@Composable
@Preview
fun BibliothequeEcran(
    dossiers: List<Dossier> = dossiersDemo,
    onBack: () -> Unit = {},
    onNouveauDossier: () -> Unit = {},
    onDossierClick: (Dossier) -> Unit = {}
) {
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
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.06f), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Retour",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onNouveauDossier,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFE57373), CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Nouveau dossier",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = "Bibliothèque",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(dossiers) { index, dossier ->
                DossierCarte(
                    dossier = dossier,
                    imageADroite = index % 2 == 0,   // alterne droite / gauche
                    onClick = { onDossierClick(dossier) }
                )
            }
            item { BottomBarSpacer() }
        }
    }
}



/**
 * Carte visuelle représentant un dossier de la bibliothèque.
 *
 * - L'image d'illustration (ronde) est superposée et dépasse légèrement du bord haut de la carte.
 * - Une "sous-carte" semi-transparente dépasse par le bas pour créer un effet d'épaisseur/d'ombre.
 * - Le layout est asymétrique : l'image et le texte changent de côté (droite/gauche)
 *   en fonction du paramètre [imageADroite].
 *
 * @param dossier Les données du dossier à afficher.
 * @param imageADroite Si `true`, l'image est à droite et le texte à gauche. Si `false`, c'est l'inverse.
 * @param onClick Callback déclenché au clic sur l'ensemble de la carte.
 */
@Composable
fun DossierCarte(
    dossier: Dossier,
    imageADroite: Boolean,
    onClick: () -> Unit
) {
    val imageSize = 120.dp
    val sousCarte = 8.dp        // partie de la sous-carte visible en bas

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(dossier.couleur.copy(0.5f))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = sousCarte)
                .clip(RoundedCornerShape(28.dp))
                .background(dossier.couleur)
                .clickable(onClick = onClick)
        )

        Column(
            modifier = Modifier
                .align(if (imageADroite) Alignment.CenterStart else Alignment.CenterEnd)
                .fillMaxWidth(0.55f)
                .padding(top = 14.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = if (imageADroite) Alignment.Start else Alignment.End
        ) {
            Text(
                text = dossier.nom,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${dossier.nbRecettes} recettes",
                fontSize = 11.sp,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(22.dp).background(Color.DarkGray, CircleShape))
                Box(modifier = Modifier.offset(x = (-8).dp).size(22.dp).background(Color.Gray, CircleShape))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${dossier.nbMembres} membres",
                    fontSize = 11.sp,
                    color = Color.Black.copy(alpha = 0.55f)
                )
            }
        }

        Image(
            painter = painterResource(id = dossier.imageRes),
            contentDescription = dossier.nom,
            modifier = Modifier
                .align(if (imageADroite) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(bottom = sousCarte)
                .padding(horizontal = 12.dp)
                .size(imageSize)
                .clip(CircleShape)
                .background(Color.White),
            contentScale = ContentScale.Crop
        )
    }
}