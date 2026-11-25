[#ftl]
[#-- comentario con corchetes --]
[#assign nombre = "mundo"]
[#if nombre?has_content]
    <p>Hola, [=nombre]</p>
[/#if]
[#list items as item]
    <li>[=item.nombre]</li>
[/#list]
[@miBoton texto="Ok" /]
