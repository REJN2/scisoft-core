package uk.ac.diamond.scisoft.analysis.processing.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.InvalidRankException;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.processing.Activator;
import uk.ac.diamond.scisoft.analysis.processing.actor.actors.OperationTransformer;
import uk.ac.diamond.scisoft.analysis.processing.actor.runner.GraphRunner;
import uk.ac.diamond.scisoft.analysis.processing.runner.OperationRunnerImpl;
import uk.ac.diamond.scisoft.analysis.processing.runner.SeriesRunner;

public class OperationDataAxesRankTest {

	private static IOperationService service;
	private int count = 0;

	/**
	 * Manually creates the service so that no extension points have to be read.
	 * 
	 * We do this use annotations
	 * @throws Exception 
	 */
	@BeforeClass
	public static void before() throws Exception {
		service = (IOperationService)Activator.getService(IOperationService.class);
		
		// Just read all these operations.
		service.createOperations(service.getClass().getClassLoader(), "uk.ac.diamond.scisoft.analysis.processing.operations");
		OperationRunnerImpl.setRunner(ExecutionType.SERIES,   new SeriesRunner());
		OperationRunnerImpl.setRunner(ExecutionType.PARALLEL, new SeriesRunner());
		OperationRunnerImpl.setRunner(ExecutionType.GRAPH,    new GraphRunner());
	
		OperationTransformer.setOperationService(service);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test2Dto2D() throws Exception {
		
		ILazyDataset ds = getDataset();
		
		final IOperationContext context = service.createContext();
		context.setData(ds);
//		context.setSlicing("all"); // All 24 images in first dimension.
		context.setDataDimensions(new int[]{1,2});
		

		final IOperation di = new Op2dto2d();

		context.setVisitor(new IExecutionVisitor.Stub() {
			@Override
			public void executed(OperationData result, IMonitor monitor) throws Exception {
				
				final IDataset integrated = result.getData();
				if (integrated.getRank() != 3) {
					throw new Exception("Unexpected data rank");
				}
				
				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
				ILazyDataset[] ax = axes.get(0).getAxes();
				
				assertEquals(ax.length, 3);
				assertArrayEquals(new int[]{1,1,1}, ax[0].getShape());
				assertArrayEquals(new int[]{1,5,1}, ax[1].getShape());
				assertArrayEquals(new int[]{1,1,10}, ax[2].getShape());
				
			}
		});
		context.setSeries(di);
		service.execute(context);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test2Dto1D() throws Exception {
		
		ILazyDataset ds = getDataset();
		
		final IOperationContext context = service.createContext();
		context.setData(ds);
//		context.setSlicing("all"); // All 24 images in first dimension.
		context.setDataDimensions(new int[]{1,2});
		
		final IOperation di = new Op2dto1d();

		context.setVisitor(new IExecutionVisitor.Stub() {
			@Override
			public void executed(OperationData result, IMonitor monitor) throws Exception {
				
				final IDataset integrated = result.getData();
				if (integrated.getRank() != 2) {
					throw new Exception("Unexpected data rank");
				}
				
				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
				ILazyDataset[] ax = axes.get(0).getAxes();
				
				assertEquals(ax.length, 2);
				assertArrayEquals(new int[]{1,1}, ax[0].getShape());
				assertArrayEquals(new int[]{1,10}, ax[1].getShape());
			}
		});
		context.setSeries(di);
		service.execute(context);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test1Dto1D() throws Exception {
		
		ILazyDataset ds = getDataset();
		
		final IOperationContext context = service.createContext();
		context.setData(ds);
//		context.setSlicing("all","all"); // All 24 images in first dimension.
		context.setDataDimensions(new int[]{2});


		final IOperation di = new Op1dto1d();
//		
//		//pixel integration
//		final IOperation azi = new PixelIntegrationOperation();
//		azi.setModel(new PowderIntegrationModel());
//		
//		
		context.setVisitor(new IExecutionVisitor.Stub() {
			@Override
			public void executed(OperationData result, IMonitor monitor) throws Exception {
				
				final IDataset integrated = result.getData();
				if (integrated.getRank() != 3) {
					throw new Exception("Unexpected data rank");
				}
				
				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
				ILazyDataset[] ax = axes.get(0).getAxes();
				
				assertEquals(ax.length, 3);
				assertArrayEquals(new int[]{1,1,1}, ax[0].getShape());
				assertArrayEquals(new int[]{1,1,1}, ax[1].getShape());
				assertArrayEquals(new int[]{1,1,10}, ax[2].getShape());
			}
		});
		context.setSeries(di);
		service.execute(context);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test1DtoAny() throws Exception {
		
		ILazyDataset ds = getDataset();
		
		final IOperationContext context = service.createContext();
		context.setData(ds);
//		context.setSlicing("all","all"); // All 24 images in first dimension.	
		context.setDataDimensions(new int[]{2});


		final IOperation di = new Op1dto1d();
		final IOperation anySame = new OpAnyToSame();

//		
//		//pixel integration
//		final IOperation azi = new PixelIntegrationOperation();
//		azi.setModel(new PowderIntegrationModel());
//		
//		
		context.setVisitor(new IExecutionVisitor.Stub() {
			@Override
			public void executed(OperationData result, IMonitor monitor) throws Exception {
				
				final IDataset integrated = result.getData();
				if (integrated.getRank() != 3) {
					throw new Exception("Unexpected data rank");
				}
				
				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
				ILazyDataset[] ax = axes.get(0).getAxes();
				
				assertEquals(ax.length, 3);
				assertArrayEquals(new int[]{1,1,1}, ax[0].getShape());
				assertArrayEquals(new int[]{1,1,1}, ax[1].getShape());
				assertArrayEquals(new int[]{1,1,10}, ax[2].getShape());
			}
		});
		context.setSeries(di, anySame);
		service.execute(context);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test2DtoAny() throws Exception {
		
		ILazyDataset ds = getDataset();
		
		final IOperationContext context = service.createContext();
		context.setData(ds);
//		context.setSlicing("all"); // All 24 images in first dimension.
		context.setDataDimensions(new int[]{1,2});
		

		final IOperation di = new Op2dto2d();
		final IOperation anySame = new OpAnyToSame();

		context.setVisitor(new IExecutionVisitor.Stub() {
			@Override
			public void executed(OperationData result, IMonitor monitor) throws Exception {
				
				final IDataset integrated = result.getData();
				if (integrated.getRank() != 3) {
					throw new Exception("Unexpected data rank");
				}
				
				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
				ILazyDataset[] ax = axes.get(0).getAxes();
				
				assertEquals(ax.length, 3);
				assertArrayEquals(new int[]{1,1,1}, ax[0].getShape());
				assertArrayEquals(new int[]{1,5,1}, ax[1].getShape());
				assertArrayEquals(new int[]{1,1,10}, ax[2].getShape());
				
			}
		});
		context.setSeries(di, anySame);
		service.execute(context);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test2DtoAnyto2D() throws Exception {
		
		ILazyDataset ds = getDataset();
		
		final IOperationContext context = service.createContext();
		context.setData(ds);
//		context.setSlicing("all"); // All 24 images in first dimension.
		context.setDataDimensions(new int[]{1,2});
		

		final IOperation di = new Op2dto2d();
		final IOperation anySame = new OpAnyToSame();
		final IOperation di2 = new Op2dto2d();

		context.setVisitor(new IExecutionVisitor.Stub() {
			@Override
			public void executed(OperationData result, IMonitor monitor) throws Exception {
				
				final IDataset integrated = result.getData();
				if (integrated.getRank() != 3) {
					throw new Exception("Unexpected data rank");
				}
				
				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
				ILazyDataset[] ax = axes.get(0).getAxes();
				
				assertEquals(ax.length, 3);
				assertArrayEquals(new int[]{1,1,1}, ax[0].getShape());
				assertArrayEquals(new int[]{1,5,1}, ax[1].getShape());
				assertArrayEquals(new int[]{1,1,10}, ax[2].getShape());
				
			}
		});
		context.setSeries(di, anySame, di2);
		service.execute(context);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test2Dto1DtoAny() throws Exception {
		
		ILazyDataset ds = getDataset();
		
		final IOperationContext context = service.createContext();
		context.setData(ds);
//		context.setSlicing("all"); // All 24 images in first dimension.
		context.setDataDimensions(new int[]{1,2});
		

		final IOperation di = new Op2dto2d();
		final IOperation di2 = new Op2dto1d();
		final IOperation anySame = new OpAnyToSame();

		context.setVisitor(new IExecutionVisitor.Stub() {
			@Override
			public void executed(OperationData result, IMonitor monitor) throws Exception {
				
				final IDataset integrated = result.getData();
				if (integrated.getRank() != 2) {
					throw new Exception("Unexpected data rank");
				}
				
				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
				ILazyDataset[] ax = axes.get(0).getAxes();
				
				assertEquals(ax.length, 2);
				assertArrayEquals(new int[]{1,1}, ax[0].getShape());
				assertArrayEquals(new int[]{1,10}, ax[1].getShape());
				
			}
		});
		context.setSeries(di, di2, anySame);
		service.execute(context);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test2Dto1DtoAnyto1D() throws Exception {
		
		ILazyDataset ds = getDataset();
		
		final IOperationContext context = service.createContext();
		context.setData(ds);
//		context.setSlicing("all"); // All 24 images in first dimension.
		context.setDataDimensions(new int[]{1,2});
		

		final IOperation di = new Op2dto2d();
		final IOperation di2 = new Op2dto1d();
		final IOperation anySame = new OpAnyToSame();
		final IOperation di3 = new Op1dto1d();

		context.setVisitor(new IExecutionVisitor.Stub() {
			@Override
			public void executed(OperationData result, IMonitor monitor) throws Exception {
				
				final IDataset integrated = result.getData();
				if (integrated.getRank() != 2) {
					throw new Exception("Unexpected data rank");
				}
				
				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
				ILazyDataset[] ax = axes.get(0).getAxes();
				
				assertEquals(ax.length, 2);
				assertArrayEquals(new int[]{1,1}, ax[0].getShape());
				assertArrayEquals(new int[]{1,10}, ax[1].getShape());
				
			}
		});
		context.setSeries(di, di2, anySame, di3);
		service.execute(context);
	}
	
	/**
	 * This sequence should fail because the Any/Same operation is followed by an operation
	 * with a different numerical rank than the previous numerical one (2 instead of 1).
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void test2Dto1DtoAnyto2D() throws Exception {
		
		ILazyDataset ds = getDataset();
		
		final IOperationContext context = service.createContext();
		context.setData(ds);
//		context.setSlicing("all"); // All 24 images in first dimension.
		context.setDataDimensions(new int[]{1,2});
		

		final IOperation di = new Op2dto2d();
		final IOperation di2 = new Op2dto1d();
		final IOperation anySame = new OpAnyToSame();
		final IOperation di3 = new Op2dto2d();

		context.setVisitor(new IExecutionVisitor.Stub() {
			@Override
			public void executed(OperationData result, IMonitor monitor) throws Exception {
				
				final IDataset integrated = result.getData();
				if (integrated.getRank() != 2) {
					throw new Exception("Unexpected data rank");
				}
				
				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
				ILazyDataset[] ax = axes.get(0).getAxes();
				
				assertEquals(ax.length, 2);
				assertArrayEquals(new int[]{5,1}, ax[0].getShape());
				assertArrayEquals(new int[]{1,10}, ax[1].getShape());
				
			}
		});
		context.setSeries(di, di2, anySame, di3);
		try {
			service.execute(context);
			fail("This test with mismatched ranks should have failed");
		} catch (InvalidRankException e) {
			//this is the expected exception so getting here is a success!
		}
	}
//	@Test
//	public void test1Dto2D() throws Exception {
//		
//		ILazyDataset ds = getDataset();
//		
//		final RichDataset   rand = new RichDataset(ds, null, null, null, null);
//		rand.setSlicing("all","all"); // All 24 images in first dimension.
//		
//
//
//		final IOperation di = new Op1dto2d();
////		
////		//pixel integration
////		final IOperation azi = new PixelIntegrationOperation();
////		azi.setModel(new PowderIntegrationModel());
////		
////		
//		service.executeSeries(rand, new IMonitor.Stub(),new IExecutionVisitor.Stub() {
//			@Override
//			public void executed(OperationData result, IMonitor monitor, Slice[] slices, int[] shape, int[] dataDims) throws Exception {
//				
//				final IDataset integrated = result.getData();
//				if (integrated.getRank() != 3) {
//					throw new Exception("Unexpected data rank");
//				}
//				
//				List<AxesMetadata> axes = integrated.getMetadata(AxesMetadata.class);
//				ILazyDataset[] ax = axes.get(0).getAxes();
//				
//				assertEquals(ax.length, 2);
//				IDataset t = ax[0].getSlice();
//				t.squeeze();
//				assertEquals(t.getShort(), count++);
//				assertEquals(integrated.getSize(), ax[1].getSlice().getSize());
//			}
//		}, di);
//	}
	
	private ILazyDataset getDataset() {
		int[] dsShape = new int[]{24, 1000, 1000};
		
		ILazyDataset lz = Random.lazyRand("test", dsShape);

		final IDataset axDataset1 = DatasetFactory.createRange(24,Dataset.INT16);
		axDataset1.setShape(new int[] {24,1,1});

		final IDataset axDataset2 = DatasetFactory.createRange(1000,Dataset.INT32);
		axDataset2.setShape(new int[] {1,1000,1});

		final IDataset axDataset3 = DatasetFactory.createRange(1000,Dataset.INT32);
		axDataset3.setShape(new int[] {1,1,1000});

		AxesMetadataImpl am = new AxesMetadataImpl(3);
		am.addAxis(0, axDataset1);
		am.addAxis(1, axDataset2);
		am.addAxis(2, axDataset3);

		lz.addMetadata(am);

		return lz;
	}
	
	private class Op2dto2d extends AbstractOperation<IOperationModel, OperationData> {

		
		protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
			
			Dataset ones = DatasetFactory.ones(new int[] {5, 10}, Dataset.INT16);
			Dataset ax1 = DatasetFactory.createRange(5, Dataset.INT16);
			ax1.setShape(new int[]{5,1});
			Dataset ax2 = DatasetFactory.createRange(10, Dataset.INT16);
			ax2.setShape(new int[]{1,10});
			AxesMetadataImpl md = new AxesMetadataImpl(2);
			md.addAxis(0, ax1);
			md.addAxis(1, ax2);
			ones.setMetadata(md);
			
			return new OperationData(ones);
		}
		
		@Override
		public String getId() {
			return "junk";
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
	
	private class Op1dto2d extends AbstractOperation<IOperationModel, OperationData> {

		
		protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
			
			Dataset ones = getNew2dDataset();
			
			return new OperationData(ones);
		}

		
		@Override
		public String getId() {
			return "junk";
		}

		@Override
		public OperationRank getInputRank() {
			return OperationRank.ONE;
		}

		@Override
		public OperationRank getOutputRank() {
			return OperationRank.TWO;
		}
		
	}
	
	private class Op2dto1d extends AbstractOperation<IOperationModel, OperationData> {

		
		protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
			
			Dataset ones = getNew1dDataset();
			
			return new OperationData(ones);
		}

		
		@Override
		public String getId() {
			return "junk";
		}

		@Override
		public OperationRank getInputRank() {
			return OperationRank.TWO;
		}

		@Override
		public OperationRank getOutputRank() {
			return OperationRank.ONE;
		}
		
	}
	
	private class Op1dto1d extends AbstractOperation<IOperationModel, OperationData> {

		
		protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
			
			Dataset ones = DatasetFactory.ones(new int[] {10}, Dataset.INT16);
			Dataset ax1 = DatasetFactory.createRange(10, Dataset.INT16);
			ax1.setShape(new int[]{10});
			AxesMetadataImpl md = new AxesMetadataImpl(1);
			md.addAxis(0, ax1);
			ones.setMetadata(md);
			
			return new OperationData(ones);
		}
		
		@Override
		public String getId() {
			return "junk";
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
	
	private class OpAnyToSame extends AbstractOperation<IOperationModel, OperationData> {
	
	
		protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
	
			if (input.getRank() == 2) {
				return new OperationData(getNew2dDataset());
			}
			else if (input.getRank() == 1) {
				return new OperationData(getNew1dDataset());
			}
			throw new OperationException(this, "rank is invalid for this test - must be 1 or 2");
		}
	
		@Override
		public String getId() {
			return "junk";
		}
	
		@Override
		public OperationRank getInputRank() {
			return OperationRank.ANY;
		}
	
		@Override
		public OperationRank getOutputRank() {
			return OperationRank.SAME;
		}
	
	}
	
	public static Dataset getNew2dDataset() {
		Dataset ones = DatasetFactory.ones(new int[] {5, 10}, Dataset.INT16);
		Dataset ax1 = DatasetFactory.createRange(5, Dataset.INT16);
		ax1.setShape(new int[]{5,1});
		Dataset ax2 = DatasetFactory.createRange(10, Dataset.INT16);
		ax2.setShape(new int[]{1,10});
		AxesMetadataImpl md = new AxesMetadataImpl(2);
		md.addAxis(0, ax1);
		md.addAxis(1, ax2);
		ones.setMetadata(md);
		return ones;
	}
	

	public static Dataset getNew1dDataset() {
		Dataset ones = DatasetFactory.ones(new int[] {10}, Dataset.INT16);
		Dataset ax1 = DatasetFactory.createRange(10, Dataset.INT16);
		ax1.setShape(new int[]{10});
		AxesMetadataImpl md = new AxesMetadataImpl(1);
		md.addAxis(0, ax1);
		ones.setMetadata(md);
		return ones;
	}
}