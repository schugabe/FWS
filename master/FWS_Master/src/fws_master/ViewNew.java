package fws_master;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ViewNew {
	private Shell shell;
	private FWSMaster master;
	private Text ipText,nameText;
	
	public ViewNew(Shell shell, FWSMaster master) {
		this.shell = shell;
		this.master = master;
		initView();
	}

	private void initView() {
		GridLayout gridLayout = new GridLayout(2,false);
		GridData gridData;
		
		shell.setLayout(gridLayout);
		
		Label nameLabel = new Label(shell, SWT.NONE);
		nameLabel.setText("Name:");
		gridData = new GridData();
		nameLabel.setLayoutData(gridData);
		
		nameText = new Text(shell,  SWT.BORDER);
		nameText.setText("New Station");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		nameText.setLayoutData(gridData);
		
		Label ipLabel = new Label(shell, SWT.NONE);
		ipLabel.setText("IP Address:");
		gridData = new GridData();
		ipLabel.setLayoutData(gridData);
		
		ipText = new Text(shell,  SWT.BORDER);
		ipText.setText(Station.defaultIP);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		ipText.setLayoutData(gridData);
		
		
		Button saveButton = new Button(shell,SWT.None);
		saveButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				master.addStationClicked(nameText.getText(),ipText.getText());
				shell.dispose();
			}
			
		});
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.CENTER;
		saveButton.setText("Add Station");
		saveButton.setLayoutData(gridData);
	}
	
	
}
