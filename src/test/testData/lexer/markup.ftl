<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>${titulo?upper_case}</title>
</head>
<body class="panel" data-id=${usuario.id}>
<p>Hola, ${usuario.nombre!"invitado"} &amp; bienvenido</p>
<a href="/detalle?id=${usuario.id}&modo=ver">detalle</a>
<!-- comentario html -->
<br/>
</body>
</html>
