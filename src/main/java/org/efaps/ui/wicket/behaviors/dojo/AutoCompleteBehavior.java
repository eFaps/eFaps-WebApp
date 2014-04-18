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

package org.efaps.ui.wicket.behaviors.dojo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.XmlAjaxResponse;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.ILogData;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.util.EFapsKey;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AutoCompleteBehavior
    extends AbstractDojoBehavior
    implements IBehaviorListener
{

    /** the component that this handler is bound to. */
    private Component component;

    /**
     * Settings for this AutoComplete.
     */
    private final AutoCompleteSettings settings = new AutoCompleteSettings();

    /**
     * Bind this handler to the given component.
     *
     * @param _hostComponent the component to bind to
     */
    @Override
    public final void bind(final Component _hostComponent)
    {
        Args.notNull(_hostComponent, "hostComponent");

        if (this.component != null) {
            throw new IllegalStateException("this kind of handler cannot be attached to "
                            + "multiple components; it is already attached to component " + this.component
                            + ", but component " + _hostComponent + " wants to be attached too");
        }
        this.component = _hostComponent;
        this.component.setOutputMarkupId(true);
    }

    @Override
    public final void unbind(final Component _component)
    {
        this.component = null;
        super.unbind(_component);
    }

    /**
     * Gets the component that this handler is bound to.
     *
     * @return the component that this handler is bound to
     */
    protected final Component getComponent()
    {
        return this.component;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {

        super.renderHead(_component, _response);
        CoreLibrariesContributor.contributeAjax(this.component.getApplication(), _response);

        final RequestCycle requestCycle = this.component.getRequestCycle();
        final Url baseUrl = requestCycle.getUrlRenderer().getBaseUrl();
        final CharSequence ajaxBaseUrl = Strings.escapeMarkup(baseUrl.toString());
        _response.render(JavaScriptHeaderItem.forScript("Wicket.Ajax.baseUrl=\"" + ajaxBaseUrl +
                        "\";", "wicket-ajax-base-url"));

        final StringBuilder js2 = new StringBuilder()
                        .append("require([\"efaps/AjaxStore\",\"efaps/AutoComplete\",\"dojo/domReady!\"],")
                        .append(" function(AjaxStore, AutoComplete){")
                        .append("var ph=\"")
                        .append(DBProperties.getProperty(AutoCompleteBehavior.class.getName() + ".PlaceHolder"))
                        .append("\"\n")
                        .append("var stateStore= new AjaxStore();")
                        .append("var comboBox = new AutoComplete({\n" +
                                        "        id: \"").append(_component.getMarkupId()).append("\",\n" +
                                        "        name: \"").append(((AutoCompleteField) _component).getFieldName())
                        .append("\",\n" +
                                        "        value: \"\",\n" +
                                        "        store: stateStore,\n");
        js2.append("hasDownArrow:").append(this.settings.isHasDownArrow()).append(",");

        if (this.settings.getMinInputLength() > 1) {
            js2.append("minInputLength:").append(this.settings.getMinInputLength()).append(",");
        }

        if (this.settings.getSearchDelay() != 200) {
            js2.append("searchDelay:").append(this.settings.getSearchDelay()).append(",");
        }

        if (!"p".equals(this.settings.getParamName())) {
            js2.append("paramName:\"").append(this.settings.getParamName()).append("\",");
        }

        js2.append("placeHolder:ph,");


        js2.append("callbackUrl:\"" + getCallbackUrl() + "\"," +
                        "        searchAttr: \"name\"\n" +
                        "    }, \"").append(_component.getMarkupId()).append("\");")
                        .append("});");

        final StringBuilder js = new StringBuilder().append("require([\"dojo/ready\"]);")
                        .append("dojo.ready(function() {")
                        .append(js2)
                        .append(";});");

        _response.render(JavaScriptHeaderItem.forScript(js.toString(), "js_" + _component.getMarkupId()));
    }

    public interface AutoCompleteField
    {

        /**
         * @return
         */
        String getFieldName();

        Iterator<Map<String, String>> getChoices(final String _input);
    }

    @Override
    public void onRequest()
    {
        final WebApplication app = (WebApplication) getComponent().getApplication();
        final ACAjaxRequestTarget target = new ACAjaxRequestTarget(app.newAjaxRequestTarget(getComponent().getPage()));

        final RequestCycle requestCycle = RequestCycle.get();
        requestCycle.scheduleRequestHandlerAfterCurrent(target);

        final String val = requestCycle.getRequest()
                        .getRequestParameters()
                        .getParameterValue(this.settings.getParamName())
                        .toOptionalString();
        respond(target, val);
    }


    /**
     * @param _target   target
     * @param _val      value
     */
    protected void respond(final ACAjaxRequestTarget _target,
                           final String _val)
    {
        final AutoCompleteField field = (AutoCompleteField) getComponent();

        final Iterator<Map<String, String>> choices = field.getChoices(_val);

        try {
            final JSONArray jsonArray = new JSONArray();
            while (choices.hasNext()) {
                final Map<String, String> map = choices.next();
                final String key = map.get(EFapsKey.AUTOCOMPLETE_KEY.getKey()) != null
                                ? map.get(EFapsKey.AUTOCOMPLETE_KEY.getKey())
                                : map.get(EFapsKey.AUTOCOMPLETE_VALUE.getKey());
                final String choice = map.get(EFapsKey.AUTOCOMPLETE_CHOICE.getKey()) != null
                                ? map.get(EFapsKey.AUTOCOMPLETE_CHOICE.getKey())
                                : map.get(EFapsKey.AUTOCOMPLETE_VALUE.getKey());
                final JSONObject object = new JSONObject();
                object.put("id", key);
                object.put("name", choice);
                jsonArray.put(object);
            }
            _target.appendJSON(jsonArray.toString());

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Gets the url that references this handler.
     *
     * @return the url that references this handler
     */
    public CharSequence getCallbackUrl()
    {
        if (getComponent() == null) {
            throw new IllegalArgumentException("Behavior must be bound to a component to create the URL");
        }
        return getComponent().urlFor(this, IBehaviorListener.INTERFACE, new PageParameters());
    }

    public static class ACAjaxRequestTarget
        implements AjaxRequestTarget
    {

        private final AjaxRequestTarget target;
        private String jsonString;

        public ACAjaxRequestTarget(final AjaxRequestTarget _target)
        {
            this.target = _target;
        }

        /**
         * @param _string
         */
        public void appendJSON(final String _json)
        {
            this.jsonString = _json;
        }

        @Override
        public Integer getPageId()
        {
            return this.target.getPageId();
        }

        @Override
        public boolean isPageInstanceCreated()
        {
            return this.target.isPageInstanceCreated();
        }

        @Override
        public Integer getRenderCount()
        {
            return this.target.getRenderCount();
        }

        @Override
        public Class<? extends IRequestablePage> getPageClass()
        {
            return this.target.getPageClass();
        }

        @Override
        public PageParameters getPageParameters()
        {
            return this.target.getPageParameters();
        }

        @Override
        public void respond(final IRequestCycle _requestCycle)
        {
            final WebResponse response = (WebResponse) _requestCycle.getResponse();

            this.target.respond(_requestCycle);
            response.write("<json><![CDATA[");
            response.write(this.jsonString);
            response.write("]]></json>");
            response.write(XmlAjaxResponse.END_ROOT_ELEMENT);
        }

        @Override
        public void detach(final IRequestCycle _requestCycle)
        {
            this.target.detach(_requestCycle);

        }

        @Override
        public ILogData getLogData()
        {

            return this.target.getLogData();
        }

        @Override
        public void add(final Component _component,
                        final String _markupId)
        {
            this.target.add(_component, _markupId);

        }

        @Override
        public void add(final Component... _components)
        {
            this.target.add(_components);
        }

        @Override
        public void addChildren(final MarkupContainer _parent,
                                final Class<?> _childCriteria)
        {
            this.target.addChildren(_parent, _childCriteria);

        }

        @Override
        public void addListener(final IListener _listener)
        {
            this.target.addListener(_listener);

        }

        @Override
        public void appendJavaScript(final CharSequence _javascript)
        {
            this.target.appendJavaScript(_javascript);
        }

        @Override
        public void prependJavaScript(final CharSequence _javascript)
        {
            this.target.prependJavaScript(_javascript);

        }

        @Override
        public void registerRespondListener(final ITargetRespondListener _listener)
        {
            this.target.registerRespondListener(_listener);

        }

        @Override
        public Collection<? extends Component> getComponents()
        {
            return this.target.getComponents();
        }

        @Override
        public void focusComponent(final Component _component)
        {
            this.target.focusComponent(_component);
        }

        @Override
        public IHeaderResponse getHeaderResponse()
        {
            return this.target.getHeaderResponse();
        }

        @Override
        public String getLastFocusedElementId()
        {
            return this.target.getLastFocusedElementId();
        }

        @Override
        public Page getPage()
        {
            return this.target.getPage();
        }
    }
}
