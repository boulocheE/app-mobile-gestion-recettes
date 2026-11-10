package com.example.myr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



/**
 * Écran **Stateful** présentant le détail complet d'une recette.
 *
 * **Design & Animation :**
 * L'écran est divisé en deux couches (`Box`) :
 * 1. Une grande image de fond fixe.
 * 2. Un panneau de contenu (les instructions).
 *
 * **Comportement dynamique :**
 * L'écran observe continuellement la position du défilement (`scrollState.value`) via
 * un `derivedStateOf`. Lorsque l'utilisateur fait défiler l'écran vers le bas, l'application
 * calcule automatiquement (via des paliers de pixels) quelle est l'étape de la recette
 * actuellement visible à l'écran, et la met en évidence visuellement.
 */
@Composable
@Preview
fun VisuRecetteEcran() {
    val scrollState = rememberScrollState()
    val yellowAccent = Color(0xFFFFD54F)

    var nbPersonnes by remember { mutableStateOf("4") }
    // Variable d'état pour gérer le "slide" : 0 = Ingrédients, 1 = Étapes
    var indexOnglet by remember { mutableIntStateOf(0) }

    val etapeActive by remember {
        derivedStateOf {
            when {
                scrollState.value > 1800 -> 3 // Défilement profond -> Étape 3
                scrollState.value > 1000 -> 2 // Défilement moyen -> Étape 2
                else -> 1                    // Tout en haut -> Étape 1
            }
        }
    }

    val note = 4
    val maxEtoiles = 5

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Image(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
            contentDescription = "Image de la recette",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .graphicsLayer {
                    translationY = scrollState.value * 0.5f
                },
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(280.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp)
            ) {
                // En-tête : Titre
                Text(text = "Crepes with Orange and Honey", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = "Western", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(24.dp))

                // Section : Étoiles et Note globale
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Avis des utilisateurs",
                        fontWeight = FontWeight.SemiBold
                    )

                    // Le conteneur gris en forme de pilule
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(maxEtoiles) { index ->
                            Icon(
                                imageVector = if (index < note) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = yellowAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "$note.0",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Une recette délicieuse et rapide, parfaite pour un brunch dominical ou un petit-déjeuner gourmand. L'association de l'orange et du miel apporte une fraîcheur incomparable.",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Nombre de personnes", fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val actuel = nbPersonnes.toIntOrNull() ?: 1
                                if (actuel > 1) nbPersonnes = (actuel - 1).toString()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuer", tint = Color.Black)
                        }

                        BasicTextField(
                            value = nbPersonnes,
                            onValueChange = { nbPersonnes = it.filter { char -> char.isDigit() } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.Black),
                            modifier = Modifier.width(32.dp)
                        )

                        IconButton(
                            onClick = {
                                val actuel = nbPersonnes.toIntOrNull() ?: 1
                                nbPersonnes = (actuel + 1).toString()
                            },
                            modifier = Modifier.size(36.dp).background(yellowAccent, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Augmenter", tint = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoBadge("35", "Mins", yellowAccent)
                    InfoBadge("103", "Cal", yellowAccent)
                    InfoBadge("", "Easy", yellowAccent, isTextOnly = true)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bouton Ingrédients
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (indexOnglet == 0) Color.Black else Color.Transparent)
                            .clickable { indexOnglet = 0 }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingredients",
                            color = if (indexOnglet == 0) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bouton Étapes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (indexOnglet == 1) Color.Black else Color.Transparent)
                            .clickable { indexOnglet = 1 }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Directions",
                            color = if (indexOnglet == 1) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // AFFICHAGE CONDITIONNEL SELON L'ONGLET SÉLECTIONNÉ
                if (indexOnglet == 0) {
                    // Contenu de l'onglet Ingrédients
                    IngredientRow("2", "Eggs", yellowAccent)
                    IngredientRow("1 Cup", "All purpose flour", yellowAccent)
                    IngredientRow("1/2 Cup", "Whole milk", yellowAccent)
                    IngredientRow("2 Tablespoon", "Butter", yellowAccent)

                } else {
                    // Contenu de l'onglet Étapes
                    Text(
                        text = "Appuyez sur une étape pour la marquer en cours",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    EtapeCard(
                        numero = 1,
                        description = "Heat olive oil in a high-sided medium skillet. Add chicken and cook until browned on all sides.",
                        dureeMinuteur = null,
                        accentColor = yellowAccent,
                        isActive = etapeActive == 1,
                        onClick = {}
                    )
                    EtapeCard(
                        numero = 2,
                        description = "Add two cups of water to a medium sauce pan. Add rice and a pinch of salt. Bring to a boil on high. Once boiling, set temperature to low and cover for 14 minutes.",
                        dureeMinuteur = "14 minutes",
                        accentColor = yellowAccent,
                        isActive = etapeActive == 2,
                        onClick = {}
                    )
                }

                BottomBarSpacer()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { /* Action retour */ },
                modifier = Modifier.size(48.dp).background(yellowAccent, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Retour", tint = Color.Black)
            }
        }
    }
}



/**
 * Bloc de texte détaillant une étape de préparation de la recette.
 *
 * **Stateless** : L'apparence de la carte est gérée par le paramètre [isActive].
 * Lorsqu'elle est active, son fond se colore légèrement et une ligne d'accentuation
 * apparaît sur son bord gauche.
 *
 * Peut optionnellement afficher un bouton de lancement de minuteur si [dureeMinuteur]
 * est renseigné.
 *
 * @param numero L'ordre de l'étape dans la recette.
 * @param description Le texte descriptif de l'action à réaliser.
 * @param dureeMinuteur Chaîne de caractères indiquant un temps de cuisson (ex: "14 minutes"),
 * ou null si l'étape ne requiert pas de minuteur.
 * @param accentColor La couleur dominante du thème de la recette.
 * @param isActive Indique si l'utilisateur est actuellement en train de lire/réaliser cette étape.
 * @param onClick Action déclenchée lorsque l'utilisateur tape sur la carte pour la forcer "active".
 */
@Composable
fun EtapeCard(
    numero: Int,
    description: String,
    dureeMinuteur: String?,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) accentColor.copy(alpha = 0.15f) else Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(4.dp)
                        .background(accentColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(text = "Step $numero", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = description, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)

        if (dureeMinuteur != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .background(accentColor.copy(alpha = 0.2f), CircleShape)
                    .clip(CircleShape)
                    .clickable { /* Logique du minuteur */ }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(28.dp).background(accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Start Timer ($dureeMinuteur)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}
