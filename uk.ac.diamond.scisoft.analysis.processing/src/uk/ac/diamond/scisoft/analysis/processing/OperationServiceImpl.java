package uk.ac.diamond.scisoft.analysis.processing;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Random;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.metadata.OriginMetadataImpl;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.slice.SliceVisitor;
import uk.ac.diamond.scisoft.analysis.slice.Slicer;

/**
 * Do not use this class externally. Instead get the IOperationService
 * from OSGI.
 * 
 * @author fcp94556
 *
 */
public class OperationServiceImpl implements IOperationService {
	
	enum ExecutionType {
		SERIES, PARALLEL, GRAPH;
	}
	
	static {
		System.out.println("Starting operation service");
	}
	private Map<String, IOperation> operations;
	
	public OperationServiceImpl() {
		// Intentionally do nothing
	}

	/**
	 * Uses the Slicer.visitAll(...) method which conversions also use to process
	 * stacks out of the rich dataset passed in.
	 */
	@Override
	public void executeSeries(final IRichDataset dataset,final IMonitor monitor, final IExecutionVisitor visitor, final IOperation... series) throws OperationException {
        execute(dataset, monitor, visitor, series, ExecutionType.SERIES);
	}


	@Override
	public void executeParallelSeries(IRichDataset dataset,final IMonitor monitor, IExecutionVisitor visitor, IOperation... series) throws OperationException {
        execute(dataset, monitor, visitor, series, ExecutionType.PARALLEL);
	}

	private long parallelTimeout;
	
	private void execute(final IRichDataset dataset,final IMonitor monitor, final IExecutionVisitor visitor, final IOperation[] series, ExecutionType type) throws OperationException {
		
		if (type==ExecutionType.GRAPH) {
			throw new OperationException(series[0], "The edges are needed to execute a graph using ptolemy!");
		}
		
		Map<Integer, String> slicing = dataset.getSlicing();
		if (slicing==null) slicing = Collections.emptyMap();
		for (Iterator<Integer> iterator = slicing.keySet().iterator(); iterator.hasNext();) {
			Integer dim = iterator.next();
			if ("".equals(slicing.get(dim))) iterator.remove();
		}
		
		//detemine data axes to populate origin metadata
		final int[] dataDims = Slicer.getDataDimensions(dataset.getData().getShape(), slicing);
			
		try {
			// We check the pipeline ranks are ok
	        final IDataset firstSlice = Slicer.getFirstSlice(dataset.getData(), slicing);
			validate(firstSlice, series);

			// Create the slice visitor
			SliceVisitor sv = new SliceVisitor() {
				
				@Override
				public void visit(IDataset slice, Slice[] slices, int[] shape) throws Exception {
					
					slice.addMetadata(new OriginMetadataImpl(dataset.getData(), slices, dataDims));
					
					OperationData  data = new OperationData(slice, (Serializable[])null);
										
					for (IOperation i : series) {
						OperationData tmp = i.execute(data.getData(), monitor);
						data = visitor.isRequiredToModifyData(i) ? tmp : data;
						visitor.notify(i, data, slices, shape, dataDims); // Optionally send intermediate result
					}
					
					visitor.executed(data, monitor, slices, shape, dataDims); // Send result.
				}
			};
			
			visitor.init();
			
			// Jakes slicing from the conversion tool is now in Slicer.
			if (type==ExecutionType.SERIES) {
				Slicer.visitAll(dataset.getData(), slicing, "Slice", sv);
				
			} else if (type==ExecutionType.PARALLEL) {
				Slicer.visitAllParallel(dataset.getData(), slicing, "Slice", sv, parallelTimeout>0 ? parallelTimeout : 5000);
				
			} else {
				throw new OperationException(series[0], "The edges are needed to execute a graph using ptolemy!");
			}
			
			
		} catch (OperationException o) {
			throw o;
		} catch (Exception e) {
			throw new OperationException(null, e);
		} finally {
			if (visitor != null)
				try {
					visitor.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	/**
	 * Checks that the pipeline passed in has a reasonable rank (for instance)
	 * 
	 * @param firstSlice - may be null, image assumed if it is
	 * @param series
	 */
	public void validate(IDataset firstSlice, IOperation... series) throws OperationException {
		       
		if (firstSlice==null) firstSlice = Random.rand(new int[]{1024, 1024});
        if (series[0].getInputRank()==OperationRank.SAME) {
        	throw new InvalidRankException(series[0], "The input rank may not be "+OperationRank.SAME);
        }
        if (series[0].getInputRank().isDiscrete()) {
	        if (firstSlice.getRank() != series[0].getInputRank().getRank()) {
	        	InvalidRankException e = new InvalidRankException(series[0], "The slicing results in a dataset of rank "+firstSlice.getRank()+" but the input rank of '"+series[0].getDescription()+"' is "+series[0].getInputRank().getRank());
	            throw e;
	        }
        }
        
        if (series.length > 1) {
        	
        	OperationRank output = series[0].getOutputRank();
        	if (output == OperationRank.SAME) output = OperationRank.get(firstSlice.getRank());
        	if (output == OperationRank.ANY)  output = OperationRank.get(firstSlice.getRank());
        	
	        for (int i = 1; i < series.length; i++) {
	        	OperationRank input = series[i].getInputRank();
	        	if (input == OperationRank.ANY)  input = OperationRank.get(firstSlice.getRank());
	        	if (!input.isCompatibleWith(output)) {
	        		throw new InvalidRankException(series[i], "The output of '"+series[i-1].getName()+"' is not compatible with the input of '"+series[i].getName()+"'.");
	        	}
	        	output = series[i].getOutputRank();
	        	if (output == OperationRank.SAME) output = input;
			}
        }
	}

//	protected IDataset getMask(IRichDataset dataset, IDataset currentSlice, Slice[] slices) {
//		
//		ILazyDataset lmask = dataset.getMask();
//		if (lmask==null) return null;
//		
//		ILazyDataset fullData = dataset.getData();
//		if (isCompatible(fullData.getShape(), lmask.getShape())) {
//			return lmask.getSlice(slices);
//		} else if (isCompatible(currentSlice.getShape(), lmask.getShape())) {
//			return lmask.getSlice((Slice)null);
//		}
//		throw new OperationException(null, "The mask is neither the shape of the full data or the shape of the requested slice!");
//	}
	
	protected static boolean isCompatible(final int[] ashape, final int[] bshape) {

		List<Integer> alist = new ArrayList<Integer>();

		for (int a : ashape) {
			if (a > 1) alist.add(a);
		}

		final int imax = alist.size();
		int i = 0;
		for (int b : bshape) {
			if (b == 1)
				continue;
			if (i >= imax || b != alist.get(i++))
				return false;
		}

		return i == imax;
	}

	public long getParallelTimeout() {
		return parallelTimeout;
	}

	public void setParallelTimeout(long parallelTimeout) {
		this.parallelTimeout = parallelTimeout;
	}

	// Reads the declared operations from extension point, if they have not been already.
	private synchronized void checkOperations() throws CoreException {
		if (operations!=null) return;
		
		operations = new HashMap<String, IOperation>(31);
		IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.diamond.scisoft.analysis.api.operation");
		for (IConfigurationElement e : eles) {
			final String     id = e.getAttribute("id");
			final IOperation op = (IOperation)e.createExecutableExtension("class");
			operations.put(id, op);
			
			if (op instanceof AbstractOperation) {
				final String name = e.getAttribute("name");
				((AbstractOperation)op).setName(name);
				
				final String desc = e.getAttribute("description");
				if (desc!=null) ((AbstractOperation)op).setDescription(desc);
			}
		}
	}

	@Override
	public Collection<IOperation> find(String regex) throws Exception {
		checkOperations();
		
		Collection<IOperation> ret = new ArrayList<IOperation>(3);
		for (String id : operations.keySet()) {
			IOperation operation = operations.get(id);
			if (matches(id, operation, regex)) {
				ret.add(operation);
			}
		}
		return ret;
	}
	
	@Override
	public Collection<IOperation> find(OperationRank rank, boolean isInput) throws Exception {
		checkOperations();

		Collection<IOperation> ret = new ArrayList<IOperation>(3);
		for (String id : operations.keySet()) {
			IOperation operation = operations.get(id);
			OperationRank r      = isInput ? operation.getInputRank() : operation.getOutputRank();
			if (rank==r) {
				ret.add(operation);
			}
		}
		return ret;
	}	
	
	@Override
	public IOperation findFirst(String regex) throws Exception {
		checkOperations();
		
		for (String id : operations.keySet()) {
			IOperation operation = operations.get(id);
			if (matches(id, operation, regex)) {
				return operation;
			}
		}
		return null;
	}

     /**
	 * NOTE the regex will be matched as follows on the id of the operation:
	 * 1. if matching on the id
	 * 2. if matching the description in lower case.
	 * 3. if indexOf the regex in the id is >0
	 * 4. if indexOf the regex in the description is >0
	 */
	private boolean matches(String id, IOperation operation, String regex) {
		if (id.matches(regex)) return true;
		final String description = operation.getDescription();
		if (description.matches(regex)) return true;
		if (id.indexOf(regex)>0) return true;
		if (description.indexOf(regex)>0) return true;
		return false;
	}

	@Override
	public Collection<String> getRegisteredOperations() throws Exception {
		checkOperations();
		return operations.keySet();
	}

	@Override
	public IOperation create(String operationId) throws Exception {
		checkOperations();
		IOperation op = operations.get(operationId).getClass().newInstance();
		if (op instanceof AbstractOperation) {
			AbstractOperation aop = (AbstractOperation)op;
			aop.setName(operations.get(operationId).getName());
			aop.setDescription(operations.get(operationId).getDescription());
		}
		return op;
	}

	@Override
	public void createOperations(ClassLoader cl, String pakage) throws Exception {
		
		final List<Class<?>> classes = ClassUtils.getClassesForPackage(cl, pakage);
		for (Class<?> class1 : classes) {
			if (Modifier.isAbstract(class1.getModifiers())) continue;
			if (IOperation.class.isAssignableFrom(class1)) {
				
				IOperation op = (IOperation) class1.newInstance();
				if (operations==null) operations = new HashMap<String, IOperation>(31);
				
				operations.put(op.getId(), op);

			}
		}
	}

	@Override
	public String getName(String id) throws Exception{
		checkOperations();
		return operations.get(id).getName();
	}

	@Override
	public String getDescription(String id) throws Exception {
		checkOperations();
		return operations.get(id).getDescription();
	}
 }
