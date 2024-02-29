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
