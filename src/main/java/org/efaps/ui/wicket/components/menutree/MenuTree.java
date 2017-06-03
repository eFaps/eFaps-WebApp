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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.ui.wicket.behaviors.update.IRemoteUpdateListener;
import org.efaps.ui.wicket.behaviors.update.IRemoteUpdateable;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author The eFaps Team
 */
public class MenuTree
    extends NestedTree<UIMenuItem>
    implements IRemoteUpdateable, IRequestListener
{

    /**
     * Reference to icon for go into button.
     */
    public static final EFapsContentReference ICON_GOINTO = new EFapsContentReference(MenuTree.class, "GoInto.gif");

    /**
     * Reference to icon for go up button.
     */
    public static final EFapsContentReference ICON_GOUP = new EFapsContentReference(MenuTree.class, "GoUp.gif");

    /**
     * Reference to icon for closed child button.
     */
    public static final EFapsContentReference ICON_CHILDCLOSED = new EFapsContentReference(MenuTree.class,
                    "ChildClosed.gif");

    /**
     * Reference to icon for open child button.
     */
    public static final EFapsContentReference ICON_CHILDOPENED = new EFapsContentReference(MenuTree.class,
                    "ChildOpened.gif");

    /**
     * Reference to style sheet for the menutree.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(MenuTree.class, "MenuTree.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Selected Component.
     */
    private Component selected = null;

    /**
     * Mapping of a key to menuitem.
     */
    private final Map<String, UIMenuItem> key2uimenuItem = new HashMap<>();

    /**
     * Constructor used for a new MenuTree.
     *
     * @param _wicketId wicket id of the component
     * @param _commandUUID uuid of the command
     * @param _oid oid
     * @param _selectCmdUUID UUID of the selected Command
     * @throws CacheReloadException on error
     */
    public MenuTree(final String _wicketId,
                    final UUID _commandUUID,
                    final String _oid,
                    final UUID _selectCmdUUID)
        throws CacheReloadException
    {
        super(_wicketId, new TreeMenuModel(_commandUUID, _oid));

        add(new HumanTheme());
        add(AttributeModifier.append("class", "eFapsTreeMenu"));
        add(new MenuUpdateBehavior());

        setDefault(_selectCmdUUID);
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */
    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(MenuTree.CSS));
    }

    /**
     * Set the default selected item.
     *
     * @param _selectCmdUUID UUID of the selected Command
     */
    public void setDefault(final UUID _selectCmdUUID)
    {
        final UIMenuItem menuItem = getProvider().getRoots().next();
        menuItem.setHeader(true);
        boolean hasDefault = false;
        for (final UIMenuItem childItem : menuItem.getChildren()) {
            if (_selectCmdUUID == null && childItem.isDefaultSelected()
                            || _selectCmdUUID != null && _selectCmdUUID.equals(childItem.getCommandUUID())) {
                hasDefault = true;
                childItem.setSelected(true);
            }
        }
        if (!hasDefault) {
            menuItem.setSelected(true);
        }
        expand(menuItem);
        expandChildren(menuItem);
    }

    /**
     * @param _menuItem menuitem the children will be expanded for
     */
    protected void expandChildren(final UIMenuItem _menuItem)
    {
        for (final UIMenuItem childItem : _menuItem.getChildren()) {
            if (childItem.isExpanded()) {
                expand(childItem);
            }
            expandChildren(childItem);
        }
    }

    @Override
    protected Component newContentComponent(final String _id,
                                            final IModel<UIMenuItem> _model)
    {
        final MenuItem menuitem = new MenuItem(_id, this, _model);
        if (_model.getObject().isSelected()) {
            this.selected = menuitem;
        }
        return menuitem;
    }

    @Override
    public Component newNodeComponent(final String _wicketId,
                                      final IModel<UIMenuItem> _model)
    {
        return new AbstractMenuNode(_wicketId, this, _model)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component createContent(final String _id,
                                              final IModel<UIMenuItem> _model)
            {
                return newContentComponent(_id, _model);
            }
        };
    }

    /**
     * Getter method for the instance variable {@link #selected}.
     *
     * @return value of instance variable {@link #selected}
     */
    public Component getSelected()
    {
        return this.selected;
    }

    /**
     * Setter method for instance variable {@link #selected}.
     *
     * @param _selected value for instance variable {@link #selected}
     */

    public void setSelected(final Component _selected)
    {
        this.selected = _selected;
    }

    /**
     * @param _commandUUID UUID of the command
     * @param _instanceKey instance key
     * @param _target the ajax target to use
     * @throws CacheReloadException on error
     */
    public void addChildMenu(final UUID _commandUUID,
                             final String _instanceKey,
                             final AjaxRequestTarget _target)
        throws CacheReloadException
    {
        final UIMenuItem menuItem = (UIMenuItem) getSelected().getDefaultModelObject();
        boolean old = false;
        for (final UIMenuItem child : menuItem.getChildren()) {
            if (child.getInstanceKey().equals(_instanceKey)
                            && child.getCommandUUID().equals(_commandUUID)) {
                old = true;
                child.setSelected(true);
                menuItem.setSelected(false);
                visitChildren(MenuItem.class, new IVisitor<MenuItem, Void>()
                {

                    @Override
                    public void component(final MenuItem _comp,
                                          final IVisit<Void> _visit)
                    {
                        final Object object = _comp.getDefaultModelObject();
                        if (object != null && object instanceof UIMenuItem) {
                            if (((UIMenuItem) object).isSelected()) {
                                _target.add(getSelected());
                                setSelected(_comp);
                                _target.add(getSelected());
                                _visit.stop();
                            }
                        }
                    }
                });
                break;
            }
        }

        if (!old) {
            final UIMenuItem newMenuItem = new UIMenuItem(_commandUUID, _instanceKey);
            newMenuItem.setSelected(true);
            newMenuItem.setAncestor(menuItem);
            menuItem.getChildren().add(newMenuItem);
            expand(menuItem);
            expand(newMenuItem);
            menuItem.setSelected(false);
            _target.add(getSelected());
        }
    }

    /**
     * @param _menuItem child to be removed
     * @param _target Ajaxtarget
     */
    public void removeChild(final UIMenuItem _menuItem,
                            final AjaxRequestTarget _target)
    {
        final UIMenuItem selectedItem = (UIMenuItem) getSelected().getDefaultModelObject();

        final UIMenuItem ancestor = _menuItem.getAncestor();
        boolean nested = false;
        if (selectedItem.isChild(_menuItem) || selectedItem.equals(_menuItem)) {
            nested = true;
        }
        ancestor.getChildren().remove(_menuItem);
        if (nested) {
            expand(ancestor);
            ancestor.setSelected(true);
            visitChildren(MenuItem.class, new IVisitor<MenuItem, Void>()
            {

                @Override
                public void component(final MenuItem _comp,
                                      final IVisit<Void> _visit)
                {
                    if (_comp.getDefaultModelObject().equals(ancestor)) {
                        setSelected(_comp);
                        _visit.stop();
                    }
                }
            });
        } else {
            expand(ancestor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerListener(final IRemoteUpdateListener _listener)
    {
        final MenuUpdateBehavior behavior = getBehaviors(MenuUpdateBehavior.class).get(0);
        behavior.register(_listener);
    }

    @Override
    public void onRequest()
    {
        final String key = getRequest().getRequestParameters().getParameterValue("D").toString();
        final UIMenuItem currentItem = this.key2uimenuItem.get(key);

        UIMenuItem menuItem = (UIMenuItem) getSelected().getDefaultModelObject();

        if (menuItem.isChild(currentItem) || menuItem.equals(currentItem)) {
            menuItem =  currentItem.getAncestor();
        }

        Page page;
        try {
            if (menuItem.getCommand().getTargetTable() != null) {
                if (menuItem.getCommand().getTargetStructurBrowserField() != null) {
                    page = new StructurBrowserPage(menuItem.getCommandUUID(),
                                    menuItem.getInstanceKey(), getPage().getPageReference());
                } else {
                    if ("GridX".equals(Configuration.getAttribute(ConfigAttribute.TABLEDEFAULTTYPETREE))) {
                        page = new GridPage(Model.of(UIGrid.get(menuItem.getCommandUUID(), PagePosition.TREE)
                                        .setCallInstance(menuItem.getInstance())));
                    } else {
                        page = new TablePage(menuItem.getCommandUUID(), menuItem.getInstanceKey(), getPage()
                                    .getPageReference());
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

    /**
     * @param _key          key
     * @param _menuItem     menuitem
     */
    public void add(final String _key,
                    final UIMenuItem _menuItem)
    {
        this.key2uimenuItem.put(_key, _menuItem);
    }

    @Override
    public void expand(final UIMenuItem _menuItem)
    {
        super.expand(_menuItem);
        _menuItem.setExpanded(true);
    }

    @Override
    public void collapse(final UIMenuItem _menuItem)
    {
        super.collapse(_menuItem);
        _menuItem.setExpanded(false);
    }

    @Override
    public void updateNode(final UIMenuItem _menuItem,
                           final IPartialPageRequestHandler _target)
    {
        if (_target != null) {
            final IModel<UIMenuItem> model = getProvider().model(_menuItem);
            visitChildren(Node.class, new IVisitor<Node<UIMenuItem>, Void>()
            {

                @Override
                public void component(final Node<UIMenuItem> _node,
                                      final IVisit<Void> _visit)
                {
                    if (model.equals(_node.getModel())) {
                        _target.add(_node);
                        _node.visitChildren(Label.class, new IVisitor<Label, Void>()
                        {
                            @Override
                            public void component(final Label _label,
                                                  final IVisit<Void> _visit)
                            {
                                _label.setDefaultModelObject(((UIMenuItem) _node.getDefaultModelObject()).getLabel());
                            }
                        });
                        _visit.stop();
                    }
                    _visit.dontGoDeeper();
                }
            });
            model.detach();
        }
    }
}
