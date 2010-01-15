/*
 * Copyright 2003 - 2010 The eFaps Team
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
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Checkout;
import org.efaps.db.PrintQuery;
import org.efaps.db.SearchQuery;
import org.efaps.ui.wicket.behaviors.dojo.DojoReference;
import org.efaps.util.EFapsException;

/**
 * The servlet checks out files from objects.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class WikiServlet extends HttpServlet
{
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
     * @throws ServletException  on error
     */
    @Override
    protected void doGet(final HttpServletRequest _req,
                         final HttpServletResponse _res)
        throws ServletException
    {

        String path = _req.getPathInfo().substring(1);
        if (!path.contains(".")) {
            final String referer = _req.getHeader("Referer");
            final String[] pack = referer.substring(referer.lastIndexOf("/") + 1).split("\\.");
            final StringBuilder newPath = new StringBuilder();
            for (int i = 0; i < pack.length - 2; i++) {
                newPath.append(pack[i]).append(".");
            }
            newPath.append(path).append(".wiki");
            path = newPath.toString();
        }

        try {
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(Type.get(EFapsClassNames.ADMIN_PROGRAM_WIKICOMPILED), false);
            query.addWhereExprEqValue("Name", path);
            query.addSelect("OID");
            query.execute();
            if (query.next()) {
                final String oid = (String) query.get("OID");
                final Checkout checkout = new Checkout(oid);
                checkout.preprocess();
                if (checkout.getFileName() != null) {
                    _res.setContentType("text/html;charset=UTF-8");

                    final StringBuilder html = new StringBuilder();
                    html.append("<html><head>")
                        .append("<script type=\"text/javascript\" src=\"../../resources/")
                        .append(DojoReference.JS_DOJO.getScope().getName()).append("/")
                        .append(DojoReference.JS_DOJO.getName())
                        .append("\" djConfig=\"parseOnLoad: true\"></script>\n")
                        .append("<script type=\"text/javascript\" src=\"../../resources/")
                        .append(DojoReference.JS_EFAPSDOJO.getScope().getName()).append("/")
                        .append(DojoReference.JS_EFAPSDOJO.getName())
                        .append("\" djConfig=\"parseOnLoad: true\"></script>\n")
                        .append("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../resources/")
                        .append(DojoReference.CSS_TUNDRA.getScope().getName()).append("/")
                        .append(DojoReference.CSS_TUNDRA.getName())
                        .append("\" />")
                        .append("<link rel=\"stylesheet\" type=\"text/css\" ")
                        .append(" href=\"../../servlet/static/org.efaps.wiki.Wiki.css?")
                        .append("\" />")
                        .append("</head><body>")
                        .append("<div dojoType=\"dijit.layout.BorderContainer\" design=\"sidebar\"")
                        .append(" liveSplitters=\"true\" gutters=\"false\" persist=\"true\" class=\"tundra\" ")
                        .append("style=\"width: 100%; height: 100%;\">")
                        .append("<div dojoType=\"dijit.layout.ContentPane\" region=\"leading\" ")
                        .append("style=\"width: 200px\" splitter=\"true\">")
                        .append("<div class=\"eFapsWikiMenu\">")
                        .append(getMenu())
                        .append("</div></div>")
                        .append("<div dojoType=\"dijit.layout.ContentPane\" region=\"center\" ")
                        .append("splitter=\"false\"><div class=\"eFapsWikiPage\">");
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(checkout.execute()));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        html.append(line);
                    }
                    html.append("</div></div></body></html>");
                    _res.setContentLength(html.length());
                    _res.getOutputStream().write(html.toString().getBytes());
                }
            }
        } catch (final Throwable e) {
            throw new ServletException(e);
        }
    }

    /**
     * @return
     * @throws EFapsException
     */
    private CharSequence getMenu()
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("<ul>");
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes(EFapsClassNames.ADMIN_WIKI_MENU.getUuid());
        //Admin_Wiki_MainMenu
        query.addWhereExprEqValue("UUID", "dead549e-5cc6-49f9-9a79-8e33aa139f6d");
        query.addSelect("Name");
        query.addSelect("OID");
        query.execute();
        if (query.next()) {
            final String name = (String) query.get("Name");
            final String oid = (String) query.get("OID");
            final PrintQuery print = new PrintQuery(oid);
            print.addSelect("linkfrom[Admin_Wiki_Menu2Wiki#FromLink].linkto[ToLink].attribute[Name]");
            print.execute();
            final String link = print
                          .<String> getSelect("linkfrom[Admin_Wiki_Menu2Wiki#FromLink].linkto[ToLink].attribute[Name]");
            ret.append("<li><a href=\"").append(link).append("\">").append(DBProperties.getProperty(name + ".Label"))
                            .append("</a></li>");
            ret.append(getSubMenues(oid));
        }
        ret.append("</ul>");
        return ret;
    }

    private CharSequence getSubMenues(final String _oid)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final SearchQuery query = new SearchQuery();
        query.setExpand(_oid, "Admin_Wiki_Menu2Menu\\FromLink.ToLink");
        query.addSelect("OID");
        query.addSelect("Name");
        query.execute();
        ret.append("<ul>");
        while (query.next()) {
            final String name = (String) query.get("Name");
            final String oid = (String) query.get("OID");
            final PrintQuery print = new PrintQuery(oid);
            print.addSelect("linkfrom[Admin_Wiki_Menu2Wiki#FromLink].linkto[ToLink].attribute[Name]");
            print.execute();
            final String link = print
                          .<String>getSelect("linkfrom[Admin_Wiki_Menu2Wiki#FromLink].linkto[ToLink].attribute[Name]");
            ret.append("<li><a href=\"").append(link).append("\">")
                .append(DBProperties.getProperty(name + ".Label")).append("</a></li>");
            ret.append(getSubMenues(oid));
        }
        ret.append("</ul>");
        return ret;
    }
}
