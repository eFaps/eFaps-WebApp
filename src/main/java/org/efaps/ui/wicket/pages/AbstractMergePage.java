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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.efaps.admin.program.bundle.BundleMaker;
import org.efaps.admin.program.bundle.TempFileBundle;
import org.efaps.ui.wicket.behaviors.AjaxDownloadBehavior;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;

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
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(AbstractMergePage.class,
                    "AbstractMergePage.css");

    /**
     * this instance variable is used to define if the merging is done or not.
     */
    private boolean mergeStatics = true;

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
        this.add(StaticHeaderContrBehavior.forCss(AbstractMergePage.CSS));
        this.body = new WebMarkupContainer("body");
        this.body.add(AttributeModifier.append("class", Configuration.getAttribute(ConfigAttribute.DOJO_CLASS)));
        super.add(this.body);
    }

    /**
     * in this method the actual merging is done depending on the value of.
     * {@link #mergeStatics()}
     *
     * @see #mergeStatics()
     * @see org.apache.wicket.Page#onBeforeRender()
     */
    @Override
    protected void onBeforeRender()
    {
        if (mergeStatics()) {
            final Map<StaticHeaderContrBehavior.HeaderType, List<StaticHeaderContrBehavior>> resources
                = new HashMap<StaticHeaderContrBehavior.HeaderType, List<StaticHeaderContrBehavior>>();

            // get all StaticHeaderContributor from all childs
            addStaticBehaviors(resources, getBehaviors(StaticHeaderContrBehavior.class));
            addChildStatics(resources, this);

            for (final Entry<StaticHeaderContrBehavior.HeaderType, List<StaticHeaderContrBehavior>> entry : resources
                            .entrySet()) {
                if (entry.getValue().size() > 1) {
                    final List<String> namelist = getReferenceNameList(entry.getValue());
                    // get a new Bundle
                    String name = "";
                    try {
                        name = BundleMaker.getBundleKey(namelist, TempFileBundle.class);
                    } catch (final EFapsException e) {
                        throw new RestartResponseException(new ErrorPage(e));
                    }
                    // add the new Bundle to the Page
                    final TempFileBundle bundle = (TempFileBundle) BundleMaker.getBundle(name);
                    if (entry.getKey().equals(StaticHeaderContrBehavior.HeaderType.CSS)) {
                        this.add(StaticHeaderContrBehavior.forCss(new EFapsContentReference(name), true));
                        bundle.setContentType("text/css");
                    } else if (entry.getKey().equals(StaticHeaderContrBehavior.HeaderType.JS)) {
                        this.add(StaticHeaderContrBehavior.forJavaScript(new EFapsContentReference(name), true));
                        bundle.setContentType("text/javascript");
                    }
                }
            }
        }
        super.onBeforeRender();
    }

    /**
     * This method removes the given Behaviors from the Components and ads the
     * Names of the References to a List.
     *
     * @param _behaviors List of Behaviors that will be removed and the Names
     *            added to a List
     * @return a List with the Names of the Reference
     */
    protected List<String> getReferenceNameList(final List<StaticHeaderContrBehavior> _behaviors)
    {
        final List<String> ret = new ArrayList<String>();
        for (final StaticHeaderContrBehavior behavior : _behaviors) {
            ret.add(behavior.getReference().getName());
            behavior.getComponent().remove(behavior);
        }
        return ret;
    }

    /**
     * This method checks for behaviors in the given List which are instances of
     * StaticHeaderContributor and puts them in the map.
     *
     * @param _resources the map the List of SaticHeaderContributors will be put
     * @param _list a List a Behaviors that will be searched for instances
     *            of StaticHeaderContributor
     * @see #addChildStatics(Map, MarkupContainer)
     */
    protected void addStaticBehaviors(
                    final Map<StaticHeaderContrBehavior.HeaderType, List<StaticHeaderContrBehavior>> _resources,
                    final List<? extends Behavior> _list)
    {

        for (final Behavior oneBehavior : _list) {
            if (oneBehavior instanceof StaticHeaderContrBehavior) {
                final StaticHeaderContrBehavior behavior = (StaticHeaderContrBehavior) oneBehavior;
                if (!behavior.isMerged()) {
                    List<StaticHeaderContrBehavior> behaviors = _resources.get(behavior.getHeaderType());
                    if (behaviors == null) {
                        behaviors = new ArrayList<StaticHeaderContrBehavior>();
                        _resources.put(behavior.getHeaderType(), behaviors);
                    }
                    behaviors.add(behavior);
                }
            }
        }
    }

    /**
     * recursive method to step through all ChildComponents and calls
     * {@link #addStaticBehaviors(Map, List)} for the Behaviors of the Component.
     *
     * @param _resources         resource to add
     * @param _markupcontainer  markupcontainer
     * @see #addStaticBehaviors(Map, List)
     */
    protected void addChildStatics(
                    final Map<StaticHeaderContrBehavior.HeaderType, List<StaticHeaderContrBehavior>> _resources,
                    final MarkupContainer _markupcontainer)
    {
        final Iterator<?> it = _markupcontainer.iterator();
        while (it.hasNext()) {
            final Component component = (Component) it.next();
            if (component instanceof MarkupContainer) {
                addChildStatics(_resources, (MarkupContainer) component);
            }
            addStaticBehaviors(_resources, component.getBehaviors(StaticHeaderContrBehavior.class));
        }
    }

    /**
     * should the merging be done.
     *
     * @see #onBeforeRender()
     * @return true or false
     */
    protected boolean mergeStatics()
    {
        return this.mergeStatics;
    }

    /**
     * This is the getter method for the instance variable
     * {@link #mergeStatics}.
     * @return value of instance variable {@link #mergeStatics}
     */
    public boolean isMergeStatics()
    {
        return this.mergeStatics;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #mergeStatics}.
     * @param _mergeStatics the mergeStatics to set
     */
    public void setMergeStatics(final boolean _mergeStatics)
    {
        this.mergeStatics = _mergeStatics;
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

    /* (non-Javadoc)
     * @see org.apache.wicket.MarkupContainer#add(org.apache.wicket.Component[])
     */
    @Override
    public MarkupContainer add(final Component... _childs)
    {
        return this.body.add(_childs);
    }
}
