package com.mastfrog.acteur.wicket.page;

import com.google.inject.ImplementedBy;
import org.apache.wicket.request.component.IRequestablePage;

/**
 *
 * @author Tim Boudreau
 */
@ImplementedBy(NoOpPageInstantiationInterceptor.class)
public interface IPageInstantiationInterceptor {
    <T extends IRequestablePage> void onInstantiation(T page) throws Exception;
}
