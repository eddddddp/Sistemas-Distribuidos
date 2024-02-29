Ejercicio propuesto en la sesión 2 de prácticas de la asignatura sistemas distribuidos.

## **Ejemplo 1. Eco (Echo)**
Ejemplo de cliente-servidor con TCP, donde el servidor responde con el mismo texto enviado por el
cliente, realizando el típico eco (echo).
Se debe observar con cuidado que el servidor sólo admite un cliente (se bloquea en espera de la
primera petición), quedando pendiente como ejercicio modificar el código para permitir múltiples
clientes (lanzando un hilo por petición aceptada).

Sobre el ejemplo de Cliente de Socket (Eco) y Servidor Multihilo, implementar una lista negra en el servidor
que permita bloquear conexiones desde diferentes orígenes (Puerto de Origen). 
La lista se definirá como un array dentro del código del servidor.

## **Instrucciones de ejecución**
Abrir tantas ventanas de cmd como clientes se vayan a utilizar y otra adicional para el servidor.
En cada ventana del terminal posicionarnos en el directorio que contiene el pom.xml correspondiente 
a cada proyecto (EchoSocketClient o EchoSocketServer).
Ejecutar "mvn exec:java" en el terminal posicionado en el directorio de EchoSocketServer.
Ejecutar "mvn exec:java" en los terminales posicionados en el directorio EchoSocketClient.

Se rechazan las conexiones desde los puertos de origen del siguiente intervalo: [50450, 50549].
