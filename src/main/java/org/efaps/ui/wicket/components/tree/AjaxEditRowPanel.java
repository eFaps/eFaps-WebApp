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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.tree;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.table.row.RowPanel;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * Panel renders the add, insert and remove buttons for tables on insert.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxEditRowPanel
    extends Panel
{

    /**
     * Content reference for the delete icon.
     */
    private static final EFapsContentReference ICON_ADD = new EFapsContentReference(AjaxEditRowPanel.class,
                    "add.png");

    /**
     * Content reference for the delete icon.
     */
    private static final EFapsContentReference ICON_DELETE = new EFapsContentReference(AjaxEditRowPanel.class,
                    "delete.png");

    /**
     * Content reference for the add folder icon.
     */
    private static final EFapsContentReference ICON_FOLDER_ADD = new EFapsContentReference(AjaxEditRowPanel.class,
                    "folder_add.png");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Script needed for the ajax call.
     */
    private CharSequence script;

    /**
     * Do be able to have more than one table in a form that can add new rows,
     * it is necessary to have unique function names.
     */
    private String functionName;

    /**
     * Constructor called from the rowpanel for each row.
     *
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _rowPanel rowpanel that must be removed
     */
    public AjaxEditRowPanel(final String _wicketId,
                                 final IModel<UIStructurBrowser> _model,
                                 final RowPanel _rowPanel)
    {
        super(_wicketId, _model);

        final InsertRow insertlink = new InsertRow("addLink");
        this.add(insertlink);
        final StaticImageComponent insertImage = new StaticImageComponent("addIcon");
        insertImage.setReference(AjaxEditRowPanel.ICON_ADD);
        insertlink.add(insertImage);

        final RemoveRow delLink = new RemoveRow("delLink");
        this.add(delLink);
        final StaticImageComponent delImage = new StaticImageComponent("delIcon");
        delImage.setReference(AjaxEditRowPanel.ICON_DELETE);
        delLink.add(delImage);

        final InsertRow insertFolderlink = new InsertRow("addFolderLink");
        this.add(insertFolderlink);
        final StaticImageComponent insertFolderImage = new StaticImageComponent("addFolderIcon");
        insertFolderImage.setReference(AjaxEditRowPanel.ICON_FOLDER_ADD);
        insertFolderlink.add(insertFolderImage);

        add(new WebComponent("script").setVisible(false));
    }

    /**
     * Class renders an ajax link that adds a row to the table.
     */
    public class AjaxAddRow
        extends WebMarkupContainer
    {

        /**
         *Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * RepeatingView.
         */
        private final RepeatingView rowsRep;

        /**
         * @param _wicketId wicket id for this component
         * @param _model model for this component
         * @param _rowsRepeater row repeater
         *
         */
        public AjaxAddRow(final String _wicketId,
                          final IModel<UITable> _model,
                          final RepeatingView _rowsRepeater)
        {
            super(_wicketId, _model);
            this.rowsRep = _rowsRepeater;
            add(new AjaxEventBehavior("onclick") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(final AjaxRequestTarget _target)
                {

                }

                /**
                 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getCallbackScript()
                 * @return
                 */
                @Override
                protected CharSequence getCallbackScript()
                {
                    final String name;
                    if (getComponent().getDefaultModelObject() instanceof UIFieldTable) {
                        name = ((UIFieldTable) getComponent().getDefaultModelObject()).getName();
                    } else {
                        name = ((UITable) getComponent().getDefaultModelObject()).getTable().getName();
                    }
                    AjaxEditRowPanel.this.functionName = "addNewRows_" + name;
                    AjaxEditRowPanel.this.script = "var w = wicketAjaxGet('" + getCallbackUrl(false)
                                    + "&eFapsNewRows=' + _count + '&eFapsRowId=' + _rowId,_successHandler,null,null)";
                    return AjaxEditRowPanel.this.functionName + "(1, null, null)";
                }
            });
        }
    }

    /**
     * Class renders a component containing a script to remove a row from a
     * table.
     */
    public class RemoveRow
        extends WebMarkupContainer
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id for this component
         * @param _rowPanel Rowpanel that must be removed
         */
        public RemoveRow(final String _wicketId)
        {
            super(_wicketId);
        }
    }


    /**
     * Render an insert button.
     *
     */
    public class InsertRow
        extends WebMarkupContainer
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket ID of this component
         * @param _model    model for this component
         * @param _rowPanel rowpnale this component belongs to
         */
        public InsertRow(final String _wicketId)
        {
            super(_wicketId);
        }

    }
}
