package uk.ac.diamond.scisoft.analysis.processing.operations.export;

import java.io.File;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;

import org.eclipse.dawnsci.analysis.api.metadata.OriginMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.AbstractOperation;
import org.eclipse.dawnsci.analysis.api.processing.IExportOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;

import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;

import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class ExportAsText1DOperation extends AbstractOperation<ExportAsText1DModel, OperationData> implements IExportOperation {

	private int counter = 0;
	private String currentFilePath = null;
	private static final String EXPORT = "export";
	private static final String DEFAULT_EXT = "dat";
	
	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.operations.export.ExportAsText1DOperation";
	}
	

	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		
		if (model.getOutputDirectoryPath() == null) throw new OperationException(this, "Output directory not set!");
		
		String filename = EXPORT;
		String slice ="";
		String count = String.valueOf(counter);
		String ext = DEFAULT_EXT;
		
		OriginMetadata om = getOriginMetadata(input);
		
		if (om != null) {
			String fn = om.getFilePath();
			if (fn != null) {
				if (!fn.equals(currentFilePath)) {
					currentFilePath = fn;
					counter = 0;
				}
				File f = new File(fn);
				filename = getFileNameNoExtension(f.getName());
				
				if (model.isIncludeSliceName()) {
					slice = Slice.createString(om.getCurrentSlice());
				}
				
				if (model.getZeroPad() != null) {
					count = String.format("%0" + String.valueOf(model.getZeroPad()) + "d", counter);
				}
				
				if (model.getExtension() != null) ext = model.getExtension();
				
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(model.getOutputDirectoryPath());
		sb.append(File.separator);
		sb.append(filename);
		sb.append("_");
		if (!slice.isEmpty()) {
			sb.append("[");
			slice = slice.replace(":", ";");
			sb.append(slice);
			sb.append("]");
			sb.append("_");
		}
		sb.append(count);
		sb.append(".");
		sb.append(ext);
		
		String fileName = sb.toString();
		counter++;
		
		ILazyDataset[] axes = getFirstAxes(input);
		
		ILazyDataset lx = axes[0];
		
		IDataset outds = input.getSlice().clone();
		
		outds.squeeze().setShape(outds.getShape()[0],1);
		
		if (lx != null) {
			IDataset x = lx.getSliceView().getSlice().squeeze();
			x.setShape(x.getShape()[0],1);
			outds = DatasetUtils.concatenate(new IDataset[]{x,outds}, 1);
		}
		
		ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(fileName);
		
		DataHolder dh = new DataHolder();
		dh.addDataset("Export", outds);
		try {
			saver.saveFile(dh);
		} catch (ScanFileHolderException e) {
			e.printStackTrace();
		}
		
		return new OperationData(input);

	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.ONE;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.ONE;
	}
	
	private String getFileNameNoExtension(String fileName) {
		int posExt = fileName.lastIndexOf(".");
		// No File Extension
		return posExt == -1 ? fileName : fileName.substring(0, posExt);
	}

}