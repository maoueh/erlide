package org.erlide.tracing.core.ui.menu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.PlatformUI;
import org.erlide.model.internal.erlang.ErlFunction;
import org.erlide.model.util.ModelUtils;
import org.erlide.tracing.core.TraceBackend;
import org.erlide.tracing.core.mvc.model.TracePattern;

public class CreateTracePatternHandler extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final ISelection selection = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getSelection();

        if (selection instanceof ITreeSelection) {

            final Object firstElement = ((ITreeSelection) selection)
                    .getFirstElement();

            if (firstElement instanceof ErlFunction) {
                final ErlFunction function = (ErlFunction) firstElement;
                final TracePattern tracePattern = new TracePattern(true);
                tracePattern.setFunctionName(function.getFunctionName());
                tracePattern.setModuleName(ModelUtils.getModule(function)
                        .getModuleName());
                tracePattern.setArity(function.getArity());
                tracePattern.setLocal(true);
                tracePattern.setEnabled(true);
                TraceBackend.getInstance().addTracePattern(tracePattern);
            }
        }
        return null;
    }
}
