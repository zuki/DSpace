package jp.zuki_ebetsu.dspace.app.webui.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.dspace.core.Context;
import org.dspace.core.Constants;
import org.dspace.core.LogManager;
import org.dspace.content.Community;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.handle.HandleManager;
import org.dspace.app.webui.servlet.DSpaceServlet;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.app.webui.util.UIUtil;

/****************************************************************************
 * This program is originally written by K. Kobayashi in AGREX, Inc.,
 * Copyright 2006- AGREX.inc. 
 * and revised by Keiji Suzuki 
 * (C)2010- SUZUKI Keiji
 ***************************************************************************/
public class BulletinServlet extends DSpaceServlet 
{
    private static Logger log = Logger.getLogger(BulletinServlet.class);

    /**
     * @param context - a DSpace Context object
     * @param request - the Http request 
     * @param response - the Http response
     *
     * @exception javax.servlet.ServletException
     * @exception java.io.IOException
     */
    public void doDSGet(Context context, HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException 
    {
	doDSPost(context, request, response);
    }

    /**
     * @param context - a DSpace Context object
     * @param request - the Http request 
     * @param response - the Http response
     *
     * @exception javax.servlet.ServletException
     * @exception java.io.IOException
     */
    public void doDSPost(Context context, HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException 
    {
        Community    bulletin = null;
        Collection[] issues   = null;
        Collection   issue    = null;
        String       handle   = null;
        try 
        {
            /* ------------------------------------------------------------ * 
             * Get community id from URL
             *   if URL likes BaseURL/handle/(bulleint handle)/bulletin
             *      Community c = request.getAttribute("dspace.community");
             *   if URL likes BaseURL/handle/(bulleint handle)/bulletin/(issue handle)
             *      Community c = request.getAttribute("dspace.community");
             *      Collection cl = request.getAttribute("dspace.collection");
             * ------------------------------------------------------------ */ 

             // Community must be set
             bulletin = (Community) request.getAttribute("dspace.community");

             // change key name and reset
             request.setAttribute("bulletin", bulletin);
             
             issues = bulletin.getIssues();
             request.setAttribute("issues", issues);

             int iPath = request.getPathInfo().substring(1).indexOf("/");
             if (iPath == -1) 
             {  // Issue is not specified (Display issues list)
                if (!handleButton(context, request, response, bulletin, issues, handle)) 
                {
                    JSPManager.showJSP(request, response, "/bulletins/issues-list.jsp");
                }
            }
            else
            {   // Issue is specified (Display the issue)
                handle = request.getPathInfo().substring(1);
                DSpaceObject dso = HandleManager.resolveToObject(context, handle);
                if (dso == null) 
                {
                    log.warn(LogManager.getHeader(context, "invalide_handle", "handle=" + handle));
                    JSPManager.showInvalidIDError(request, response, handle, -1);
                }
                if (dso.getType() == Constants.COLLECTION) 
                {
                   issue = (Collection) dso;
                   request.setAttribute("issue", issue);
                } 
                else 
                {
                    log.warn(LogManager.getHeader(context, "invalide_handle_type", "handle: " + handle + " type: " + dso.getType()));
                    JSPManager.showInvalidIDError(request, response, handle, -1);
                }
                if (!handleButton(context, request, response, bulletin, issues, handle)) 
                {
                    JSPManager.showJSP(request, response, "/bulletins/issue.jsp");
                }
           }
        } 
        catch (Exception e) 
        {
            log.warn(LogManager.getHeader(context, "error", e.toString()));
            JSPManager.showInternalError(request, response);
        }
    }

    /**
     * Check to see if a browse or search button has been pressed on a community
     * or collection home page. If so, redirect to the appropriate URL.
     * 
     * @param request
     *            HTTP request
     * @param response
     *            HTTP response
     * @param handle
     *            Handle of the community/collection home page
     * 
     * @return true if a browse/search button was pressed and the user was
     *         redirected
     */

    private boolean handleButton(Context context, HttpServletRequest request,
            HttpServletResponse response, Community bulletin,
	    Collection[] issues, String handle) throws ServletException, IOException
    {
        String button   = UIUtil.getSubmitButton(request, "");
        String location = request.getParameter("location");
        String prefix   = "/";
        String url      = null;
        HashMap<String, Integer> handles = new HashMap<String, Integer>();

        // Set HashMap for sorting
        if ((issues != null) && (issues.length > 0)) 
        {
            for (int i=0; i<issues.length; i++) 
            {
                handles.put(issues[i].getHandle(), new Integer(i));
            }
        }

        /*
         * Work out the "prefix" to which to redirect If "/", scope is all of
         * DSpace, so prefix is "/" If prefix is a handle, scope is a community
         * or collection, so "/handle/1721.x/xxxx/" is the prefix.
         */
        if (location != null && !location.equals("/"))
        {
            prefix = "/handle/" + location + "/";
        }

        if (button.equals("submit_next_issue"))
        {
            // Redirect to previous issue
            int pos = ((Integer) handles.get(handle)).intValue();
            if ((pos > 0) && (pos < handles.size())) 
            {
                if (request.getAttribute("error") != null) 
                {
                    request.removeAttribute("error");
                }
                url = request.getContextPath()+"/handle/"+bulletin.getHandle()+"/bulletin/"+issues[pos-1].getHandle();
            } 
            else 
            {
                request.setAttribute("error", "nonext");
                return false;
            }
        }
        else if (button.equals("submit_prev_issue"))
        {
            // Redirect to next issue
            int pos = ((Integer) handles.get(handle)).intValue();
            if ((pos >= 0) && (pos < handles.size() - 1)) 
            {
                if (request.getAttribute("error") != null) 
                {
                    request.removeAttribute("error");
                }
                    url = request.getContextPath()+"/handle/"+bulletin.getHandle()+"/bulletin/"+issues[pos+1].getHandle();
                } 
                else 
                {
                    request.setAttribute("error", "noprev");
                    return false;
                }
        }
        else if (button.equals("submit_list_issues"))
        {
            JSPManager.showJSP(request, response, "/bulletins/issues-list.jsp");
            return true;
        }
        else if (button.equals("submit_search")
                || (request.getParameter("query") != null))
        {
            /*
             * Have to check for search button and query - in some browsers,
             * typing a query into the box and hitting return doesn't produce a
             * submit button parameter. Redirect to appropriate search page
             */
            url = request.getContextPath()
                    + prefix
                    + "simple-search?query="
                    + URLEncoder.encode(request.getParameter("query"),
                            Constants.DEFAULT_ENCODING);
        }

        // If a button was pressed, redirect to appropriate page
        if (url != null)
        {
            response.sendRedirect(response.encodeRedirectURL(url));
            return true;
        }

        return false;
    }

}
