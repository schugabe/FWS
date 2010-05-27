package fws_master;

import java.text.SimpleDateFormat;

import java.util.Vector;

import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


public class ViewData {
	private FWSMaster master;
	private Shell shell;
	private List dataList;
	private Table dataTable;
	
	public ViewData(FWSMaster master, Shell shell) {
		this.master = master;
		this.shell = shell;
		this.buildView();
		this.fillList();
	}

	private void buildView() {
		GridLayout gridLayout = new GridLayout(2,false);
		final Composite comp = new Composite(shell, SWT.NONE);

		shell.setLayout(new FillLayout());

		comp.setLayout(gridLayout);
		
		GridData gridData = new GridData();
		
		dataList = new List(comp, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.widthHint = 200;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalSpan = 2;
		dataList.setLayoutData(gridData);
		dataList.addListener(SWT.Selection, new ListListener());
		
		dataTable = new Table(comp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		dataTable.setLinesVisible (true);
		dataTable.setHeaderVisible (true);

		gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		dataTable.setLayoutData(gridData);
		
		final TableColumn column1 = new TableColumn(dataTable, SWT.NONE);
		final TableColumn column2 = new TableColumn(dataTable, SWT.NONE);
		
		column1.setText("Time");
		column2.setText("Value");
		column1.setWidth(180);
		column2.setWidth(270);
		
		Composite buttonsComp = new Composite(comp, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 40;
		buttonsComp.setLayoutData(gridData);
		RowLayout rowLayout = new RowLayout();
		rowLayout.type = SWT.HORIZONTAL;
		
		rowLayout.justify = true;
		rowLayout.pack = true;
		buttonsComp.setLayout(rowLayout);
		
		ButtonListener bl = new ButtonListener();
		
		Button refreshButton = new Button(buttonsComp, SWT.BORDER);
		refreshButton.setText("Refresh");
		refreshButton.addSelectionListener(bl);
		
		Button exportButton = new Button(buttonsComp, SWT.BORDER);
		exportButton.setText("Export");
		exportButton.addSelectionListener(bl);
		
		Button deleteButton = new Button(buttonsComp, SWT.BORDER);
		deleteButton.setText("Delete Selection");
		deleteButton.addSelectionListener(bl);
		
		Button deleteallButton = new Button(buttonsComp, SWT.BORDER);
		deleteallButton.setText("Delete all");
		deleteallButton.addSelectionListener(bl);
		
	}
	
	private void addKeys(Vector<String> tmp) {
		String last = "";
		for(String key : tmp) {
			if (!key.equals(last)) {
				this.dataList.add(key);
				last = key;
			}
		}
	}
	
	private void fillList() {
		MeasurementHistoryController hist = master.getHistoryController();
		this.dataList.removeAll();
		Vector<String> tmp = new Vector<String>();
		tmp.addAll(hist.getDataHours().keySet());
		
		tmp.addAll(hist.getDataDays().keySet());
		
		this.addKeys(tmp);
	}
	
	class ListListener implements Listener{
		public void handleEvent(Event e) {
			list_selected();
		}
	}

	private void list_selected() {
		String key;
		try {
			key = getCurrentKey();
		} catch (Exception e1) {
			return;
		}
		MeasurementHistoryController hist = master.getHistoryController();
		MeasurementHistory data = hist.getDataHours().get(key);
		MeasurementHistory data2 = hist.getDataDays().get(key);
		Vector<MeasurementHistoryEntry> newData = new Vector<MeasurementHistoryEntry>(data.getValues().size()+data2.getValues().size());
		
		newData.addAll(data.getValues());
		newData.addAll(data2.getValues());
		
		dataTable.removeAll();
		String DATE_FORMAT = "HH:mm:ss dd.MM.yyyy";
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
	    
		for(MeasurementHistoryEntry e : newData) {
			TableItem item = new TableItem(dataTable, SWT.NONE);
			item.setText(new String[] {sdf.format(e.getTimestamp()),""+e.getValue()});
		}
	}

	/**
	 * @return
	 */
	private String getCurrentKey() throws Exception {
		return dataList.getSelection()[0];
	}
	
	class ButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if (((Button) event.widget).getText().equals("Refresh")) {
				list_selected();
			}
			else if (((Button) event.widget).getText().equals("Export")) {
				export();
			}
			else if (((Button) event.widget).getText().equals("Delete Selection")) {
				delete(false);
			}
			else if (((Button) event.widget).getText().equals("Delete all")) {
				delete(true);
			}
		}

		
	}
	private void export() {
		// TODO Auto-generated method stub
		
	}

	private void delete(boolean all) {
		String key;
		try {
			key = getCurrentKey();
		} catch (Exception e) {
			return;
		}
		MeasurementHistoryController hist = master.getHistoryController();
		hist.getDataHours().get(key).removeAll();
		hist.getDataDays().get(key).removeAll();
		this.list_selected();
		
	}
}
