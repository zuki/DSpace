<%--
  - bulletin/issue.jsp
  -
  - Version: $Revision: 1.23 $
  -
  - Date: $Date: 2006/10/28 16:24:26 $
  -
  - This file was originally written by Agrex Co. for 
  -  HUSCAP (Hokkaido University Repository)
  -
  - Revised by SUZUKI Keiji
  -
  --%>

<%--
  - bulletin/issue JSP
  -
  - Attributes required:
  -    bulletin - Bulletin (Community)
  -    issue    - Issue (Collection)
  -    issues   - array of issues (Collection[])
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>

<%@ page import="org.dspace.content.Community" %>
<%@ page import="org.dspace.content.Collection" %>
<%@ page import="org.dspace.content.Item" %>
<%@ page import="org.dspace.content.Bitstream" %>
<%@ page import="org.dspace.content.DCValue" %>
<%@ page import="org.dspace.content.ItemIterator" %>
<%@ page import="org.dspace.core.Utils" %>
<%@ page import="org.dspace.core.ConfigurationManager" %>
<%@ page import="jp.zuki_ebetsu.dspace.util.ItemUtility" %>

<%!
    private static String[] getPage(ItemUtility iu)
    {
        String[] page = { "<br/>", "<br/>", "<br/>" };
        String spage = iu.getField("source", "spage");
        String epage = iu.getField("source", "epage");
          
        if (!spage.equals("") && !spage.equals(""))
        {
           page[0] = spage;
           page[1] = " - ";
           page[2] = epage;
        }
        else if (!spage.equals(""))
        {
           page[2] = spage;
        } 
        else if (!epage.equals(""))
        {
           page[2] = epage;
        } 

        return page;
    }

    private static String getAuthor(ItemUtility iu, String mode) 
    {
        String[] authors  = null;
        if (mode.equals("address"))
        {
           authors = iu.getFields("address", null);        
        }
        else
        {
           authors = iu.getAuthors(true);        
        }             
        if (authors == null || authors.length <= 0) return "";        
           
        StringBuffer author = new StringBuffer();
        for (int i=0; i<authors.length; i++) {
           author.append("; ").append(authors[i]);
        }
        return author.toString().substring(2);
    }
%>
<%
    // Retrieve attributes
    Community community = (Community) request.getAttribute("bulletin");
    Collection[] collections =
        (Collection[]) request.getAttribute("issues");
    Collection collection =
        (Collection) request.getAttribute("issue");
    String errortag = (String) request.getAttribute("error");

    // Put the metadata values into guaranteed non-null variables
    String name = community.getMetadataForLocale("name");
    String intro_bulletin = community.getMetadataForLocale("introductory_text");
    if (intro_bulletin == null) {
       intro_bulletin = "";
    }
    String intro_issue = collection.getMetadataForLocale("introductory_text");
    if (intro_issue == null) {
       intro_issue = "";
    }
    String copyright = community.getMetadataForLocale("copyright_text");
    if (copyright == null) {
       copyright = "";
    }
    String sidebar = community.getMetadataForLocale("side_bar_text");
    if (sidebar == null) {
       sidebar = "";
    }
    String mode = community.getMetadataForLocale("mode");
    if (mode == null) {
       mode = "standard";
    }

    String typeheader = "";

    Bitstream logo = community.getLogo();
%>

<dspace:layout locbar="commLink" title="<%= name %>">

<div class="well">
  <div class="row">
	<div class="col-md-10">
      <h2><%= name %></h2>
        <p><%= intro_bulletin %></p>      
        <p><%= intro_issue %></p>      
    </div>
<%  if (logo != null) { %>
	<div class="col-md-2">
        <img alt="Logo" src="<%= request.getContextPath() %>/retrieve/<%= logo.getID() %>" />
    </div>
<% } %>
  </div>
</div>

<div class="row">
  <div class="col-md-12">
    <form class="form-inline" role="form">
      <div class="col-md-4">
        <button type="submit" class="btn btn-info btn-block" name="submit_prev_issue">
          <span class="glyphicon glyphicon-chevron-left"></span>&nbsp;<fmt:message key="jsp.bulletin.list.previssue"/>
        </button>
      </div>
      <div class="col-md-4">
        <button type="submit" class="btn btn-primary btn-block" name="submit_list_issues">
          <fmt:message key="jsp.bulletin.list.button"/>
        </button>
      </div>
      <div class="col-md-4">
        <button type="submit" class="btn btn-info btn-block" name="submit_next_issue">
          <fmt:message key="jsp.bulletin.list.nextissue"/>&nbsp;<span class="glyphicon glyphicon-chevron-right"></span>
        </button>
      </div>
    </form>
  </div>
</div>

<% if ((errortag != null) && (errortag.length() > 0))
   {
%>
<div class="row">
  <div class="col-md-12">
<%
       if (errortag.equals("noprev"))
       {
%>
       <div class="col-md-12"><p class="text-danger"><strong><fmt:message key="jsp.bulletin.error.noprev"/></strong></p></div>
<%     }
       else if (errortag.equals("nonext"))
       { 
%>
      <div class="col-md-12"><p class="text-danger"><strong><fmt:message key="jsp.bulletin.error.nonext"/></strong></p></div>
<%     } %>
  </div>
</div>
<%
   }
%>


<%
    if (collection != null) 
    {
%>
<hr/>
<h3 class="text-center"><%= collection.getMetadataForLocale("name") %> <fmt:message key="jsp.bulletin.issue.toc"/></h3>  

<div class="row">
  <div class="col-md-12">
    <table class="table table-striped">
      <tbody>
<%
       ItemIterator iterator = collection.getItems();
       String colClass = "oddRowEvenCol";
       while (iterator.hasNext()) 
       {
          Item item = iterator.next();
          ItemUtility iu    = new ItemUtility(item);
          String title      = iu.getField("title", null);
          String author     = getAuthor(iu, mode);
          String[] pages    = getPage(iu);
          String type       = iu.getField("type", "publisher");
          colClass          = ( colClass.equals("oddRowEvenCol") 
                            ? "oddRowOddCol" : "oddRowEvenCol");
          if (!type.equals("") && !type.equals(typeheader))
          {
             typeheader = type;
             colClass = "oddRowOddCol";
%>
        <tr>
          <td class="text-left">【<%= type %>】</td>
        </tr>
<%
          }
%>

        <tr>
          <td class="text-left">
            <a href="<%= request.getContextPath() %>/handle/<%= item.getHandle() %>">
              <strong><%= title %></strong></a><br/>
<%        if (!author.equals("")) { %>
        &nbsp;&nbsp;<%= author %>
<%        } else {              %>
        <br/>
<%        }                     %>
          </td>
          <td class="text-right"><%= pages[0] %></td>
          <td class="text-center"><%= pages[1] %></td>
          <td class="text-right"><%= pages[2] %></td>
        </tr>
<%     }                        %>
      </tbody>
    </table>
<%  }                           %>
  </div>
</div>

  <p class="text-center"><%= copyright %></p>

</dspace:layout>

