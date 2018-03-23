/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.ui.wicket.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.ajax.AjaxRequestHandler;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.efaps.admin.program.bundle.BundleMaker;
import org.efaps.admin.program.bundle.TempFileBundle;
import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteHeaderItem;
import org.efaps.ui.wicket.behaviors.dojo.OnDojoReadyHeaderItem;
import org.efaps.ui.wicket.behaviors.dojo.RequireHeaderItem;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsJavaScriptHeaderItem;
import org.efaps.ui.wicket.util.DojoClass;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class EFapsResourceAggregator
    extends ResourceAggregator
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsResourceAggregator.class);

    /**
     * List of HeaderItems that will be rendered in on dojo ready script.
     */
    private final List<OnDojoReadyHeaderItem> dojoReadyItems = new ArrayList<>();

    /**
    * List of HeaderItems that will be rendered in on dojo ready script.
    */
    private final List<AutoCompleteHeaderItem> autoCompleteItems = new ArrayList<>();

    /**
     * List of HeaderItems that will be rendered file using the eFaps kernel.
     */
    private final List<AbstractEFapsHeaderItem> eFapsHeaderItems = new ArrayList<>();

    /**
     * List of HeaderItems that will be rendered file using the eFaps kernel.
     */
    private final Set<RequireHeaderItem> requireHeaderItems = new HashSet<>();

    /**
     * @param _real orginial Response
     */
    public EFapsResourceAggregator(final IHeaderResponse _real)
    {
        super(_real);
    }

    /**
     * Renders the given {@link HeaderItem} to the response if none of the
     * {@linkplain HeaderItem#getRenderTokens() tokens} of the item has been rendered before.
     *
     * @param _item  The item to render.
     */
    @Override
    public void render(final HeaderItem _item)
    {
        if (_item instanceof OnDojoReadyHeaderItem) {
            this.dojoReadyItems.add((OnDojoReadyHeaderItem) _item);
        } else if (_item instanceof AbstractEFapsHeaderItem) {
            this.eFapsHeaderItems.add((AbstractEFapsHeaderItem) _item);
        } else if (_item instanceof AutoCompleteHeaderItem) {
            this.autoCompleteItems.add((AutoCompleteHeaderItem) _item);
        } else if (_item instanceof RequireHeaderItem) {
            this.requireHeaderItems.add((RequireHeaderItem) _item);
        } else {
            super.render(_item);
        }
    }

    /**
     * Before closing the combined Script and EFapsHeaderItems are added.
     */
    @Override
    public void close()
    {
        renderCombinedEventScripts();
        renderEFapsHeaderItems();
        renderCombinedAutoCompleteScripts();
        renderCombinedRequireScripts();
        super.close();
    }

    /**
     * render the eFaps Resource items.
     */
    private void renderEFapsHeaderItems()
    {
        Collections.sort(this.eFapsHeaderItems, (_item0, _item1) -> _item0.getSortWeight().compareTo(_item1.getSortWeight()));

        final List<String> css = new ArrayList<>();
        final List<String> js = new ArrayList<>();
        for (final AbstractEFapsHeaderItem item : this.eFapsHeaderItems) {
            if (item instanceof EFapsJavaScriptHeaderItem) {
                js.add(item.getReference().getName());
            } else {
                css.add(item.getReference().getName());
            }
        }
        try {
            if (!css.isEmpty()) {
                final String key = BundleMaker.getBundleKey(css, TempFileBundle.class);
                final TempFileBundle bundle = (TempFileBundle) BundleMaker.getBundle(key);
                bundle.setContentType("text/css");
                super.render(CssHeaderItem.forUrl(EFapsApplication.get().getServletContext()
                                .getContextPath() + "/servlet/static/" + key));
            }
            if (!js.isEmpty()) {
                final String key = BundleMaker.getBundleKey(js, TempFileBundle.class);
                final TempFileBundle bundle = (TempFileBundle) BundleMaker.getBundle(key);
                bundle.setContentType("text/javascript");
                super.render(JavaScriptHeaderItem.forUrl(EFapsApplication.get().getServletContext()
                                .getContextPath() + "/servlet/static/" + key));
            }
        } catch (final EFapsException e) {
            EFapsResourceAggregator.LOG.error("Error on rendering eFaps Header items: ", e);
        }
    }

    /**
     * Combines all DOM ready and onLoad scripts and renders them as 2 script
     * tags.
     */
    private void renderCombinedEventScripts()
    {
        final StringBuilder combinedScript = new StringBuilder();

        for (final OnDojoReadyHeaderItem curItem : this.dojoReadyItems) {
            combinedScript.append("\n");
            combinedScript.append(curItem.getJavaScript());
            combinedScript.append(";");
        }

        if (combinedScript.length() > 0) {
            super.render(OnDojoReadyHeaderItem.forScript(combinedScript.append("\n").toString()));
        }
    }


    /**
     * Combines all DOM ready and onLoad scripts and renders them as 2 script
     * tags.
     */
    private void renderCombinedAutoCompleteScripts()
    {
        final StringBuilder combinedScript = new StringBuilder();
        final EnumSet<AutoCompleteBehavior.Type> types = EnumSet.noneOf(AutoCompleteBehavior.Type.class);
        for (final AutoCompleteHeaderItem curItem : this.autoCompleteItems) {
            for (final AutoCompleteBehavior.Type type : curItem.getTypes()) {
                if (!types.contains(type)) {
                    types.add(type);
                }
            }
            combinedScript.append("\n");
            combinedScript.append(curItem.getJavaScript());
            combinedScript.append(";");
        }

        if (combinedScript.length() > 0) {
            if (RequestCycle.get().getActiveRequestHandler() instanceof AjaxRequestHandler) {
                super.render(new OnDomReadyHeaderItem(AutoCompleteHeaderItem.writeJavaScript(
                                combinedScript.append("\n"), types, false)));
            } else {
                super.render(AutoCompleteHeaderItem.forScript(combinedScript.append("\n").toString(), types));
            }
        }
    }

    /**
     * Render combined requir scripts.
     */
    private void renderCombinedRequireScripts()
    {
        final Set<DojoClass> dojoClasses = this.requireHeaderItems.stream().flatMap(o -> o.getDojoClasses().stream())
                        .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(dojoClasses)) {
            super.render(new HeaderItem()
            {

                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 1L;

                @Override
                public Iterable<?> getRenderTokens()
                {
                    return dojoClasses;
                }

                @Override
                public void render(final Response _response)
                {
                    JavaScriptUtils.writeJavaScript(_response,
                                    DojoWrapper.require(null, dojoClasses.toArray(new DojoClass[dojoClasses.size()])),
                                    RequireHeaderItem.class.getName());
                }
            });
        }
    }
}
