package uk.ac.diamond.scisoft.analysis.processing.operations.mask;

import java.util.List;

import org.dawb.common.services.IImageFilterService;
import org.dawb.common.services.ServiceManager;

import uk.ac.diamond.scisoft.analysis.dataset.Comparisons;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.metadata.MaskMetadata;
import uk.ac.diamond.scisoft.analysis.metadata.MaskMetadataImpl;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.processing.AbstractOperation;
import uk.ac.diamond.scisoft.analysis.processing.OperationData;
import uk.ac.diamond.scisoft.analysis.processing.OperationException;
import uk.ac.diamond.scisoft.analysis.processing.OperationRank;

public class DilateMaskOperation extends AbstractOperation<DilateMaskModel, OperationData> {

	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.operations.DilateMaskOperation";
	}

	@Override
	public OperationData execute(IDataset slice, IMonitor monitor) throws OperationException {

		IDataset mask = null;
		try {
			List<MaskMetadata> maskMetadata = slice.getMetadata(MaskMetadata.class);
			if (maskMetadata != null && !maskMetadata.isEmpty()) {
				mask = DatasetUtils.convertToDataset(maskMetadata.get(0).getMask());
			}
				 
		} catch (Exception e) {
			throw new OperationException(this, e);
		}
		
		if (mask == null) throw new OperationException(this, "No mask to dilate!");
		
		IImageFilterService service = null;
		
		try {
			service = (IImageFilterService)ServiceManager.getService(IImageFilterService.class);
		} catch (Exception e) {
			throw new OperationException(this, "Could not get image processing service");
		}

		if (service == null) throw new OperationException(this, "Could not get image processing service");
		
		IDataset not = Comparisons.logicalNot(mask);
		
		for (int i = 0; i < ((DilateMaskModel)model).getNumberOfPixelsToDilate();i++) {
			not= service.filterDilate(not, true);
		}
		
		not = Comparisons.logicalNot(not);
		
		MaskMetadata mm = new MaskMetadataImpl(not);
		slice.setMetadata(mm);
		
		return new OperationData(slice);
		
	}
	
	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}

}