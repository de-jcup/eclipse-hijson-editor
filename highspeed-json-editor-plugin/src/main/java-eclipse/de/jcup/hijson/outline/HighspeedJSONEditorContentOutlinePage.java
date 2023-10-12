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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import de.jcup.hijson.EclipseUtil;
import de.jcup.hijson.HighspeedJSONEditor;
import de.jcup.hijson.HighspeedJSONEditorActivator;
import de.jcup.hijson.script.HighspeedJSONModel;

public class HighspeedJSONEditorContentOutlinePage extends ContentOutlinePage implements IDoubleClickListener {
    /* @formatter:on */
    private static ImageDescriptor IMG_DESC_LINKED = EclipseUtil.createImageDescriptor("/icons/outline/synced.png", HighspeedJSONEditorActivator.PLUGIN_ID);
    private static ImageDescriptor IMG_DESC_NOT_LINKED = EclipseUtil.createImageDescriptor("/icons/outline/sync_broken.png", HighspeedJSONEditorActivator.PLUGIN_ID);
    private static ImageDescriptor IMG_DESC_OUTLINE_ENABLED = EclipseUtil.createImageDescriptor("/icons/outline/public_co.png", HighspeedJSONEditorActivator.PLUGIN_ID);
    private static ImageDescriptor IMG_DESC_OUTLINE_DISABLED = EclipseUtil.createImageDescriptor("/icons/outline/skip_outline.png", HighspeedJSONEditorActivator.PLUGIN_ID);

    private static ImageDescriptor IMG_DESC_COPY_FULLPATH_TO_CLIPBOARD = EclipseUtil.createImageDescriptor("/icons/outline/copy_fullpath_to_clipboard.png", HighspeedJSONEditorActivator.PLUGIN_ID);

    private static ImageDescriptor IMG_DESC_EXPAND_ALL = EclipseUtil.createImageDescriptor("/icons/expandall.png", HighspeedJSONEditorActivator.PLUGIN_ID);
    private static ImageDescriptor IMG_DESC_COLLAPSE_ALL = EclipseUtil.createImageDescriptor("/icons/collapseall.png", HighspeedJSONEditorActivator.PLUGIN_ID);

    /* @formatter:off */

	private HighspeedJSONEditorTreeContentProvider contentProvider;
	private Object input;
	private HighspeedJSONEditor editor;
	private HighspeedJSONEditorOutlineLabelProvider labelProvider;
	private ISelection selection;

	private boolean linkingWithEditorEnabled;
	private boolean ignoreNextSelectionEvents;
	private ToggleLinkingAction toggleLinkingAction;
	private ToggleEnableOutlineAction toggleEnableOutlineAction;

	public HighspeedJSONEditorContentOutlinePage(HighspeedJSONEditor editor) {
		this.editor = editor;
		this.contentProvider = new HighspeedJSONEditorTreeContentProvider();
	}

	public HighspeedJSONEditorTreeContentProvider getContentProvider() {
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
		
		CopyFullKeyPathToClipboardAction copyFullKeyPathToClipboardAction = new CopyFullKeyPathToClipboardAction();
		
	    ExpandAllAction expandAllAction = new ExpandAllAction();
        CollapseAllAction collapseAllAction = new CollapseAllAction();
        
        ExpandSelectionAction expandSelectionAction = new ExpandSelectionAction();
        CollapseSelectionAction collapseSelectionAction = new CollapseSelectionAction();
		
        MenuManager menuMgr = new MenuManager();
        menuMgr.add(expandSelectionAction);
        menuMgr.add(collapseSelectionAction);
        menuMgr.add(new Separator("clipboardGroup"));
        menuMgr.add(copyFullKeyPathToClipboardAction);
        
        Menu menu = menuMgr.createContextMenu(viewer.getTree());
        viewer.getControl().setMenu(menu);
        
		IActionBars actionBars = getSite().getActionBars();
		
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(toggleEnableOutlineAction);
		toolBarManager.add(toggleLinkingAction);
		toolBarManager.add(copyFullKeyPathToClipboardAction);
		
		IMenuManager viewMenuManager = actionBars.getMenuManager();
		viewMenuManager.add(new Separator("EndFilterGroup")); //$NON-NLS-1$
	
		viewMenuManager.add(new Separator("treeGroup")); //$NON-NLS-1$
		viewMenuManager.add(toggleEnableOutlineAction);
		viewMenuManager.add(toggleLinkingAction);
		viewMenuManager.add(expandAllAction);
        viewMenuManager.add(collapseAllAction);
        viewMenuManager.add(new Separator("treeSelectionGroup")); //$NON-NLS-1$
        viewMenuManager.add(expandSelectionAction);
        viewMenuManager.add(collapseSelectionAction);
	    viewMenuManager.add(new Separator("clipboardGroup")); //$NON-NLS-1$
	    viewMenuManager.add(copyFullKeyPathToClipboardAction);
		
		
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
		
		selection = event.getSelection();
		
		if (!linkingWithEditorEnabled) {
			return;
		}
		if (ignoreNextSelectionEvents) {
			return;
		}
		editor.openSelectedTreeItemInEditor(selection, false);
	}

	public void onEditorCaretMoved(int caretOffset) {
		if (!linkingWithEditorEnabled) {
			return;
		}
		ignoreNextSelectionEvents = true;
		if (contentProvider instanceof HighspeedJSONEditorTreeContentProvider) {
			HighspeedJSONEditorTreeContentProvider gcp = (HighspeedJSONEditorTreeContentProvider) contentProvider;
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
	
    private Object getFirstSelectedElement() {
        if (! (selection instanceof IStructuredSelection)) {
            return null;
        }
        IStructuredSelection ss = (IStructuredSelection) selection;
        Object element = ss.getFirstElement();
        return element;
    }

    
    class CollapseAllAction extends Action {

        private CollapseAllAction() {
            setImageDescriptor(IMG_DESC_COLLAPSE_ALL);
            setText("Collapse all");
        }

        @Override
        public void run() {
            getTreeViewer().collapseAll();
        }
    }

    class ExpandAllAction extends Action {

        private ExpandAllAction() {
            setImageDescriptor(IMG_DESC_EXPAND_ALL);
            setText("Expand all");
        }

        @Override
        public void run() {
            getTreeViewer().expandAll();
        }
    }
    
    class ExpandSelectionAction extends Action {

        private ExpandSelectionAction() {
            setImageDescriptor(IMG_DESC_EXPAND_ALL);
            setText("Expand children");
        }

        @Override
        public void run() {
            Object element = getFirstSelectedElement();
            if (element==null) {
                return ;
            }
            getTreeViewer().expandToLevel(element,TreeViewer.ALL_LEVELS);
        }
    }

    class CollapseSelectionAction extends Action {

        private CollapseSelectionAction() {
            setImageDescriptor(IMG_DESC_COLLAPSE_ALL);
            setText("Collapse children");
        }

        @Override
        public void run() {
            Object element = getFirstSelectedElement();
            if (element==null) {
                return ;
            }
            getTreeViewer().collapseToLevel(element,TreeViewer.ALL_LEVELS);
        }
       
    }
	
	class CopyFullKeyPathToClipboardAction extends Action {

        private CopyFullKeyPathToClipboardAction() {
            setImageDescriptor(IMG_DESC_COPY_FULLPATH_TO_CLIPBOARD);
            setText("Copy qualified key to clipboard");
            setToolTipText("Copy qualified key to clipboard.\n"
                    + "Only the selected node and its parents are checked. Select the deepest key node to copy the full path.");
        }

        @Override
        public void run() {
            Object element = getFirstSelectedElement();
            if (element instanceof Item) {
                Item item = (Item) element;
                String keyFullPath = getContentProvider().createFullPath(item);
                
                StringSelection selection = new StringSelection(keyFullPath);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                
            }
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
