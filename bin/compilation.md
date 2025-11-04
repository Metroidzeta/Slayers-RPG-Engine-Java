Les fichiers .class seront ici une fois le programme compilé.

Pour compiler sous Windows, à partir du dossier racine :  
```javac -cp "libs/*" -d bin src/core/*.java src/core/ressources/*.java src/core/events/*.java```  
Puis exécuter :  
```java -cp "bin;libs/*" core.Config```  