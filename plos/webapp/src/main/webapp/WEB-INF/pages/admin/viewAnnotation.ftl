<html>
  <head>
    <title>PLoS ONE: Administration: Annotation Details</title>
  </head>
  <body>
    <h1 style="text-align: center">PLoS ONE: Administration: Annotation Details</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">
      <@s.a href="${adminTop}">Admin Top</@s.a>
    </p>
    <hr/>

    <#include "templates/messages.ftl">

    <fieldset>
      <legend><b>Annotation Details</b></legend>
	<table width="100%">
          <tr><td width="100px">&nbsp;</td><td/></tr>
          <tr><td>Id</td><td>${annotation.id}</td></tr>
          <tr><td>Title</td><td>${annotation.commentTitle}</td></tr>
          <tr><td>Created</td><td>${annotation.createdAsDate?datetime}</td></tr>
          <tr><td>Creator</td><td><a href="../user/showUser.action?userId=${annotation.creator}">${annotation.creator}</a></td></tr>
          <tr>
            <td colspan="2">
              <fieldset><legend><b>Content</b></legend>${annotation.comment}</fieldset>
            </td>
          </tr>
        </table>
    </fieldset>

  </body>
</html>
