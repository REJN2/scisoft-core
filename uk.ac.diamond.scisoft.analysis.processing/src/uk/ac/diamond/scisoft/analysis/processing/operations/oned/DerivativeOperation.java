/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.operations.oned;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.Atomic;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;

@Atomic
public class DerivativeOperation extends AbstractOperation<DerivativeModel, OperationData> {

	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.operations.oned.DerivativeOperation";
	}
	
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		
		if (model.getDerivative() < 1) throw new OperationException(this,"Invalid derivative order!");
		
		ILazyDataset[] firstAxes = getFirstAxes(input);
		IDataset ax;
		if (firstAxes == null || firstAxes[0] == null) {
			ax = DatasetFactory.createRange(input.getShape()[0], Dataset.INT64);
		} else {
			ax = firstAxes[0].getSlice();
		}
		
		Dataset out = DatasetUtils.convertToDataset(input);
		Dataset a = DatasetUtils.convertToDataset(ax);
		
		for (int i = 0; i < model.getDerivative(); i++) {
			out = Maths.derivative(a, out, model.getSmoothing());
		}
		
		AxesMetadataImpl axm = new AxesMetadataImpl(1);
		axm.setAxis(0, a);
		out.setMetadata(axm);
		
		return new OperationData(out);
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.ONE;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.ONE;
	}

}
