* Frost - A propos de Freenet *
_-----------------------------_


Frost peut fonctionner avec Freenet 0.5 et Freenet 0.7 mais une m�me installation ne peut pas utiliser les deux r�seaux conjointement. Lors du premier lancement, la fen�tre affich�e vous permet de choisir � quelle version de Freenet se connecter et, optionnellement, d'importer d'anciennes identit�s � partir d'un fichier identities.xml existant.

ATTENTION: Vous devriez TOUJOURS conserver une copie de sauvegarde de vos identit�s. Vous pouvez dor�navant les exporter � partir de l'interface.


Mise � jour depuis Frost version 23-Dec-2006 ou plus r�cent:
----------------------------------------------------------

Si Frost est lanc�, arr�tez-le puis copiez le contenu du fichier ZIP t�l�charg� dans le r�pertoire o� il est install� ; en �crasant tous les fichiers existants. Tous vos param�tres seront conserv�s. Comme toujours, il est recommand� de faire une sauvegarde avant la mise � jour. :)


Mise � jour � partir d'une version plus ancienne que 23-Dec-2006:
-------------------------------------------------------------------------------

Il n'existe pas de proc�dure de mise � jour : Installez la version 23-Dec-2006 en suivant sa proc�dure de mise � jour puis faites la mise � jour vers la nouvelle version.


Vous utilisez d�ja Frost 0.5 et vous voulez d�marrer Frost 0.7 (ou vice versa):
-------------------------------------------------------------------------------

Copiez le contenu  du fichier ZIP t�l�charg� dans un NOUVEAU r�pertoire et lancez Frost. Dans la premi�re fen�tre de d�marrage, choisissez la version de Freenet que vous souhaitez utiliser et �ventuellement le fichier identities.xml que vous souhaitez importer d'une installation pr�c�dente (il est recommand� d'arr�ter l'ancien Frost avant l'importation). Ou vous pouvez d�cider de cr�er une nouvelle identit�.


En cas de probl�me:
-------------------
Frost suppose que votre noeud Freenet tourne sur la m�me machine, avec le num�ro de port FCP par d�faut. Pour Freenet 0.5 c'est "127.0.0.1:8481", et pour Freenet 0.7 c'est "127.0.0.1:9481". Si votre noeud Freenet tourne sur une autre machine, ou si vous avez configur� FCP pour un autre num�ro de port, le premier d�marrage �chouera. Dans ce cas, vous devrez �diter le fichier "frost.ini" qui se trouve dans le r�pertoire "config". Ce fichier est g�n�r� automatiquement durant le premier d�marrage de Frost. Trouvez la ligne commen�ant par "availableNodes=" et changez ce param�tre pour qu'il corresponde � votre configuration, puis relancez Frost.
Frost devrait maintenant �tre capable de se connecter � votre noeud.
Faites attention � bien configurer votre noeud Freenet pour qu'il accepte les connexions FCP en provenance d'autres machines que lui-m�me si n�cessaire ! Apr�s le d�marrage de Frost, vous pourrez � nouveau changer ce param�tre depuis la fen�tre de configuration.

Si (et seulement si) vous avez choisi la mauvaise version de Freenet durant la mise � jour, vous pouvez changer cela en �ditant le fichier "frost.ini". Trouvez la ligne "freenetVersion=" et mettez-y "05" ou "07".


Note � l'attention des UNIXiens:
--------------------------------
Apr�s l'extraction de l'archive ZIP, les fichiers "*.sh" peuvent ne pas avoir le bit ex�cutable position�. Ex�cutez la commande "chmod +x *.sh" depuis le r�pertoire de Frost pour rem�dier au probl�me.

Note pour des utilisateurs de beryl:
---------------------
Si vous employez le beryl, vous devez ajouter une ligne dans votre frost.sh habituellement
situé dans votre annuaire de gel ~/Freenet/frost
 
  export AWT_TOOLKIT="MToolkit"
 
Il devrait ressembler à ceci:
  [...]
  cd $PROGDIR
  
  export AWT_TOOLKIT="MToolkit"
  java -jar frost.jar "$@"
  [...]
