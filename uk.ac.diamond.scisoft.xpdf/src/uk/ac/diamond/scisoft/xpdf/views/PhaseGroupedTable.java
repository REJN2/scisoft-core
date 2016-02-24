/*
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.xpdf.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TableColumn;

import uk.ac.diamond.scisoft.xpdf.views.XPDFPhase.LabelledAtom;

/**
 * Display and edit phases for the XPDF project
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
class PhaseGroupedTable {

	private List<XPDFPhase> phases;
	
	private XPDFGroupedTable groupedTable;

	private TreeSet<Integer> usedIDs;
	
	private SampleGroupedTable sampleTable;
	private List<XPDFPhase> visiblePhases;
	
	public PhaseGroupedTable(Composite parent, int style) {
		
		phases = new ArrayList<XPDFPhase>();
		
		visiblePhases = new ArrayList<XPDFPhase>();
		
		groupedTable = new XPDFGroupedTable(parent, SWT.NONE);
		
		List<String> groupNames = new ArrayList<String>();
		List<ColumnInterface> columnInterfaces = new ArrayList<ColumnInterface>();
		List<List<ColumnInterface>> groupedColumnInterfaces = new ArrayList<List<ColumnInterface>>();
		
		groupNames.add("Phase Identification");
		columnInterfaces.add(new NameColumnInterface());
		columnInterfaces.add(new CodeColumnInterface());
		groupedColumnInterfaces.add(columnInterfaces);
		
		groupNames.add("Physical Form");
		columnInterfaces = new ArrayList<ColumnInterface>();
		columnInterfaces.add(new FormColumnInterface());
		columnInterfaces.add(new CrystalColumnInterface());
		groupedColumnInterfaces.add(columnInterfaces);

		groupNames.add("Unit cell");
		columnInterfaces = new ArrayList<ColumnInterface>();
		columnInterfaces.add(new GroupColumnInterface());
		columnInterfaces.add(new UnitCellColumnInterface(0));
		columnInterfaces.add(new UnitCellColumnInterface(1));
		columnInterfaces.add(new UnitCellColumnInterface(2));
		columnInterfaces.add(new InternalAngleColumnInterface(0));
		columnInterfaces.add(new InternalAngleColumnInterface(1));
		columnInterfaces.add(new InternalAngleColumnInterface(2));
		groupedColumnInterfaces.add(columnInterfaces);

		groupNames.add("Properties");
		columnInterfaces = new ArrayList<ColumnInterface>();
		columnInterfaces.add(new CompositionColumnInterface());
		columnInterfaces.add(new DensityColumnInterface());
		groupedColumnInterfaces.add(columnInterfaces);
		
		groupNames.add("‍");
		columnInterfaces = new ArrayList<ColumnInterface>();
		columnInterfaces.add(new CommentColumnInterface());
		groupedColumnInterfaces.add(columnInterfaces);

		for (int iGroup = 0; iGroup < groupNames.size(); iGroup++) {
			groupedTable.createColumnGroup(groupNames.get(iGroup), iGroup == groupNames.size() - 1);
			for (int iColumn = 0; iColumn < groupedColumnInterfaces.get(iGroup).size(); iColumn++) {
				ColumnInterface colI = groupedColumnInterfaces.get(iGroup).get(iColumn);
				TableViewerColumn col = groupedTable.addColumn(groupNames.get(iGroup), SWT.NONE);
				col.getColumn().setText(colI.getName());
				groupedTable.setColumnWidth(col, colI.getWeight());
				col.setLabelProvider(colI.getLabelProvider());
				groupedTable.setColumnEditingSupport(col, colI);
				if (colI.getSelectionAdapter(this, col) != null) col.getColumn().addSelectionListener(colI.getSelectionAdapter(this, col));
			}
		}

		
		PhaseContentProvider contentProvider = new PhaseContentProvider();
		groupedTable.setContentProvider(contentProvider);

		// For now, drag support only. Drop support for phase-defining CIF files will eventually be added
		groupedTable.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[]{LocalSelectionTransfer.getTransfer()}, new LocalDragSupportListener(groupedTable));

		// phases with test data
		List<XPDFPhase> testPhases = new ArrayList<XPDFPhase>();
//		testPhases.add(SampleTestData.createTestPhase("Crown Glass"));
		testPhases.add(SampleTestData.createTestPhase("Flint Glass"));
		testPhases.add(SampleTestData.createTestPhase("microcline"));
		testPhases.add(SampleTestData.createTestPhase("cryolite"));
		testPhases.add(SampleTestData.createTestPhase("ilmenite"));
		addPhases(testPhases);

	}

 	// Generate a new id
	private	int getUniqueID() {
		final int lowestID = 2564;
		if (usedIDs == null)
			usedIDs = new TreeSet<Integer>();
		int theID = (usedIDs.isEmpty()) ? lowestID : usedIDs.last()+1;
		usedIDs.add(theID);
		return theID;
	}

	
	class PhaseContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (visiblePhases == null || visiblePhases.size() == 0)
				return phases.toArray(new XPDFPhase[]{});
			else
				return visiblePhases.toArray(new XPDFPhase[]{});
		}
		
	}
	
	/**
	 * Sets the input of the delegated viewer objects to the List of phases.
	 */
	public void setInput() {
		groupedTable.setInput(phases);
	}
	
	/**
	 * Sets the {@link Layout} data of the underlying Composite.
	 * @param layout
	 */
	public void setLayoutData(Object layout) {
		groupedTable.setLayoutData(layout);
	}

	public SelectionAdapter getColumnSelectionAdapter(final TableColumn tableColumn, final Comparator<XPDFPhase> comparator) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (comparator == null) return;
				// Find the present sorted column, if any
				TableColumn presentSorted = null;
				int oldSortDirection = SWT.NONE;
				presentSorted = groupedTable.getSortColumn();
				oldSortDirection = groupedTable.getSortDirection();

				groupedTable.setSortColumn(null);
				groupedTable.setSortDirection(SWT.NONE);

				int newSortDirection = SWT.DOWN;
				
				// If the same column is sorted as is now selected, then reverse the sorting
				if (presentSorted == tableColumn)
					newSortDirection = (oldSortDirection == SWT.UP) ? SWT.DOWN : SWT.UP;

				// Do the sort
				Collections.sort(phases, comparator);
				if (newSortDirection == SWT.UP)
					Collections.reverse(phases);

				groupedTable.setSortColumn(tableColumn);
				groupedTable.setSortDirection(newSortDirection);

				groupedTable.refresh();
			}
		};
	}

	public void setSampleTable(SampleGroupedTable sampleTable) {
		this.sampleTable = sampleTable;
	}

	/**
	 * Adds new phases.
	 * @param addedPhases
	 * 					Collection of phases to add to the internal list
	 */
	public void addPhases(Collection<XPDFPhase> addedPhases) {
		for (XPDFPhase phase: addedPhases) {
			phase.setId(getUniqueID());
			phases.add(phase);
		}
		groupedTable.refresh();
	}
	
	public List<XPDFPhase> getAll() {
		return phases;
	}
	
	public void removeAll(Collection<XPDFPhase> phasesToGo) {
		phases.removeAll(phasesToGo);
		refresh();
	}
	
	public List<XPDFPhase> getSelectedPhases() {
		List<XPDFPhase> selectedPhases= new ArrayList<XPDFPhase>();
		ISelection selection = groupedTable.getSelection();
		// No items? return, having done nothing.
		if (selection.isEmpty()) return selectedPhases;
		// If it is not an IStructureSelection, then I don't know what to do with it.
		if (!(selection instanceof IStructuredSelection)) return selectedPhases;
		// Get the list of all selected data
		List<?> selectedData = ((IStructuredSelection) selection).toList();
		for (Object datum : selectedData)
			if (datum instanceof XPDFPhase)
				selectedPhases.add((XPDFPhase) datum);
		return selectedPhases;		
	}
	
	public void createContextMenu(MenuManager menuManager) {
		groupedTable.createContextMenu(menuManager);			
	}

	/**
	 * Sets the phases that should be visible.
	 * @param visiblePhases
	 * 						Collection of the phases that should be visible in the table.
	 */
	public void setVisiblePhases(Collection<XPDFPhase> visiblePhases) {
		this.visiblePhases.clear();
		for (XPDFPhase phase : visiblePhases) {
			// Ensure any phases set to be visible exist in the list of all phases
			if (!phases.contains(phase)) {
				phase.setId(getUniqueID());
				phases.add(phase);
			}
			this.visiblePhases.add(phase);
		}
	}
	
	class LocalDragSupportListener extends DragSourceAdapter {
		private XPDFGroupedTable gT;
		public LocalDragSupportListener(XPDFGroupedTable gT) {
			this.gT = gT;
		}
		
		@Override
		public void dragSetData(DragSourceEvent event) {
			LocalSelectionTransfer.getTransfer().setSelection(gT.getSelection());
		}
	}
	
	private interface ColumnInterface extends EditingSupportFactory {
		public SelectionAdapter getSelectionAdapter(final PhaseGroupedTable tab, final TableViewerColumn col);
		public ColumnLabelProvider getLabelProvider();
		public String getName();
		public int getWeight();
		public boolean presentAsUneditable(Object element);
	}
	
	static class DummyColumnInterface implements ColumnInterface {

		@Override
		public EditingSupport get(ColumnViewer v) {
			return new DummyEditingSupport(v);
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return DummySelectionAdapter.get(tab, col);
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return "This space left intentionally blank";
				}
			};
		}

		@Override
		public String getName() {
			return "Column";
		}

		@Override
		public int getWeight() {
			return 10;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			return false;
		}
		
	}
	
	static class DummyEditingSupport extends EditingSupport {
		DummyEditingSupport(ColumnViewer v) {
			super(v);
		}
		@Override
		protected CellEditor getCellEditor(Object element) {
			return null;
		}
		@Override
		protected boolean canEdit(Object element) {
			return false;
		}
		@Override
		protected Object getValue(Object element) {
			return null;
		}
		@Override
		protected void setValue(Object element, Object value) {
		}
	}

	static class DummySelectionAdapter {
		public static SelectionAdapter get(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return tab.getColumnSelectionAdapter(col.getColumn(), new Comparator<XPDFPhase>() {
				@Override
				public int compare(XPDFPhase o1, XPDFPhase o2) {
					return 0;
				}
			});
		}
	}
	
	static class DummyLabelProvider extends ColumnLabelProvider {
		String text;
		public DummyLabelProvider(String text) {
			this.text = text;
		}
		@Override
		public String getText(Object element) {
			return text;
		}
	}
	
	static class NameColumnInterface implements ColumnInterface {

		@Override
		public EditingSupport get(final ColumnViewer v) {
			return new EditingSupport(v) {

				@Override
				protected CellEditor getCellEditor(Object element) {
					return new TextCellEditor(((TableViewer) v).getTable());
				}

				@Override
				protected boolean canEdit(Object element) {
					return true;
				}

				@Override
				protected Object getValue(Object element) {
					XPDFPhase phase = (XPDFPhase) element;
					return (phase != null && phase.getName() != null) ? phase.getName() : "";
				}

				@Override
				protected void setValue(Object element, Object value) {
					((XPDFPhase) element).setName( (value != null) ? (String) value : "");
					v.refresh();
				}
			};
		}

		@Override
		public SelectionAdapter getSelectionAdapter(final PhaseGroupedTable tab,
				final TableViewerColumn col) {
			return tab.getColumnSelectionAdapter(col.getColumn(), new Comparator<XPDFPhase>() {
				@Override
				public int compare(XPDFPhase o1, XPDFPhase o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return ((XPDFPhase) element).getName();
				}
			};
		}

		@Override
		public String getName() {
			return "Name";
		}

		@Override
		public int getWeight() {
			return 20;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			return false;
		}
		
	}
	
	static class CodeColumnInterface implements ColumnInterface {

		@Override
		public EditingSupport get(ColumnViewer v) {
			return new DummyEditingSupport(v);
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return tab.getColumnSelectionAdapter(col.getColumn(), new Comparator<XPDFPhase>() {
				@Override
				public int compare (XPDFPhase o1, XPDFPhase o2) {
					return Integer.compare(o1.getId(), o2.getId());
				}
			});
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return "P"+String.format("%05d", ((XPDFPhase) element).getId());
				}
			};
		}

		@Override
		public String getName() {
			return "Code";
		}

		@Override
		public int getWeight() {
			return 5;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			return false;
		}
	}
	
	static class FormColumnInterface implements ColumnInterface {
		
		@Override
		public EditingSupport get(final ColumnViewer v) {
			return new EditingSupport(v) {
				
				@Override
				protected void setValue(Object element, Object value) {
					XPDFPhase phase = (XPDFPhase) element;
					XPDFPhaseForm oldForm = phase.getForm();
					XPDFPhaseForm newForm = XPDFPhaseForm.get(XPDFPhaseForm.Forms.values()[(int) value]); 
					phase.setForm(newForm);
					if (oldForm != newForm)
						if (newForm == XPDFPhaseForm.get(XPDFPhaseForm.Forms.CRYSTALLINE))
							phase.setCrystalSystem(CrystalSystem.get(0)); // Triclinic
						else
							phase.setCrystalSystem(null);
					v.refresh();
				}
				
				@Override
				protected Object getValue(Object element) {
					return ((XPDFPhase) element).getForm().getOrdinal();
				}
				
				@Override
				protected CellEditor getCellEditor(Object element) {
					return new ComboBoxCellEditor(((TableViewer) v).getTable(), XPDFPhaseForm.getNames());
				}
				
				@Override
				protected boolean canEdit(Object element) {
					return true;
				}
			};
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return DummySelectionAdapter.get(tab, col);
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return ((XPDFPhase) element).getForm().getName();
				}
			};
		}

		@Override
		public String getName() {
			return "Form";
		}

		@Override
		public int getWeight() {
			return 10;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			return false;
		}
		
	}
	
	static class CrystalColumnInterface implements ColumnInterface {

		@Override
		public EditingSupport get(final ColumnViewer v) {
			return new EditingSupport(v) {

				@Override
				protected CellEditor getCellEditor(Object element) {
					return new ComboBoxCellEditor(((TableViewer) v).getTable(), CrystalSystem.getNames());
				}

				@Override
				protected boolean canEdit(Object element) {
					return ((XPDFPhase) element).isCrystalline();
				}

				@Override
				protected Object getValue(Object element) {
					return ((XPDFPhase) element).getCrystalSystem().getOrdinal();
				}

				@Override
				protected void setValue(Object element, Object value) {
					XPDFPhase phase = (XPDFPhase) element;
					CrystalSystem oldSystem = phase.getCrystalSystem(); 
					CrystalSystem newSystem = CrystalSystem.get((int) value);
					
					phase.setCrystalSystem(newSystem);					

					v.refresh();
				}
				
			};
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return DummySelectionAdapter.get(tab, col);
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return (!presentAsUneditable(element)) ? 
							((XPDFPhase) element).getCrystalSystem().getName() :
								 "-";
				}
			};
		}

		@Override
		public String getName() {
			return "Crystal System";
		}

		@Override
		public int getWeight() {
			return 10;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			return !((XPDFPhase) element).isCrystalline();
		}
	}

	static class GroupColumnInterface implements ColumnInterface {

		@Override
		public EditingSupport get(final ColumnViewer v) {
			return new EditingSupport(v){

				@Override
				protected CellEditor getCellEditor(Object element) {
					XPDFPhase phase = (XPDFPhase) element;
					List<String> groupNames = new LinkedList<String>();
					for (XPDFSpaceGroup group : phase.getCrystalSystem().getGroups())
						groupNames.add(group.getName());

					return new ComboBoxCellEditor(((TableViewer) v).getTable(), groupNames.toArray(new String[groupNames.size()]));
				}

				@Override
				protected boolean canEdit(Object element) {
					return ((XPDFPhase) element).isCrystalline();
				}

				@Override
				protected Object getValue(Object element) {
					XPDFPhase phase = (XPDFPhase) element;
					return phase.getSpaceGroup().getNumber() - phase.getCrystalSystem().getGroups().get(0).getNumber();
				}

				@Override
				protected void setValue(Object element, Object value) {
					XPDFPhase phase = (XPDFPhase) element;
					int ordinalInSystem = (int) value;
					int firstInSystem = phase.getCrystalSystem().getGroups().get(0).getNumber();
					phase.setSpaceGroup(firstInSystem + ordinalInSystem);
					v.refresh();
				}
				
			};
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return DummySelectionAdapter.get(tab, col);
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					XPDFPhase phase = (XPDFPhase) element; 
					return (phase.isCrystalline()) ? phase.getSpaceGroup().getName() : "-";
				}
			};
		}

		@Override
		public String getName() {
			return "Space Group";
		}

		@Override
		public int getWeight() {
			return 10;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			return !((XPDFPhase) element).isCrystalline();
		}
		
	}
	
	static class UnitCellColumnInterface implements ColumnInterface {
		static final String[] axisNames = {"a", "b", "c"};
		final int axisIndex;
		
		public UnitCellColumnInterface(int axisIndex) {
			this.axisIndex = axisIndex; 
		}

		@Override
		public EditingSupport get(final ColumnViewer v) {
			return new EditingSupport(v) {

				@Override
				protected CellEditor getCellEditor(Object element) {
					return new TextCellEditor(((TableViewer) v).getTable());
				}

				@Override
				protected boolean canEdit(Object element) {
					return !presentAsUneditable(element);
				}

				@Override
				protected Object getValue(Object element) {
					XPDFPhase phase = (XPDFPhase) element;
					int axisIndexData = phase.getSpaceGroup().getSystem().getAxisIndices()[axisIndex];
					double cellLength = phase.getUnitCellLength(axisIndexData);
					return (cellLength == 0.0) ? axisNames[axisIndexData] : Double.toString(cellLength); 
				}

				@Override
				protected void setValue(Object element, Object value) {
					double newLength;
					try {
						newLength = Double.parseDouble((String) value);
					} catch (NumberFormatException nFE) {
						// Do nothing, get out of here
						return;
					}
					((XPDFPhase) element).setUnitCellLength(axisIndex, newLength);
					v.refresh();
				}
				
			};
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return DummySelectionAdapter.get(tab, col);
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					XPDFPhase phase = (XPDFPhase) element;
					String label;
					if (!phase.isCrystalline())
						label = "-";
					else {
						int axisIndexData = phase.getSpaceGroup().getSystem().getAxisIndices()[axisIndex];
						double cellLength = phase.getUnitCellLength(axisIndexData);

						if (cellLength == 0.0)
							label = axisNames[axisIndexData];
						else
							label = Double.toString(cellLength);
					}
					return label;
				}

				@Override
				public Font getFont(Object element) {
					return (presentAsUneditable((XPDFPhase) element)) ?
							JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT) :
								JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
				}
			};
		}

		@Override
		public String getName() {
			return axisNames[axisIndex]+" (Å)";
		}

		@Override
		public int getWeight() {
			return 5;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			XPDFPhase phase = (XPDFPhase) element;
			return !phase.isCrystalline() || phase.getSpaceGroup().getSystem().getAxisIndices()[axisIndex] != axisIndex;
		}
	}
	
	static class InternalAngleColumnInterface implements ColumnInterface {
		static final String[] angleNames = {"α", "β", "γ"};
		int angleIndex;
		
		public InternalAngleColumnInterface(int angleIndex) {
			this.angleIndex = angleIndex;
		}

		@Override
		public EditingSupport get(final ColumnViewer v) {
			return new EditingSupport(v) {

				@Override
				protected CellEditor getCellEditor(Object element) {
					return new TextCellEditor(((TableViewer) v).getTable());
				}

				@Override
				protected boolean canEdit(Object element) {
					return !presentAsUneditable(element);
				}

				@Override
				protected Object getValue(Object element) {
					XPDFPhase phase = (XPDFPhase) element;
					int rawAngle = phase.getSpaceGroup().getSystem().getFixedAngles()[angleIndex];
					if (rawAngle > 0) {
						return Integer.toString(rawAngle);
					} else {
						int angleAxis = -rawAngle-1;
						double angle = phase.getUnitCellAngle(angleAxis);
						return (angle == 0.0) ? angleNames[angleAxis] : Double.toString(angle); 
					}
						
				}

				@Override
				protected void setValue(Object element, Object value) {
					double newAngle;
					try {
						newAngle = Double.parseDouble((String) value);
					} catch (NumberFormatException nFE) {
						// Do nothing, get out of here
						return;
					}
					((XPDFPhase) element).setUnitCellAngle(angleIndex, newAngle);
					v.refresh();
				}
				
			};
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return DummySelectionAdapter.get(tab, col);
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					XPDFPhase phase = (XPDFPhase) element;
					if (!phase.isCrystalline()) {
						// Non-crystalline phase
						return "-";
					} else {
						int rawAngle = phase.getSpaceGroup().getSystem().getFixedAngles()[angleIndex];
						if (rawAngle > 0) {
							return Integer.toString(rawAngle);
						} else {
							int angleAxis = -rawAngle-1;
							double angle = phase.getUnitCellAngle(angleAxis);
							return (angle == 0.0) ? angleNames[angleAxis] : Double.toString(angle); 
						}
					}
				}

				@Override
				public Font getFont(Object element) {
					return (presentAsUneditable((XPDFPhase) element)) ?
							JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT) :
								JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
				}
			};
		}

		@Override
		public String getName() {
			return angleNames[angleIndex] + " (°)";
		}

		@Override
		public int getWeight() {
			return 5;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			XPDFPhase phase = (XPDFPhase) element;
			return !phase.isCrystalline() || 
					phase.getSpaceGroup().getSystem().getFixedAngles()[angleIndex] > 0 ||
					-phase.getSpaceGroup().getSystem().getFixedAngles()[angleIndex]-1 != angleIndex;
		}
	}
	
	static class CompositionColumnInterface implements ColumnInterface {

		@Override
		public EditingSupport get(final ColumnViewer v) {
			return new EditingSupport(v) {

				@Override
				protected CellEditor getCellEditor(Object element) {
					return new DialogCellEditor(((TableViewer) v).getTable()) {
						
						private UnitCellDialog unitCell;
						private Collection<LabelledAtom> atoms;
						
						@Override
						protected Object openDialogBox(Control cellEditorWindow) {
							unitCell = new UnitCellDialog(cellEditorWindow.getShell());
							unitCell.createDialogArea(((TableViewer) v).getTable());
							unitCell.setAllAtoms(atoms);
							unitCell.open();
							return null;
						}
					    @Override
					    protected Button createButton(Composite parent) {
					           Button button = super.createButton(parent);
					           button.setText("+");
					           return button;
					    }
					    @Override
					    protected Object doGetValue() {
					    	if (unitCell != null) {
					    		Map<String, XPDFAtom> atoms = unitCell.getAllAtoms();
					    		v.refresh();
					    		return atoms;
					    	}
					    	else return null;
					    }
					    @Override
					    protected void doSetValue(Object value) {
					    	if (value instanceof Collection)
					    		atoms = ((Collection<LabelledAtom>) value);
					    }
					};
				}

				@Override
				protected boolean canEdit(Object element) {
					return true;
				}

				@Override
				protected Object getValue(Object element) {
					if (element instanceof XPDFPhase)
						return ((XPDFPhase) element).getLabelledAtoms();
					else 
						return getLabelProvider().getText(element);
				}

				@Override
				protected void setValue(Object element, Object value) {
					if (value instanceof Map<?,?>) {
						Map<?, ?> genericMap = (Map<?,?>) value;
						Map<String, XPDFAtom> atomMap;
						try {
							atomMap = (Map<String, XPDFAtom>) genericMap;
						} catch (ClassCastException cCE) {
							return;
						}
						XPDFPhase phase = (XPDFPhase) element;
						for (Map.Entry<String, XPDFAtom> entry : atomMap.entrySet()) {
							phase.addAtom(entry.getKey(), entry.getValue());
						}
					}
					v.refresh();
					return;
				}
				
			};
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return DummySelectionAdapter.get(tab, col);
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
//			return new DummyLabelProvider("Elements!");
			return new ColumnLabelProvider() {
				
				@Override
				public String getText(Object element) {
					return (element instanceof XPDFPhase) ?
						((XPDFPhase) element).getHallNotation(true) :
						"N/A";
				}				
				
				@Override
				public Font getFont(Object element) {
					return (presentAsUneditable(element)) ?
							JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT) :
								JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
				}
			};
		}

		@Override
		public String getName() {
			return "Composition";
		}

		@Override
		public int getWeight() {
			return 20;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			return false;
		}
		
	}
	
	static class DensityColumnInterface implements ColumnInterface {

		@Override
		public EditingSupport get(ColumnViewer v) {
			return new DummyEditingSupport(v);
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return DummySelectionAdapter.get(tab, col);
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new DummyLabelProvider("Density");
		}

		@Override
		public String getName() {
			return "Density (g cm⁻³)";
		}

		@Override
		public int getWeight() {
			return 10;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			return ((XPDFPhase) element).isCrystalline();
		}
		
	}
	
	static class CommentColumnInterface implements ColumnInterface {

		@Override
		public EditingSupport get(final ColumnViewer v) {
			return new EditingSupport(v) {

				@Override
				protected CellEditor getCellEditor(Object element) {
					return new TextCellEditor(((TableViewer) v).getTable());
				}

				@Override
				protected boolean canEdit(Object element) {
					return true;
				}

				@Override
				protected Object getValue(Object element) {
					return (element != null) ? ((XPDFPhase) element).getComment() : "";
				}

				@Override
				protected void setValue(Object element, Object value) {
					if (!(element instanceof XPDFPhase)) return; 
					XPDFPhase phase = (XPDFPhase) element;
					if (value != null) {
						phase.clearComment();
						phase.addComment((String) value);
					}
					v.refresh();
				}
			};
		}

		@Override
		public SelectionAdapter getSelectionAdapter(PhaseGroupedTable tab,
				TableViewerColumn col) {
			return DummySelectionAdapter.get(tab, col);
		}

		@Override
		public ColumnLabelProvider getLabelProvider() {
			return new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return ((XPDFPhase) element).getComment();
				}
			};
		}

		@Override
		public String getName() {
			return "Comment";
		}

		@Override
		public int getWeight() {
			return 30;
		}

		@Override
		public boolean presentAsUneditable(Object element) {
			return false;
		}
		
	}

	/**
	 * Refreshes the internal table.
	 */
	public void refresh() {
		groupedTable.refresh();
	}
}
