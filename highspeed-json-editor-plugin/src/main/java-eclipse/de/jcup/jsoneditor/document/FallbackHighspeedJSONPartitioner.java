package de.jcup.jsoneditor.document;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;

public class FallbackHighspeedJSONPartitioner implements IDocumentPartitioner {

    private static final String[] LEGAL_CONTENTTYPES = new String[] {IDocument.DEFAULT_CONTENT_TYPE};
    private ITypedRegion[] PARTITITIONING = new ITypedRegion[] {};
    private IDocument document;
    private ITypedRegion documentAllRegion;

    @Override
    public void connect(IDocument document) {
        this.document = document;
        updateRegion();
    }

    private boolean updateRegion() {
        if (document == null) {
            this.documentAllRegion = null;
            return false;
        }
        if (documentAllRegion == null) {
            calculateNewAllRegion();
            return true;
        }
        if (document.getLength() == documentAllRegion.getLength()) {
            return false;
        }
        calculateNewAllRegion();
        return true;
    }

    private void calculateNewAllRegion() {
        this.documentAllRegion = new TypedRegion(0, document.getLength(), IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Override
    public void disconnect() {
        this.document = null;
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {

    }

    @Override
    public boolean documentChanged(DocumentEvent event) {
        return updateRegion();
    }

    @Override
    public String[] getLegalContentTypes() {
        return LEGAL_CONTENTTYPES;
    }

    @Override
    public String getContentType(int offset) {
        return IDocument.DEFAULT_CONTENT_TYPE;
    }

    @Override
    public ITypedRegion[] computePartitioning(int offset, int length) {
        return PARTITITIONING;
    }

    @Override
    public ITypedRegion getPartition(int offset) {
        return documentAllRegion;
    }

}
