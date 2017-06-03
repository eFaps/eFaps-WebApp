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
package org.efaps.ui.wicket.store;

import java.lang.ref.SoftReference;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.SecondLevelPageCache;
import org.apache.wicket.util.lang.Args;

/**
 * The Class EFapsPageCache.
 *
 * @author The eFaps Team
 */
public class EFapsMainPagesCache
    implements SecondLevelPageCache<String, Integer, IManageablePage>
{

    /** The max entries per session. */
    private final int maxEntriesPerSession;

    /** The cache. */
    private final ConcurrentMap<String, SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>>> cache;

    /**
     * Constructor.
     */
    public EFapsMainPagesCache()
    {
        this.cache = new ConcurrentHashMap<>();
        this.maxEntriesPerSession = 1;
    }

    /**
     *
     * @param _sessionId
     *            The id of the http session
     * @param _pageId
     *            The id of the page to remove from the cache
     * @return the removed {@link org.apache.wicket.page.IManageablePage} or
     *         <code>null</code> - otherwise
     */
    @Override
    public IManageablePage removePage(final String _sessionId,
                                      final Integer _pageId)
    {
        IManageablePage result = null;

        if (this.maxEntriesPerSession > 0) {
            Args.notNull(_sessionId, "sessionId");
            Args.notNull(_pageId, "pageId");

            final SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> pagesPerSession = this.cache.get(
                            _sessionId);
            if (pagesPerSession != null) {
                final ConcurrentMap<PageValue, IManageablePage> pages = pagesPerSession.get();
                if (pages != null) {
                    final PageValue sample = new PageValue(_pageId);
                    final Iterator<Map.Entry<PageValue, IManageablePage>> iterator = pages.entrySet().iterator();
                    while (iterator.hasNext()) {
                        final Map.Entry<PageValue, IManageablePage> entry = iterator.next();
                        if (sample.equals(entry.getKey())) {
                            result = entry.getValue();
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Removes all {@link org.apache.wicket.page.IManageablePage}s for the
     * session
     * with <code>sessionId</code> from the cache.
     *
     * @param _sessionId
     *            The id of the expired http session
     */
    @Override
    public void removePages(final String _sessionId)
    {
        Args.notNull(_sessionId, "sessionId");
        if (this.maxEntriesPerSession > 0) {
            this.cache.remove(_sessionId);
        }
    }

    /**
     * Returns a {@link org.apache.wicket.page.IManageablePage} by looking it up
     * by <code>sessionId</code> and
     * <code>pageId</code>. If there is a match then it is <i>touched</i>, i.e.
     * it is moved at
     * the top of the cache.
     *
     * @param _sessionId
     *            The id of the http session
     * @param _pageId
     *            The id of the page to find
     * @return the found serialized page or <code>null</code> when not found
     */
    @Override
    public IManageablePage getPage(final String _sessionId,
                                   final Integer _pageId)
    {
        IManageablePage result = null;

        if (this.maxEntriesPerSession > 0) {
            Args.notNull(_sessionId, "sessionId");
            Args.notNull(_pageId, "pageId");

            final SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> pagesPerSession = this.cache.get(
                            _sessionId);
            if (pagesPerSession != null) {
                final ConcurrentSkipListMap<PageValue, IManageablePage> pages = pagesPerSession.get();
                if (pages != null) {
                    final PageValue sample = new PageValue(_pageId);
                    for (final Map.Entry<PageValue, IManageablePage> entry : pages.entrySet()) {
                        if (sample.equals(entry.getKey())) {
                            // touch the entry
                            entry.getKey().touch();
                            result = entry.getValue();
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Store the serialized page in cache.
     *
     * @param _page
     *            the data to serialize (page id, session id, bytes)
     */
    @Override
    public void storePage(final String _sessionId,
                          final Integer _pageId,
                          final IManageablePage _page)
    {
        if (this.maxEntriesPerSession > 0) {
            Args.notNull(_sessionId, "sessionId");
            Args.notNull(_pageId, "pageId");

            SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> pagesPerSession = this.cache.get(
                            _sessionId);
            if (pagesPerSession == null) {
                final ConcurrentSkipListMap<PageValue, IManageablePage> pages = new ConcurrentSkipListMap<>(
                                new PageComparator());
                pagesPerSession = new SoftReference<>(pages);
                final SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> old = this.cache.putIfAbsent(
                                _sessionId, pagesPerSession);
                if (old != null) {
                    pagesPerSession = old;
                }
            }

            ConcurrentSkipListMap<PageValue, IManageablePage> pages = pagesPerSession.get();
            if (pages == null) {
                pages = new ConcurrentSkipListMap<>();
                pagesPerSession = new SoftReference<>(pages);
                final SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> old = this.cache.putIfAbsent(
                                _sessionId, pagesPerSession);
                if (old != null) {
                    pages = old.get();
                }
            }

            if (pages != null) {
                removePage(_sessionId, _pageId);

                final PageValue pv = new PageValue(_page);
                pages.put(pv, _page);

                while (pages.size() > this.maxEntriesPerSession) {
                    pages.pollFirstEntry();
                }
            }
        }
    }

    @Override
    public void destroy()
    {
        this.cache.clear();
    }

    /**
     * Helper class used to compare the page entries in the cache by their
     * access time.
     */
    private static final class PageValue
    {
        /**
         * The id of the cached page.
         */
        private final int pageId;

        /**
         * The last time this page has been used/accessed.
         */
        private long accessTime;

        /**
         * Instantiates a new page value.
         *
         * @param _page the page
         */
        private PageValue(final IManageablePage _page)
        {
            this(_page.getPageId());
        }

        /**
         * Instantiates a new page value.
         *
         * @param _pageId the page id
         */
        private PageValue(final int _pageId)
        {
            this.pageId = _pageId;
            touch();
        }

        /**
         * Updates the access time with the current time.
         */
        private void touch()
        {
            this.accessTime = System.nanoTime();
        }

        @Override
        public boolean equals(final Object _object)
        {
            if (this == _object) {
                return true;
            }
            if (_object == null || getClass() != _object.getClass()) {
                return false;
            }

            final PageValue pageValue = (PageValue) _object;

            return this.pageId == pageValue.pageId;
        }

        @Override
        public int hashCode()
        {
            return this.pageId;
        }
    }

    /**
     * The Class PageComparator.
     */
    private static class PageComparator
        implements Comparator<PageValue>
    {

        @Override
        public int compare(final PageValue _p1,
                           final PageValue _p2)
        {
            return Long.compare(_p1.accessTime, _p2.accessTime);
        }
    }
}
