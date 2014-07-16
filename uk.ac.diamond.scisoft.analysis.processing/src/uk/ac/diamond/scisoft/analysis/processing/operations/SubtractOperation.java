package uk.ac.diamond.scisoft.analysis.processing.operations;

import java.io.Serializable;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.processing.IOperation;
import uk.ac.diamond.scisoft.analysis.processing.IRichDataset;
import uk.ac.diamond.scisoft.analysis.processing.OperationException;
import uk.ac.diamond.scisoft.analysis.processing.RichDataset;

/**
 * Subtracts either one dataset from another or a scalar value from all values of a dataset.
 * @author fcp94556
 *
 */
public class SubtractOperation implements IOperation {

	private IRichDataset[] data;
	private Number         value;

	@Override
	public String getOperationDescription() {
		return "Subtract dataset mathematics";
	}

	@Override
	public void setData(IRichDataset... data) throws IllegalArgumentException {
		if (data.length<1 || data.length>2) throw new IllegalArgumentException("You can only set one or two datasets for "+getClass().getSimpleName());
	    this.data = data;
	}

	/**
	 * TODO This operation is only an example.
	 */
	@Override
	public IRichDataset execute() throws OperationException {
		
		try {
			IDataset result;
			if (value!=null) {
				// TODO FIXME We simply get all data out of the lazy and return it
				final Dataset a = (Dataset)data[0].getData().getSlice((Slice)null);
				result = a.isubtract(value);
			} else {
				final Dataset a = (Dataset)data[0].getData().getSlice((Slice)null);
				final Dataset b = (Dataset)data[1].getData().getSlice((Slice)null);
				result = a.isubtract(b);
			}
			// TODO Need to set up axes and meta correctly.
			return new RichDataset(result, data[0].getAxes(), data[0].getMask(), data[0].getMeta());
			
		} catch (Exception e) {
			throw new OperationException(this, e.getMessage());
		}
	}

	@Override
	public void setParameters(Serializable... parameters) throws IllegalArgumentException {
		if (parameters.length!=1) throw new IllegalArgumentException("You can only set one value to subtract "+getClass().getSimpleName());
		this.value = (Number)parameters[0];
	}

}
