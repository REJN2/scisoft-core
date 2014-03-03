/*-
 * Copyright 2011 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.roi;


import java.io.Serializable;
import java.util.Arrays;

/**
 * Class for rectangular region of interest
 */
public class RectangularROI extends OrientableROIBase implements IRectangularROI, Serializable {
	protected double[] len; // width and height
	private boolean clippingCompensation; // compensate for clipping

	/**
	 * @param clippingCompensation The clippingCompensation to set.
	 */
	public void setClippingCompensation(boolean clippingCompensation) {
		this.clippingCompensation = clippingCompensation;
	}

	/**
	 * @return Returns the clippingCompensation.
	 */
	public boolean isClippingCompensation() {
		return clippingCompensation;
	}

	/**
	 * Default square of 10 pixels
	 */
	public RectangularROI() {
		this(10, 0.0);
	}

	/**
	 * Square constructor
	 * 
	 * @param width
	 * @param angle
	 */
	public RectangularROI(double width, double angle) {
		this(0, 0, width, width, angle);
	}

	/**
	 * @param width
	 * @param height
	 * @param angle
	 */
	public RectangularROI(double width, double height, double angle) {
		this(0, 0, width, height, angle);
	}

	/**
	 * @param ptx
	 * @param pty
	 * @param width
	 * @param height
	 * @param angle
	 */
	public RectangularROI(double ptx, double pty, double width, double height, double angle) {
		this(ptx, pty, width, height, angle, false);
	}

	/**
	 * @param ptx
	 * @param pty
	 * @param width
	 * @param height
	 * @param angle
	 * @param clip 
	 */
	public RectangularROI(double ptx, double pty, double width, double height, double angle, boolean clip) {
		spt = new double[] { ptx, pty };
		len = new double[] { width, height };
		ang = angle;
		checkAngle();
		clippingCompensation = clip;
	}

	/**
	 * @param len (width, height)
	 */
	public void setLengths(double len[]) {
		this.len[0] = len[0];
		this.len[1] = len[1];
		bounds = null;
	}

	/**
	 * @param major (width)
	 * @param minor (height)
	 */
	public void setLengths(double major, double minor) {
		len[0] = major;
		len[1] = minor;
		bounds = null;
	}

	/**
	 * @param x (width)
	 * @param y (height)
	 */
	public void addToLengths(double x, double y) {
		len[0] += x;
		len[1] += y;
		bounds = null;
	}

	/**
	 * Return point from normalized coordinates within rectangle
	 * 
	 * @param fx
	 * @param fy
	 * @return point
	 */
	public double[] getPoint(double fx, double fy) {
		return new double[] { spt[0] + fx * len[0] * cang - fy * len[1] * sang,
				spt[1] + fx * len[0] * sang + fy * len[1] * cang };
	}

	private double[] getRelativePoint(double fx, double fy) {
		return new double[] { fx * len[0] * cang - fy * len[1] * sang,
				fx * len[0] * sang + fy * len[1] * cang };
	}

	/**
	 * Return point from normalized coordinates within rectangle
	 * 
	 * @param fx
	 * @param fy
	 * @return point
	 */
	public int[] getIntPoint(double fx, double fy) {
		return new int[] { (int) (spt[0] + fx * len[0] * cang - fy * len[1] * sang),
				(int) (spt[1] + fx * len[0] * sang + fy * len[1] * cang) };
	}

	@Override
	public double[] getEndPoint() {
		return getPoint(1., 1.);
	}

	/**
	 * @return mid point
	 */
	public double[] getMidPoint() {
		return getPoint(0.5, 0.5);
	}

	/**
	 * Change rectangle to have specified mid point
	 * @param mpt
	 */
	public void setMidPoint(double[] mpt) {
		spt[0] = mpt[0] - 0.5*len[0]*cang + 0.5*len[1]*sang;
		spt[1] = mpt[1] - 0.5*len[0]*sang - 0.5*len[1]*cang;
		bounds = null;
	}

	/**
	 * @param index (0 for width, 1 for height)
	 * @return length
	 */
	@Override
	public double getLength(int index) {
		return len[index];
	}

	/**
	 * @return reference to the lengths
	 */
	public double[] getLengths() {
		return len;
	}

	/**
	 * @return integer lengths
	 */
	public int[] getIntLengths() {
		return new int[] { (int) len[0], (int) len[1] };
	}

	@Override
	public double getAngle() {
		return ang;
	}

	/**
	 * For Jython
	 * @param angle The angle in degrees to set
	 */
	public void setAngledegrees(double angle) {
		setAngleDegrees(angle);
	}

	@Override
	public RectangularROI copy() {
		RectangularROI c = new RectangularROI(spt[0], spt[1], len[0], len[1], ang, clippingCompensation);
		c.name = name;
		c.plot = plot;
		return c;
	}

	/**
	 * @param pt
	 * @return angle as measured from midpoint to given point
	 */
	public double getAngleRelativeToMidPoint(int[] pt) {
		return getAngleRelativeToPoint(0.5, 0.5, ROIUtils.convertToDoubleArray(pt));
	}

	/**
	 * @param pt
	 * @return angle as measured from midpoint to given point
	 */
	public double getAngleRelativeToMidPoint(double[] pt) {
		return getAngleRelativeToPoint(0.5, 0.5, pt);
	}

	/**
	 * @param fx 
	 * @param fy 
	 * @param pt 
	 * @return angle as measured from normalized coordinates within rectangle to given point
	 */
	public double getAngleRelativeToPoint(double fx, double fy, int[] pt) {
		return getAngleRelativeToPoint(fx, fy, ROIUtils.convertToDoubleArray(pt));
	}

	/**
	 * @param fx 
	 * @param fy 
	 * @param pt 
	 * @return angle as measured from normalized coordinates within rectangle to given point
	 */
	public double getAngleRelativeToPoint(double fx, double fy, double[] pt) {
		double[] fpt = getPoint(fx, fy);
		fpt[0] = pt[0] - fpt[0];
		fpt[1] = pt[1] - fpt[1];
		return Math.atan2(fpt[1], fpt[0]);
	}
	
	/**
	 * Start a rotated rectangle with predefined starting point and given end point and determine new starting point
	 * 
	 * @param pt
	 */
	public void setEndPointKeepLengths(double[] pt) {
		double[] ps = null;

		// work in rotated coords
		ps = transformToRotated(pt[0], pt[1]);

		spt = transformToOriginal(ps[0] - len[0], ps[1] - len[1]);
		bounds = null;
	}

	/**
	 * @param pt
	 */
	public void setEndPoint(int[] pt) {
		setEndPoint(new double[] { pt[0], pt[1] });
	}

	/**
	 * Start a rotated rectangle with predefined starting point and given end point and determine new starting point and
	 * lengths
	 * 
	 * @param pt
	 */
	public void setEndPoint(double[] pt) {
		double[] ps = null;
		double[] pe = null;

		// work in rotated coords
		ps = transformToRotated(spt[0], spt[1]);
		pe = transformToRotated(pt[0], pt[1]);
		// check and correct bounding box
		if (ps[0] > pe[0]) {
			double t = ps[0];
			ps[0] = pe[0];
			pe[0] = t;
		}
		if (ps[1] > pe[1]) {
			double t = ps[1];
			ps[1] = pe[1];
			pe[1] = t;
		}

		len[0] = pe[0] - ps[0];
		len[1] = pe[1] - ps[1];
		spt = transformToOriginal(ps[0], ps[1]);
		bounds = null;
	}

	/**
	 * Start a rotated rectangle with predefined starting point and given end point and determine new starting point and
	 * lengths
	 * 
	 * @param pt
	 * @param moveX
	 * @param moveY
	 */
	public void setEndPoint(int[] pt, boolean moveX, boolean moveY) {
		double[] ps = null;
		double[] pe = null;

		// work in rotated coords
		ps = transformToRotated(spt[0], spt[1]);
		pe = transformToRotated(pt[0], pt[1]);

		if (moveX) {
			len[0] = pe[0] - ps[0];
			if (len[0] < 0) { // don't allow negative lengths
				len[0] = 0;
			}
		}
		if (moveY) {
			len[1] = pe[1] - ps[1];
			if (len[1] < 0) { // don't allow negative lengths
				len[1] = 0;
			}
		}
		bounds = null;
	}

	/**
	 * Start a rotated rectangle with predefined starting point and given end point and determine new starting point and
	 * lengths
	 * 
	 * @param pt
	 * @param moveX
	 * @param moveY
	 */
	public void setEndPoint(double[] pt, boolean moveX, boolean moveY) {
		double[] ps = null;
		double[] pe = null;

		// work in rotated coords
		ps = transformToRotated(spt[0], spt[1]);
		pe = transformToRotated(pt[0], pt[1]);

		if (moveX) {
			len[0] = pe[0] - ps[0];
			if (len[0] < 0) { // don't allow negative lengths
				len[0] = 0;
			}
		}
		if (moveY) {
			len[1] = pe[1] - ps[1];
			if (len[1] < 0) { // don't allow negative lengths
				len[1] = 0;
			}
		}
		bounds = null;
	}

	/**
	 * Add an offset to angle
	 * 
	 * @param angle
	 */
	public void addAngle(double angle) {
		ang += angle;
		bounds = null;
		checkAngle();
	}

	/**
	 * Subtract an offset from starting point
	 * 
	 * @param pt
	 */
	public void subPoint(int[] pt) {
		spt[0] -= pt[0];
		spt[1] -= pt[1];
		bounds = null;
	}

	
	/**
	 * Translate by normalized coordinates (in rotated frame)
	 * @param fx
	 * @param fy
	 */
	public void translate(double fx, double fy) {
		double[] ps = getRelativePoint(fx, fy);

		spt[0] += ps[0];
		spt[1] += ps[1];
		bounds = null;
	}

	/**
	 * Set start point whilst keeping end point
	 * 
	 * @param dpt
	 *            change in start point
	 * @param moveX
	 * @param moveY
	 */
	public void setPointKeepEndPoint(int[] dpt, boolean moveX, boolean moveY) {
		double[] ps = null;
		double[] pe = null;

		// work in rotated coords
		pe = transformToRotated(dpt[0], dpt[1]);

		if (moveX) {
			ps = transformToOriginal(pe[0], 0);
			if (len[0] > pe[0]) { // don't allow negative lengths
				len[0] -= pe[0];
				spt[0] += ps[0];
				spt[1] += ps[1];
			} else {
				spt[0] += len[0];
				len[0] = 0;
			}
		}
		if (moveY) {
			ps = transformToOriginal(0, pe[1]);
			if (len[1] > pe[1]) { // don't allow negative lengths
				len[1] -= pe[1];
				spt[0] += ps[0];
				spt[1] += ps[1];
			} else {
				spt[1] += len[1];
				len[1] = 0;
			}
		}
		bounds = null;
	}

	/**
	 * Set start point whilst keeping end point
	 * 
	 * @param dpt
	 *            change in start point
	 * @param moveX
	 * @param moveY
	 */
	public void setPointKeepEndPoint(double[] dpt, boolean moveX, boolean moveY) {
		double[] ps = null;
		double[] pe = null;

		// work in rotated coords
		pe = transformToRotated(dpt[0], dpt[1]);

		if (moveX) {
			ps = transformToOriginal(pe[0], 0);
			if (len[0] > pe[0]) { // don't allow negative lengths
				len[0] -= pe[0];
				spt[0] += ps[0];
				spt[1] += ps[1];
			} else {
				spt[0] += len[0];
				len[0] = 0;
			}
		}
		if (moveY) {
			ps = transformToOriginal(0, pe[1]);
			if (len[1] > pe[1]) { // don't allow negative lengths
				len[1] -= pe[1];
				spt[0] += ps[0];
				spt[1] += ps[1];
			} else {
				spt[1] += len[1];
				len[1] = 0;
			}
		}
		bounds = null;
	}

	/**
	 * Adjust ROI whilst keeping a diagonal point in place
	 * 
	 * @param cpt
	 * @param ept
	 * @param pt
	 * @param first
	 */
	public void adjustKeepDiagonalPoint(int[] cpt, double[] ept, int[] pt, boolean first) {
		adjustKeepDiagonalPoint(ROIUtils.convertToDoubleArray(cpt), ept, ROIUtils.convertToDoubleArray(pt), first);
	}

	/**
	 * Adjust ROI whilst keeping a diagonal point in place
	 * 
	 * @param cpt
	 * @param ept
	 * @param pt
	 * @param first
	 */
	public void adjustKeepDiagonalPoint(double[] cpt, double[] ept, double[] pt, boolean first) {
		double[] ps = null;
		double[] pe = null;

		// work in rotated coords
		ps = transformToRotated(spt[0], spt[1]);
		pe = transformToRotated(pt[0] - cpt[0] + ept[0], pt[1] - cpt[1] + ept[1]);

		if (first) { // move end x, start y
			len[0] = pe[0] - ps[0];
			if (len[0] < 0)
				len[0] = 0;

			pe = transformToRotated(pt[0] - cpt[0], pt[1] - cpt[1]);
			ps = transformToOriginal(0, pe[1]);
			if (len[1] > pe[1]) { // don't allow negative lengths
				len[1] -= pe[1];
				spt[0] += ps[0];
				spt[1] += ps[1];
			} else {
				spt[1] += len[1];
				len[1] = 0;
			}
		} else { // move end y, start x
			len[1] = pe[1] - ps[1];
			if (len[1] < 0)
				len[1] = 0;

			pe = transformToRotated(pt[0] - cpt[0], pt[1] - cpt[1]);
			ps = transformToOriginal(pe[0], 0);
			if (len[0] > pe[0]) { // don't allow negative lengths
				len[0] -= pe[0];
				spt[0] += ps[0];
				spt[1] += ps[1];
			} else {
				spt[0] += len[0];
				len[0] = 0;
			}
		}
		bounds = null;
	}

	@Override
	public RectangularROI getBounds() {
		if (bounds == null) {
			if (ang == 0) {
				bounds = copy();
			} else {
				bounds = new RectangularROI();
				double ac = Math.abs(cang);
				double as = Math.abs(sang);
				bounds.setPoint(spt.clone());
				bounds.setLengths(ac * len[0] + as * len[1], as * len[0] + ac * len[1]);
				if (sang >= 0) {
					if (cang >= 0) {
						bounds.addPoint(-sang * len[1], 0);
					} else {
						bounds.addPoint(-bounds.getLength(0), cang * len[1]);
					}
				} else {
					if (cang < 0) {
						bounds.addPoint(cang * len[0], -bounds.getLength(1));
					} else {
						bounds.addPoint(0, sang * len[0]);
					}
				}
			}
		}
		return bounds;
	}

	@Override
	public boolean containsPoint(double x, double y) {
		x -= spt[0];
		y -= spt[1];
		if (ang == 0) {
			if (x < 0 || x > len[0])
				return false;
			return y >= 0 && y <= len[1];
		}
		// work in rotated coords
		if (bounds == null) {
			getBounds();
		}
		double[] pr = transformToRotated(x, y);
		if (pr[0] < 0 || pr[0] > len[0])
			return false;
		return pr[1] >= 0 && pr[1] <= len[1];
	}

	@Override
	public boolean isNearOutline(double x, double y, double distance) {
		if (!super.isNearOutline(x, y, distance))
			return false;

		double[] pta = spt;
		double[] ptb = getPoint(1, 0);
		if (ROIUtils.isNearSegment(ptb[0] - pta[0], ptb[1] - pta[1], x - pta[0], y - pta[1], distance))
			return true;
		pta = ptb;
		ptb = getPoint(1, 1);
		if (ROIUtils.isNearSegment(ptb[0] - pta[0], ptb[1] - pta[1], x - pta[0], y - pta[1], distance))
			return true;
		pta = ptb;
		ptb = getPoint(0, 1);
		if (ROIUtils.isNearSegment(ptb[0] - pta[0], ptb[1] - pta[1], x - pta[0], y - pta[1], distance))
			return true;
		pta = ptb;
		ptb = spt;
		return ROIUtils.isNearSegment(ptb[0] - pta[0], ptb[1] - pta[1], x - pta[0], y - pta[1], distance);
	}

	@Override
	public void downsample(double subFactor) {
		spt[0] /= subFactor;
		spt[1] /= subFactor;
		len[0] /= subFactor;
		len[1] /= subFactor;
		bounds = null;
	}

	@Override
	public String toString() {
		return super.toString() + String.format("point=%s, lengths=%s, angle=%g", Arrays.toString(spt), Arrays.toString(len), getAngleDegrees());
	}
}
