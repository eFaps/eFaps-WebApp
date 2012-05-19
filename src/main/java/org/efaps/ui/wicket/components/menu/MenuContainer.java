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

package org.efaps.ui.wicket.components.menu;

import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.components.AbstractParentMarkupContainer;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UISearchItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.util.RequestHandler;

/**
 * Class is responsible to render the Menu for eFaps.
 *
 * @author The eFaps Team
 * @version $Id:MenuContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class MenuContainer
    extends AbstractParentMarkupContainer
{
    /**
     * Unique identifier used for serialization.
     */
    private static final long serialVersionUID = -6120467631452919272L;

    /**
     * Content reference to the theme java script.
     */
    private static final EFapsContentReference THEME = new EFapsContentReference(MenuContainer.class, "theme.js");

    /**
     * URL of the theme images.
     */
    private static final String URL_THEME_IMAGES = RequestHandler.URL_IMAGE + "org.efaps.ui.wicket.components.menu.";

    /**
     * Content reference to the EFapsExtension java script.
     */
    private static final EFapsContentReference EFAPSEXTENSION
        = new EFapsContentReference(MenuContainer.class, "EFapsExtension.js");

    /**
     * Content reference to the EFapsMenu cascade style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(MenuContainer.class, "EFapsMenu.css");

    /**
     * Content reference to the an image.
     */
    private static final EFapsContentReference IMG_BLANK = new EFapsContentReference(MenuContainer.class, "blank.gif");

    /**
     * Variable stores the form, so it can be accessed by links.
     */
    private final FormContainer form;

    /**
     * Counter for the children of this menu.
     */
    private int childID = 0;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id
     * @param _model model of this component
     */
    public MenuContainer(final String _wicketId,
                         final IModel<?> _model)
    {
        this(_wicketId, _model, null);
    }

    /**
     * <p>Constructor defining the header and the menu itself. The header is
     * extended to include
     * <ul>
     * <li>link to the {@link #CSS menu cascade style sheet}</li>
     * <li>link to the {@link #EFAPSEXTENSION eFaps menu extension} Javascript
     *     </li>
     * <li>link to the {@link #THEME menu theme} Javascript</li>
     * </ul></p>
     * <p>For the Javascript of the {@link #THEME menu theme} the link to the
     * images must be defined before the script itself is called. So the
     * original link could not be used and the link itself is build manually
     * (and the URL for the image is defined correctly).</p>
     *
     * @param _wicketId     wicket id
     * @param _model        model of this component
     * @param _form         form
     */
    public MenuContainer(final String _wicketId,
                         final IModel<?> _model,
                         final FormContainer _form)
    {
        super(_wicketId, _model);
        this.form = _form;

        add(StaticHeaderContrBehavior.forCss(MenuContainer.CSS));
        add(StaticHeaderContrBehavior.forJavaScript(MenuContainer.EFAPSEXTENSION));

        final UIMenuItem model = (UIMenuItem) super.getDefaultModelObject();
        for (final UIMenuItem menuItem : model.getChilds()) {
            addLink(menuItem);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.Component#renderHead(org.apache.wicket.markup.head.IHeaderResponse)
     */
    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        final StringBuilder js = new StringBuilder()
            .append("var myThemeOfficeBase=\"")
            .append(RequestHandler.replaceMacrosInUrl(MenuContainer.URL_THEME_IMAGES).replaceAll("\\?", ""))
            .append("\";");
        _response.render(JavaScriptHeaderItem.forScript(js, MenuContainer.class.getName()));
        _response.render(JavaScriptHeaderItem.forUrl(MenuContainer.THEME.getStaticContentUrl()));
    }

    /**
     * Method to get a new wicket id for a child of this menu.
     *
     * @return new id
     */
    private String getNewChildId()
    {
        return "ItemLink" + String.valueOf(this.childID++);
    }

    /**
     * Recursive method to add a link or a child to one item in the menu.
     *
     * @param _menuItem menu item to add
     */
    private void addLink(final UIMenuItem _menuItem)
    {
        final IModel<UIMenuItem> model = new UIModel<UIMenuItem>(_menuItem);
        // if we have no more childs we add a lnk
        if (!_menuItem.hasChilds()) {
            if (_menuItem.getTarget() != Target.UNKNOWN) {
                if (_menuItem.getTarget() == Target.MODAL) {
                    final AjaxOpenModalComponent item = new AjaxOpenModalComponent(getNewChildId(), model,
                                                                  _menuItem.getCommand().isSubmit() ? this.form : null);
                    this.add(item);
                } else {
                    final StandardLink item = new StandardLink(getNewChildId(), model);
                    this.add(item);
                }
            } else {
                if (_menuItem.getCommand().isSubmit()) {
                    final AjaxSubmitComponent item = new AjaxSubmitComponent(getNewChildId(), model, this.form);
                    this.add(item);
                } else if (super.getDefaultModelObject() instanceof UISearchItem) {
                    final AjaxSearchComponent item = new AjaxSearchComponent(getNewChildId(), model);
                    this.add(item);
                }
            }
        } else if (_menuItem.getCommand().hasEvents(EventType.UI_COMMAND_EXECUTE)) {
            final StandardLink item = new StandardLink(getNewChildId(), model);
            this.add(item);
        }
        if (_menuItem.getReference() != null) {
            _menuItem.setURL(_menuItem.getReference());
            if (_menuItem.getReference().equals("/" + getSession().getApplication().getApplicationKey() + "/logout?")) {
                this.add(new LogOutLink(getNewChildId(), model));
            } else if (_menuItem.getReference().equals(
                            "/" + getSession().getApplication().getApplicationKey() + "/setcompany?")) {
                this.add(new AjaxSetCompanyLink(getNewChildId(), model));
            }
        }
        // add the children
        for (final UIMenuItem childs : _menuItem.getChilds()) {
            addLink(childs);
        }
    }

    /**
     * Before rendering the URL's of all items must be evaluated.
     */
    @Override
    protected void onBeforeRender()
    {
        final Iterator<?> childs = this.iterator();
        while (childs.hasNext()) {
            final Object child = childs.next();
            if (child instanceof StandardLink) {
                final StandardLink item = (StandardLink) child;
                final UIMenuItem childModel = item.getModelObject();

                CharSequence url = item.urlFor(ILinkListener.INTERFACE, new PageParameters());
                if (childModel.getTarget() == Target.POPUP) {
                    final AbstractCommand command = childModel.getCommand();

                    final PopupSettings popup = new PopupSettings("popup").setHeight(
                                    command.getWindowHeight()).setWidth(command.getWindowWidth());
                    item.setPopupSettings(popup);
                    popup.setTarget("\"" + url + "\"");
                    final String tmp = popup.getPopupJavaScript().replaceAll("'", "\"");
                    url = "javascript:" + tmp.replace("return false;", "");
                }
                childModel.setURL(url.toString());
            } else if (child instanceof AbstractMenuItemAjaxComponent) {
                final AbstractMenuItemAjaxComponent item = (AbstractMenuItemAjaxComponent) child;
                final UIMenuItem childModel = (UIMenuItem) item.getDefaultModelObject();

                final String url = item.getJavaScript();
                childModel.setURL(url);

            } else if (child instanceof AjaxSearchComponent) {
                final AjaxSearchComponent item = (AjaxSearchComponent) child;
                final UIMenuItem childModel = (UIMenuItem) item.getDefaultModelObject();

                final CharSequence url = item.urlFor(IBehaviorListener.INTERFACE, new PageParameters());
                childModel.setURL(url.toString());
            }
        }
        super.onBeforeRender();
    }

    /**
     * On the tag of the component the script must be rendered.
     *
     * @param _markupStream markup stream
     * @param _openTag      tag
     */
    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                      final ComponentTag _openTag)
    {

        super.replaceComponentTagBody(_markupStream, _openTag, convert2Html(_openTag));
    }

    /**
     * Method to convert all menu items to HTML code.
     *
     * @param _openTag tag to be used
     * @return String with valid HTML code
     */
    private String convert2Html(final ComponentTag _openTag)
    {
        final CharSequence id = _openTag.getAttribute("id");

        final UIMenuItem model = (UIMenuItem) super.getDefaultModelObject();

        final StringBuilder html = new StringBuilder();

        html.append(JavaScriptUtils.SCRIPT_OPEN_TAG).append("var ").append(id).append("=[");

        for (final UIMenuItem menuItem : model.getChilds()) {
            convertItem2Html(menuItem, html, true, new StringBuilder());
            html.append(",\n");
        }
        html.append("];").append("cmDraw ('").append(id).append("', ").append(id).append(", 'hbr', cmThemeOffice);")
                        .append(JavaScriptUtils.SCRIPT_CLOSE_TAG);
        return html.toString();
    }

    /**
     * Recursive method to render one item.
     *
     * @param _menuItem     menu item to be rendered
     * @param _html         html to be appended to
     * @param _isMain       is this the first item or a recursive call
     * @param _prefix       prefix for an item
     */
    private void convertItem2Html(final UIMenuItem _menuItem,
                                  final StringBuilder _html,
                                  final boolean _isMain,
                                  final StringBuilder _prefix)
    {
        _html.append("['");
        if (_menuItem.getImage() != null) {
            if (_isMain) {
                _html.append("<img src=\"/..").append(_menuItem.getImage()).append("\" class=\"eFapsMenuMainImage\"/>");
            } else {
                _html.append("<img src=\"/..").append(_menuItem.getImage()).append("\" class=\"eFapsMenuSubImage\"/>");
            }
        } else if (!_isMain) {
            _html.append("<img src=\"").append(MenuContainer.IMG_BLANK.getImageUrl())
                            .append("\" class=\"eFapsMenuSubImage\"/>");
        } else {
            _html.append("<img src=\"").append(MenuContainer.IMG_BLANK.getImageUrl())
                            .append("\" class=\"eFapsMenuMainBlankImage\"/>");
        }
        _html.append("','<span class=\"eFapsMenuLabel\">")
            .append(StringEscapeUtils.escapeJavaScript(_menuItem.getLabel())).append("</span>', '");
        if (_menuItem.getUrl() != null) {
            _html.append(_menuItem.getUrl());
        }
        if (_menuItem.getTarget() == Target.HIDDEN) {
            _html.append("', 'eFapsFrameHidden', '");
        } else if (_menuItem.getTarget() == Target.MODAL) {
            _html.append("', 'top', '");
        } else {
            _html.append("', '_self', '");
        }

        if (_menuItem.getDescription() != null) {
            _html.append(_menuItem.getDescription());
        }
        _html.append("'");
        for (final UIMenuItem menuItem : _menuItem.getChilds()) {
            _html.append("\n").append(_prefix).append("  ,");
            convertItem2Html(menuItem, _html, false, new StringBuilder(_prefix).append("  "));
        }
        _html.append("]");
    }
}
