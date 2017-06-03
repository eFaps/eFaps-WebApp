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

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.DefaultPageStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.serialize.ISerializer;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EFapsPageStore.
 *
 * @author The eFaps Team
 */
public class EFapsPageStore
    extends DefaultPageStore
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsPageStore.class);

    /**
     * The cache implementation.
     */
    private final EFapsMainPagesCache mainPagesCache;

    /**
     * Instantiates a new eFaps page store.
     *
     * @param _pageSerializer the page serializer
     * @param _dataStore the data store
     * @param _cacheSize the cache size
     */
    public EFapsPageStore(final ISerializer _pageSerializer,
                          final IDataStore _dataStore,
                          final int _cacheSize)
    {
        super(_pageSerializer, _dataStore, _cacheSize);
        this.mainPagesCache = new EFapsMainPagesCache();
    }

    @Override
    public void storePage(final String _sessionId,
                          final IManageablePage _page)
    {
        if (_page instanceof MainPage) {
            this.mainPagesCache.storePage(_sessionId, _page.getPageId(), _page);
        } else {
            super.storePage(_sessionId, _page);
        }
    }

    @Override
    public IManageablePage getPage(final String _sessionId,
                                   final int _pageId)
    {
        IManageablePage ret = this.mainPagesCache.getPage(_sessionId, _pageId);
        if (ret == null) {
            final DefaultPageStore.SerializedPage fromCache = this.pagesCache.getPage(_sessionId, _pageId);
            if (fromCache != null) {
                ret = convertToPage(fromCache);
            }
        }
        if (ret == null) {
            final byte[] data = getPageData(_sessionId, _pageId);
            if (data != null) {
                ret = deserializePage(data);
            }
        }
        return ret;
    }
}
