package uk.ac.diamond.scisoft.analysis.processing.operations.powder;

import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.NonPixelSplittingIntegration2D;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelSplittingIntegration2D;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.metadata.AxesMetadataImpl;
import uk.ac.diamond.scisoft.analysis.processing.OperationRank;

public class CakePixelIntegrationOperation extends AbstractPixelIntegrationOperation {


	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}

	@Override
	protected void setAxes(IDataset data, ILazyDataset[] axes, int[] dataDims,
			List<Dataset> out) {
		
		AxesMetadataImpl amd = new AxesMetadataImpl(axes.length);

		int count = 0;
		for (int i = 0; i < axes.length; i++) {
			boolean contained = false;
			for (int j : dataDims) {
				if (i == j){
					contained = true;
					amd.setAxis(i, new ILazyDataset[]{out.get(count++)});
					break;
				}
			}
			if (!contained) {
				amd.setAxis(i, new ILazyDataset[] {axes[i]});
			}
		}
		
	}

	@Override
	protected AbstractPixelIntegration createIntegrator(
			PixelIntegrationModel model, IDiffractionMetadata md) {
		
		AbstractPixelIntegration integ = null;
		
		if (model.isPixelSplitting()) {
			integ = new PixelSplittingIntegration2D(md, model.getNumberOfBins(),((CakePixelIntegrationModel)model).getNumberOfBins2ndAxis());
		} else {
			integ = new NonPixelSplittingIntegration2D(md, model.getNumberOfBins(),((CakePixelIntegrationModel)model).getNumberOfBins2ndAxis());
		}
		
		integ.setAxisType(((CakePixelIntegrationModel)model).getAxisType());
		
		if (model.getRadialRange() == null) integ.setRadialRange(null);
		else integ.setRadialRange(model.getRadialRange().clone());
		
		if (model.getAzimuthalRange() == null) integ.setAzimuthalRange(null);
		else integ.setAzimuthalRange(model.getAzimuthalRange().clone());
		
		return integ;
	}

}