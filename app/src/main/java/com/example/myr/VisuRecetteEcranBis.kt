package com.example.myr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Preview
fun VisuRecetteEcranBis() {
    val scrollState = rememberScrollState()
    val yellowAccent = Color(0xFFFFD54F)

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // 1. Image avec effet Parallax
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

        // 2. Contenu défilant (Panneau blanc)
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
                    .padding(24.dp)
            ) {
                // En-tête : Titre et Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Crepes with Orange and Honey",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Western",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Badge de note
                    Row(
                        modifier = Modifier
                            .background(yellowAccent, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Note",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "4.5", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Rangée de badges d'information (Temps, Portions, Calories, Difficulté)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoBadge("35", "Mins", yellowAccent)
                    InfoBadge("03", "Servings", yellowAccent)
                    InfoBadge("103", "Cal", yellowAccent)
                    InfoBadge("", "Easy", yellowAccent, isTextOnly = true)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section Ingrédients
                Text(
                    text = "Ingredients",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                IngredientRow("2", "Eggs", yellowAccent)
                IngredientRow("1 Cup", "All purpose flour", yellowAccent)
                IngredientRow("1/2 Cup", "Whole milk", yellowAccent)
                IngredientRow("2 Tablespoon", "Butter", yellowAccent)

                Spacer(modifier = Modifier.height(32.dp)) // Espace de défilement


                Text(
                    text = "Etapes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))


                StepRow("Voici les instructions pour la première étape... Voici les instructions pour la première étape...", yellowAccent)
                StepRow("Voici les instructions pour la deuxième étape... Voici les instructions pour la deuxième étape...", yellowAccent)
                StepRow("Voici les instructions pour la troisème étape... Voici les instructions pour la troisème étape...", yellowAccent)
                StepRow("Voici les instructions pour la quatrième étape... Voici les instructions pour la quatrième étape...", yellowAccent)
                StepRow("Voici les instructions pour la cinquième étape... Voici les instructions pour la cinquième étape...", yellowAccent)

            }
        }

        // 3. Boutons flottants (Retour et Favoris) placés par-dessus tout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { /* Action retour */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(yellowAccent, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.Black)
            }

            IconButton(
                onClick = { /* Action favoris */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape)
            ) {
                //Icon(Icons.Default.BookmarkBorder, contentDescription = "Favoris", tint = Color.Black)
            }
        }
    }
}

// Composant réutilisable pour les badges jaunes ronds
@Composable
fun InfoBadge(value: String, label: String, accentColor: Color, isTextOnly: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isTextOnly) {
                Text(text = label, fontWeight = FontWeight.Bold)
            } else {
                Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        if (!isTextOnly) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// Composant réutilisable pour les lignes d'ingrédients avec la puce jaune
@Composable
fun IngredientRow(quantity: String, name: String, bulletColor: Color) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(bulletColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = quantity, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name, fontSize = 14.sp, color = Color.DarkGray)
    }
}



// Composant réutilisable pour les lignes des étapes avec la puce jaune
@Composable
fun StepRow(instructions: String, bulletColor: Color) {
    val texteAvecRond = buildAnnotatedString {
        appendInlineContent(id = "mon_rond")
        append(instructions)
    }

    val composantsIntegres = mapOf(
        "mon_rond" to InlineTextContent(
            Placeholder(
                width = 32.sp,
                height = 14.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Box(
                modifier = Modifier
                    .offset(y = 4.dp, x = 12.dp)
                    .size(8.dp)
                    .background(bulletColor, CircleShape)
            )
        }
    )

    Text(
        text = texteAvecRond,
        inlineContent = composantsIntegres,
        fontSize = 14.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}