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

import java.io.Serializable;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.AbstractCachingPageStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.serialize.ISerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EFapsPageStore.
 *
 * @author The eFaps Team
 */
public class EFapsPageStore
    extends AbstractCachingPageStore<SerializedPage>
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsPageStore.class);

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
        super(_pageSerializer, _dataStore, new EFapsPageCache(_cacheSize));
    }

    @Override
    public void storePage(final String _sessionId,
                          final IManageablePage _page)
    {
        final SerializedPage serialized = createSerializedPage(_sessionId, _page);
        if (serialized != null) {
            final int pageId = _page.getPageId();
            this.pagesCache.storePage(_sessionId, pageId, serialized);
            storePageData(_sessionId, pageId, serialized.getData());
        }
    }

    @Override
    public IManageablePage convertToPage(final Object _object)
    {
        final IManageablePage ret;
        if (_object == null) {
            ret = null;
        } else if (_object instanceof IManageablePage) {
            ret = (IManageablePage) _object;
        } else if (_object instanceof SerializedPage) {
            final SerializedPage page = (SerializedPage) _object;
            byte[] data = page.getData();
            if (data == null) {
                data = getPageData(page.getSessionId(), page.getPageId());
            }
            if (data != null) {
                ret = deserializePage(data);
            } else {
                ret = null;
            }
        } else {
            final String type = _object.getClass().getName();
            throw new IllegalArgumentException("Unknown object type " + type);
        }
        return ret;
    }

    /**
     * Reloads the {@link SerializedPage} from the backing {@link IDataStore} if
     * the {@link SerializedPage#data} is stripped earlier.
     *
     * @param _serializedPage the serialized page
     * @return the fully functional {@link SerializedPage}
     */
    private SerializedPage restoreStrippedSerializedPage(final SerializedPage _serializedPage)
    {
        SerializedPage result = this.pagesCache.getPage(_serializedPage.getSessionId(), _serializedPage.getPageId());
        if (result == null) {
            final byte[] data = getPageData(_serializedPage.getSessionId(), _serializedPage.getPageId());
            result = new SerializedPage(_serializedPage.getSessionId(), _serializedPage.getPageId(), _serializedPage
                            .getKey(), data);
        }
        return result;
    }

    @Override
    public Serializable prepareForSerialization(final String _sessionId,
                                                final Serializable _page)
    {
        SerializedPage result = null;
        if (!this.dataStore.isReplicated()) {
            if (_page instanceof IManageablePage) {
                final IManageablePage pageTmp = (IManageablePage) _page;
                result = this.pagesCache.getPage(_sessionId, pageTmp.getPageId());
                if (result == null) {
                    result = createSerializedPage(_sessionId, pageTmp);
                    if (result != null) {
                        this.pagesCache.storePage(_sessionId, pageTmp.getPageId(), result);
                    }
                }
            } else if (_page instanceof SerializedPage) {
                final SerializedPage pageTmp = (SerializedPage) _page;
                if (pageTmp.getData() == null) {
                    result = restoreStrippedSerializedPage(pageTmp);
                } else {
                    result = pageTmp;
                }
            }
        }
        return result == null ? _page : result;
    }

    /**
     *
     * @return Always true for this implementation
     */
    protected boolean storeAfterSessionReplication()
    {
        return true;
    }

    @Override
    public Object restoreAfterSerialization(final Serializable _serializable)
    {
        final Object ret;
        if (_serializable == null) {
            ret = null;
        } else if (!storeAfterSessionReplication() || _serializable instanceof IManageablePage) {
            ret = _serializable;
        } else if (_serializable instanceof SerializedPage) {
            final SerializedPage page = (SerializedPage) _serializable;
            if (page.getData() != null) {
                storePageData(page.getSessionId(), page.getPageId(), page.getData());
                ret = new SerializedPage(page.getSessionId(), page.getPageId(), page.getKey(), null);
            } else {
                ret = page;
            }
        } else {
            final String type = _serializable.getClass().getName();
            throw new IllegalArgumentException("Unknown object type " + type);
        }
        return ret;
    }

    /**
     * Creates the serialized page.
     *
     * @param _sessionId the session id
     * @param _page the page
     * @return the serialized page information
     */
    protected SerializedPage createSerializedPage(final String _sessionId,
                                                  final IManageablePage _page)
    {
        SerializedPage serializedPage = null;

        final byte[] data = serializePage(_page);

        if (data != null) {
            serializedPage = new SerializedPage(_sessionId, _page.getPageId(), _page.getClass().getName(), data);
        } else if (LOG.isWarnEnabled()) {
            LOG.warn("Page {} cannot be serialized. See previous logs for possible reasons.", _page);
        }
        return serializedPage;
    }
}
