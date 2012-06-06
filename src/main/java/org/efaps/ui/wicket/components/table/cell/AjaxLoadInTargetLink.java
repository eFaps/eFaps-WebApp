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

package org.efaps.ui.wicket.components.table.cell;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.model.IModel;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.cell.UIStructurBrowserTableCell;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * Class is used as link to load a page from an popup window inside the opener window.
 *
 * @author The eFaps Team
 * @version $Id$
 * @param <T>
 */
public class AjaxLoadInTargetLink<T>
    extends AjaxLink<T>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * target used for the script.
     */
    public static enum ScriptTarget
    {
        /** top. */
        TOP("top"),
        /** opener. */
        OPENER("opener");

        /***/
        private String key;
        /**
         * @param _key key
         */
        ScriptTarget(final String _key)
        {
            this.key = _key;
        }
    }

    /**
     * target for this link.
     */
    private final ScriptTarget target;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id for this component
     * @param _model    model for this component
     * @param _target   target for this link.
     */
    public AjaxLoadInTargetLink(final String _wicketId,
                                final IModel<T> _model,
                                final ScriptTarget _target)
    {
        super(_wicketId, _model);
        this.target = _target;
    }

    /**
     * Method to load something inside the opener window.
     *
     * @param _target AjaxRequestTarget
     */
    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
        Instance instance = null;

        final UITableCell cellmodel = (UITableCell) super.getModelObject();
        if (cellmodel.getInstanceKey() != null) {
            Menu menu = null;
            try {
                instance = cellmodel.getInstance();
                menu = Menu.getTypeTreeMenu(instance.getType());

            //CHECKSTYLE:OFF
            } catch (final Exception e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            //CHECKSTYLE:ON
            if (menu == null) {
                final Exception ex = new Exception("no tree menu defined for type " + instance.getType().getName());
                throw new RestartResponseException(new ErrorPage(ex));
            }
            try {
                final ContentContainerPage page = new ContentContainerPage(menu.getUUID(), cellmodel.getInstanceKey(),
                                cellmodel instanceof UIStructurBrowserTableCell);
                final CharSequence url = urlFor(new RenderPageRequestHandler(new PageProvider(page)));

                final StringBuilder js = new StringBuilder()
                    .append(this.target.key).append(".dijit.byId(\"").append("mainPanel")
                    .append("\").set(\"content\", dojo.create(\"iframe\", {")
                    .append("\"src\": \"./wicket/").append(url)
                    .append("\",\"style\": \"border: 0; width: 100%; height: 100%\"")
                    .append(",\"id\": \"").append(MainPage.IFRAME_ID).append("\"")
                    .append("}));");
                _target.prependJavaScript(js);
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
        }
    }
}
