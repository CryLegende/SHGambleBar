<div align="center">

<img src="assets/logo.png" alt="SkyHand" width="140">

# SHGambleBar

*Bar a paris (remplacant de GambleBarPro) - Vault, EXP, animations, stats.*

<img src="https://img.shields.io/badge/Paper-1.20.2-f2a900?style=for-the-badge" alt="Paper 1.20.2"> <img src="https://img.shields.io/badge/Java-17-e76f00?style=for-the-badge" alt="Java 17"> <img src="https://img.shields.io/badge/SkyHand-Opale-3aa655?style=for-the-badge" alt="SkyHand Opale">

</div>

---

## Présentation

Bar a paris (remplacant de GambleBarPro) - Vault, EXP, animations, stats.

Utilisé sur : **Opale**.

## Dépendances

- Optionnelles : `Vault`, `PlaceholderAPI`

## Commandes

| Commande | Alias | Description | Permission |
|---|---|---|---|
| `/shgamblebar` | `/bar` | Ouvre le bar a paris. | — |

## Permissions

| Permission | Description | Par défaut |
|---|---|---|
| `shgamblebar.use` | Ouvrir un bar avec /bar. | `true` |
| `shgamblebar.bypass.cooldown` | Ignorer le cooldown entre deux fioles. | `op` |
| `shgamblebar.bypass.playtime` | Ouvrir un bar sans le temps de jeu minimum. | `op` |
| `shgamblebar.admin` | Acces a toutes les commandes d'administration. | `op` |
| `shgamblebar.admin.reload` | /shgamblebar reload | `op` |
| `shgamblebar.admin.give` | /shgamblebar give <joueur> <bar> <fiole> [quantite] | `op` |
| `shgamblebar.admin.open` | /shgamblebar open <joueur> [bar] | `op` |
| `shgamblebar.admin.stats` | /shgamblebar stats <joueur> | `op` |

## Configuration

Fichiers par défaut repris du serveur (secrets remplacés par `CHANGE_ME`) :

- `bars/mainbar.yml`
- `config.yml`
- `messages.yml`

## Compilation

Java 17 et Maven :

```bash
mvn clean package
```

Le jar est généré dans `target/`.

---

<div align="center">

<sub>Développé par <b>Cry_Legende</b> pour le serveur <b>SkyHand</b> · Tous droits réservés</sub>

</div>
