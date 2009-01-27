package p2psip.ui.pruebas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DogShowRegistrationWindow {
	Text dogName;
	Combo dogBreed;
	Canvas dogPhoto;
	Image dogImage;
	List categories;
	Text ownerName;
	Text ownerPhone;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new DogShowRegistrationWindow().createShell(display);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	public Shell createShell(final Display display) {
		final Shell shell = new Shell(display);
		shell.setText("Dog Show Entry");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		shell.setLayout(gridLayout);
		
		new Label(shell, SWT.NONE).setText("Dog's Name:");
		
		dogName = new Text(shell, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		dogName.setLayoutData(gridData);
		
		new Label(shell, SWT.NONE).setText("Breed:");
		
		dogBreed = new Combo(shell, SWT.NONE);
		dogBreed.setItems(new String[] { "Collie", "Pitbull", "Poodle",
				"Scottie", "Black Lab" });
		dogBreed.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		Label label = new Label(shell, SWT.NONE);
		label.setText("Categories");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		
		new Label(shell, SWT.NONE).setText("Photo:");
		dogPhoto = new Canvas(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = 80;
		gridData.heightHint = 80;
		gridData.verticalSpan = 3;
		dogPhoto.setLayoutData(gridData);
		dogPhoto.addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {
				if (dogImage != null) {
					event.gc.drawImage(dogImage, 0, 0);
				}
			}
		});
		
		categories = new List(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		categories.setItems(new String[] { "Best of Breed", "Prettiest Female",
				"Handsomest Male", "Best Dressed", "Fluffiest Ears",
				"Most Colors", "Best Performer", "Loudest Bark",
				"Best Behaved", "Prettiest Eyes", "Most Hair", "Longest Tail",
				"Cutest Trick" });
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.verticalSpan = 4;
		int listHeight = categories.getItemHeight() * 12;
		Rectangle trim = categories.computeTrim(0, 0, 0, listHeight);
		gridData.heightHint = trim.height;
		categories.setLayoutData(gridData);
		
		Button browse = new Button(shell, SWT.PUSH);
		browse.setText("Browse...");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalIndent = 5;
		browse.setLayoutData(gridData);
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String fileName = new FileDialog(shell).open();
				if (fileName != null) {
					dogImage = new Image(display, fileName);
				}
			}
		});
		
		Button delete = new Button(shell, SWT.PUSH);
		delete.setText("Delete");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		gridData.horizontalIndent = 5;
		delete.setLayoutData(gridData);
		delete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (dogImage != null) {
					dogImage.dispose();
					dogImage = null;
					dogPhoto.redraw();
				}
			}
		});
		
		Group ownerInfo = new Group(shell, SWT.NONE);
		ownerInfo.setText("Owner Info");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		ownerInfo.setLayout(gridLayout);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		ownerInfo.setLayoutData(gridData);
		
		new Label(ownerInfo, SWT.NONE).setText("Name:");
		ownerName = new Text(ownerInfo, SWT.SINGLE | SWT.BORDER);
		ownerName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(ownerInfo, SWT.NONE).setText("Phone:");
		ownerPhone = new Text(ownerInfo, SWT.SINGLE | SWT.BORDER);
		ownerPhone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button enter = new Button(shell, SWT.PUSH);
		enter.setText("Enter");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.horizontalSpan = 3;
		enter.setLayoutData(gridData);
		enter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				System.out.println("\nDog Name: " + dogName.getText());
				System.out.println("Dog Breed: " + dogBreed.getText());
				System.out.println("Owner Name: " + ownerName.getText());
				System.out.println("Owner Phone: " + ownerPhone.getText());
				System.out.println("Categories:");
				String cats[] = categories.getSelection();
				for (int i = 0; i > cats.length; i++) {
					System.out.println("\t" + cats[i]);
				}
			}
		});
		
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				if (dogImage != null) {
					dogImage.dispose();
					dogImage = null;
				}
			}			
		});
		
		shell.pack();
		
		return shell;
	}
}

