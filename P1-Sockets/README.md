# Práctica 1. Sockets. Chat 1.0

## Enunciado
El objetivo es tener una primera versión que implemente un chat en modo texto (tanto servidor
como cliente), con una solución basada en sockets.
El chat permitirá la comunicación remota entre varios clientes, utilizando un servidor central
que recoge los mensajes, y reenvía los mismos a todos los participantes.
La implementación se realizará con sockets Java, de tipo TCP, orientados a conexión. Por lo
tanto, el servidor se implementará siguiendo un modelo push, donde el servidor guarda registro
de los clientes, y reenvía los mensajes recibidos, uno a uno, a todos los participantes actuales.
Por motivos de simplicidad, los usuarios se registran con un apodo (nickname) y se supondrá
que nunca se registran dos usuarios con el mismo.
Se manejarán dos tipos básicos de mensaje:
- texto libre: texto introducido por el cliente para retrasmitir a todos los otros clientes conectados
- logout: comando para cerrar la conexión y salir de la aplicación cliente.
Por otro lado, un usuario (cliente) tendrá la posibilidad de bloquear los mensajes de otro usuario
incluso si el usuario a bloquear no está todavía conectado. Para realizar este bloqueo se implementará el comando “ban”, pudiendo volver a aceptar sus mensajes con el comando “unban”.
Cuando un usuario haya sido bloqueado, simplemente no se mostrarán sus mensajes. Ejemplo:
- ban usuariobaneado
- unban usuariounbaneado
Cuando un usuario bloquea a otro, se deberá comunicar este hecho al servidor a través de un
mensaje del tipo “usuarioactual ha baneado a usuariobaneado”.


## Ejecución

Ejecutar el servidor sin argumentos:
  * Desde la consola de comandos posicionarnos en el directorio del proyecto server `cd "Ruta a los proyectos"\server`
  * Ejecutar `mvn exec:java`

Ejecutar el cliente:
  * Desde la consola de comandos posicionarnos en el directorio del proyecto client `cd "Ruta a los proyectos"\client`
  * Ejecutar `mvn exec:java -Dexec.args="<nickname>"` si no se pasa el hostname del servidor o `mvn exec:java -Dexec.args="<hostname>" -Dexec.args="<nickname>"` si se quiere pasar el hostname del servidor.

Si se pasa un hostname al lanzar el cliente se tratará de conectar a ese servidor. En caso contrario el hostname por defecto es localhost.
