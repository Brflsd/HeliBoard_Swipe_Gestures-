# HeliFlick

**Un clavier Android rapide aux gestes, avec tous les emojis récents, 100 % hors ligne.**
*A fast gesture keyboard for Android with all the latest emojis — 100 % offline.* ([English below](#english))

Vous utilisiez **Fleksy** et il n'est plus mis à jour depuis 2023 ? HeliFlick reprend les gestes qui faisaient sa force — espace, effacement et correction d'un simple glissement — sur la base moderne et maintenue de [HeliBoard](https://github.com/HeliBorg/HeliBoard).

> HeliFlick est une **version non officielle** de HeliBoard, indépendante de ses auteurs. Il n'est pas non plus affilié à Fleksy ; ce nom est cité uniquement pour décrire la façon de taper dont HeliFlick s'inspire.

[**⬇ Télécharger la dernière version (APK)**](../../releases/latest)

## Pourquoi HeliFlick

- **Gestes façon Fleksy** : tapez sans viser la barre d'espace ni la touche effacer.
- **Tous les emojis récents** : Unicode 17 inclus (🫩 🫆 🫪 …), mis à jour avec HeliBoard.
- **Vie privée** : l'application n'a **pas l'autorisation d'accéder à Internet**. Rien de ce que vous tapez ne peut quitter votre téléphone par le clavier.
- **Multilingue** : français, anglais, allemand, portugais et des dizaines d'autres langues, avec correction automatique et suggestions.
- **Libre** : code source ouvert (GPL-3.0), compilé publiquement par GitHub.

## Les gestes

| Geste | Effet |
|---|---|
| Glisser **à droite** sur les lettres | Espace (deux fois : point) |
| Glisser **à gauche** sur les lettres | Effacer le mot précédent |
| Glisser **vers le bas** après un mot | Suggestion suivante |
| Glisser **vers le haut** après un mot | Annuler la correction (mot tapé) |
| Glisser **vers le haut** une 2ᵉ fois | Ajouter le mot au dictionnaire personnel (📖) |
| **Deux pouces** vers le bas / le haut | Cacher / afficher la rangée de la barre d'espace |
| **Maj** → glisser vers le haut | Emojis |
| **Maj** → glisser à droite | Clavier des symboles |
| **Effacer** → glisser vers le haut | Retour à la ligne (ou Entrée dans une recherche / un formulaire) |
| Glisser **horizontalement sur la barre du haut** | Pages : chiffres, raccourcis, copier/coller, disposition |

**Appui long** sur une lettre : accents et symboles (é è ê, & _ …). Sans lever le doigt, glissez sur une autre lettre pour voir ses variantes. Appui long sur **b** : point et virgule.

**Personnalisation** : couleur du clavier et de la rangée du milieu, taille de la barre d'outils, raccourcis texte (adresse e-mail, téléphone…), QWERTY / QWERTZ / AZERTY.

## Installation

1. Sur votre téléphone, téléchargez le fichier `.apk` de la [dernière version](../../releases/latest).
2. Ouvrez-le et autorisez l'installation depuis cette source si Android le demande.
3. **Paramètres → Système → Clavier → Clavier à l'écran** : activez **HeliFlick**.
4. Dans les réglages de HeliFlick, **Langues et dispositions** : ajoutez vos langues.

HeliFlick s'installe **à côté** de HeliBoard ou d'un autre clavier, sans les remplacer. Les mises à jour s'installent par-dessus la version précédente.

**Sécurité** : n'installez HeliFlick que depuis la page [Releases](../../releases) de ce dépôt. Chaque version y est compilée automatiquement depuis le code source visible ici, et signée avec une clé qui n'est jamais publiée.

## Crédits et licence

HeliFlick est une version modifiée de [HeliBoard](https://github.com/HeliBorg/HeliBoard) (Helium314 et contributeurs), lui-même basé sur OpenBoard et le clavier AOSP. Tout le travail de fond — dictionnaires, correction, dispositions, emojis — vient de ces projets ; le [README d'origine](README-HeliBoard.md) détaille leurs fonctionnalités et leurs auteurs.

Les modifications de HeliFlick (gestes, barre d'outils en pages, couleurs, raccourcis) ont été écrites avec l'aide d'une IA (Claude) et testées à la main. Merci de ne **pas** signaler les problèmes de HeliFlick au projet HeliBoard : ouvrez plutôt une [issue ici](../../issues).

Licence : [GPL-3.0](LICENSE), comme HeliBoard.

---

## English

**HeliFlick** is an unofficial fork of [HeliBoard](https://github.com/HeliBorg/HeliBoard) that brings back the gesture typing style of the discontinued Fleksy keyboard (not affiliated): swipe right for space, left to delete a word, up/down to cycle corrections, two thumbs to hide the space row, plus a swipeable toolbar (numbers, text shortcuts, clipboard, layout). It includes all recent emojis (Unicode 17), has **no internet permission**, and is licensed under GPL-3.0.

[**⬇ Download the latest APK**](../../releases/latest) · Settings → System → Keyboard → On-screen keyboard → enable **HeliFlick**.

The HeliFlick-specific settings are currently in French only.
