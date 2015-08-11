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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.links;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.behaviors.update.AbstractRemoteUpdateListenerBehavior;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.menutree.MenuUpdateBehavior;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class extends a Link to work inside a content container. Used also by the
 * StructurBrowserTable. Updates the menu and the page itself.
 *
 * @author The eFaps Team
 * @version $Id:AjaxLinkContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class MenuContentAjaxLink
    extends WebMarkupContainer
    implements ILabelProvider<String>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MenuContentAjaxLink.class);

    /**
     * Content for this link.
     */
    private final String content;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _model model for thid component
     */
    public MenuContentAjaxLink(final String _wicketId,
                               final IModel<AbstractUIField> _model,
                               final String _content)
    {
        super(_wicketId, _model);
        this.add(new AjaxMenuContentBehavior());
        this.add(new UpdateMenuBehavior(this));
        this.content = _content;
    }

    /**
     * Getter method for the instance variable {@link #config}.
     *
     * @return value of instance variable {@link #config}
     */
    protected FieldConfiguration getConfig()
    {
        return ((AbstractUIField) getDefaultModelObject()).getFieldConfiguration();
    }

    /**
     * Getter method for the instance variable {@link #content}.
     *
     * @return value of instance variable {@link #content}
     */
    protected String getContent()
    {
        return this.content;
    }

    @Override
    public IModel<String> getLabel()
    {
        String ret = "NONE";
        try {
            ret = ((AbstractUIField) getDefaultModelObject()).getLabel();
        } catch (final EFapsException e) {
            LOG.error("Catched error on evaluating label: {}", this);
        }
        return Model.of(ret);
    }

    /**
     * The tag must be overwritten.
     *
     * @param _tag tag to write.
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        _tag.setName("a");
        _tag.put("href", "#");
        onComponentTagInternal(_tag);
    }

    /**
     * Add to the tag.
     * @param _tag tag to write
     */
    protected void onComponentTagInternal(final ComponentTag _tag)
    {
        try {
            _tag.put("name", getConfig().getName());
            _tag.append("style", "text-align:" + getConfig().getAlign(), ";");
        } catch (final EFapsException e) {
            LOG.error("Catched error on setting name in tag for: {}", this);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
    {
        Object ret = null;
        try {
            if (this.content == null) {
                final AbstractUIField uiField = (AbstractUIField) getDefaultModelObject();
                ret = uiField.getValue().getReadOnlyValue(uiField.getParent().getMode());
            } else {
                ret = this.content;
            }
        } catch (final EFapsException e) {
            LOG.error("Catched error on setting tag body for: {}", this);
        }
        replaceComponentTagBody(markupStream, openTag, ret == null ? "" : String.valueOf(ret));
    }


    /**
     * Behavior to update the menu.
     */
    public class UpdateMenuBehavior
        extends AbstractRemoteUpdateListenerBehavior

    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _component Component the behavior belongs to
         */
        public UpdateMenuBehavior(final Component _component)
        {
           super(_component);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onEvent(final Component _component,
                            final AjaxRequestTarget _target)
        {
            try {
                final AbstractUIField uiField = (AbstractUIField) getComponent().getDefaultModelObject();
                Instance instance = null;
                if (uiField.getInstanceKey() != null) {
                    Menu menu = null;
                    try {
                        instance = uiField.getInstance();
                        menu = Menu.getTypeTreeMenu(instance.getType());
                        // CHECKSTYLE:OFF
                    } catch (final Exception e) {
                        throw new RestartResponseException(new ErrorPage(e));
                    } // CHECKSTYLE:ON
                    if (menu == null) {
                        final Exception ex = new Exception("no tree menu defined for type "
                                        + instance.getType().getName());
                        throw new RestartResponseException(new ErrorPage(ex));
                    }
                    final Page page = getPage();
                    if (page instanceof AbstractContentPage) {
                        final MenuTree menutree = (MenuTree) _component;
                        menutree.addChildMenu(menu.getUUID(), uiField.getInstanceKey(), _target);
                    }
                }
            } catch (final CacheReloadException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
        }
    }

    /**
     * Class is used to call an event from inside istself.
     *
     */
    public class AjaxMenuContentBehavior
        extends AjaxEventBehavior
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         */
        public AjaxMenuContentBehavior()
        {
            super("Click");
        }

        @Override
        protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
        {
            super.updateAjaxAttributes(_attributes);

            final PageReference calledByPageRef = ((AbstractContentPage) getPage()).getCalledByPageReference();
            if (calledByPageRef != null) {
                final UpdateMenuBehavior up = getComponent().getBehaviors(UpdateMenuBehavior.class).get(0);
                ((ContentContainerPage) calledByPageRef.getPage()).getMenuTree().registerListener(up);
                final AjaxCallListener listener = new AjaxCallListener();
                final StringBuilder js = new StringBuilder()
                    .append("var frameWin = top.dojo.doc.getElementById(\"").append(MainPage.IFRAME_ID)
                    .append("\").contentWindow;")
                    .append(" frameWin.")
                    .append(MenuUpdateBehavior.FUNCTION_NAME).append("(\"").append(up.getKey()).append("\");")
                    .append("return true;");
                listener.onPrecondition(js);
                _attributes.getAjaxCallListeners().add(listener);
            }
        }

        /**
         * Method is executed on click.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final AbstractUIField uiField = (AbstractUIField) super.getComponent().getDefaultModelObject();
            Instance instance = null;
            if (uiField.getInstanceKey() != null) {
                AbstractCommand menu = null;
                try {
                    instance = uiField.getInstance();
                    menu = Menu.getTypeTreeMenu(instance.getType());
                    // CHECKSTYLE:OFF
                } catch (final Exception e) {
                    throw new RestartResponseException(new ErrorPage(e));
                } // CHECKSTYLE:ON
                if (menu == null) {
                    final Exception ex = new Exception("no tree menu defined for type " + instance.getType().getName());
                    throw new RestartResponseException(new ErrorPage(ex));
                }

                for (final AbstractCommand childcmd : ((Menu) menu).getCommands()) {
                    if (childcmd.isDefaultSelected()) {
                        menu = childcmd;
                        break;
                    }
                }

                final PageReference calledByPageRef = ((AbstractContentPage) getPage()).getCalledByPageReference();
                if (calledByPageRef != null) {
                    Page page;
                    try {
                        if (menu.getTargetTable() != null) {
                            if (menu.getTargetStructurBrowserField() == null) {
                                page = new TablePage(menu.getUUID(), uiField.getInstanceKey(), calledByPageRef);
                            } else {
                                page = new StructurBrowserPage(menu.getUUID(),
                                                uiField.getInstanceKey(), calledByPageRef);
                            }
                        } else {
                            page = new FormPage(menu.getUUID(), uiField.getInstanceKey(), calledByPageRef);
                        }
                    } catch (final EFapsException e) {
                        page = new ErrorPage(e);
                    }
                    super.getComponent().getRequestCycle().setResponsePage(page);
                }
            }
        }
    }
}
