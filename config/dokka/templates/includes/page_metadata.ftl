<#-- This changes the page title to "Pillarbox" on the homepage -->
<#-- https://github.com/Kotlin/dokka/blob/master/dokka-subprojects/plugin-base/src/main/resources/dokka/templates/includes/page_metadata.ftl -->
<#macro display>
    <#if pageName == "All modules">
        <title>Pillarbox</title>
    <#else>
        <title>${pageName}</title>
    </#if>
    <@template_cmd name="pathToRoot">
    <link href="${pathToRoot}images/logo-icon.svg" rel="icon" type="image/svg">
    </@template_cmd>
</#macro>
