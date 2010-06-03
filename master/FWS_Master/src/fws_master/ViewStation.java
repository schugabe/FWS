package fws_master;


import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;



public class ViewStation {
	private Shell shell;
	private StationController station_controller;
	private ParameterController parameter_controller;
	private Station selected_station;
	private StationConfigBinding selectedCfgBinding;
	private StationInputBinding selectedIpBinding;
	

	private List stationList,cfgParameterList,ipParameterList;
	private Text nameText,ipText,pollingText;
	
	private Text cfgAddressText,cfgValueText;
	private Text ipAddressText, ipPlotText;
	private Button cfgActive,ipActive, cfgBindButton, ipBindButton;

	
	public ViewStation(Shell shell,StationController sc,ParameterController p) {
		this.shell = shell;
		this.station_controller = sc;
		this.parameter_controller = p;
		this.InitView();
		loadStationList();
		loadCfgList();
		loadIpList();
		this.enableCfgFields(false);
		this.enableIpFields(false);
		this.select_first();
		
	}
	
	private void enableText(boolean enabled) {
		this.nameText.setEnabled(enabled);
		this.ipText.setEnabled(enabled);
		this.pollingText.setEnabled(enabled);
	}
	
	private void enableIpFields(boolean enabled) {
		ipActive.setEnabled(enabled);
		ipBindButton.setEnabled(enabled);
		ipAddressText.setEnabled(enabled);
		ipPlotText.setEnabled(enabled);
	}
	
	private void enableCfgFields(boolean enabled) {
		cfgActive.setEnabled(enabled);
		cfgBindButton.setEnabled(enabled);
		cfgAddressText.setEnabled(enabled);
		cfgValueText.setEnabled(enabled);
	}
	
	private void select_first() {
		if(this.stationList.getItemCount() >0) {
			this.stationList.select(0);
			this.stationListSelected();
			
		}
		else {
			this.enableText(false);
		}
	}
	
	private void InitView() {
		GridLayout gridLayout = new GridLayout(3,false);
		ButtonListener bl = new ButtonListener();
		shell.setLayout(gridLayout);

		stationList = new List(this.shell,SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL );	
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 6;
		gridData.widthHint = 150;
		stationList.setLayoutData(gridData);
		stationList.addListener(SWT.Selection, new ListListener());
		
		
		Label nameLabel = new Label(shell, SWT.NONE);
		nameLabel.setText("Name:");
		
		nameText = new Text(shell, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		nameText.setLayoutData(gridData);
		nameText.setText("");
		
		Label ipLabel = new Label(shell, SWT.NONE);
		ipLabel.setText("IP Address:");
		
		ipText = new Text(shell, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		ipText.setLayoutData(gridData);
		ipText.setText("");
		
		Label pollingLabel = new Label(shell, SWT.NONE);
		pollingLabel.setText("Intervall:");
		
		pollingText = new Text(shell, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		pollingText.setLayoutData(gridData);
		pollingText.setText("");
		
		Button saveButton = new Button(shell, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addSelectionListener(bl);
		gridData = new GridData();
		//gridData.horizontalSpan=2;
		saveButton.setLayoutData(gridData);
				
		
		Composite params = new Composite(shell, SWT.BORDER);
		
		params.setLayout(new GridLayout(3,false));
		gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		params.setLayoutData(gridData);
		
		
		
		this.cfgParameterList = new List(params,SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL  );
		gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 6;
		gridData.widthHint = 200;
		cfgParameterList.setLayoutData(gridData);
		cfgParameterList.addListener(SWT.Selection, new PListListener());
				
		Label parmLabel = new Label(params, SWT.NONE);
		parmLabel.setText("Configuration Parameters");
		
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan =2;
		parmLabel.setLayoutData(gridData);
		
		Label addressLabel = new Label(params, SWT.NONE);
		addressLabel.setText("Address:");
		
		cfgAddressText = new Text(params, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		cfgAddressText.setLayoutData(gridData);
		cfgAddressText.setText("");
		
				
		Label valueLabel = new Label(params, SWT.NONE);
		valueLabel.setText("Value:");
		
		cfgValueText = new Text(params, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		cfgValueText.setLayoutData(gridData);
		cfgValueText.setText("");
		
		this.cfgActive = new Button(params,SWT.CHECK);
		this.cfgActive.setText("Active");
		gridData = new GridData();
		gridData.horizontalSpan=2;
		cfgActive.setLayoutData(gridData);
		
		cfgBindButton = new Button(params, SWT.PUSH);
		cfgBindButton.setText("Bind");
		cfgBindButton.addSelectionListener(bl);
		gridData = new GridData();
		gridData.horizontalSpan=2;
		cfgBindButton.setLayoutData(gridData);
		
		Button uploadButton = new Button(params, SWT.PUSH);
		uploadButton.setText("Upload");
		uploadButton.setToolTipText("Transfer the configuration parameters to the device.");
		uploadButton.addSelectionListener(bl);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		uploadButton.setLayoutData(gridData);
				
		params = new Composite(shell, SWT.BORDER);
		
		params.setLayout(new GridLayout(3,false));
		gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		params.setLayoutData(gridData);
		
		this.ipParameterList = new List(params,SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL );
		gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 6;
		gridData.widthHint = 200;
		ipParameterList.setLayoutData(gridData);
		ipParameterList.addListener(SWT.Selection, new PListListener());
				
		parmLabel = new Label(params, SWT.NONE);
		parmLabel.setText("Input Parameters");
		
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan =2;
		parmLabel.setLayoutData(gridData);
		
		addressLabel = new Label(params, SWT.NONE);
		addressLabel.setText("Address:");
		
		ipAddressText = new Text(params, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		ipAddressText.setLayoutData(gridData);
		ipAddressText.setText("");
		
		Label plotLabel = new Label(params, SWT.NONE);
		plotLabel.setText("PlotConfig:");
		
		ipPlotText = new Text(params, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		ipPlotText.setLayoutData(gridData);
		ipPlotText.setText("");
		
		ipActive = new Button(params,SWT.CHECK);
		ipActive.setText("Active");
		gridData = new GridData();
		gridData.horizontalSpan=2;
		ipActive.setLayoutData(gridData);
		
		ipBindButton = new Button(params, SWT.PUSH);
		ipBindButton.setText("Bind");
		ipBindButton.addSelectionListener(bl);
		gridData = new GridData();
		gridData.horizontalSpan=2;
		ipBindButton.setLayoutData(gridData);
	}

	private void loadCfgList() {
		this.cfgParameterList.removeAll();
		for(Parameter p:this.parameter_controller.getParameters()) {
			if (p instanceof ConfigParameter) {
				String update = "";
				for (Binding b: p.getStationsBindings()) {
					if (b instanceof StationConfigBinding && b.getStation() == this.selected_station) {
						if (!((StationConfigBinding)b).isTransfered())
							update = " (not transfered)";
					}
				}
				this.cfgParameterList.add(p.getName()+update);
			}
		}
	}
	
	private void loadIpList() {
		this.ipParameterList.removeAll();
		for(Parameter p:this.parameter_controller.getParameters()) {
			if (p instanceof InputParameter)
				this.ipParameterList.add(p.getName());
		}
		
	}

	private void loadStationList() {
		this.stationList.removeAll();
		for (Station s:this.station_controller.getStations()) {
			stationList.add(s.getStationName());
		}
	}
	
	private void stationListSelected() {
		try {
			this.selected_station = this.station_controller.findStation((this.stationList.getSelection()[0]));
			this.nameText.setText(selected_station.getStationName());
			this.ipText.setText(selected_station.getIpAddress());
			this.pollingText.setText(""+selected_station.getPollingInterval());
			
			this.enableText(true);
						
			this.loadCfgList();
			this.loadIpList();
			
			this.cfgParameterList.setEnabled(true);
			this.cfgParameterList.select(0);
			
			this.ipParameterList.setEnabled(true);
			this.ipParameterList.select(0);
			
			this.ipListSelected();
			this.cfgListSelected();
			
			
		} 
		catch (Exception e) {
			this.nameText.setText("");
			this.ipText.setText("");
			this.pollingText.setText("");
			this.selected_station = null;
			this.selectedCfgBinding = null;
			this.selectedIpBinding = null;
			
			this.cfgParameterList.setEnabled(false);
			this.ipParameterList.setEnabled(false);
			
			this.enableCfgFields(false);
			this.enableIpFields(false);
			this.enableText(false);
		}
	}
	
	private void saveClicked() {
		if (this.selected_station != null) {
			String name,ip,polling;
			name = this.nameText.getText();
			ip = this.ipText.getText();
			polling = this.pollingText.getText();
			int tmp_p;
			if (name.equals("") || ip.equals("") || polling.equals("")) {
				showErrorMessage("Input incomplete", "Please fill valid data in all fields");
				return;
			}
			
			try {
				tmp_p = Integer.parseInt(polling);
			} catch (Exception e) {
				showErrorMessage("Invalid number for intervall", "Intervall was not a number");
				return;
			}
			
			if (this.station_controller.findStation(name) != null && this.station_controller.findStation(name)!=this.selected_station) {
				showErrorMessage("Name for station has to be unique", "Another station with the same name exists");
				return;
			}
			try {
				this.selected_station.setStationName(name);
			} catch (Exception ex) {
				showErrorMessage("Invalid name for station", "The name for the station contains invalid characters");
			}
			this.selected_station.setPollingInterval(tmp_p);
			
			this.selected_station.changeIPAddress(ip);
			int tmp_sel = this.stationList.getSelectionIndex();
			this.loadStationList();
			this.stationList.setSelection(tmp_sel);
			this.stationListSelected();
		}
	}
	
	private void showErrorMessage(String title, String msg) {
		MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
		messageBox.setMessage(msg);
		messageBox.setText(title);
		messageBox.open();
	}
	
	private void bindIpClicked() {
		if (this.selectedIpBinding == null)
			return;
		
		int address = getAddress(this.ipAddressText);
		
		
		if (address == -1) {
			this.showErrorMessage("Error during saving address", "Address must be >= 0");
			return;
		}
		
		this.selectedIpBinding.setAddress(address);
		this.selectedIpBinding.setActive(this.ipActive.getSelection());
		if (!this.selectedIpBinding.setPlotConfig(this.ipPlotText.getText())) {
			this.showErrorMessage("Error during saving plot config", "Invalid plot configuration: Valid Syntax: (([0-9]*)([h|d]{1})([0-9]+)(;))+");			
			return;
		}
	}
	
	private int getAddress(Text field) {
		int address = -1;
		try {
			address = Integer.parseInt(field.getText());
		} catch (Exception ex) {}
		return address;
	}
	
	
	
	private void bindCfgClicked() {
		if (this.selectedCfgBinding == null)
			return;
		
		int address = getAddress(this.cfgAddressText);
		int value = 0;
		
		if (address == -1) {
			this.showErrorMessage("Error during saving address", "Address must be >= 0");
			return;
		}
		
		try {
			value = Integer.parseInt(this.cfgValueText.getText());
		} catch (Exception ex) {
			this.showErrorMessage("Error during saving configuration value", "Value is not a number");
			return;
		}
		
		this.selectedCfgBinding.setAddress(address);
		this.selectedCfgBinding.setActive(this.cfgActive.getSelection());
		this.selectedCfgBinding.setValue(value);
		
		int tmp = this.cfgParameterList.getSelectionIndex();
		this.loadCfgList();
		this.cfgParameterList.setSelection(tmp);
	}
	
	private InputParameter getSelectedIpParameter() {
		String sel = this.ipParameterList.getSelection()[0];
		return (InputParameter)this.parameter_controller.findParameter(sel);
	}
	
	private StationInputBinding getSelectedIpBinding() {
		InputParameter param = getSelectedIpParameter();
		StationInputBinding binding = null;
		
		for (Binding b: param.getStationsBindings()) {
			if (b instanceof StationInputBinding && b.getStation() == this.selected_station) {
				binding = (StationInputBinding)b;
				break;
			}
		}
		
		return binding;
	}
	
	private void ipListSelected() {
		
		try {
			StationInputBinding binding = getSelectedIpBinding();
			
			if (binding==null) {
				binding = new StationInputBinding(this.selected_station,getSelectedIpParameter(),-1,"h24;",false);
			}
			
			this.ipAddressText.setText(""+binding.getAddress());
			this.ipPlotText.setText(binding.getPlotConfig());
			this.ipActive.setSelection(binding.isActive());
			
			this.selectedIpBinding = binding;
			this.enableIpFields(true);
		} catch (Exception e) 
		{
			this.selectedIpBinding = null;
			this.enableIpFields(false);
		}
	}
	
	private ConfigParameter getSelectedCfgParameter() {
		String sel = this.cfgParameterList.getSelection()[0];
		int tmp = sel.indexOf("(");
		String name = sel;
		if (tmp > 0)
			name = sel.substring(0, tmp-1);
		return (ConfigParameter)this.parameter_controller.findParameter(name);
	}
	
	private StationConfigBinding getSelectedCfgBinding() {
		ConfigParameter param = getSelectedCfgParameter();
		StationConfigBinding binding = null;
		
		for (Binding b: param.getStationsBindings()) {
			if (b instanceof StationConfigBinding && b.getStation() == this.selected_station) {
				binding = (StationConfigBinding)b;
				break;
			}
		}
		
		return binding;
	}
	
	private void cfgListSelected() {
		try {
			StationConfigBinding binding = getSelectedCfgBinding();
			
			if (binding==null) {
				binding = new StationConfigBinding(this.selected_station,getSelectedCfgParameter(),-1,false,false);
			}
			
			this.cfgAddressText.setText(""+binding.getAddress());
			this.cfgActive.setSelection(binding.isActive());
			this.cfgValueText.setText(""+binding.getValue());	
			
			this.selectedCfgBinding = binding;
			this.enableCfgFields(true);
		} catch (Exception e) 
		{
			this.selectedCfgBinding = null;
			this.enableCfgFields(false);
		}
		
	}
	
	private void uploadClicked() {
		if (this.selected_station != null)
			this.selected_station.uploadParamsConfig();
	}
	
	
	class ListListener implements Listener{
		public void handleEvent(Event e) {
			stationListSelected();
		}
	}
	
	class PListListener implements Listener{
		public void handleEvent(Event e) {
			if (((List)e.widget) == ipParameterList)
				ipListSelected();
			else if(((List)e.widget) == cfgParameterList)
				cfgListSelected();
		}
	}
	
	class ButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if (((Button) event.widget).getText().equals("Save")) {
				saveClicked();
			}
			else if (((Button) event.widget).getText().equals("Upload")) {
				uploadClicked();
			}
			
			if (((Button) event.widget) == cfgBindButton) {
				bindCfgClicked();
			}
			else if (((Button) event.widget) == ipBindButton) {
				bindIpClicked();
			}
			
		}
	}
}
