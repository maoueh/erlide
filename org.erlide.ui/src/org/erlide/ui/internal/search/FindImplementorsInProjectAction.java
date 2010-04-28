/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.erlide.ui.internal.search;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IWorkbenchSite;
import org.erlide.ui.editors.erl.ErlangEditor;

/**
 * Finds references to the selected element in the enclosing project of the
 * selected element. The action is applicable to selections representing a Java
 * element.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 3.0
 */
public class FindImplementorsInProjectAction extends FindImplementorsAction {

	/**
	 * Creates a new <code>FindReferencesInProjectAction</code>. The action
	 * requires that the selection provided by the site's selection provider is
	 * of type <code>IStructuredSelection</code>.
	 * 
	 * @param site
	 *            the site providing context information for this action
	 */
	public FindImplementorsInProjectAction(final IWorkbenchSite site) {
		super(site);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call
	 * this constructor.
	 * 
	 * @param editor
	 *            the Java editor
	 */
	public FindImplementorsInProjectAction(final ErlangEditor editor) {
		super(editor);
	}

	@Override
	void init() {
		setText("Project");
		setToolTipText("Find declarations in selected projects");
		// FIXME setImageDescriptor(JavaPluginImages.DESC_OBJS_SEARCH_REF);
		// FIXME PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
		// IJavaHelpContextIds.FIND_REFERENCES_IN_PROJECT_ACTION);
	}

	@Override
	protected Collection<IResource> getScope() {
		return getProjectScope();
	}

	@Override
	protected String getScopeDescription() {
		return SearchUtil.getProjectScopeDescription(getProjectScope());
	}
	// QuerySpecification createQuery(IErlElement element)
	// throws JavaModelException {
	// JavaSearchScopeFactory factory = JavaSearchScopeFactory.getInstance();
	// JavaEditor editor = getEditor();
	//
	// IJavaSearchScope scope;
	// String description;
	// final boolean isInsideJRE = factory.isInsideJRE(element);
	// if (editor != null) {
	// scope = factory.createJavaProjectSearchScope(editor
	// .getEditorInput(), isInsideJRE);
	// description = factory.getProjectScopeDescription(editor
	// .getEditorInput(), isInsideJRE);
	// } else {
	// scope = factory.createJavaProjectSearchScope(element
	// .getJavaProject(), isInsideJRE);
	// description = factory.getProjectScopeDescription(element
	// .getJavaProject(), isInsideJRE);
	// }
	// return new ElementQuerySpecification(element, getLimitTo(), scope,
	// description);
	// }

}