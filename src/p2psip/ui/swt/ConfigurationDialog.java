package p2psip.ui.swt;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConfigurationDialog {

	private static ResourceBundle resAddressBook = ResourceBundle.getBundle("p2psip");
	
	private Shell shell;
	private Configurable config;
	private BeanInfo info;
	
	public ConfigurationDialog(Shell parent, Configurable config) throws IntrospectionException {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		shell.setLayout(new GridLayout());
		this.config=config;
		this.info = Introspector.getBeanInfo(Configurable.class);
	}
	
	private void addTextListener(final Text text) {
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e){
				Method setter = (Method)(text.getData("setter"));
				String[] args = new String[1];
				args[0]=text.getText();
				try {
					setter.invoke(config, args);
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}
			}
		});
	}
	private void createControlButtons() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		Button okButton = new Button(composite, SWT.PUSH);
		okButton.setText(resAddressBook.getString("OK"));
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		
		Button cancelButton = new Button(composite, SWT.PUSH);
		cancelButton.setText(resAddressBook.getString("Cancel"));
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		
		shell.setDefaultButton(okButton);
	}
	
	private void createTextWidgets() {
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout= new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		PropertyDescriptor pd[] =info.getPropertyDescriptors();
		Method m;
		for (int i = 0; i < pd.length; i++) {
			Label label = new Label(composite, SWT.RIGHT);
			label.setText(pd[i].getName());	
			Text text = new Text(composite, SWT.BORDER);
			GridData gridData = new GridData();
			gridData.widthHint = 400;
			text.setLayoutData(gridData);
			m=pd[i].getReadMethod();
			try {
				String txt = (String) m.invoke(config, new Object[0]);
				text.setText(txt==null?"":txt);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			text.setData("setter", pd[i].getWriteMethod());
			addTextListener(text);	
		}
	}
	
	public String getTitle() {
		return shell.getText();
	}

	public void open() {
		createTextWidgets();
		createControlButtons();
		shell.pack();
		shell.open();
		Display display = shell.getDisplay();
		while(!shell.isDisposed()){
			if(!display.readAndDispatch())
				display.sleep();
		}
	}
	public void setTitle(String title) {
		shell.setText(title);
	}
}
