package com.example.myr

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color



/**
 * Écran principal de l'application agissant comme conteneur de navigation racine.
 *
 * Ce composant "Stateful" (qui gère son propre état) orchestre la structure globale de l'UI
 * via un [Scaffold] et gère le routage entre les différents écrans de l'application.
 *
 * **Comportement de navigation :**
 * - Gère l'onglet actuellement sélectionné dans le [BottomMenu].
 * - Affiche le contenu correspondant à l'onglet actif (Accueil, Explorer, Créer, Planning, Courses).
 * - Implémente une logique de superposition (overlay) pour [BibliothequeEcran] :
 *   si la bibliothèque est ouverte, elle remplace le contenu de l'onglet courant.
 *   Changer d'onglet via le menu du bas ferme automatiquement la bibliothèque.
 *
 * **Design :**
 * Applique une couleur de fond globale (Off-white: #F8F9FA) à l'ensemble de l'application.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen() {
    // On retient quel onglet est sélectionné
    var tab by remember { mutableStateOf(BottomTab.Accueil) }
    val appBackground = Color(0xFFF8F9FA)
    var afficherBibliotheque by remember { mutableStateOf(false) }


    Scaffold(
        containerColor = appBackground,
        bottomBar = {
            BottomMenu(
                selected = tab,
                onTabSelected = {
                    afficherBibliotheque = false
                    tab = it }   // clique = onglet choisit
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (afficherBibliotheque) {
                BibliothequeEcran(onBack = { afficherBibliotheque = false })
            } else {
                when (tab) {
                    BottomTab.Accueil -> HomePage()
                    BottomTab.Explorer -> CategoriesEcran(
                        onOuvrirBibliotheque = { afficherBibliotheque = true }
                    )
                    BottomTab.Creer -> StockBatchEcran()
                    BottomTab.Planning -> PlanningEcran(onGenererCourses = { tab = BottomTab.Courses })
                    BottomTab.Courses -> GestionAlimentaireEcran()
                }
            }
        }
    }
}