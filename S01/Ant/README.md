Ejercicio 1. Script con ant

Objetivo

Crear un script (build.xml) en un proyecto Eclipse que permita compilar, generar la
documentación y empaquetar los binarios (.class) en un fichero .jar suponiendo que nuestro
proyecto tiene una carpeta en el directorio raíz del proyecto src a partir de la cual se encuentran
todos los ficheros fuente.
Para ello se deben utilizar las tareas: javac, javadoc y jar

Se implementa la solución el fichero buil.xml usando eclipse 2022-12 (4.26.0) y JDK8 sobre Windows 10.

Instrucciones de uso.

Desde la consola de comandos el sistema o cmd nos posicionamos en el directorio que contiene el fichero buil.xml.
Para ejecutar todas las tareas (compilar, empaquetar y documentar) se ejecuta lo siguiente:

$ ant

Esto ejecutará la tarea por defecto, allTask. Tambien se obtiene el mismo resultado ejecutando directamente la tarea allTask.


$ ant allTask

Para ejecutar la tarea compile, que compila el proyecto Java se ejecuta la siguiente línea.

$ ant compile

Para empaquetar los binarios en un .jar se ejecuta la siguiente línea.

$ ant zip

Esta tarea, al requerir que el proyecto ya se haya compilado ejecutará previamente la tarea compile antes de ejecutar la tarea zip.
Para generar la documentación Javadoc se ejecuta la tarea genDoc ejecutando la siguiente línea.

$ ant genDoc
