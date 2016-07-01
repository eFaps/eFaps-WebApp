/*
 * Copyright 2003 - 2016 The eFaps Team
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
package org.efaps.ui.wicket.store;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.wicket.pageStore.SecondLevelPageCache;
import org.efaps.ui.wicket.pages.main.MainPage;

/**
 * The Class EFapsPageCache.
 *
 * @author The eFaps Team
 */
public class EFapsPageCache
    implements SecondLevelPageCache<String, Integer, SerializedPage>
{

    /** The max size. */
    private final int maxSize;

    /** The cache. */
    private final ConcurrentLinkedDeque<SoftReference<SerializedPage>> cache;

    /**
     * Constructor.
     *
     * @param _maxSize the max size
     */
    public EFapsPageCache(final int _maxSize)
    {
        this.maxSize = _maxSize;
        this.cache = new ConcurrentLinkedDeque<>();
    }

    /**
     * Removes the page.
     *
     * @param _sessionId the session id
     * @param _pageId the page id
     * @return the removed {@link SerializedPage} or <code>null</code> -
     *         otherwise
     */
    @Override
    public SerializedPage removePage(final String _sessionId,
                                     final Integer _pageId)
    {
        SerializedPage ret = null;
        if (this.cache.size() > 0) {
            final SerializedPage sample = new SerializedPage(_sessionId, _pageId, null, null);

            for (final Iterator<SoftReference<SerializedPage>> i = this.cache.iterator(); i.hasNext();) {
                final SoftReference<SerializedPage> ref = i.next();
                final SerializedPage entry = ref.get();
                if (sample.equals(entry)) {
                    i.remove();
                    ret =  entry;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Removes all {@link SerializedPage}s for the session with
     * <code>sessionId</code> from the pagesCache.
     *
     * @param _sessionId the session id
     */
    @Override
    public void removePages(final String _sessionId)
    {
        if (this.cache.size() > 0) {
            for (final Iterator<SoftReference<SerializedPage>> i = this.cache.iterator(); i.hasNext();) {
                final SoftReference<SerializedPage> ref = i.next();
                final SerializedPage entry = ref.get();
                if (entry != null && entry.getSessionId().equals(_sessionId)) {
                    i.remove();
                }
            }
        }
    }

    /**
     * Returns a {@link SerializedPage} by looking it up by
     * <code>sessionId</code> and <code>pageId</code>. If there is a match then
     * it is <i>touched</i>, i.e. it is moved at the top of the pagesCache.
     *
     * @param _sessionId
     * @param _pageId
     * @return the found serialized page or <code>null</code> when not found
     */
    @Override
    public SerializedPage getPage(final String _sessionId,
                                  final Integer _pageId)
    {
        SerializedPage result = null;
        if (this.cache.size() > 0) {
            final SerializedPage sample = new SerializedPage(_sessionId, _pageId, null, null);

            for (final Iterator<SoftReference<SerializedPage>> i = this.cache.iterator(); i.hasNext();) {
                final SoftReference<SerializedPage> ref = i.next();
                final SerializedPage entry = ref.get();
                if (sample.equals(entry)) {
                    i.remove();
                    result = entry;
                    break;
                }
            }

            if (result != null) {
                internalStore(result);
            }
        }
        return result;
    }

    /**
     * Store the serialized page in pagesCache.
     *
     * @param _sessionId the session id
     * @param _pageId the page id
     * @param _page the data to serialize (page id, session id, bytes)
     */
    @Override
    public void storePage(final String _sessionId,
                          final Integer _pageId,
                          final SerializedPage _page)
    {
        if (this.maxSize > 0 || MainPage.class.getName().equals(_page.getKey())) {
            for (final Iterator<SoftReference<SerializedPage>> i = this.cache.iterator(); i.hasNext();) {
                final SoftReference<SerializedPage> r = i.next();
                final SerializedPage entry = r.get();
                if (entry != null && entry.equals(_page)) {
                    i.remove();
                    break;
                }
            }
            internalStore(_page);
        }
    }

    /**
     * Internal store.
     *
     * @param _page the page
     */
    private void internalStore(final SerializedPage _page)
    {
        this.cache.push(new SoftReference<>(_page));
        SoftReference<SerializedPage> removedMain = null;
        while (this.cache.size() > this.maxSize) {
            final SoftReference<SerializedPage> remove = this.cache.pollLast();
            if (MainPage.class.getName().equals(remove.get().getKey())) {
                removedMain = remove;
            }
        }
        if (removedMain != null) {
            for (final SoftReference<SerializedPage> ref : this.cache) {
                if (MainPage.class.getName().equals(ref.get().getKey())) {
                    removedMain = null;
                    break;
                }
            }
        }
        if (removedMain != null) {
            this.cache.push(removedMain);
        }
    }

    @Override
    public void destroy()
    {
        this.cache.clear();
    }
}
