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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.ui.wicket.components;

import java.io.StringReader;
import java.util.UUID;

import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseException;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Menu;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RecentObjectLink
    implements IRecent, IClusterable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Instance of the Object thi slink belongs to.
     */
    private final Instance instance;

    /**
     * Label of this link.
     */

    private String label;
    /**
     * UUID of the menu.
     */
    private UUID menuUUID;

    /**
     * UUID of the selected command.
     */
    private UUID cmdUUID;

    /**
     * @param _uiObject     uiObject this Link belongs to
     * @throws EFapsException on error
     */
    public RecentObjectLink(final AbstractUIPageObject _uiObject)
        throws EFapsException
    {
        this.instance = _uiObject.getInstance();
        try {
            if (getInstance() != null && getInstance().isValid()) {
                final Menu menu = Menu.getTypeTreeMenu(getInstance().getType());
                this.menuUUID = menu.getUUID();
                this.label = DBProperties.getProperty(menu.getLabel());
                this.cmdUUID = _uiObject.getCommandUUID();

                final ValueParser parser = new ValueParser(new StringReader(getLabel()));
                final ValueList list = parser.ExpressionString();
                if (list.getExpressions().size() > 0) {
                    final PrintQuery print = new PrintQuery(getInstance());
                    list.makeSelect(print);
                    if (print.execute()) {
                        this.label = list.makeString(getInstance(), print, TargetMode.VIEW);
                    }
                }
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        } catch (final ParseException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(final Component _openComponent)
        throws EFapsException
    {
        Page page;
        try {
            page = new ContentContainerPage(PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME),
                                                this.menuUUID, getInstance().getOid(), this.cmdUUID);
        } catch (final EFapsException e) {
            page = new ErrorPage(e);
        }
        RequestCycle.get().setResponsePage(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel(final int _maxLength)
        throws EFapsException
    {
        String labelTmp = getLabel();
        if (labelTmp.length() > _maxLength) {
            labelTmp = labelTmp.substring(0, _maxLength - 3) + "...";
        }
        return labelTmp;
    }

    /**
     * Getter method for the instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     */
    protected String getLabel()
    {
        return this.label;
    }

    /**
     * Getter method for the instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     */
    protected Instance getInstance()
    {
        return this.instance;
    }

}
