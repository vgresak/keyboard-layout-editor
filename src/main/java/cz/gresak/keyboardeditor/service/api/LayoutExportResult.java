package cz.gresak.keyboardeditor.service.api;

import cz.gresak.keyboardeditor.service.impl.LayoutExporterImpl;

/**
 * Crate containing outcome of the {@link LayoutExporterImpl#export} operation.
 */
public class LayoutExportResult {

    private Result result;
    private Throwable exception;

    private LayoutExportResult() {
        this.result = Result.OK;
    }

    private LayoutExportResult(Throwable exception) {
        this.exception = exception;
        this.result = Result.ERROR;
    }

    public static LayoutExportResult ok() {
        return new LayoutExportResult();
    }

    public static LayoutExportResult error(Throwable exception) {
        return new LayoutExportResult(exception);
    }

    public boolean isOk() {
        return Result.OK.equals(result);
    }

    public Throwable getException() {
        return exception;
    }

    enum Result {
        OK, ERROR
    }
}
