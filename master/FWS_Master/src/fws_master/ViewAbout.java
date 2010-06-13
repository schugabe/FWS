package fws_master;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ViewAbout {
	private Shell shell;
	
	public ViewAbout(Shell shell) {
		this.shell = shell;
		initView();
	}

	private void initView() {
		shell.setLayout(new FillLayout());
		Label tmp = new Label(shell, SWT.NONE);
		tmp.setText("FWS Master\nVersion 0.0.1\n13. Juni 2010");
	}
}
