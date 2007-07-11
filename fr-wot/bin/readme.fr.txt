* Frost - A propos de Freenet *
_-----------------------------_


Frost peut fonctionner avec Freenet 0.5 et Freenet 0.7 mais une même installation ne peut pas utiliser les deux réseaux conjointement. Lors du premier lancement, la fenêtre affichée vous permet de choisir à quelle version de Freenet se connecter et, optionnellement, d'importer d'anciennes identités à partir d'un fichier identities.xml existant.

ATTENTION: Vous devriez TOUJOURS conserver une copie de sauvegarde de vos identités. Vous pouvez dorénavant les exporter à partir de l'interface.


Mise à jour depuis Frost version 23-Dec-2006 ou plus récent:
----------------------------------------------------------

Si Frost est lancé, arrêtez-le puis copiez le contenu du fichier ZIP téléchargé dans le répertoire où il est installé ; en écrasant tous les fichiers existants. Tous vos paramètres seront conservés. Comme toujours, il est recommandé de faire une sauvegarde avant la mise à jour. :)


Mise à jour à partir d'une version plus ancienne que 23-Dec-2006:
-------------------------------------------------------------------------------

Il n'existe pas de procédure de mise à jour : Installez la version 23-Dec-2006 en suivant sa procédure de mise à jour puis faites la mise à jour vers la nouvelle version.


Vous utilisez déja Frost 0.5 et vous voulez démarrer Frost 0.7 (ou vice versa):
-------------------------------------------------------------------------------

Copiez le contenu  du fichier ZIP téléchargé dans un NOUVEAU répertoire et lancez Frost. Dans la première fenêtre de démarrage, choisissez la version de Freenet que vous souhaitez utiliser et éventuellement le fichier identities.xml que vous souhaitez importer d'une installation précédente (il est recommandé d'arrêter l'ancien Frost avant l'importation). Ou vous pouvez décider de créer une nouvelle identité.


En cas de problème:
-------------------
Frost suppose que votre noeud Freenet tourne sur la même machine, avec le numéro de port FCP par défaut. Pour Freenet 0.5 c'est "127.0.0.1:8481", et pour Freenet 0.7 c'est "127.0.0.1:9481". Si votre noeud Freenet tourne sur une autre machine, ou si vous avez configuré FCP pour un autre numéro de port, le premier démarrage échouera. Dans ce cas, vous devrez éditer le fichier "frost.ini" qui se trouve dans le répertoire "config". Ce fichier est généré automatiquement durant le premier démarrage de Frost. Trouvez la ligne commençant par "availableNodes=" et changez ce paramètre pour qu'il corresponde à votre configuration, puis relancez Frost.
Frost devrait maintenant être capable de se connecter à votre noeud.
Faites attention à bien configurer votre noeud Freenet pour qu'il accepte les connexions FCP en provenance d'autres machines que lui-même si nécessaire ! Après le démarrage de Frost, vous pourrez à nouveau changer ce paramètre depuis la fenêtre de configuration.

Si (et seulement si) vous avez choisi la mauvaise version de Freenet durant la mise à jour, vous pouvez changer cela en éditant le fichier "frost.ini". Trouvez la ligne "freenetVersion=" et mettez-y "05" ou "07".


Note à l'attention des UNIXiens:
--------------------------------
Après l'extraction de l'archive ZIP, les fichiers "*.sh" peuvent ne pas avoir le bit exécutable positioné. Exécutez la commande "chmod +x *.sh" depuis le répertoire de Frost pour remédier au problème.
