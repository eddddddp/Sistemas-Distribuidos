Ejercicio 2. Proyecto con Maven
Objetivo
1. Crear un proyecto desde consola con el arquetipo maven-archetype-quickstart.
Importar el proyecto en Eclipse.
2. Crear un proyecto directamente Eclipse integrado con Maven (tipo Maven Project). Una
vez integrado compilar, generar la documentación y empaquetar los binarios (.class) en un
fichero .jar. (Añadir por lo menos un fichero fuente, con una clase simple para tener
realmente algún .class).
3. Convertir un proyecto Eclipse tradicional en uno Maven con la opción del menú contextual
sobre el proyecto Configure>Convert to Maven Project.

Se crea el proyecto y se añade el plugin para generación de documentación Javadoc.

Instrucciones de uso.

Desde la consola del sistema o cmd posicionarnos en el directorio que contiene el pom.xml.

$ cd "Mi_Directorio"/myMavenProyect

Sustituir "Mi_Directorio" por la ruta del directorio que contiene el proyecto Maven.

Para compilar el proyecto usando Maven se ejecuta la siguiente línea en el cmd.

$ mvm compile

Para empaquetar los binarios en un .jar se ejecuta la siguiente línea.

$ mvn package

Para generar la documentación del proyecto se ejecuta la siguiente línea.

$ mvn javadoc:javadoc
