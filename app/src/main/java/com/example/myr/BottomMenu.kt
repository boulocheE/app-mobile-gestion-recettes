package com.example.myr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Représente les différentes destinations accessibles via la barre de navigation.
 *
 * @property label Le texte affiché sous l'icône.
 * @property icon L'icône vectorielle correspondante (Material Design).
 */
enum class BottomTab(val label: String, val icon: ImageVector) {
    Accueil("Accueil", Icons.Default.Home),
    Explorer("Explorer", Icons.Default.Search),
    Creer("Créer", Icons.Default.Add),
    Planning("Planning", Icons.Default.CalendarMonth),
    Courses("Courses", Icons.Default.ShoppingCart)
}

private val Accent = Color(0xFFE57373)
private val BarColor = Color.White



/**
 * Barre de navigation principale de l'application (Bottom Navigation).
 *
 * Ce composant est totalement **Stateless** : il ne conserve pas l'onglet actif en mémoire.
 * Il se contente d'afficher l'état qu'on lui passe et de notifier son parent via un callback.
 *
 * **Design :**
 * - Présente des bords supérieurs arrondis avec une ombre portée.
 * - Intègre un bouton central flottant pour l'action "Créer", qui dépasse
 *   délibérément de la moitié de sa hauteur au-dessus de la barre blanche.
 * - S'adapte automatiquement à l'espace de navigation du système d'exploitation (Insets)
 *   pour ne pas être masqué par la barre de gestes d'Android.
 *
 * @param selected L'onglet actuellement actif (géré par le parent).
 * @param onTabSelected Callback déclenché lorsque l'utilisateur clique sur un onglet.
 * @param modifier Permet au parent d'ajuster le positionnement du composant global.
 */
@Composable
fun BottomMenu(
    selected: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val fabSize = 60.dp
    val barHeight = 72.dp
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight + navInset + fabSize / 2), // réserve la place du bouton qui dépasse
        contentAlignment = Alignment.BottomCenter
    ) {

        // La barre
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight + navInset)
                .shadow(8.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(BarColor, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(bottom = navInset),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                if (tab == BottomTab.Creer) {
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    BottomItem(
                        tab = tab,
                        isSelected = tab == selected,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Le bouton Créer, par-dessus, à moitié hors de la barre
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(fabSize)
                    .shadow(10.dp, CircleShape)
                    .background(Accent, CircleShape)
                    .clip(CircleShape)
                    .clickable { onTabSelected(BottomTab.Creer) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = BottomTab.Creer.icon,
                    contentDescription = "Créer une recette",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}



/**
 * Élément individuel (onglet) de la barre de navigation.
 *
 * Modifie dynamiquement sa couleur (Accent ou Gris) et l'épaisseur de sa police
 * (Gras ou Medium) selon qu'il est sélectionné ou non.
 */
@Composable
private fun BottomItem(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isSelected) Accent else Color.Gray

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tab.label,
            fontSize = 11.sp,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}




/**
 * Espaceur utilitaire à placer à la toute fin des listes défilantes de l'application.
 *
 * Le [BottomMenu] ayant un design superposé avec un bouton central dépassant vers le haut,
 * il risque de masquer le dernier élément d'une liste (`LazyColumn` ou `verticalScroll`).
 * Ce composant insère un espace vide correspondant exactement à la hauteur de la barre
 * de navigation, incluant la portion saillante du bouton flottant et les Insets du système.
 */
@Composable
fun BottomBarSpacer() {
    Spacer(modifier = Modifier.height(72.dp + 30.dp))
    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
}