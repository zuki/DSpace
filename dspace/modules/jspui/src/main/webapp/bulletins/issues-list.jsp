<%--
  - bulletin/issues-list.jsp
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
  - bulletin/issues-list JSP
  -
  - Attributes required:
  -    bulletin - Bulletin (Community)
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
<%@ page import="org.dspace.core.Context" %>
<%@ page import="org.dspace.core.Utils" %>
<%@ page import="org.dspace.core.LogManager" %>
<%@ page import="org.dspace.core.ConfigurationManager" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="org.dspace.app.webui.util.JSPManager" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.apache.log4j.Logger" %>

<%
    // Retrieve attributes
    Community community      = (Community) request.getAttribute("bulletin");
    Collection[] collections = (Collection[]) request.getAttribute("issues");

    // Put the metadata values into guaranteed non-null variables
    String name = community.getMetadataForLocale("name");
    String intro = community.getMetadataForLocale("introductory_text");
    if (intro == null) {
       intro = "";
    }
    String copyright = community.getMetadataForLocale("copyright_text");
    if (copyright == null) {
       copyright = "";
    }
    String sidebar = community.getMetadataForLocale("side_bar_text");
    if (sidebar == null) 
    {
       sidebar = "";
    }

    Bitstream logo = community.getLogo();
    
    boolean feedEnabled = ConfigurationManager.getBooleanProperty("webui.feed.enable");
    String feedData = "NONE";
    if (feedEnabled)
    {
        feedData = "comm:" + ConfigurationManager.getProperty("webui.feed.formats");
    }
%>

<dspace:layout locbar="commLink" title="<%= name %>" feedData="<%= feedData %>">
<div class="well">
  <div class="row">
	<div class="col-md-9">
        <h2><%= name %></h2>
        <p><%= intro %></p>
    </div>

<%  if (logo != null) { %>
    <div class="col-md-3">
      <img class="img-responsive" alt="Logo" src="<%= request.getContextPath() %>/retrieve/<%= logo.getID() %>" />
    </div>
<%  } %>
  </div>

  <div class="row">
    <div class="col-md-12">&nbsp</div>
  </div>

  <div class="row">
    <form class="form-inline" role="form" method="get" action="">
      <div class="col-md-8 col-md-offset-1">
        <div class="form-group">
          <label for="tlocation"><strong><fmt:message key="jsp.general.location"/></strong></label>
        </div>
        <div class="form-group">
          <select name="location" id="tlocation" class="form-control">
  	        <option value="/"><fmt:message key="jsp.general.genericScope"/></option>
            <option selected="selected" value="<%= community.getHandle() %>"><%= community.getMetadataForLocale("name") %></option>
          </select>
        </div>
        <div class="form-group">
          <label class="sr-only" for="tquery">Query</label>
  	      <input type="text" class="form-control" name="query" id="tquery" size="50" placeholder="Enter search query"/>
        </div>
        <div class="form-group">
          <label class="sr-only" for="sumit_search">Query</label>
   	      <button type="submit" class="btn btn-primary" name="submit_search" id="sumit_search"><fmt:message key="jsp.general.go"/></button>
        </div>
      </div>
    </form>
  
<%  if(feedEnabled) {
    	String[] fmts = feedData.substring(5).split(",");
    	String icon = null;
    	int width = 0;
%>
    <div class="col-md-3">
<%
    	for (int j = 0; j < fmts.length; j++)
    	{
    		if ("rss_1.0".equals(fmts[j]))
    		{
    		   icon = "rss1.gif";
    		   width = 80;
    		}
    		else if ("rss_2.0".equals(fmts[j]))
    		{
    		   icon = "rss2.gif";
    		   width = 80;
    		}
    		else
    	    {
    	       icon = "rss.gif";
    	       width = 36;
    	    }
%>
        <a href="<%= request.getContextPath() %>/feed/<%= fmts[j] %>/<%= community.getHandle() %>">
        <img src="<%= request.getContextPath() %>/image/<%= icon %>" alt="RSS Feed" width="<%= width %>" height="15" align="top" border="0" /></a>&nbsp;
<%      } %>
    </div>
  <%  } %>
  </div>
</div>

<%
    if (collections.length != 0) 
    {
%>
  <h3 class="text-center"><fmt:message key="jsp.bulletin.list.heading"/></h3>

  <div class="row">
	<div class="col-md-12">
      <table class="table table-striped">
        <thead>
          <tr>
            <th class="text-left"><fmt:message key="jsp.bulletin.list.header1"/></th>
            <th class="text-right"><fmt:message key="jsp.bulletin.list.header2"/></th>
          </tr>
        </thead>
        <tbody>
<%
       for (int i = 0; i < collections.length; i++) 
       {
           String pubdate = collections[i].getMetadataForLocale("short_description");
           if (!pubdate.equals("")) 
           {
               if (pubdate.indexOf("-") == -1)
               {
                   if (pubdate.length() > 6) 
                   {
                       pubdate =  pubdate.substring(0,4) + "-" + pubdate.substring(4,6) + "-" + pubdate.substring(6);
                   } 
                   else if (pubdate.length() > 4) 
                   {
                       pubdate =  pubdate.substring(0,4) + "-" + pubdate.substring(4);
                   }
               }
           }
%>
          <tr>
            <td class="text-left">
              <a href="<%= request.getContextPath() %>/handle/<%= community.getHandle() %>/bulletin/<%= collections[i].getHandle() %>">
                <%= collections[i].getMetadataForLocale("name") %>
              </a>
            </td>
            <td class="text-right"><%= pubdate %></td>
          </tr>
<%
       }
%>
        </tbody>
      </table>
    </div>
  </div>
<%
    }
%>

  <p class="text-center"><%= copyright %></p>

</dspace:layout>
