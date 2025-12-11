package com.smartlogi.sdms.domain.model.vo;

import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object représentant un numéro de téléphone international.
 * Immuable et encapsule les règles de formatage et de validation des numéros.
 * Utilisé par ClientExpéditeur, Destinataire et Livreur.
 * <p>
 * Utilisation du type 'record' pour garantir l'immutabilité et générer
 * automatiquement les méthodes equals/hashCode/toString (égalité basée sur la valeur).
 */
@Embeddable

public record Telephone(String codePays, String nombre) {

    // Regex simplifiée pour un numéro de téléphone au Maroc (+212 et 9 chiffres de 5 à 8)
// La suppression du \n final corrige la validation
    private static final Pattern MAROC_PATTERN = Pattern.compile("^\\+212[5-8]\\d{8}$");    private static final String DEFAULT_MAROC_CODE = "+212";
    // Le \n final a été supprimé
    /**
     * Constructeur compact (Canonical Constructor) pour valider et normaliser les données.
     * Les champs sont implicitement final.
     *
     * @param codePays Le code pays (ex : "+212"). Peut être null/vide si le numéro commence par '+'.
     * @param nombre   Le numéro local ou le numéro complet.
     */
    public Telephone {
        Objects.requireNonNull(nombre, "Le numéro de téléphone est obligatoire.");
        String cleannombre = nombre.trim().replaceAll("[\\s-()]+", ""); // Nettoyage des espaces et symboles

        if (cleannombre.isEmpty()) {
            throw new IllegalArgumentException("Le numéro de téléphone ne peut être vide.");
        }

        String nombreComplet;
        String finalCodePays;
        String finalnombre;

        // 1. Détermination du numéro complet et du code pays
        if (cleannombre.startsWith("+")) {
            // Le numéro est déjà au format international
            nombreComplet = cleannombre;
        } else {
            // Le numéro est local, on ajoute le code pays fourni ou celui par défaut
            String determinedCodePays = (codePays != null && !codePays.trim().isEmpty())
                    ? codePays.trim()
                    : DEFAULT_MAROC_CODE;

            if (!determinedCodePays.startsWith("+")) {
                determinedCodePays = "+" + determinedCodePays; // Assurer le préfixe '+'
            }
            nombreComplet = determinedCodePays + cleannombre;
        }

        // 2. Validation métier (on vérifie que c'est un numéro marocain pour SDMS)
        if (!MAROC_PATTERN.matcher(nombreComplet).matches()) {
            throw new IllegalArgumentException("Format de numéro de téléphone invalide ou non marocain (attendu: +212XXXXXXXXX).");
        }

        // 3. Normalisation pour le stockage (les champs du record sont implicitement assignés ici)
        // Note: Dans un record, on peut réassigner les champs ici (shadowing), mais il est plus propre
        // de laisser les champs du record prendre les valeurs du constructeur s'ils sont déjà normalisés.
        // Ici, nous faisons le calcul, puis nous assignons aux variables locales (qui sont les paramètres
        // du constructeur) pour qu'elles deviennent les valeurs du record.

        finalCodePays = nombreComplet.substring(0, nombreComplet.length() - 9);
        finalnombre = nombreComplet.substring(nombreComplet.length() - 9);

        codePays = finalCodePays;
        nombre = finalnombre;

        // Cette ligne est nécessaire dans le constructeur compact pour que les champs du record
        // prennent les valeurs calculées, car les paramètres du constructeur compact sont les champs.
        // NOTE: Si vous voulez le comportement de normalisation complet, utilisez this(finalCodePays, finalnombre)
        // dans un constructeur complet, mais ici nous utilisons la forme compacte avec un léger hack :

        // Simplement, nous forçons les paramètres à être les valeurs normalisées (si les valeurs d'entrée n'étaient pas utilisées)
        // Dans ce cas, nous devons utiliser un constructeur complet ou s'assurer que les paramètres passés sont les valeurs finales souhaitées.

        // Pour un record, le plus simple est de s'assurer que les valeurs passées au constructeur *sont* les valeurs validées :
        // Le code suivant est le comportement "automatique" du record, que nous évitons en utilisant le constructeur compact.

        // Pour que la normalisation fonctionne correctement dans un record, le plus sûr est de passer
        // les valeurs normalisées au constructeur, ou d'utiliser un constructeur complet.

        // SOLUTION POUR LE CONSTRUCTEUR COMPACT:
        // Puisque nous ne pouvons pas réassigner les champs directement (ils sont finaux), nous devons
        // utiliser des variables locales pour effectuer le calcul et vérifier la validité, puis s'assurer
        // que les paramètres initiaux correspondent aux valeurs finales.
        // Puisque le record est conçu pour la simplicité, nous allons utiliser un constructeur complet avec le compact
        // pour gérer la normalisation. Laissez le compact pour la validation uniquement.
    }

    // Le code du record ci-dessus est valide, mais nécessite d'utiliser une logique d'usine
    // ou un constructeur privé pour faire la normalisation complète.
    // Laissons-le comme ceci en se concentrant sur la validation dans le constructeur compact.

    // On utilise les accesseurs générés (getCodePays(), getnombre())
    // et on ajoute la méthode utilitaire :

    /**
     * Retourne le numéro de téléphone au format international complet (ex: +212612345678).
     */
    public String getnombreComplet() {
        return this.codePays + this.nombre;
    }
}
