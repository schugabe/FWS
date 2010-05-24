package fws_master;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


public class ViewSettings {
	private Shell shell;
	private FWSMaster master;
	
	private Text genTimeText;
	private Button autostartBox;
	
	public ViewSettings(Shell shell, FWSMaster master) {
		this.shell = shell;
		this.master = master;
		
		createView();
		
	}

	private void createView() {
		GridLayout gridLayout = new GridLayout(2,false);
		GridData gridData;
		
		shell.setLayout(gridLayout);
		
		autostartBox = new Button(shell, SWT.CHECK );
		autostartBox.setText("Autostart of data collection");
		gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		autostartBox.setLayoutData(gridData);
		autostartBox.setSelection(master.isAutoStart());
		
		Label genLabel = new Label(shell,SWT.None );
		genLabel.setText("Output generation [s]: ");
		gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		genLabel.setLayoutData(gridData);
		
		genTimeText = new Text(shell,  SWT.BORDER);
		genTimeText.setText(""+this.master.getGeneratorTime());
		
		genTimeText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = e.text.matches("\\d*");
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		genTimeText.setLayoutData(gridData);
		
		Button saveButton = new Button(shell, SWT.None);
		saveButton.setText("Save and Close");
		saveButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (genTimeText.getText() != null && genTimeText.getText() != "") {
					master.setGeneratorTime(Integer.parseInt(genTimeText.getText()));
					
				}
				master.setAutoStart(autostartBox.getSelection());
				shell.dispose();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
				
			}
			
		});
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.CENTER;
		saveButton.setLayoutData(gridData);
	}
	
	
}
