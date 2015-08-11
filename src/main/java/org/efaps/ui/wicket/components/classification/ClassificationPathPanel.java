/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.ui.wicket.components.classification;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.components.picker.AjaxPickerLink;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationPathPanel
    extends Panel
{

    /**
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(ClassificationPathPanel.class,
                                                                              "ClassificationPathPanel.css");

    /**
     * Reference to the Icon.
     */
    public static final EFapsContentReference ICON = new EFapsContentReference(AjaxPickerLink.class, "valuepicker.png");

    /** Needed for serialization. */
    private static final long serialVersionUID = 1L;

    private final ClassificationTreePanel clazzPanel;

    /**
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     * @throws CacheReloadException on error
     */
    public ClassificationPathPanel(final String _wicketId,
                                   final IModel<UIClassification> _model)
        throws CacheReloadException
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);

        this.add(new ClassificationPath("path", _model));
        if (_model.getObject().getMode().equals(TargetMode.EDIT)
                        || _model.getObject().getMode().equals(TargetMode.CREATE)) {
            this.add(new ClassTreeOpener("button", _model));
        } else {
            this.add(new WebMarkupContainer("button").setVisible(false));
        }

        this.clazzPanel = new ClassificationTreePanel("tree", Model.of((UIClassification) getDefaultModelObject()));
        add(this.clazzPanel);
        this.clazzPanel.setVisible(false).setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(ClassificationPathPanel.CSS));
    }

    /**
     * Class renders a button to open the form containing the classifcation tree.
     */
    public class ClassTreeOpener
        extends WebComponent
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id for this component
         * @param _model model for this component
         */
        public ClassTreeOpener(final String _wicketId,
                               final IModel<UIClassification> _model)
        {
            super(_wicketId, _model);
            this.add(new AjaxOpenClassTreeBehavior());
        }

        /**
         * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
         *      org.apache.wicket.markup.ComponentTag)
         * @param _markupStream markup stream
         * @param _openTag tag
         */
        @Override
        public void onComponentTagBody(final MarkupStream _markupStream,
                                          final ComponentTag _openTag)
        {
            super.onComponentTagBody(_markupStream, _openTag);
            final StringBuilder html = new StringBuilder();
            html.append("<img alt=\"\" src=\"").append(ClassificationPathPanel.ICON.getImageUrl()).append("\"/>");
            replaceComponentTagBody(_markupStream, _openTag, html);
        }
    }

    /**
     * Behavior used to open the form with the classification tree.
     */
    public class AjaxOpenClassTreeBehavior
        extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public AjaxOpenClassTreeBehavior()
        {
            super("click");
        }

        /**
         * Show the modal window.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            ClassificationPathPanel.this.clazzPanel.setVisible(true);
            _target.add(ClassificationPathPanel.this.clazzPanel);
        }
    }
}
