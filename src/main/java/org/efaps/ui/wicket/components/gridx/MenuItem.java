package org.efaps.ui.wicket.components.gridx;

import org.apache.wicket.markup.html.WebComponent;
import org.efaps.ui.wicket.components.gridx.behaviors.OpenModalBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.SubmitBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.SubmitModalBehavior;

/**
 * The Class MenuItem.
 */
public class MenuItem
    extends WebComponent
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    public MenuItem(final String id)
    {
        super(id);
        add(new OpenModalBehavior());
        add(new SubmitBehavior());
        add(new SubmitModalBehavior());
    }
}
