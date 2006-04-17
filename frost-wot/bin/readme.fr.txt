* Frost - A propos de Freenet *
_-----------------------------_


Frost peut fonctionner avec Freenet 0.5 et Freenet 0.7. Vous choisissez lors
du premier démarrage la version de Freenet que vous utilisez. Chaque
installation de Frost ne peut fonctionner qu'avec une seule version de Freenet.
Vous devrez faire fonctionner deux version de Frost différentes si vous
souhaitez accéder aux deux réseaux, parce que le format des clés est différent.

Frost suppose qu'il est démarré pour la première fois s'il ne trouve pas le
fichier identities.xml. La fenêtre affichée lors du permier démarrage vous
permet de choisir la version de Freenet et, optionnellement, d'importer un
fichier identities.xml existant à partir d'une installation de Frost existante
(0.5 ou 0.7).


Mise à jour de Frost 0.5 ou 0.7 vers une nouvelle version:
----------------------------------------------------------
Si Frost est démarré, arrêtez-le, puis copiez le contenu du fichier ZIP
téléchargé dans le répertoire où est installé Frost, en écrasant tous les
fichiers existants. Tous vos paramètres seront conservés. Comme toujours,
il est recommandé de faire une sauvegarde avant la mise à jour. :)


Vous utilisez déja Frost 0.5 et vous voulez démarrer Frost 0.7 (ou vice versa):
-------------------------------------------------------------------------------
Copiez le contenu  du fichier ZIP téléchargé dans un NOUVEAU répertoire et
lancez Frost. Dans la première fenêtre de démarrage, choisissez la version
de Freenet que vous souhaitez utiliser et éventuellement le fichier
identities.xml que vous souhaitez importer d'une insatllation de Frost
précédente (il est recommandé d'arrêter l'ancien Frost avant l'importation).
Ou vous pouvez décider de créer une nouvelle identité.


En cas de problème:
-------------------
Frost suppose que votre noeud Freenet tourne sur la même machine, avec le numéro
de port FCP par défaut. Pour Freenet 0.5 c'est "127.0.0.1:8481", et pour
Freenet 0.7 c'est "127.0.0.1:9481". Si votre noeud Freenet tourne sur une autre
machine, ou si vous avez configuré FCP pour un autre numéro de port, le premier
démarrage échouera. Dans ce cas, vous devrez éditer le fichier "frost.ini" qui
se trouve dans le répertoire "config". Ce fichier est généré automatiquement
durant le premier démarrage de Frost. Ouvrez le fichier "frost.ini" et trouvez
la ligne contenant "availableNodes=127.0.0.1:8481". Changez le paramètre pour
qu'il corresponde à vos besoins ("availableNodes=autrenoeud:12345", par exemple),
et démarrez Frost.
Il devrait maintenant être capable de se connecter à votre noeud.
Faites attention à bien configurer votre noeud Freenet pour qu'il accepte les
connexions FCP en provenance d'autres machines que lui-même ! Après le démarrage
de Frost, vous pourrez à nouveau changer ce paramètre depuis la fenêtre de
configuration.

Si (et seulement si) vous avez choisi la mauvaise version de Freenet durant la
mise à jour, vous pouvez changer cela dans le fichier "frost.ini". Trouvez la
ligne "freenetVersion=" et mettez-y "freenetVersion=05" ou "freenetVersion=07".


Note à l'attention des UNIXiens:
--------------------------------
Après l'extraction du fichier ZIP, les fichiers "*.sh" ne seront pas exécutables
sur votre système. Exécutez un "chmod +x *.sh" dans le répertoire de Frost pour
remédier à ce problème.
