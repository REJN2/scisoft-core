/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.optimize;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapter;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer.Formula;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;

public class ApacheOptimizer extends AbstractOptimizer implements ILeastSquaresOptimizer {
	private static Logger logger = LoggerFactory.getLogger(ApacheOptimizer.class);

	public MultivariateFunction createFunction() {
		// provide the fitting function which wrappers all the normal fitting functionality
		MultivariateFunction f = new MultivariateFunction() {

			@Override
			public double value(double[] parameters) {
				return calculateResidual(parameters);
			}
		};

		return f;
	}

	public MultivariateVectorFunction createGradientFunction() {
		MultivariateVectorFunction f = new MultivariateVectorFunction() {
			
			@Override
			public double[] value(double[] parameters) throws IllegalArgumentException {
				double[] result = new double[n];
				
				for (int i = 0; i < n; i++) {
					result[i] = calculateResidualDerivative(params.get(i), parameters);
				}
				return result;
			}
		};

		return f;
	}

	public MultivariateJacobianFunction createJacobianFunction() {
		final int size = coords[0].getSize();
		final AFunction afn;
		final CoordinatesIterator it;
		final DoubleDataset vd, pvd;
		final double[][] dm = new double[size][n];
		if (function instanceof AFunction) {
			afn = (AFunction) function;
			it = afn.getIterator(coords);
			vd = (DoubleDataset) DatasetFactory.zeros(coords[0].getShapeRef(), Dataset.FLOAT64);
			pvd = vd.clone();
		} else {
			afn = null;
			it = null;
			vd = null;
			pvd = null;
		}

		MultivariateJacobianFunction f = new MultivariateJacobianFunction() {
			@SuppressWarnings("null")
			@Override
			public Pair<RealVector, RealMatrix> value(RealVector point) {
				IMonitor monitor = function.getMonitor();
				if (monitor != null && monitor.isCancelled()) {
					throw new IllegalMonitorStateException("Monitor cancelled");
				}

				if (point instanceof ArrayRealVector) {
					setParameterValues(((ArrayRealVector) point).getDataRef());
				} else {
					setParameterValues(point.toArray());
				}
				final double[] dv ;
				if (afn != null) {
					dv = vd.getData();
					AFunction afn = (AFunction) function;
					afn.fillWithValues(vd, it);
					double[] pd = pvd.getData();
					for (int i = 0; i < n; i++) { // assuming number of parameters is less than number of coordinates
						IParameter p = params.get(i);
						afn.fillWithPartialDerivativeValues(p, pvd, it);
						for (int j = 0; j < size; j++) {
							dm[j][i] = pd[j];
						}
					}
				} else {
					dv = calculateValues().getData();
					for (int i = 0; i < n; i++) {
						IParameter p = params.get(i);
						DoubleDataset dp = (DoubleDataset) DatasetUtils.cast(function.calculatePartialDerivativeValues(p, coords), Dataset.FLOAT64);
						double[] pd = dp.getData();
						for (int j = 0; j < size; j++) {
							dm[j][i] = pd[j];
						}
					}
				}
				return new Pair<RealVector, RealMatrix>(new ArrayRealVector(dv, false), new Array2DRowRealMatrix(dm, false));
			}
		};

		return f;
	}

	public Long seed = null;

	private static final int MAX_ITER = 10000;
	private static final int MAX_EVAL = 1000000;
	private static final double REL_TOL = 1e-7;
	private static final double ABS_TOL = 1e-15;

	public enum Optimizer { SIMPLEX_MD, SIMPLEX_NM, POWELL, CMAES, BOBYQA, CONJUGATE_GRADIENT, GAUSS_NEWTON, LEVENBERG_MARQUARDT }

	private Optimizer optimizer;
	private double[] errors = null;

	public ApacheOptimizer(Optimizer opt) {
		optimizer = opt;
	}

	private MultivariateOptimizer createOptimizer() {
		SimplePointChecker<PointValuePair> checker = new SimplePointChecker<PointValuePair>(REL_TOL, ABS_TOL);
		switch (optimizer) {
		case CONJUGATE_GRADIENT:
			return new NonLinearConjugateGradientOptimizer(Formula.POLAK_RIBIERE, checker);
		case BOBYQA:
			return new BOBYQAOptimizer(n + 2);
		case CMAES:
			return new CMAESOptimizer(MAX_ITER, 0., true, 0, 10, seed == null ? new Well19937c() : new Well19937c(seed),
					false, new SimplePointChecker<PointValuePair>(REL_TOL, ABS_TOL));
		case POWELL:
			return new PowellOptimizer(REL_TOL, ABS_TOL, checker);
		case SIMPLEX_MD:
		case SIMPLEX_NM:
			return new SimplexOptimizer(checker);
		default:
			throw new IllegalStateException("Should not be called");
		}
	}

	private LeastSquaresOptimizer createLeastSquaresOptimizer() {
		switch (optimizer) {
		case GAUSS_NEWTON:
			return new GaussNewtonOptimizer();
		case LEVENBERG_MARQUARDT:
			return new LevenbergMarquardtOptimizer();
		default:
			throw new IllegalStateException("Should not be called");
		}
	}

	private SimpleBounds createBounds() {
		double[] lb = new double[n];
		double[] ub = new double[n];
		for (int i = 0; i < n; i++) {
			IParameter p = params.get(i);
			lb[i] = p.getLowerLimit();
			ub[i] = p.getUpperLimit();
		}

		return new SimpleBounds(lb, ub);
	}

	@Override
	void internalOptimize() throws Exception {
		switch (optimizer) {
		case LEVENBERG_MARQUARDT:
		case GAUSS_NEWTON:
			internalLeastSquaresOptimize();
			break;
		default:
			internalScalarOptimize();
			break;
		}
	}

	private void internalScalarOptimize() {
		MultivariateOptimizer opt = createOptimizer();
		SimpleBounds bd = createBounds();
		double offset = 1e12;
		double[] scale = new double[n];
		for (int i = 0; i < n; i++) {
			scale[i] = offset*0.25;
		}
		MultivariateFunction fn = createFunction();
		if (optimizer == Optimizer.SIMPLEX_MD || optimizer == Optimizer.SIMPLEX_NM) {
			fn = new MultivariateFunctionPenaltyAdapter(fn, bd.getLower(), bd.getUpper(), offset, scale);
		}
		ObjectiveFunction of = new ObjectiveFunction(fn);
		InitialGuess ig = new InitialGuess(getParameterValues());
		MaxEval me = new MaxEval(MAX_EVAL);
		double min = Double.POSITIVE_INFINITY;
		double res = Double.NaN;

		try {
			PointValuePair result;

			switch (optimizer) {
			case CONJUGATE_GRADIENT:
//				af = new MultivariateFunctionPenaltyAdapter(fn, bd.getLower(), bd.getUpper(), offset, scale);
				result = opt.optimize(ig, GoalType.MINIMIZE, of, me,
						new ObjectiveFunctionGradient(createGradientFunction()));
				break;
			case BOBYQA:
				result = opt.optimize(ig, GoalType.MINIMIZE, of, me, bd);
				break;
			case CMAES:
				double[] sigma = new double[n];
				for (int i = 0; i < n; i++) {
					IParameter p = params.get(i);
					double v = p.getValue();
					double r = Math.max(p.getUpperLimit()-v, v - p.getLowerLimit());
					sigma[i] = r*0.05; // 5% of range
				}
				int p = (int) Math.ceil(4 + Math.log(n)) + 1;
				logger.trace("Population size: {}", p);
				result = opt.optimize(ig, GoalType.MINIMIZE, of, me, bd,
						new CMAESOptimizer.Sigma(sigma), new CMAESOptimizer.PopulationSize(p));
				break;
			case SIMPLEX_MD:
				result = opt.optimize(ig, GoalType.MINIMIZE, of, me, new MultiDirectionalSimplex(n));
				break;
			case SIMPLEX_NM:
				result = opt.optimize(ig, GoalType.MINIMIZE, of, me, new NelderMeadSimplex(n));
				break;
			default:
				throw new IllegalStateException("Should not be called");
			}

			// logger.info("Q-space fit: rms = {}, x^2 = {}", opt.getRMS(), opt.getChiSquare());
			double ires = calculateResidual(opt.getStartPoint());
			logger.trace("Residual: {} from {}", result.getValue(), ires);
			res = result.getValue();
			if (res < min)
				setParameterValues(result.getPoint());
			logger.trace("Used {} evals and {} iters", opt.getEvaluations(), opt.getIterations());
//			System.err.printf("Used %d evals and %d iters\n", opt.getEvaluations(), opt.getIterations());
			// logger.info("Q-space fit: rms = {}, x^2 = {}", opt.getRMS(), opt.getChiSquare());
		} catch (IllegalArgumentException e) {
			logger.error("Start point has wrong dimension", e);
			// should not happen!
		} catch (TooManyEvaluationsException e) {
			throw new IllegalArgumentException("Could not fit as optimizer did not converge");
//				logger.error("Convergence problem: max iterations ({}) exceeded", opt.getMaxIterations());
		}
	}

	/**
	 * create a multivariateJacobianFunction from MVF and MMF (using builder?)
	 * 
	 */
	private void internalLeastSquaresOptimize() {
		LeastSquaresOptimizer opt = createLeastSquaresOptimizer();

		try {
			
			LeastSquaresBuilder builder = new LeastSquaresBuilder().model(createJacobianFunction())
					.target(data.getData()).start(getParameterValues()).lazyEvaluation(false)
					.maxEvaluations(MAX_EVAL).maxIterations(MAX_ITER);

			builder.checker(new EvaluationRmsChecker(REL_TOL, ABS_TOL));

			if (weight != null) {
				builder.weight(MatrixUtils.createRealDiagonalMatrix(weight.getData()));
			}

			// TODO add checker, validator
			LeastSquaresProblem problem = builder.build();

			Optimum result = opt.optimize(problem);

			RealVector res = result.getPoint();
			setParameterValues(res instanceof ArrayRealVector ? ((ArrayRealVector) res).getDataRef() : res.toArray());
			try {
				RealVector err = result.getSigma(1e-14);
				
//				sqrt(S / (n - m) * C[i][i]);
				double c = result.getCost();
				int n = data.getSize();
				int m = getParameterValues().length;
				
				double[] s = err instanceof ArrayRealVector ? ((ArrayRealVector) err).getDataRef() : err.toArray();
				
				errors = new double[s.length];
				
				for (int i = 0; i < errors.length; i++) errors[i] = Math.sqrt(((c*c)/((n-m)) * (s[i]*s[i])));
				
				
			} catch (SingularMatrixException e) {
				logger.warn("Could not find errors as covariance matrix was singular");
			}

			logger.trace("Residual: {} from {}", result.getRMS(), Math.sqrt(calculateResidual()));
		} catch (Exception e) {
			logger.error("Problem with least squares optimizer", e);
			throw new IllegalArgumentException("Problem with least squares optimizer");
		}
	}

	@Override
	public double[] guessParametersErrors() {
		return errors;
	}
}
