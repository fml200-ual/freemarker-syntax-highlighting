<#ftl output_format="HTML">
<#assign titulo = "Panel de " + usuario.nombre>
<#assign total = 0>
<#if usuario?has_content>
    <#list listaItems as item>
        <p>${item?index + 1}. ${item.nombre} - ${item.precio?string("0.00")} EUR</p>
        <#assign total = total + item.precio>
    <#else>
        <p>Sin elementos.</p>
    </#list>
<#elseif modo == "debug">
    <p>Debug</p>
<#else>
    <p class="aviso">Sin usuario.</p>
</#if>
<#include "/pie.ftl">
