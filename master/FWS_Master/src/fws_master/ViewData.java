package fws_master;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * This class creates a View with a list of all available data. It's also possible to show a table of all time/value combinations.
 * It's possible to delete data from the history and export it to a csv file.
 * 
 * The data is loaded from the MeasurementHistoryController. In the list there are two possible entries. The differ in the
 * timebase. One is for the recent history and has the label "hours". The other one is for the long term history and has the 
 * label "hours".
 * @author Johannes Kasberger
 *
 */
public class ViewData {
	private FWSMaster master;
	private Shell shell;
	private List dataList;
	private Table dataTable;
	private static String DATE_FORMAT = "HH:mm:ss:SSS dd.MM.yyyy";
	
	/**
	 * Default Constructor
	 * @param master master where the data is loaded
	 * @param shell shell for the view
	 */
	public ViewData(FWSMaster master, Shell shell) {
		this.master = master;
		this.shell = shell;
		this.buildView();
		this.fillList();
	}

	/**
	 * build the view
	 */
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
	
	/**
	 * add a collection of strings to the list, adds the postfix (hours|days) to the entry in the list
	 * @param tmp list of keys
	 * @param postfix that is added
	 */
	private void addKeys(Vector<String> tmp, String postfix) {
		
		for(String key : tmp) {
			this.dataList.add(key+";"+postfix);
		}
	}
	
	/**
	 * Load all available data from the master into the list
	 */
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
	
	/**
	 * Listener for selection of the list
	 * @author Johannes Kasberger
	 *
	 */
	class ListListener implements Listener{
		/**
		 * Called when list is selected
		 */
		public void handleEvent(Event e) {
			list_selected();
		}
	}

	/**
	 * Load the data of the selected parameter into the table
	 */
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

	/**
	 * returns the timebase (e.g. hours) 
	 * @return the timebase
	 * @throws Exception when syntax of list is wrong
	 */
	private String getCurrentTimeBase() throws Exception {
		return dataList.getSelection()[0].split(";")[1];
	}
	/**
	 * @return the key for the hashmap that is selected in the list
	 */
	private String getCurrentKey() throws Exception {
		return dataList.getSelection()[0].split(";")[0];
	}
	
	/**
	 * Button Listener
	 * @author Johannes Kasberger
	 *
	 */
	class ButtonListener extends SelectionAdapter {
		/**
		 * Determines which button is clicked.
		 */
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
	
	/**
	 * Export the selected values from the table to a csv file
	 */
	private void export() {
		
		FileDialog dialog = new FileDialog (shell, SWT.SAVE);
		String [] filterNames = new String [] {"CSV Files", "All Files (*)"};
		String [] filterExtensions = new String [] {"*.csv;", "*"};
		String filterPath = "/";
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterNames = new String [] {"CSV Files", "All Files (*.*)"};
			filterExtensions = new String [] {"*.csv", "*.*"};
			filterPath = "c:\\";
		}
		dialog.setFilterNames (filterNames);
		dialog.setFilterExtensions (filterExtensions);
		dialog.setFilterPath (filterPath);
		try {
			dialog.setFileName (this.getCurrentKey()+".csv");
		} catch (Exception e) {
			dialog.setFileName("export.csv");
		}
		String fileName = dialog.open();

		FileOutputStream fos;
		OutputStreamWriter out;
		try {
			fos = new FileOutputStream(fileName);
			out = new OutputStreamWriter(fos, "UTF-8"); 
		} catch (Exception e) {
			MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			messageBox.setMessage("Error creating export file "+fileName+" : "+e.getMessage());
			messageBox.open();
			return;
		}
		
		Vector<TableItem> sel = new Vector<TableItem>(this.dataTable.getSelection().length);
		
		sel.addAll(Arrays.asList(this.dataTable.getSelection()));
		Collections.reverse(sel);
		
		
		for (TableItem item : sel) {
			String date = item.getText(0);
			String value = item.getText(1);
			try {
				out.write(date+","+value+"\n");
			} catch (IOException e) {
				continue;
			}
		}
		
		try {
			out.close();
			fos.close();
		} catch (IOException e) {
			MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			messageBox.setMessage("Error writing export file "+fileName+" : "+e.getMessage());
			messageBox.open();
			e.printStackTrace();
		}
	}

	/**
	 * Delete Values from the history
	 * @param all when true the whole history of the selected parameter is deleted
	 */
	private void delete(boolean all) {
		
		MessageBox messageBox = new MessageBox(shell, SWT.YES| SWT.NO | SWT.ICON_QUESTION);
		messageBox.setMessage("Are you shure you want to delete these entries?");
		messageBox.setText("Delete History?");
		
		if (messageBox.open() == SWT.NO)
			return;
		
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
