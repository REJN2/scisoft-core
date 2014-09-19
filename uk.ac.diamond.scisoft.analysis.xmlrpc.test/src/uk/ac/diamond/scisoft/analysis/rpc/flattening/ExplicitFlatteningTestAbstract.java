/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rpc.flattening;

import org.junit.Assert;
import org.junit.BeforeClass;

import uk.ac.diamond.scisoft.analysis.rpc.flattening.IFlattener.FlattenedFormChecker;

abstract public class ExplicitFlatteningTestAbstract extends FlatteningTestAbstract {

	private static FlattenedFormChecker flattenedFormChecker;

	@BeforeClass
	public static void createChecker() {
		flattenedFormChecker = new FlattenedFormChecker();
	}

	/**
	 * Make sure the flattened object contains only legal types for XML RPC transmission.
	 * 
	 * @param flat
	 */
	protected void checkFlattenedState(Object flat) {

		Assert.assertTrue("Unexpected object type: " + flat.getClass().toString(),
				flattenedFormChecker.legal(flat));
	}

	protected Object flattenAndCheck(Object toFlatten) {
		Object flat = flattener.flatten(toFlatten);
		checkFlattenedState(flat);
		return flat;
	}

	protected abstract Object doAdditionalWorkOnFlattendForm(Object flat);

	@Override
	protected Object doActualFlattenAndUnflatten(Object inObj) {
		Assert.assertTrue(flattener.canFlatten(inObj));
		Object flat = flattenAndCheck(inObj);

		flat = doAdditionalWorkOnFlattendForm(flat);

		Assert.assertTrue(flattener.canUnFlatten(flat));
		return flattener.unflatten(flat);
	}

}
