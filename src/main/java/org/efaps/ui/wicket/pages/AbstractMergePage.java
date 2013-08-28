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

package org.efaps.ui.wicket.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.behaviors.AjaxDownloadBehavior;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.IconHeaderItem;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;

/**
 * This abstract Page extends WebPage to deliver the functionality of merging
 * specific behaviors to one behavior to reduce the amount of requests per
 * page.<br>
 * Before the page is rendered
 * {@link #org.efaps.ui.wicket.resources.StaticHeaderContributor}
 * will be bundled using {@link #org.efaps.admin.program.bundle.BundleMaker}.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractMergePage
    extends WebPage
{
    /**
     * Reference to the StyleSheet for this Page.
     */
    public static final EFapsContentReference FAVICON = new EFapsContentReference(AbstractMergePage.class, "favicon");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(AbstractMergePage.class,
                    "AbstractMergePage.css");

    /**
     * The DownloadBehavior used for downloading files.
     */
    private final AjaxDownloadBehavior downloadBehavior  = new AjaxDownloadBehavior();

    /**
     * Base Container for the body.
     */
    private final WebMarkupContainer body;

    /**
     * Constructor.
     */
    public AbstractMergePage()
    {
        this(null);
    }

    /**
     * Constructor that passes to the SuperConstructor.
     * @param _model model for this page
     */
    public AbstractMergePage(final IModel<?> _model)
    {
        super(_model);
        add(this.downloadBehavior);
        this.body = new WebMarkupContainer("body");
        this.body.add(AttributeModifier.append("class", Configuration.getAttribute(ConfigAttribute.DOJO_CLASS)));
        super.add(this.body);

    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(AbstractMergePage.CSS));
        _response.render(new IconHeaderItem(AbstractMergePage.FAVICON));
    }

    /**
     * Getter method for the instance variable {@link #downloadBehavior}.
     *
     * @return value of instance variable {@link #downloadBehavior}
     */
    public AjaxDownloadBehavior getDownloadBehavior()
    {
        return this.downloadBehavior;
    }

    @Override
    public MarkupContainer add(final Component... _childs)
    {
        return this.body.add(_childs);
    }
}
