package fws_master;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Date;
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
	private static String DATE_FORMAT = "HH:mm:ss:SSS dd.MM.yyyy";
	
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
		
		Button refreshButton = new Button(buttonsComp, SWT.NONE);
		refreshButton.setText("Refresh");
		refreshButton.addSelectionListener(bl);
		
		Button exportButton = new Button(buttonsComp, SWT.NONE);
		exportButton.setText("Export");
		exportButton.addSelectionListener(bl);
		
		Button deleteButton = new Button(buttonsComp, SWT.NONE);
		deleteButton.setText("Delete Selection");
		deleteButton.addSelectionListener(bl);
		
		Button deleteallButton = new Button(buttonsComp, SWT.NONE);
		deleteallButton.setText("Delete all");
		deleteallButton.addSelectionListener(bl);
		
	}
	
	private void addKeys(Vector<String> tmp, String postfix) {
		
		for(String key : tmp) {
			this.dataList.add(key+";"+postfix);
		}
	}
	
	private void fillList() {
		MeasurementHistoryController hist = master.getHistoryController();
		this.dataList.removeAll();
		Vector<String> tmp = new Vector<String>();
		tmp.addAll(hist.getDataHours().keySet());
		this.addKeys(tmp,"hours");
		
		tmp.removeAllElements();
		tmp.addAll(hist.getDataDays().keySet());
		this.addKeys(tmp,"days");
		
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
		MeasurementHistory data;
		try {
			if (getCurrentTimeBase().equals("hours"))
				data = hist.getDataHours().get(key);
			else
				data = hist.getDataDays().get(key);
			}
		catch (Exception ex) {
			return;
		}
		
		Vector<MeasurementHistoryEntry> newData = new Vector<MeasurementHistoryEntry>(data.getValues().size());
		newData.addAll(data.getValues());
		
		
		Collections.sort(newData);
		Collections.reverse(newData);
		dataTable.removeAll();
		
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
	    
		for(MeasurementHistoryEntry e : newData) {
			TableItem item = new TableItem(dataTable, SWT.NONE);
			item.setText(new String[] {sdf.format(e.getTimestamp()),""+e.getValue()});
		}
	}

	private String getCurrentTimeBase() throws Exception {
		return dataList.getSelection()[0].split(";")[1];
	}
	/**
	 * @return
	 */
	private String getCurrentKey() throws Exception {
		return dataList.getSelection()[0].split(";")[0];
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
		String key,timebase;
		try {
			key = getCurrentKey();
			timebase = getCurrentTimeBase();
		} catch (Exception e) {
			return;
		} 
		MeasurementHistoryController hist = master.getHistoryController();
		MeasurementHistory measurements;
		
		if (timebase.equals("hours"))
			measurements = hist.getDataHours().get(key);
		else
			measurements = hist.getDataDays().get(key);
		
		if (all) {
			measurements.removeAll();
		}
		else {
			for (TableItem item : this.dataTable.getSelection()) {
				String date = item.getText(0);
				String value = item.getText(1);
				
				SimpleDateFormat bla = new SimpleDateFormat(DATE_FORMAT);
				Date tmpdate;
				try {
					tmpdate = bla.parse(date);
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
				
				MeasurementHistoryEntry tmp = new MeasurementHistoryEntry(Double.parseDouble(value),tmpdate);
				measurements.removeEntry(tmp);
			}
		}
		this.list_selected();
		
	}
}
