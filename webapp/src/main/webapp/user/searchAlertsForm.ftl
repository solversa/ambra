<#include "/includes/global_variables.ftl">

<#function isFound collection value>
  <#list collection as element>
    <#if element = value>
      <#return "true">
    </#if>
  </#list>
  <#return "false">
</#function>

<form action="/user/secure/profile/alerts/search/save" method="post" class="ambra-form" title="Search Alert Form" name="userSearchAlerts">
  <fieldset id="alert-form">
    <legend>Manage your search alert emails</legend>
    <ol>
      <#if userSearchAlerts?has_content>
        <li>
          <span class="search-alerts-title">&nbsp;</span>
          <ol>
            <li class="search-alerts-weekly">
              <label for="checkAllWeeklySavedSearch">
                <input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" id="checkAllWeeklySavedSearch"/> Select All
              </label>
            </li>
            <li class="search-alerts-monthly">
              <label for="checkAllMonthlySavedSearch">
                <input type="checkbox" value="checkAllMonthly" name="checkAllMonthly" id="checkAllMonthlySavedSearch"/>
                Select All
              </label>
            </li>
            <li class="search-alerts-delete">
              <label for="checkAllDeleteSavedSearch">
                <input type="checkbox" value="checkAllDelete" name="checkAllDelete" id="checkAllDeleteSavedSearch"/> Select All
              </label>
            </li>
          </ol>
        </li>
      </#if>
      <#list userSearchAlerts as ua>
        <li>
          <span class="search-alerts-title">${ua.searchName}</span>
          <ol>
            <li class="search-alerts-weekly">
              <label for="${ua.savedSearchId}">
                <@s.checkbox name="weeklyAlerts" fieldValue="${ua.savedSearchId?c}" value="${ua.weekly?string}"/>
                Weekly </label>
            </li>

            <li class="search-alerts-monthly">
              <label for="${ua.savedSearchId}">
                <@s.checkbox name="monthlyAlerts" fieldValue="${ua.savedSearchId?c}" value="${ua.monthly?string}"/>
                Monthly </label>
            </li>

            <li class="search-alerts-delete">
              <label for="${ua.savedSearchId}">
                <@s.checkbox name="deleteAlerts" fieldValue="${ua.savedSearchId?c}" value="false"/>
                Delete </label>
            </li>
          </ol>
        </li>
      </#list>
    </ol>
    <#if userSearchAlerts?has_content>
      <div class="btnwrap"><input type="submit" id="formSubmit" class="btn primary" name="formSubmit" value="Save" tabindex="99"/></div>
    <#else>
      You have no search alerts defined.  You can create one by performing a <a href="/search/advanced">search</a> on our site and save it by clicking on the "Search Alert" button.
    </#if>
  </fieldset>

</form>

