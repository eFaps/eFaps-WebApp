/*
 * Copyright 2003 - 2017 The eFaps Team
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
 */

package org.efaps.ui.wicket.components.menutree;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class MenuItem
    extends Panel
{
    /**
     * Reference to icon for remove button.
     */
    public static final EFapsContentReference ICON_REMOVE = new EFapsContentReference(MenuItem.class, "Remove.gif");

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MenuItem.class);

    /**
     * The tree the menuitem belongs to.
     */
    private final MenuTree tree;

    /**
     * Instantiates a new menu item.
     *
     * @param _wicketID the wicket ID
     * @param _tree the tree
     * @param _model the model
     */
    public MenuItem(final String _wicketID,
                    final MenuTree _tree,
                    final IModel<UIMenuItem> _model)
    {
        super(_wicketID, _model);
        this.tree = _tree;
        setOutputMarkupId(true);
        add(new SelectedAttributeModifier());

        final MarkupContainer link = new Item("link", _model);
        add(link);

        final Label label = new Label("label", _model.getObject().getLabel());
        label.setOutputMarkupId(true);
        link.add(label);

        if (_model.getObject().isHeader()) {
            label.add(AttributeModifier.append("class", "eFapsMenuTreeHeader"));

            String image = _model.getObject().getImage();
            if (image == null) {
                try {
                    image = _model.getObject().getTypeImage();
                } catch (final EFapsException e) {
                    MenuItem.LOG.error("Error on retrieving the image for a image: {}",
                                    _model.getObject().getImage());
                }
            }
            if (image == null) {
                link.add(new WebMarkupContainer("icon").setVisible(false));
            } else {
                link.add(new StaticImageComponent("icon", new EFapsContentReference(image)));
            }

        } else {
            label.add(AttributeModifier.append("class", "eFapsMenuTreeSubItem"));
            link.add(new WebMarkupContainer("icon").setVisible(false));
        }

        if (_model.getObject().getAncestor() == null) {
            final WebMarkupContainer remove = new WebMarkupContainer("removeLink") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    _tag.setName("span");
                    super.onComponentTag(_tag);
                }
            };
            add(remove);
            remove.add(new WebMarkupContainer("removeIcon").setVisible(false));
        } else {
            final AjaxRemoveLink removelink = new AjaxRemoveLink("removeLink", _model);
            add(removelink);
            removelink.add(new StaticImageComponent("removeIcon", MenuItem.ICON_REMOVE));
        }

    }

    /**
     * The Class Item.
     */
    public class Item
        extends WebMarkupContainer
        implements IRequestListener
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicketid of this component
         * @param _model    model for this component
         */
        public Item(final String _wicketId,
                    final IModel<UIMenuItem> _model)
        {
            super(_wicketId, _model);
            add(new ItemBehavior());
        }

        @Override
        public void onRequest()
        {
            final UIMenuItem menuItem = (UIMenuItem) getDefaultModelObject();
            Page page;
            try {
                if (menuItem.getCommand().getTargetTable() != null) {
                    if (menuItem.getCommand().getTargetStructurBrowserField() != null) {
                        final UIStructurBrowser uiStrBrws = new UIStructurBrowser(menuItem.getCommandUUID(),
                                        menuItem.getInstanceKey()).setPagePosition(PagePosition.TREE);

                        page = new StructurBrowserPage(Model.of(uiStrBrws), getPage().getPageReference());
                    } else {
                        if ("GridX".equals(Configuration.getAttribute(ConfigAttribute.TABLEDEFAULTTYPETREE))) {
                            page = new GridPage(Model.of(UIGrid.get(menuItem.getCommandUUID(), PagePosition.TREE)
                                            .setCallInstance(menuItem.getInstance())));
                        } else {
                            final UITable uiTable = new UITable(menuItem.getCommandUUID(), menuItem.getInstanceKey())
                                            .setPagePosition(PagePosition.TREE);
                            page = new TablePage(Model.of(uiTable));
                        }
                    }
                } else {
                    final UIForm uiForm = new UIForm(menuItem.getCommandUUID(), menuItem.getInstanceKey())
                                    .setPagePosition(PagePosition.TREE);
                    page = new FormPage(Model.of(uiForm), getPage().getPageReference());
                }
            } catch (final EFapsException e) {
                page = new ErrorPage(e);
            }
            setResponsePage(page);
        }
    }

    /**
     * The Class SelectedAttributeModifier.
     */
    public static class SelectedAttributeModifier
        extends Behavior
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void onComponentTag(final Component _component,
                                   final ComponentTag _tag)
        {
            super.onComponentTag(_component, _tag);
            final UIMenuItem menuItem = (UIMenuItem) _component.getDefaultModelObject();
            if (menuItem.isSelected()) {
                _tag.put("class", "eFapsMenuTreeItemSelected");
            } else {
                _tag.put("class", "eFapsMenuTreeItem");
            }
        }
    }

    /**
     * The Class ItemBehavior.
     */
    private final class ItemBehavior
        extends AjaxEventBehavior
    {

        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new item behavior.
         */
        ItemBehavior()
        {
            super("click");
        }

        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            if (MenuItem.this.tree.getSelected() != null) {
                _target.add(MenuItem.this.tree.getSelected());
                final UIMenuItem menuItem = (UIMenuItem) MenuItem.this.tree.getSelected().getDefaultModelObject();
                menuItem.setSelected(false);
            }
            _target.add(getComponent().findParent(MenuItem.class));
            MenuItem.this.tree.setSelected(getComponent().findParent(MenuItem.class));
            final UIMenuItem menuItem = (UIMenuItem) MenuItem.this.tree.getSelected().getDefaultModelObject();
            menuItem.setSelected(true);
        }

        /**
         * Add a script that sets the content of the iframe of the ContentContainer.
         * @param _attributes the attribute to be updated
         */
        @Override
        protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
        {
            super.updateAjaxAttributes(_attributes);
            final AjaxCallListener listener = new AjaxCallListener();
            final StringBuilder js = new StringBuilder();
            js.append("registry.byId(\"").append(((ContentContainerPage) getPage()).getCenterPanelId())
                .append("\").set(\"content\", domConstruct.create(\"iframe\", {")
                .append("\"src\": \"")
                .append(getComponent().urlForListener(new PageParameters()))
                .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"")
                .append("})); ");
            listener.onAfter(DojoWrapper.require(js, DojoClasses.registry, DojoClasses.domConstruct));
            _attributes.getAjaxCallListeners().add(listener);
        }
    }
}
