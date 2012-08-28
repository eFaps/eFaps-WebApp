/*
 * Copyright 2003 - 2012 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The servlet shows the help.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class HelpServlet
    extends HttpServlet
{
    /**
     * Key to store the menu in the session context of the user.
     */
    public static final String MENU_SESSION_KEY = "eFapsHelpServletMenu";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HelpServlet.class);

    /**
     * Defaut serial Number.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The method checks the file from the object out and returns them in a
     * output stream to the web client. The object id must be given with
     * paramter {@link #PARAM_OID}.<br/>
     *
     * @param _req request variable
     * @param _res response variable
     * @throws ServletException on error
     */
    @Override
    protected void doGet(final HttpServletRequest _req,
                         final HttpServletResponse _res)
        throws ServletException
    {
        HelpServlet.LOG.debug("Recieved Request for Help Servlet: {}", _req);
        try {
            final List<String> wikis = new ArrayList<String>();
            String path = _req.getPathInfo().substring(1);
            final String end = path.substring(path.lastIndexOf("."), path.length());
            if (end.equalsIgnoreCase(".png") || end.equalsIgnoreCase(".jpg") || end.equalsIgnoreCase(".jpeg")
                            || end.equalsIgnoreCase(".gif")) {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.WikiImage);
                queryBldr.addWhereAttrEqValue("Name", path);
                final MultiPrintQuery multi = queryBldr.getPrint();
                multi.addAttribute("FileLength");
                multi.execute();
                if (multi.next()) {
                    final Long length = multi.<Long>getAttribute("FileLength");
                    final Checkout checkout = new Checkout(multi.getCurrentInstance());
                    _res.setContentType(getServletContext().getMimeType(end));
                    _res.setContentLength(length.intValue());
                    _res.setDateHeader("Expires", System.currentTimeMillis() + (3600 * 1000));
                    _res.setHeader("Cache-Control", "max-age=3600");
                    checkout.execute(_res.getOutputStream());
                    checkout.close();
                }
            } else {
                if (!path.contains(".")) {
                    String referer = _req.getHeader("Referer");
                    if (referer.contains(":")) {
                        final String[] paths = referer.split(":");
                        referer = paths[0];
                    }
                    final String[] pack = referer.substring(referer.lastIndexOf("/") + 1).split("\\.");
                    final StringBuilder newPath = new StringBuilder();
                    for (int i = 0; i < pack.length - 2; i++) {
                        newPath.append(pack[i]).append(".");
                    }
                    newPath.append(path).append(".wiki");
                    path = newPath.toString();
                    wikis.add(path);
                } else if (path.contains(":")) {
                    final String[] paths = path.split(":");
                    for (final String apath : paths) {
                        wikis.add(apath);
                    }
                } else {
                    wikis.add(path);
                }

                final String menuStr;
                if (Context.getThreadContext().containsSessionAttribute(HelpServlet.MENU_SESSION_KEY)) {
                    menuStr = (String) Context.getThreadContext().getSessionAttribute(HelpServlet.MENU_SESSION_KEY);
                } else {
                    menuStr = getMenu();
                    Context.getThreadContext().setSessionAttribute(HelpServlet.MENU_SESSION_KEY, menuStr);
                }

                final StringBuilder html = new StringBuilder();
                html.append("<html><head>")
                    .append("<script type=\"text/javascript\" src=\"../../wicket/resource/")
                    .append(AbstractDojoBehavior.JS_DOJO.getScope().getName()).append("/")
                    .append(AbstractDojoBehavior.JS_DOJO.getName())
                    .append("\" data-dojo-config=\"async: true,parseOnLoad: true\"></script>\n")
                    .append("<script type=\"text/javascript\" >")
                    .append("/*<![CDATA[*/\n")
                    .append("require([\"dijit/layout/BorderContainer\",\"dijit/layout/ContentPane\",")
                    .append(" \"dojo/parser\"]);\n")
                    .append("/*]]>*/")
                    .append("</script>\n")
                    .append("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../wicket/resource/")
                    .append(AbstractDojoBehavior.CSS_TUNDRA.getScope().getName()).append("/")
                    .append(AbstractDojoBehavior.CSS_TUNDRA.getName())
                    .append("\" />")
                    .append("<link rel=\"stylesheet\" type=\"text/css\" ")
                    .append(" href=\"../../servlet/static/org.efaps.help.Help.css?")
                    .append("\" />")
                    .append("</head><body>")
                    .append("<div data-dojo-type=\"dijit/layout/BorderContainer\" ")
                    .append("data-dojo-props=\"design: &#039;sidebar&#039;\"")
                    .append(" class=\"tundra\" ")
                    .append("style=\"width: 100%; height: 99%;\">")
                    .append("<div data-dojo-type=\"dijit/layout/ContentPane\" ")
                    .append("data-dojo-props=\"region: &#039;leading&#039;,splitter: true\" ")
                    .append("style=\"width: 200px\">")
                    .append("<div class=\"eFapsHelpMenu\">")
                    .append(menuStr)
                    .append("</div></div>")
                    .append("<div data-dojo-type=\"dijit/layout/ContentPane\" ")
                    .append("data-dojo-props=\"region: &#039;center&#039;\" ")
                    .append("><div class=\"eFapsWikiPage\">");

                for (final String wiki : wikis) {
                    final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.WikiCompiled);
                    queryBldr.addWhereAttrEqValue("Name", wiki);
                    final InstanceQuery query = queryBldr.getQuery();
                    query.execute();
                    if (query.next()) {
                        final Checkout checkout = new Checkout(query.getCurrentValue());
                        final InputStreamReader in = new InputStreamReader(checkout.execute());
                        if (in != null) {
                            final BufferedReader reader = new BufferedReader(in);
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                html.append(line);
                            }
                        }
                    }
                }
                html.append("</div></div></body></html>");
                _res.setContentType("text/html;charset=UTF-8");
                _res.setContentLength(html.length());
                _res.getOutputStream().write(html.toString().getBytes());
            }
        } catch (final EFapsException e) {
            throw new ServletException(e);
        } catch (final IOException e) {
            throw new ServletException(e);
        }
    }

    /**
     * get the CharSequence for the menu.
     *
     * @return the menu
     * @throws EFapsException on error
     */
    private String getMenu()
        throws EFapsException
    {
        HelpServlet.LOG.debug("Reading Main Help Menu");
        final StringBuilder ret = new StringBuilder();
        ret.append("<ul>");
        final QueryBuilder queryBldr = new QueryBuilder(Type.get("Admin_Help_Menu"));
        // Admin_Help_MainMenu
        queryBldr.addWhereAttrEqValue("UUID", "dead549e-5cc6-49f9-9a79-8e33aa139f6d");
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute("Name");
        multi.addSelect("linkfrom[Admin_Help_Menu2Wiki#FromLink].linkto[ToLink].attribute[Name]");
        multi.execute();
        if (multi.next()) {
            final String name = multi.<String>getAttribute("Name");
            String link = "";
            final Object links = multi
                            .getSelect("linkfrom[Admin_Help_Menu2Wiki#FromLink].linkto[ToLink].attribute[Name]");
            if (links instanceof List<?>) {
                for (final Object alink : (List<?>) links) {
                    link = link + ":" + alink;
                }
            } else {
                link = (String) links;
            }
            ret.append("<li><a href=\"").append(link).append("\">").append(DBProperties.getProperty(name + ".Label"))
                            .append("</a></li>");
            ret.append(getSubMenues(multi.getCurrentInstance()));
        }
        ret.append("</ul>");
        return ret.toString();
    }

    /**
     * Recursive method to get the CharSequence for the sub menu.
     *
     * @param _instance Instance of the parent menu
     * @return the menu
     * @throws EFapsException on error
     */
    private CharSequence getSubMenues(final Instance _instance)
        throws EFapsException
    {
        HelpServlet.LOG.debug("Reading Submenues for OID: {}", _instance.getOid());
        final StringBuilder ret = new StringBuilder();

        final QueryBuilder queryBldr = new QueryBuilder(Type.get("Admin_Help_Menu2Menu"));
        queryBldr.addWhereAttrEqValue("FromLink", _instance.getId());
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addSelect("linkto[ToLink].attribute[Name]", "linkto[ToLink].oid");
        multi.execute();
        final Map<Long, String> subs = new TreeMap<Long, String>();

        while (multi.next()) {
            final String name = multi.<String>getSelect("linkto[ToLink].attribute[Name]");
            final String oid = multi.<String>getSelect("linkto[ToLink].oid");
            final PrintQuery print = new PrintQuery(oid);
            print.addSelect("linkfrom[Admin_Help_Menu2Wiki#FromLink].linkto[ToLink].attribute[Name]");
            print.execute();
            String link = "";
            final Object links = print
                            .getSelect("linkfrom[Admin_Help_Menu2Wiki#FromLink].linkto[ToLink].attribute[Name]");
            if (links instanceof List<?>) {
                for (final Object alink : (List<?>) links) {
                    link = link + ":" + alink;
                }
            } else {
                link = (String) links;
            }
            HelpServlet.LOG.debug("  Submenue '{}' with OID: {}", name, print.getCurrentInstance().getOid());
            final StringBuilder menu = new StringBuilder()
                .append("<li><a href=\"").append(link).append("\">")
                .append(DBProperties.getProperty(name + ".Label")).append("</a></li>")
                .append(getSubMenues(print.getCurrentInstance()));
            subs.put(multi.getCurrentInstance().getId(), menu.toString());
        }
        if (!subs.isEmpty()) {
            ret.append("<ul>");
            for (final String sub : subs.values()) {
                ret.append(sub).append("\n");
            }
            ret.append("</ul>");
        }
        return ret;
    }
}
