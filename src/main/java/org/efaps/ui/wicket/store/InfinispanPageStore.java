/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.store;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.IPageContext;
import org.apache.wicket.pageStore.IPageStore;
import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class InfinispanPageStore.
 *
 * @author The eFaps Team
 */
public class InfinispanPageStore
    implements IPageStore
{

    /**
     * Name of the Cache for Instances.
     */
    public static final String PAGECACHE = InfinispanPageStore.class.getName() + ".Pages";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InfinispanPageStore.class);

    /**
     * Instantiates a new infinispan page store.
     */
    public InfinispanPageStore()
    {
        if (!((EmbeddedCacheManager) InfinispanCache.get().getContainer()).cacheExists(InfinispanPageStore.PAGECACHE)) {
            final Cache<Object, Object> cache = InfinispanCache.get().initCache(InfinispanPageStore.PAGECACHE);
            cache.addListener(new CacheLogListener(InfinispanPageStore.LOG));
        }
    }

    @Override
    public boolean supportsVersioning()
    {
        return true;
    }

    @Override
    public void destroy()
    {
        // do nothing
    }

    @Override
    public IManageablePage getPage(final IPageContext context, final int _pageId)
    {
        final StoredPage ret = InfinispanCache.get().<String, StoredPage>getIgnReCache(InfinispanPageStore.PAGECACHE)
                        .get(context.getSessionId(false) + "::" + _pageId);
        return ret == null ? null : ret.getPage();
    }

    @Override
    public void addPage(final IPageContext _context, final IManageablePage _page)
    {
        final var sessionId = _context.getSessionId(false);
        final StoredPage storedPage = new StoredPage().setPage(_page).setSessionId(sessionId);
        final String key = sessionId + "::" + _page.getPageId();
        if (_page instanceof MainPage) {
            InfinispanCache.get().getIgnReCache(InfinispanPageStore.PAGECACHE).put(key, storedPage, -1,
                            TimeUnit.SECONDS, getIdleSeconds(), TimeUnit.SECONDS);
        } else if (_page instanceof ContentContainerPage) {
            InfinispanCache.get().getIgnReCache(InfinispanPageStore.PAGECACHE).put(key, storedPage, -1,
                            TimeUnit.SECONDS, getIdleSeconds() / 2, TimeUnit.SECONDS);
        } else {
            InfinispanCache.get().getIgnReCache(InfinispanPageStore.PAGECACHE).put(key, storedPage, -1,
                            TimeUnit.SECONDS, getIdleSeconds() / 3, TimeUnit.SECONDS);
        }
    }

    @Override
    public void removePage(final IPageContext context, final IManageablePage page)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAllPages(final IPageContext context)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canBeAsynchronous(final IPageContext context)
    {
        return true;
    }

    /**
     * Gets the idle seconds.
     *
     * @return the idle seconds
     */
    private int getIdleSeconds()
    {
        return EFapsApplication.getMaxInactiveInterval() == 0 ? 3600 : EFapsApplication.getMaxInactiveInterval();
    }

    /**
     * Removes the session.
     *
     * @param _sessionId the session id
     */
    public static void removePages4Session(final String _sessionId)
    {
        final AdvancedCache<String, StoredPage> cache = InfinispanCache.get().<String, StoredPage>getIgnReCache(
                        InfinispanPageStore.PAGECACHE);
        final QueryFactory queryFactory = Search.getQueryFactory(cache);
        final var query = queryFactory
                        .<StoredPage>create("FROM org.efaps.ui.wicket.store.InfinispanPageStore$StoredPage "
                                        + "WHERE sessionId = \"" + _sessionId + "\"");
        query.execute().list().forEach(storedPage -> cache.remove(storedPage.getSessionId() + "::" + storedPage
                        .getPage().getPageId()));
    }

    /**
     * The Class StoredPage.
     */
    public static final class StoredPage
        implements Serializable
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The page. */
        private IManageablePage page;

        /** The session. */
        private String sessionId;

        /**
         * Getter method for the instance variable {@link #page}.
         *
         * @return value of instance variable {@link #page}
         */
        public IManageablePage getPage()
        {
            return page;
        }

        /**
         * Setter method for instance variable {@link #page}.
         *
         * @param _page value for instance variable {@link #page}
         * @return the stored page
         */
        public StoredPage setPage(final IManageablePage _page)
        {
            page = _page;
            return this;
        }

        /**
         * Getter method for the instance variable {@link #sessionId}.
         *
         * @return value of instance variable {@link #session}
         */
        public String getSessionId()
        {
            return sessionId;
        }

        /**
         * Setter method for instance variable {@link #sessionId}.
         *
         * @param _sessionId the session id
         * @return the stored page
         */
        public StoredPage setSessionId(final String _sessionId)
        {
            sessionId = _sessionId;
            return this;
        }
    }
}
