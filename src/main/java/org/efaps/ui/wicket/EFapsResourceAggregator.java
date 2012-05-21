/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.ui.wicket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.NoHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.ResourceAggregator.RecordedHeaderItem;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.resource.CircularDependencyException;
import org.efaps.ui.wicket.behaviors.dojo.OnDojoReadyHeaderItem;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsResourceAggregator
    extends DecoratingHeaderResponse
{

    private final Map<HeaderItem, RecordedHeaderItem> itemsToBeRendered = new LinkedHashMap<HeaderItem, RecordedHeaderItem>();
    private final List<OnDomReadyHeaderItem> domReadyItemsToBeRendered = new ArrayList<OnDomReadyHeaderItem>();
    private final List<OnDojoReadyHeaderItem> dojoReadyItemsToBeRendered = new ArrayList<OnDojoReadyHeaderItem>();
    private final List<OnLoadHeaderItem> loadItemsToBeRendered = new ArrayList<OnLoadHeaderItem>();

    private Object renderBase;
    private int indexInRenderBase;
    private int indexInRequest;

    /**
     * @param _real
     */
    public EFapsResourceAggregator(final IHeaderResponse _real)
    {
        super(_real);
    }

    @Override
    public void markRendered(final Object object)
    {
        super.markRendered(object);
        if (object instanceof Component || object instanceof Behavior) {
            this.renderBase = null;
            this.indexInRenderBase = 0;
        }
    }

    @Override
    public boolean wasRendered(final Object object)
    {
        final boolean ret = super.wasRendered(object);
        if (!ret && object instanceof Component || object instanceof Behavior) {
            this.renderBase = object;
            this.indexInRenderBase = 0;
        }
        return ret;
    }

    private void recordHeaderItem(final HeaderItem _item,
                                  final Set<HeaderItem> _depsDone)
    {
        renderDependencies(_item, _depsDone);
        RecordedHeaderItem recordedItem = this.itemsToBeRendered.get(_item);
        if (recordedItem == null) {
            recordedItem = new RecordedHeaderItem(_item);
            this.itemsToBeRendered.put(_item, recordedItem);
        }
        recordedItem.addLocation(this.renderBase, this.indexInRenderBase, this.indexInRequest);
        this.indexInRenderBase++;
        this.indexInRequest++;
    }

    private void renderDependencies(final HeaderItem _item,
                                    final Set<HeaderItem> _depsDone)
    {
        for (final HeaderItem curDependency : _item.getDependencies()) {
            if (_depsDone.add(curDependency)) {
                recordHeaderItem(curDependency, _depsDone);
            } else {
                throw new CircularDependencyException(_depsDone, curDependency);
            }
            _depsDone.remove(curDependency);
        }
    }

    @Override
    public void render(final HeaderItem _item)
    {
        if (_item instanceof OnDomReadyHeaderItem) {
            renderDependencies(_item, new LinkedHashSet<HeaderItem>());
            this.domReadyItemsToBeRendered.add((OnDomReadyHeaderItem) _item);
        } else if (_item instanceof OnLoadHeaderItem) {
            renderDependencies(_item, new LinkedHashSet<HeaderItem>());
            this.loadItemsToBeRendered.add((OnLoadHeaderItem) _item);
        } else if (_item instanceof OnDojoReadyHeaderItem) {
            renderDependencies(_item, new LinkedHashSet<HeaderItem>());
            this.dojoReadyItemsToBeRendered.add((OnDojoReadyHeaderItem) _item);
        } else {
            final Set<HeaderItem> depsDone = new LinkedHashSet<HeaderItem>();
            depsDone.add(_item);
            recordHeaderItem(_item, depsDone);
        }
    }

    /**
     * Renders all normal header items, sorting them and taking bundles into
     * account.
     */
    private void renderHeaderItems()
    {
        final List<RecordedHeaderItem> sortedItemsToBeRendered = new ArrayList<RecordedHeaderItem>(
                        this.itemsToBeRendered.values());
        final Comparator<? super RecordedHeaderItem> headerItemComparator = Application.get()
                        .getResourceSettings()
                        .getHeaderItemComparator();
        if (headerItemComparator != null) {
            Collections.sort(sortedItemsToBeRendered, headerItemComparator);
        }
        for (final RecordedHeaderItem curRenderItem : sortedItemsToBeRendered) {
            getRealResponse().render(getItemToBeRendered(curRenderItem.getItem()));
        }
    }

    /**
     * Combines all DOM ready and onLoad scripts and renders them as 2 script
     * tags.
     */
    private void renderCombinedEventScripts()
    {
        final StringBuilder combinedScript = new StringBuilder();
        for (final OnDomReadyHeaderItem curItem : this.domReadyItemsToBeRendered) {
            final HeaderItem itemToBeRendered = getItemToBeRendered(curItem);
            if (itemToBeRendered == curItem) {
                combinedScript.append("\n");
                combinedScript.append(curItem.getJavaScript());
                combinedScript.append(";");
            } else {
                getRealResponse().render(itemToBeRendered);
            }
        }
        if (combinedScript.length() > 0) {
            getRealResponse().render(
                            OnDomReadyHeaderItem.forScript(combinedScript.append("\n").toString()));
        }

        combinedScript.setLength(0);
        for (final OnLoadHeaderItem curItem : this.loadItemsToBeRendered) {
            final HeaderItem itemToBeRendered = getItemToBeRendered(curItem);
            if (itemToBeRendered == curItem) {
                combinedScript.append("\n");
                combinedScript.append(curItem.getJavaScript());
                combinedScript.append(";");
            } else {
                getRealResponse().render(itemToBeRendered);
            }
        }
        if (combinedScript.length() > 0) {
            getRealResponse().render(
                            OnLoadHeaderItem.forScript(combinedScript.append("\n").toString()));
        }

        combinedScript.setLength(0);
        for (final OnDojoReadyHeaderItem curItem : this.dojoReadyItemsToBeRendered) {
            final HeaderItem itemToBeRendered = getItemToBeRendered(curItem);
            if (itemToBeRendered == curItem) {
                combinedScript.append("\n");
                combinedScript.append(curItem.getJavaScript());
                combinedScript.append(";");
            } else {
                getRealResponse().render(itemToBeRendered);
            }
        }

        if (combinedScript.length() > 0) {
            getRealResponse().render(OnDojoReadyHeaderItem.forScript(combinedScript.append("\n").toString()));
        }
    }

    /**
     * Renders the DOM ready and onLoad scripts as separate tags.
     */
    private void renderSeperateEventScripts()
    {
        for (final OnDomReadyHeaderItem curItem : this.domReadyItemsToBeRendered) {
            getRealResponse().render(getItemToBeRendered(curItem));
        }
        for (final OnLoadHeaderItem curItem : this.loadItemsToBeRendered) {
            getRealResponse().render(getItemToBeRendered(curItem));
        }
        for (final OnDojoReadyHeaderItem curItem : this.dojoReadyItemsToBeRendered) {
            getRealResponse().render(getItemToBeRendered(curItem));
        }
    }

    /**
     * Resolves the actual item that needs to be rendered for the given item.
     * This can be a {@link NoHeaderItem} when the item was already rendered. It
     * can also be a bundle or the item itself, when it is not part of a bundle.
     *
     * @param _item
     * @return The item to be rendered
     */
    private HeaderItem getItemToBeRendered(final HeaderItem _item)
    {
        HeaderItem ret;
        if (getRealResponse().wasRendered(_item)) {
            ret =  NoHeaderItem.get();
        } else {
            getRealResponse().markRendered(_item);
            ret = Application.get().getResourceBundles().findBundle(_item);
            if (ret == null) {
                ret = _item;
            } else {
                for (final HeaderItem curProvided : ret.getProvidedResources()) {
                    getRealResponse().markRendered(curProvided);
                }
            }
        }
        return ret;
    }

    /**
     * Mark Header rendering is completed and subsequent usage will be ignored. If some kind of
     * buffering is used internally, this action will mark that the contents has to be flushed out.
     */
    @Override
    public void close()
    {
        renderHeaderItems();
        if (RequestCycle.get().find(AjaxRequestTarget.class) == null)
        {
            renderCombinedEventScripts();
        }
        else
        {
            renderSeperateEventScripts();
        }
        super.close();
    }
}
