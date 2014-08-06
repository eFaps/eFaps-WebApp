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

package org.efaps.ui.wicket.components.picker;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.cell.UIPicker;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxPickerLink
    extends WebComponent
{

    /**
     * Reference to the icon used as link.
     */
    public static final EFapsContentReference ICON =
                    new EFapsContentReference(AjaxPickerLink.class, "valuepicker.png");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId         wicket id of this component
     * @param _model            model for this component
     * @param _targetComponent  component used as the target for this Picker
     */
    public AjaxPickerLink(final String _wicketId,
                          final IModel<?> _model,
                          final Component _targetComponent)
    {
        super(_wicketId, _model);
        _targetComponent.setOutputMarkupId(true);
        add(new AjaxOpenPickerBehavior(_targetComponent.getMarkupId(true)));

    }

    /**
     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
     * @param _tag Tag
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        final UITableCell uiObject = (UITableCell) getDefaultModelObject();
        _tag.put("title", uiObject == null ? "" : uiObject.getPicker().getLabel());
        _tag.put("class", "eFapsPickerLink");
    }

    /**
     * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
     *      org.apache.wicket.markup.ComponentTag)
     * @param _markupStream markup stream
     * @param _openTag      open tag
     */
    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                      final ComponentTag _openTag)
    {
        super.onComponentTagBody(_markupStream, _openTag);
        final StringBuilder html = new StringBuilder();
        html.append("<img alt=\"\" src=\"")
                        .append(AjaxPickerLink.ICON.getImageUrl()).append("\"/>");
        replaceComponentTagBody(_markupStream, _openTag, html);
    }

    /**
     * Behavior that opens the modal window for the picker.
     */
    public class AjaxOpenPickerBehavior
        extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * MarkupId of the Target Component.
         */
        private final String targetMarkupId;

        /**
         * Constructor.
         * @param _targetMarkupId markupId of the target
         */
        public AjaxOpenPickerBehavior(final String _targetMarkupId)
        {
            super("onclick");
            this.targetMarkupId = _targetMarkupId;
        }

        /**
         * This Method returns the JavaScript which is executed by the JSCooKMenu.
         *
         * @return String with the JavaScript
         */
        public String getJavaScript()
        {
            final String script = super.getCallbackScript().toString();
            return "javascript:" + script.replace("'", "\"");
        }

        /**
         * Show the modal window.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            ModalWindowContainer modal;
            if (getPage() instanceof MainPage) {
                modal = ((MainPage) getPage()).getModal();
            } else {
                modal = ((AbstractContentPage) getPage()).getModal();
            }
            modal.reset();
            try {
                final UIPicker picker = ((UITableCell) getDefaultModelObject()).getPicker();
                picker.setUserinterfaceId(this.targetMarkupId);
                picker.setParentParameters(Context.getThreadContext().getParameters());
                final PageCreator pageCreator = new ModalWindowAjaxPageCreator(picker, modal);
                modal.setPageCreator(pageCreator);
                modal.setInitialHeight(picker.getWindowHeight());
                modal.setInitialWidth(picker.getWindowWidth());
                modal.setWindowClosedCallback(new PickerCallBack(this.targetMarkupId, getPage().getPageReference()));
                modal.show(_target);
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
