'''
scisoftpy is a NumPy-like wrapper around the Diamond Scisoft Analysis plugin
----------------------------------------------------------------------------

Classes available:
    ndarrayA - boolean dataset
    ndarrayB - byte dataset
    ndarrayS - short dataset
    ndarrayI - int dataset
    ndarrayL - long dataset
    ndarrayF - float dataset
    ndarrayD - double dataset
    ndarrayCB - compound byte dataset
    ndarrayCS - compound short dataset
    ndarrayCI - compound int dataset
    ndarrayCL - compound long dataset
    ndarrayCF - compound float dataset
    ndarrayCD - compound double dataset
    ndarrayC - complex float dataset
    ndarrayZ - complex double dataset
    ndarrayRGB - colour RGB dataset

dtypes available:
    bool
    int8
    int16
    int32 = int
    int64
    float32
    float64 = float
    cint8
    cint16
    cint32
    cint64
    cfloat32
    cfloat64
    complex64
    complex128 = complex

Functions available:
    arange(start, stop=None, step=1, dtype=None):
    array(object, dtype=None):
    asarray(data, dtype=None)
    asanyarray(data, dtype=None)
    ones(shape, dtype=float64):
    zeros(shape, dtype=float64):
    empty = zeros
    eye(N, M=None, k=0, dtype=float64):
    identity(n, dtype=float64):
    diag(v, k=0):
    diagflat(v, k=0):
    take(a, indices, axis=None):
    put(a, indices, values):
    concatenate(a, axis=0):
    vstack(tup):
    hstack(tup):
    dstack(tup):
    split(ary, indices_or_sections, axis=0):
    array_split(ary, indices_or_sections, axis=0):
    vsplit(ary, indices_or_sections):
    hsplit(ary, indices_or_sections):
    dsplit(ary, indices_or_sections):
    sort(a, axis=-1):
    tile(a, reps):
    repeat(a, repeats, axis=-1):
    cast(a, dtype):
    any(a, axis=None):
    all(a, axis=None):
    squeeze(a):
    transpose(a, axes=None):
    swapaxes(a, axis1, axis2):
    argmax(a, axis=None):
    argmin(a, axis=None):
    maximum(a, b):
    minimum(a, b):
    meshgrid(*a):
    indices(dimensions, dtype=int32):
    norm(a, allelements=True):
    compoundarray(a, view=True):

    Check also in maths, comparisons, fft, random, io, plot and signal sub-modules
'''
import sys
if sys.hexversion < 0x02040000:
    raise 'Must use python of at least version 2.4'

import os
if os.name == 'java':
    from jython.jycore import *
    from jython.jycomparisons import *

    import fit    
    import signal   
    import image
else:
    from python.pycore import *
    from python.pymaths import *
    from python.pycomparisons import *
    

from maths import *

import nexus

try:
    import io
except Exception, e:
    print >> sys.stderr, "Could not import input/output routines"
    print >> sys.stderr, e

try:
    import plot
except Exception, e:
    print >> sys.stderr, "Could not import plotting routines"
    print >> sys.stderr, e

import random
import flatten
import rpc

import fft

