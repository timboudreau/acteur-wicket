package com.mastfrog.acteur.wicket.page;

import org.apache.wicket.request.component.IRequestablePage;

/**
 * Default do nothing implementation
 *
 * @author Tim Boudreau
 */
class NoOpPageInstantiationInterceptor implements IPageInstantiationInterceptor {

    @Override
    public <T extends IRequestablePage> void onInstantiation(T page) throws Exception {
        //do nothing
    }
}
