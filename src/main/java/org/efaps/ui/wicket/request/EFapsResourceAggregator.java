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

package org.efaps.ui.wicket.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.apache.wicket.request.cycle.RequestCycle;
import org.efaps.admin.program.bundle.BundleMaker;
import org.efaps.admin.program.bundle.TempFileBundle;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteHeaderItem;
import org.efaps.ui.wicket.behaviors.dojo.OnDojoReadyHeaderItem;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.EFapsJavaScriptHeaderItem;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsResourceAggregator
    extends DecoratingHeaderResponse
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsResourceAggregator.class);

    /**
     * List of HeaderItems that will be rendered in on dojo ready script.
     */
    private final List<OnDojoReadyHeaderItem> dojoReadyItems = new ArrayList<OnDojoReadyHeaderItem>();

    /**
    * List of HeaderItems that will be rendered in on dojo ready script.
    */
    private final List<AutoCompleteHeaderItem> autoCompleteItems = new ArrayList<AutoCompleteHeaderItem>();

    /**
     * List of HeaderItems that will be rendered file using the eFaps kernel.
     */
    private final List<AbstractEFapsHeaderItem> eFapsHeaderItems = new ArrayList<AbstractEFapsHeaderItem>();

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
        } else {
            getRealResponse().render(_item);
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

        super.close();
    }

    /**
     * render the eFaps Resource items.
     */
    private void renderEFapsHeaderItems()
    {
        Collections.sort(this.eFapsHeaderItems, new Comparator<AbstractEFapsHeaderItem>()
        {

            @Override
            public int compare(final AbstractEFapsHeaderItem _item0,
                               final AbstractEFapsHeaderItem _item1)
            {
                return _item0.getSortWeight().compareTo(_item1.getSortWeight());
            }
        });

        final List<String> css = new ArrayList<String>();
        final List<String> js = new ArrayList<String>();
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
                getRealResponse().render(CssHeaderItem.forUrl(new EFapsContentReference(key).getStaticContentUrl()));
            }
            if (!js.isEmpty()) {
                final String key = BundleMaker.getBundleKey(js, TempFileBundle.class);
                final TempFileBundle bundle = (TempFileBundle) BundleMaker.getBundle(key);
                bundle.setContentType("text/javascript");
                getRealResponse().render(
                                JavaScriptHeaderItem.forUrl(new EFapsContentReference(key).getStaticContentUrl()));
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
            getRealResponse().render(OnDojoReadyHeaderItem.forScript(combinedScript.append("\n").toString()));
        }
    }


    /**
     * Combines all DOM ready and onLoad scripts and renders them as 2 script
     * tags.
     */
    private void renderCombinedAutoCompleteScripts()
    {
        final StringBuilder combinedScript = new StringBuilder();

        for (final AutoCompleteHeaderItem curItem : this.autoCompleteItems) {
            combinedScript.append("\n");
            combinedScript.append(curItem.getJavaScript());
            combinedScript.append(";");
        }

        if (combinedScript.length() > 0) {
            if (RequestCycle.get().getActiveRequestHandler() instanceof AjaxRequestHandler) {
                ((AjaxRequestHandler) RequestCycle.get().getActiveRequestHandler())
                    .appendJavaScript(AutoCompleteHeaderItem.writeJavaScript(combinedScript.append("\n"), false));
            } else {
                getRealResponse().render(AutoCompleteHeaderItem.forScript(combinedScript.append("\n").toString()));
            }
        }
    }
}
