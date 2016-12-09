package org.efaps.ui.wicket.models.objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * The Class AbstractUI.
 */
public class AbstractUI
    implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The idmap. */
    private final Map<String, Long> random2id = new HashMap<>();

    /**
     * Gets the random for ID.
     *
     * @param _id the id
     * @return the random for ID
     */
    public String getRandom4ID(final Long _id)
    {
        final String rid = RandomStringUtils.randomAlphanumeric(8);
        this.random2id.put(rid, _id);
        return rid;
    };

    /**
     * Gets the ID for random.
     *
     * @param _rid the rid
     * @return the ID for random
     */
    public Long getID4Random(final String _rid)
    {
        return this.random2id.get(_rid);
    };
}
