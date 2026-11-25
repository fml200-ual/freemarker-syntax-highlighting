<#macro miBoton texto clase="secundario">
    <button class="${clase}">${texto}</button>
</#macro>

<@miBoton texto="Guardar" clase="primario"/>
<@miBoton texto="Cancelar"/>

<@liferay.portlet url=volverURL>
    <p>contenido</p>
</@liferay.portlet>
