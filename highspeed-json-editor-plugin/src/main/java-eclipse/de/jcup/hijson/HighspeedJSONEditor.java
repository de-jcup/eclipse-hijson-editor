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
package de.jcup.hijson;

import static de.jcup.hijson.preferences.HighspeedJSONEditorPreferenceConstants.*;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;

import de.jcup.hijson.document.HighspeedJSONFileDocumentProvider;
import de.jcup.hijson.document.HighspeedJSONTextFileDocumentProvider;
import de.jcup.hijson.document.JSONFormatSupport;
import de.jcup.hijson.document.JSONFormatSupport.FormatterResult;
import de.jcup.hijson.outline.HighspeedJSONEditorContentOutlinePage;
import de.jcup.hijson.outline.HighspeedJSONEditorTreeContentProvider;
import de.jcup.hijson.outline.HighspeedJSONQuickOutlineDialog;
import de.jcup.hijson.outline.Item;
import de.jcup.hijson.preferences.HighspeedJSONEditorPreferences;
import de.jcup.hijson.script.HighSpeedJSONModelBuilder;
import de.jcup.hijson.script.HighspeedJSONError;
import de.jcup.hijson.script.HighspeedJSONModel;
import de.jcup.hijson.script.DefaultHighspeedJSONModelBuilder;

public class HighspeedJSONEditor extends TextEditor implements StatusMessageSupport, IResourceChangeListener {

    /** The COMMAND_ID of this editor as defined in plugin.xml */
    public static final String EDITOR_ID = "org.jsoneditor.editors.HighspeedJSONEditor";
    /** The COMMAND_ID of the editor context menu */
    public static final String EDITOR_CONTEXT_MENU_ID = EDITOR_ID + ".context";
    /** The COMMAND_ID of the editor ruler context menu */
    public static final String EDITOR_RULER_CONTEXT_MENU_ID = EDITOR_CONTEXT_MENU_ID + ".ruler";

    private static final HighspeedJSONModel FALLBACK_EMPTY_MODEL = new HighspeedJSONModel();

    private HighspeedJSONBracketsSupport bracketMatcher = new HighspeedJSONBracketsSupport();
    private SourceViewerDecorationSupport additionalSourceViewerSupport;
    private HighspeedJSONEditorContentOutlinePage outlinePage;
    private HighSpeedJSONModelBuilder modelBuilder;
    private Object monitor = new Object();
    private boolean quickOutlineOpened;
    private int lastCaretPosition;

    public HighspeedJSONEditor() {
        this.modelBuilder = DefaultHighspeedJSONModelBuilder.INSTANCE; // we use a shared build instance between editors (reduces memory foot print)
    }

    public void resourceChanged(IResourceChangeEvent event) {
        if (isMarkerChangeForThisEditor(event)) {
            int severity = getSeverity();

            setTitleImageDependingOnSeverity(severity);
        }
    }

    public void formatJSON() {
        FormatterResult result = JSONFormatSupport.DEFAULT.formatJSON(getDocumentText());
        if (!result.state.hasContentChanged()) {
            return;
        }
        getDocument().set(result.getFormatted());
    }

    public void validateJSON() {
        try {
            JSONFormatSupport.DEFAULT.validateJSON(getDocumentText());
        } catch (JsonProcessingException e) {
            JsonLocation location = e.getLocation();
            HighspeedJSONEditorUtil.addErrorMarker(location.getLineNr(), e.getMessage(), getEditorInput(), (int) location.getCharOffset(), (int) location.getCharOffset());
        }
    }

    /**
     * Opens quick outline
     */
    public void openQuickOutline() {
        synchronized (monitor) {
            if (quickOutlineOpened) {
                /*
                 * already opened - this is in future the anker point for ctrl+o+o...
                 */
                return;
            }
            quickOutlineOpened = true;
        }
        Shell shell = getEditorSite().getShell();
        HighspeedJSONModel model = buildOutlineModelWithoutValidation();
        HighspeedJSONQuickOutlineDialog dialog = new HighspeedJSONQuickOutlineDialog(this, shell, "Quick outline");
        dialog.setInput(model);

        dialog.open();
        synchronized (monitor) {
            quickOutlineOpened = false;
        }
    }

    private HighspeedJSONModel buildOutlineModelWithoutValidation() {
        String text = getDocumentText();
        HighspeedJSONModel model = modelBuilder.build(text, getGroupdArraysTreshold(), true);
        return model;
    }

    void setTitleImageDependingOnSeverity(int severity) {
        EclipseUtil.safeAsyncExec(() -> {
            if (severity == IMarker.SEVERITY_ERROR) {
                setTitleImage(EclipseUtil.getImage("icons/highspeed-json-editor-with-error.png", HighspeedJSONEditorActivator.PLUGIN_ID));
            } else {
                setTitleImage(EclipseUtil.getImage("icons/highspeed-json-editor.png", HighspeedJSONEditorActivator.PLUGIN_ID));
            }
        });
    }

    private int getSeverity() {
        IEditorInput editorInput = getEditorInput();
        if (editorInput == null) {
            return IMarker.SEVERITY_INFO;
        }
        try {
            final IResource resource = ResourceUtil.getResource(editorInput);
            if (resource == null) {
                return IMarker.SEVERITY_INFO;
            }
            int severity = resource.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            return severity;
        } catch (CoreException e) {
            // Might be a project that is not open
        }
        return IMarker.SEVERITY_INFO;
    }

    protected void addErrorMarkers(HighspeedJSONModel model) {
        if (model == null) {
            return;
        }
        IDocument document = getDocument();
        if (document == null) {
            return;
        }

        List<HighspeedJSONError> errors = model.getErrors();
        for (HighspeedJSONError error : errors) {
            int startPos = (int) error.offset;
            int line;
            try {
                line = document.getLineOfOffset(startPos);
            } catch (BadLocationException e) {
                HighspeedJSONEditorUtil.logError("Cannot get line offset for " + startPos, e);
                line = 0;
            }
            HighspeedJSONEditorUtil.addErrorMarker(this, line, error);
        }

    }

    public void setErrorMessage(String message) {
        super.setStatusLineErrorMessage(message);
    }

    public HighspeedJSONBracketsSupport getBracketMatcher() {
        return bracketMatcher;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        Control adapter = getAdapter(Control.class);
        if (adapter instanceof StyledText) {
            StyledText text = (StyledText) adapter;
            text.addCaretListener(new HighspeedJSONEditorCaretListener());
        }

        activateHighspeedJSONEditorContext();

        installAdditionalSourceViewerSupport();

        StyledText styledText = getSourceViewer().getTextWidget();
        styledText.addKeyListener(new HighspeedJSONBracketInsertionCompleter(this));

        /*
         * register as resource change listener to provide marker change listening
         */
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);

        setTitleImageInitial();
    }

    public HighspeedJSONEditorContentOutlinePage getOutlinePage() {
        if (outlinePage == null) {
            outlinePage = new HighspeedJSONEditorContentOutlinePage(this);
        }
        return outlinePage;
    }

    /**
     * Installs an additional source viewer support which uses editor preferences
     * instead of standard text preferences. If standard source viewer support would
     * be set with editor preferences all standard preferences would be lost or had
     * to be reimplmented. To avoid this another source viewer support is
     * installed...
     */
    private void installAdditionalSourceViewerSupport() {

        additionalSourceViewerSupport = new SourceViewerDecorationSupport(getSourceViewer(), getOverviewRuler(), getAnnotationAccess(), getSharedColors());
        additionalSourceViewerSupport.setCharacterPairMatcher(bracketMatcher);
        additionalSourceViewerSupport.setMatchingCharacterPainterPreferenceKeys(P_EDITOR_MATCHING_BRACKETS_ENABLED.getId(), P_EDITOR_MATCHING_BRACKETS_COLOR.getId(),
                P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION.getId(), P_EDITOR_ENCLOSING_BRACKETS.getId());

        IPreferenceStore preferenceStoreForDecorationSupport = HighspeedJSONEditorUtil.getPreferences().getPreferenceStore();
        additionalSourceViewerSupport.install(preferenceStoreForDecorationSupport);
    }

    @Override
    public void dispose() {
        super.dispose();

        if (additionalSourceViewerSupport != null) {
            additionalSourceViewerSupport.dispose();
        }
        if (bracketMatcher != null) {
            bracketMatcher.dispose();
            bracketMatcher = null;
        }

        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }

    public String getBackGroundColorAsWeb() {
        ensureColorsFetched();
        return bgColor;
    }

    public String getForeGroundColorAsWeb() {
        ensureColorsFetched();
        return fgColor;
    }

    private void ensureColorsFetched() {
        if (bgColor == null || fgColor == null) {

            ISourceViewer sourceViewer = getSourceViewer();
            if (sourceViewer == null) {
                return;
            }
            StyledText textWidget = sourceViewer.getTextWidget();
            if (textWidget == null) {
                return;
            }

            EclipseUtil.getSafeDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    bgColor = ColorUtil.convertToHexColor(textWidget.getBackground());
                    fgColor = ColorUtil.convertToHexColor(textWidget.getForeground());
                }
            });
        }

    }

    private String bgColor;
    private String fgColor;
    private boolean ignoreNextCaretMove;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (HighspeedJSONEditor.class.equals(adapter)) {
            return (T) this;
        }
        if (IContentOutlinePage.class.equals(adapter)) {
            return (T) getOutlinePage();
        }
        if (ColorManager.class.equals(adapter)) {
            return (T) getColorManager();
        }
        if (IFile.class.equals(adapter)) {
            IEditorInput input = getEditorInput();
            if (input instanceof IFileEditorInput) {
                IFileEditorInput feditorInput = (IFileEditorInput) input;
                return (T) feditorInput.getFile();
            }
            return null;
        }
        if (ISourceViewer.class.equals(adapter)) {
            return (T) getSourceViewer();
        }
        if (StatusMessageSupport.class.equals(adapter)) {
            return (T) this;
        }
        if (ITreeContentProvider.class.equals(adapter) || HighspeedJSONEditorTreeContentProvider.class.equals(adapter)) {
            if (outlinePage == null) {
                return null;
            }
            return (T) outlinePage.getContentProvider();
        }
        return super.getAdapter(adapter);
    }

    /**
     * Jumps to the matching bracket.
     */
    public void gotoMatchingBracket() {

        bracketMatcher.gotoMatchingBracket(this);
    }

    /**
     * Get document text - safe way.
     * 
     * @return string, never <code>null</code>
     */
    String getDocumentText() {
        IDocument doc = getDocument();
        if (doc == null) {
            return "";
        }
        return doc.get();
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        setDocumentProvider(createDocumentProvider(input));
        super.doSetInput(input);

        rebuildOutlineAndOrValidate();
    }

    @Override
    protected void editorSaved() {
        super.editorSaved();
        rebuildOutlineAndOrValidate();
    }

    private int getGroupdArraysTreshold() {
        return HighspeedJSONEditorPreferences.getInstance().getGroupdArraysTreshold();
    }

    /**
     * Does rebuild the outline - this is done asynchronous
     */
    public void rebuildOutlineAndOrValidate() {

        Runnable r = new Runnable() {

            @Override
            public void run() {
                HighspeedJSONEditorUtil.removeScriptErrors(HighspeedJSONEditor.this);

                HighspeedJSONEditorContentOutlinePage page = getOutlinePage();

                boolean outlineBuildEnabled = page.isOutlineBuildEnabled();
                boolean validateOnSaveEnabled = HighspeedJSONEditorPreferences.getInstance().isValidateOnSaveEnabled();

                if (validateOnSaveEnabled) {
                    validateJSON();
                }

                if (!outlineBuildEnabled) {
                    page.rebuild(FALLBACK_EMPTY_MODEL); // reset tree
                    return;
                }
                HighspeedJSONModel outlineJSONModel = buildOutlineModelWithoutValidation();
                if (outlineJSONModel == null) {
                    outlineJSONModel = FALLBACK_EMPTY_MODEL;
                }

                page.rebuild(outlineJSONModel);
            }
        };

        UIJob job = new UIJob("Build HiJSON outline") {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                r.run();
                return Status.OK_STATUS;
            }
        };
        job.schedule(300);
    }

    /**
     * Set initial title image dependent on current marker severity. This will mark
     * error icon on startup time which is not handled by resource change handling,
     * because having no change...
     */
    private void setTitleImageInitial() {
        IResource resource = resolveResource();
        if (resource != null) {
            try {
                int maxSeverity = resource.findMaxProblemSeverity(null, true, IResource.DEPTH_INFINITE);
                setTitleImageDependingOnSeverity(maxSeverity);
            } catch (CoreException e) {
                /* ignore */
            }
        }
    }

    /**
     * Resolves resource from current editor input.
     * 
     * @return file resource or <code>null</code>
     */
    private IResource resolveResource() {
        IEditorInput input = getEditorInput();
        if (!(input instanceof IFileEditorInput)) {
            return null;
        }
        return ((IFileEditorInput) input).getFile();
    }

    private boolean isMarkerChangeForThisEditor(IResourceChangeEvent event) {
        IResource resource = ResourceUtil.getResource(getEditorInput());
        if (resource == null) {
            return false;
        }
        IPath path = resource.getFullPath();
        if (path == null) {
            return false;
        }
        IResourceDelta eventDelta = event.getDelta();
        if (eventDelta == null) {
            return false;
        }
        IResourceDelta delta = eventDelta.findMember(path);
        if (delta == null) {
            return false;
        }
        boolean isMarkerChangeForThisResource = (delta.getFlags() & IResourceDelta.MARKERS) != 0;
        return isMarkerChangeForThisResource;
    }

    private IDocumentProvider createDocumentProvider(IEditorInput input) {
        if (input instanceof FileStoreEditorInput) {
            return new HighspeedJSONTextFileDocumentProvider(this);
        } else {
            return new HighspeedJSONFileDocumentProvider(this);
        }
    }

    public IDocument getDocument() {
        return getDocumentProvider().getDocument(getEditorInput());
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        if (site == null) {
            return;
        }
        IWorkbenchPage page = site.getPage();
        if (page == null) {
            return;
        }

        // workaround to show action set for block mode etc.
        // https://www.eclipse.org/forums/index.php/t/366630/
        page.showActionSet("org.eclipse.ui.edit.text.actionSet.presentation");

    }

    @Override
    protected void initializeEditor() {
        setSourceViewerConfiguration(new HighspeedJSONSourceViewerConfiguration(this));
        super.initializeEditor();
        setEditorContextMenuId(EDITOR_CONTEXT_MENU_ID);
        setRulerContextMenuId(EDITOR_RULER_CONTEXT_MENU_ID);
    }

    private void activateHighspeedJSONEditorContext() {
        IContextService contextService = getSite().getService(IContextService.class);
        if (contextService != null) {
            contextService.activateContext(EDITOR_CONTEXT_MENU_ID);
        }
    }

    private ColorManager getColorManager() {
        return HighspeedJSONEditorActivator.getDefault().getColorManager();
    }

    public void handleColorSettingsChanged() {
        // done like in TextEditor for spelling
        ISourceViewer viewer = getSourceViewer();
        SourceViewerConfiguration configuration = getSourceViewerConfiguration();
        if (viewer instanceof ISourceViewerExtension2) {
            ISourceViewerExtension2 viewerExtension2 = (ISourceViewerExtension2) viewer;
            viewerExtension2.unconfigure();
            if (configuration instanceof HighspeedJSONSourceViewerConfiguration) {
                HighspeedJSONSourceViewerConfiguration gconf = (HighspeedJSONSourceViewerConfiguration) configuration;
                gconf.updateTextScannerDefaultColorToken();
            }
            viewer.configure(configuration);
        }
    }

    /**
     * Toggles comment of current selected lines
     */
    public void toggleComment() {
        ISelection selection = getSelectionProvider().getSelection();
        if (!(selection instanceof TextSelection)) {
            return;
        }
        IDocumentProvider dp = getDocumentProvider();
        IDocument doc = dp.getDocument(getEditorInput());
        TextSelection ts = (TextSelection) selection;
        int startLine = ts.getStartLine();
        int endLine = ts.getEndLine();

        /* do comment /uncomment */
        for (int i = startLine; i <= endLine; i++) {
            IRegion info;
            try {
                info = doc.getLineInformation(i);
                int offset = info.getOffset();
                String line = doc.get(info.getOffset(), info.getLength());
                StringBuilder foundCode = new StringBuilder();
                StringBuilder whitespaces = new StringBuilder();
                for (int j = 0; j < line.length(); j++) {
                    char ch = line.charAt(j);
                    if (Character.isWhitespace(ch)) {
                        if (foundCode.length() == 0) {
                            whitespaces.append(ch);
                        }
                    } else {
                        foundCode.append(ch);
                    }
                    if (foundCode.length() > 2) {
                        break;
                    }
                }
                int whitespaceOffsetAdd = whitespaces.length();
                if ("REM".equals(foundCode.toString())) {
                    /* comment before */
                    doc.replace(offset + whitespaceOffsetAdd, 4, "");
                } else {
                    /* not commented */
                    doc.replace(offset, 0, "REM ");
                }

            } catch (BadLocationException e) {
                /* ignore and continue */
                continue;
            }

        }
        /* reselect */
        int selectionStartOffset;
        try {
            selectionStartOffset = doc.getLineOffset(startLine);
            int endlineOffset = doc.getLineOffset(endLine);
            int endlineLength = doc.getLineLength(endLine);
            int endlineLastPartOffset = endlineOffset + endlineLength;
            int length = endlineLastPartOffset - selectionStartOffset;

            ISelection newSelection = new TextSelection(selectionStartOffset, length);
            getSelectionProvider().setSelection(newSelection);
        } catch (BadLocationException e) {
            /* ignore */
        }
    }

    public void openSelectedTreeItemInEditor(ISelection selection, boolean grabFocus) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object firstElement = ss.getFirstElement();
            if (firstElement instanceof Item) {
                Item item = (Item) firstElement;
                int offset = item.getOffset();
                int length = item.getLength();
                if (length == 0) {
                    /* fall back */
                    length = 1;
                }
                ignoreNextCaretMove = true;
                selectAndReveal(offset, length);
                if (grabFocus) {
                    setFocus();
                }
            }
        }
    }

    public Item getItemAtCarretPosition() {
        return getItemAt(lastCaretPosition);
    }

    public Item getItemAt(int offset) {
        if (outlinePage == null) {
            return null;
        }
        HighspeedJSONEditorTreeContentProvider contentProvider = outlinePage.getContentProvider();
        if (contentProvider == null) {
            return null;
        }
        Item item = contentProvider.tryToFindByOffset(offset);
        return item;
    }

    public void selectFunction(String text) {
        System.out.println("should select functin:" + text);

    }

    public HighspeedJSONEditorPreferences getPreferences() {
        return HighspeedJSONEditorPreferences.getInstance();
    }

    private class HighspeedJSONEditorCaretListener implements CaretListener {

        @Override
        public void caretMoved(CaretEvent event) {
            if (event == null) {
                return;
            }
            lastCaretPosition = event.caretOffset;
            if (ignoreNextCaretMove) {
                ignoreNextCaretMove = false;
                return;
            }
            if (outlinePage == null) {
                return;
            }
            outlinePage.onEditorCaretMoved(event.caretOffset);
        }

    }

    private void markAsDirty() {
        IDocumentProvider provider = getDocumentProvider();
        if (provider instanceof IDocumentProviderExtension) {
            IDocumentProviderExtension ext = (IDocumentProviderExtension) provider;
            ext.setCanSaveDocument(getEditorInput());
        }
//        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    @Override
    public boolean isDirty() {
        return super.isDirty();
    }

    public void markDirtyBecauseFormatWasNecessaryForOneLinerHandling() {
        /*
         * document was a "one liner" and was transformed to multi lines - so rendered
         * now fast by eclipse
         */
        String message = "Auto format done - was necessary because one liners in eclipse would be extreme slow!";
        setStatusLineMessage(message);
        EclipseUtil.getSafeDisplay().asyncExec(() -> {
            HighspeedJSONEditorUtil.addInfoMarker(this, 0, message);
            markAsDirty();
        });
    }

}
