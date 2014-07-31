package org.teavm.eclipse.debugger.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class TeaVMTab extends AbstractLaunchConfigurationTab {
    private Text portField;

    @Override
    public void createControl(Composite container) {
        Composite root = new Composite(container, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 6;
        layout.numColumns = 2;
        root.setLayout(layout);

        Label portLabel = new Label(root, SWT.NONE);
        portLabel.setText("&Port");
        portLabel.setLayoutData(new GridData(GridData.BEGINNING));

        portField = new Text(root, SWT.SINGLE | SWT.BORDER);
        portField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        portField.addModifyListener(new ModifyListener() {
            @Override public void modifyText(ModifyEvent event) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            int attr = configuration.getAttribute("teavm-debugger-port", 2357);
            portField.setText(String.valueOf(attr));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute("teavm-debugger-port", Integer.parseInt(portField.getText()));
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute("teavm-debugger-port", 2357);
    }
}