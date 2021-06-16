/*
 * Copyright 2020 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */
package de.jcup.hijson.outline;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import de.jcup.hijson.EclipseUtil;
import de.jcup.hijson.HighspeedJSONEditor;
import de.jcup.hijson.HighspeedJSONEditorActivator;
import de.jcup.hijson.outline.Item;
import de.jcup.hijson.script.HighspeedJSONModel;

public class HighspeedJSONEditorContentOutlinePage extends ContentOutlinePage implements IDoubleClickListener {
    /* @formatter:on */
    private static ImageDescriptor IMG_DESC_LINKED = EclipseUtil.createImageDescriptor("/icons/outline/synced.png", HighspeedJSONEditorActivator.PLUGIN_ID);
    private static ImageDescriptor IMG_DESC_NOT_LINKED = EclipseUtil.createImageDescriptor("/icons/outline/sync_broken.png", HighspeedJSONEditorActivator.PLUGIN_ID);
    private static ImageDescriptor IMG_DESC_OUTLINE_ENABLED = EclipseUtil.createImageDescriptor("/icons/outline/public_co.png", HighspeedJSONEditorActivator.PLUGIN_ID);
    private static ImageDescriptor IMG_DESC_OUTLINE_DISABLED = EclipseUtil.createImageDescriptor("/icons/outline/skip_outline.png", HighspeedJSONEditorActivator.PLUGIN_ID);
    /* @formatter:off */

	private HighspeedJSONEditorTreeContentProvider2 contentProvider;
	private Object input;
	private HighspeedJSONEditor editor;
	private HighspeedJSONEditorOutlineLabelProvider labelProvider;

	private boolean linkingWithEditorEnabled;
	private boolean ignoreNextSelectionEvents;
	private ToggleLinkingAction toggleLinkingAction;
	private ToggleEnableOutlineAction toggleEnableOutlineAction;

	public HighspeedJSONEditorContentOutlinePage(HighspeedJSONEditor editor) {
		this.editor = editor;
		this.contentProvider = new HighspeedJSONEditorTreeContentProvider2();
	}

	public HighspeedJSONEditorTreeContentProvider2 getContentProvider() {
		return contentProvider;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);

		labelProvider = new HighspeedJSONEditorOutlineLabelProvider();

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(contentProvider);
		viewer.addDoubleClickListener(this);
		viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(labelProvider));
		viewer.addSelectionChangedListener(this);

		/* it can happen that input is already updated before control created */
		if (input != null) {
			viewer.setInput(input);
		}
		toggleLinkingAction = new ToggleLinkingAction();
		toggleLinkingAction.setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_TOGGLE_LINK_WITH_EDITOR);
		
		toggleEnableOutlineAction = new ToggleEnableOutlineAction();
		
		IActionBars actionBars = getSite().getActionBars();
		
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(toggleEnableOutlineAction);
		toolBarManager.add(toggleLinkingAction);
		
		IMenuManager viewMenuManager = actionBars.getMenuManager();
		viewMenuManager.add(new Separator("EndFilterGroup")); //$NON-NLS-1$
	
		viewMenuManager.add(new Separator("treeGroup")); //$NON-NLS-1$
		viewMenuManager.add(toggleEnableOutlineAction);
		viewMenuManager.add(toggleLinkingAction);
		
		
		/*
		 * when no input is set on init state - let the editor rebuild outline
		 * (async)
		 */
		if (input == null && editor != null) {
			editor.rebuildOutlineAndOrValidate();
		}

	}
	
	public boolean isOutlineBuildEnabled() {
	    return contentProvider.outlineEnabled;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		if (editor == null) {
			return;
		}
		if (linkingWithEditorEnabled) {
			editor.setFocus();
			// selection itself is already handled by single click
			return;
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		if (!linkingWithEditorEnabled) {
			return;
		}
		if (ignoreNextSelectionEvents) {
			return;
		}
		ISelection selection = event.getSelection();
		editor.openSelectedTreeItemInEditor(selection, false);
	}

	public void onEditorCaretMoved(int caretOffset) {
		if (!linkingWithEditorEnabled) {
			return;
		}
		ignoreNextSelectionEvents = true;
		if (contentProvider instanceof HighspeedJSONEditorTreeContentProvider2) {
			HighspeedJSONEditorTreeContentProvider2 gcp = (HighspeedJSONEditorTreeContentProvider2) contentProvider;
			Item item = gcp.tryToFindByOffset(caretOffset);
			if (item != null) {
				StructuredSelection selection = new StructuredSelection(item);
				getTreeViewer().setSelection(selection, true);
			}
		}
		ignoreNextSelectionEvents = false;
	}

	public void rebuild(HighspeedJSONModel model) {
		contentProvider.rebuildTree(model);

		TreeViewer treeViewer = getTreeViewer();
		if (treeViewer != null) {
			Control control = treeViewer.getControl();
			if (control == null || control.isDisposed()){
				return;
			}
			treeViewer.setInput(model);
		}
	}

	class ToggleEnableOutlineAction extends Action {

        private ToggleEnableOutlineAction() {
			if (editor != null) {
				contentProvider.outlineEnabled = editor.getPreferences().isOutlineBuildEnabled();
			}
			setDescription("enable/disable outline model build");
			initImage();
			initText();
		}

		@Override
		public void run() {
		    contentProvider.outlineEnabled= !contentProvider.outlineEnabled;

			initText();
			initImage();
			
			editor.rebuildOutlineAndOrValidate();
		}

		private void initImage() {
			setImageDescriptor(
			        contentProvider.outlineEnabled ? getImageDescriptionForOutlineEnabled() : getImageDescriptionForOutlineDisabled());
		}


        private void initText() {
			setText(contentProvider.outlineEnabled ? "Click to disable outline creation" : "Click to enable outline creation");
		}

	}
	
	class ToggleLinkingAction extends Action {

        private ToggleLinkingAction() {
            if (editor != null) {
                linkingWithEditorEnabled = editor.getPreferences().isLinkOutlineWithEditorEnabled();
            }
            setDescription("link with editor");
            initImage();
            initText();
        }

        @Override
        public void run() {
            linkingWithEditorEnabled = !linkingWithEditorEnabled;

            initText();
            initImage();
        }

        private void initImage() {
            setImageDescriptor(
                    linkingWithEditorEnabled ? getImageDescriptionForLinked() : getImageDescriptionNotLinked());
        }

        private void initText() {
            setText(linkingWithEditorEnabled ? "Click to unlink from editor" : "Click to link with editor");
        }

    }

	protected ImageDescriptor getImageDescriptionForLinked() {
		return IMG_DESC_LINKED;
	}

	public ImageDescriptor getImageDescriptionForOutlineDisabled() {
	    return IMG_DESC_OUTLINE_DISABLED;
    }

    public ImageDescriptor getImageDescriptionForOutlineEnabled() {
        return IMG_DESC_OUTLINE_ENABLED;
    }

    protected ImageDescriptor getImageDescriptionNotLinked() {
		return IMG_DESC_NOT_LINKED;
	}

}
