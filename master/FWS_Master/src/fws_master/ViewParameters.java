package fws_master;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


public class ViewParameters {
	private Shell shell;
	private List list;
	private Text nameText;
	private Combo typeCombo,unitCombo,formatCombo,funcCombo;
	
	private Parameter_Controller controller;
	
	private Parameter selected_parameter;
	private boolean new_parameter;

	
	public ViewParameters(Shell shell,Parameter_Controller controller) {
		this.shell = shell;
		this.controller = controller;
		this.selected_parameter = null;
		this.new_parameter = false;
		
		this.InitView();
		this.select_first();
			
	}
	
	private void select_first() {
		if(list.getItemCount()>0) {
			list.select(0);
			this.list_selected();
		}
	}
	
	private void new_param() {
		this.selected_parameter = new Parameter("Neuer Parameter");
		this.nameText.setText(this.selected_parameter.getName());
		this.new_parameter = true;
		this.typeCombo.setEnabled(true);
		
		this.unitCombo.select(0);
		this.formatCombo.select(0);
		this.funcCombo.select(0);
		
		
		this.list.deselectAll();
	}
	
	private void del_param() {
		if (this.new_parameter) {
			this.selected_parameter = null;
			this.new_parameter = false;
			this.select_first();
			return;
		}
		
		if (this.list.getSelectionCount()==0)
			return;
		
		else if (!this.controller.removeParameter(selected_parameter)) {
			MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			messageBox.setMessage("Dieser Parameter kann nicht gelöscht werden, da er in Verwendung ist.");
			messageBox.open();
		}
		this.loadList();
		
	}
	
	private void save_param() {
		if (this.selected_parameter == null)
			return;
		
		String tmpName = this.nameText.getText();
		Units u = Units.SPEEDMS;
		Output_Formats f = Output_Formats.NK0;
		History_Functions func = History_Functions.MAX;
		int last_sel = -1;
		boolean is_input = false;
		
		if (tmpName.equals("")) {
			MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			messageBox.setMessage("Es muss ein Name eingegeben werden!");
			messageBox.open();
			return;
		}
		
		if (new_parameter && this.controller.findParameter(tmpName)!=null) {
			MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			messageBox.setMessage("Dieser Name ist bereits vorhanden!");
			messageBox.open();
			return;
		}
		
		if (this.typeCombo.getSelectionIndex() != 0) {
			if (unitCombo.getSelectionIndex() == 0 || formatCombo.getSelectionIndex() == 0 || funcCombo.getSelectionIndex() == 0) {
				MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
				messageBox.setMessage("Es müssen überall gültige Werte ausgewählt werden!");
				messageBox.open();
				return;
			}
			
			u = Units.getUnit(unitCombo.getItem(unitCombo.getSelectionIndex()));
			f = Output_Formats.getFormat(formatCombo.getItem(formatCombo.getSelectionIndex()));
			func = History_Functions.getHist(this.funcCombo.getItem(this.funcCombo.getSelectionIndex()));
			is_input = true;
		}
		
		if (!new_parameter) {
			if (this.selected_parameter.inUse()) {
				MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
				messageBox.setMessage("Dieser Parameter kann nicht bearbeitet werden, da er in Verwendung ist.");
				messageBox.open();
				return;
			}
			this.selected_parameter.setName(tmpName);
			if (is_input) {
				Input_Parameter tmp_input = (Input_Parameter)this.selected_parameter;
				tmp_input.setFormat(f);
				tmp_input.setHistory_function(func);
				tmp_input.setUnit(u);
			}
			last_sel = list.getSelectionIndex();
		}
		else {
			Parameter p;
			if (this.typeCombo.getSelectionIndex() == 0) {
				p = new Config_Parameter(tmpName);
			}
			else {
				u = Units.getUnit(unitCombo.getItem(unitCombo.getSelectionIndex()));
				f = Output_Formats.getFormat(formatCombo.getItem(formatCombo.getSelectionIndex()));
				func = History_Functions.getHist(this.funcCombo.getItem(this.funcCombo.getSelectionIndex()));
				
				p = new Input_Parameter(tmpName,u,f,func);
			}
			this.controller.addParameter(p);
			
			last_sel = this.controller.getParameters().size()-1;
		}
		this.typeCombo.setEnabled(false);
		this.loadList();
		this.list.setSelection(last_sel);
		list_selected();
		this.new_parameter = false;
	}
	
	private void list_selected() {
		Parameter sel;
		try {
			sel = this.controller.findParameter((list.getSelection()[0]));
			this.selected_parameter = sel;
			this.nameText.setText(sel.getName());
			this.typeCombo.setEnabled(false);
			
			if (sel instanceof Config_Parameter) {
				this.typeCombo.select(0);
				this.unitCombo.select(0);
				this.formatCombo.select(0);
				this.funcCombo.select(0);
				this.disableInputCombos();
			}
			else {
				Input_Parameter input = (Input_Parameter)sel;
				this.typeCombo.select(1);
				this.enableInputCombos();
				
				this.unitCombo.select(unitCombo.indexOf(Units.getString(input.getUnit())));
				this.formatCombo.select(formatCombo.indexOf(Output_Formats.getString(input.getFormat())));
				this.funcCombo.select(funcCombo.indexOf(input.getHistory_function().toString()));
			}
		}
		catch (Exception e) {
			this.nameText.setText("");
			this.unitCombo.select(0);
			this.formatCombo.select(0);
			this.funcCombo.select(0);
			select_first();
			
		}
		
	}
	
	private void config_selected() {
		this.disableInputCombos();
	}
	
	private void input_selected() {
		this.enableInputCombos();
	}
	
	private void disableInputCombos() {
		this.unitCombo.setEnabled(false);
		this.formatCombo.setEnabled(false);
		this.funcCombo.setEnabled(false);
	}
	
	private void enableInputCombos() {
		this.unitCombo.setEnabled(true);
		this.formatCombo.setEnabled(true);
		this.funcCombo.setEnabled(true);
	}
	
	private void loadList() {
		this.list.removeAll();
		for (int i =0; i < this.controller.getParameters().size(); i++) {
			list.add(this.controller.getParameters().get(i).getName());
		}
	}
	
	private void InitView() {
		GridLayout gridLayout = new GridLayout(3,false);
		ButtonListener bl = new ButtonListener();
		
		shell.setLayout(gridLayout);

		list = new List(this.shell,SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);	
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 6;
		gridData.widthHint = 150;
		list.setLayoutData(gridData);
		list.addListener(SWT.Selection, new ListListener());
		loadList();
		
		Label nameLabel = new Label(shell, SWT.NONE);
		nameLabel.setText("Name:");
		
		nameText = new Text(shell, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		nameText.setLayoutData(gridData);
		nameText.setText("");
		
		
		Label typeLabel = new Label(shell, SWT.NONE);
		typeLabel.setText("Type:");
		
		typeCombo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		
		typeCombo.setLayoutData(gridData);
		typeCombo.add("Configuration Parameter");
		typeCombo.add("Input Parameter");
		
		ComboListener combListener = new ComboListener();
		typeCombo.addSelectionListener(combListener);

		Label unitLabel = new Label(shell, SWT.NONE);
		unitLabel.setText("Einheit:");
		
		unitCombo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		unitCombo.setEnabled(false);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		unitCombo.setLayoutData(gridData);
		
		for (Units unit:Units.values()) {
			unitCombo.add(Units.getString(unit));
		}
		
		Label formatLabel = new Label(shell, SWT.NONE);
		formatLabel.setText("Format:");
		
		formatCombo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		formatCombo.setEnabled(false);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		formatCombo.setLayoutData(gridData);
		for (Output_Formats format:Output_Formats.values()) {
			formatCombo.add(Output_Formats.getString(format));
		}
		
		Label funcLabel = new Label(shell, SWT.NONE);
		funcLabel.setText("Funktion:");
		
		funcCombo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		funcCombo.setEnabled(false);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		funcCombo.setLayoutData(gridData);
		funcCombo.add("");
		for (History_Functions func:History_Functions.values()) {
			funcCombo.add(func.toString());
		}
		
		
		Button saveButton = new Button(shell, SWT.PUSH);
		saveButton.setText("Speichern");
		saveButton.addSelectionListener(bl);
		
		
		Composite filler = new Composite(shell,SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 10;
		filler.setLayoutData(gridData);
	
		
		
		
		Composite comp_buttons = new Composite(shell,SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.widthHint = 100;
		comp_buttons.setLayoutData(gridData);
		
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = false;
		rowLayout.justify = false;
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.marginLeft = 5;
		rowLayout.marginTop = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginBottom = 5;
		rowLayout.spacing = 0;
		comp_buttons.setLayout(rowLayout);
		
		Button newButton = new Button(comp_buttons, SWT.PUSH);
		newButton.setText("+");
		newButton.addSelectionListener(bl);
		
		Button delButton = new Button(comp_buttons, SWT.PUSH);
		delButton.setText("-");
		delButton.addSelectionListener(bl);
		
	}
	
	class ButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if (((Button) event.widget).getText().equals("+")) {
				new_param();
			}
			else if (((Button) event.widget).getText().equals("-")) {
				del_param();
			}
			else if (((Button) event.widget).getText().equals("Speichern")) {
				save_param();
			}
		}
	}
	
	class ComboListener extends SelectionAdapter{
		public void widgetSelected(SelectionEvent event) {
			if(((Combo)event.widget).getSelectionIndex() == 0) {
				config_selected();
			}
			else
				input_selected();
		}
	}
	
	class ListListener implements Listener{
		public void handleEvent(Event e) {
			list_selected();
		}
	}
}
