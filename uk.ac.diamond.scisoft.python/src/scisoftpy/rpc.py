'''
Analysis RPC Package
'''

import os
if os.name == 'java':
    import jython.jyrpc as _rpc #@UnusedImport
    import jython.jywrapper as _wrapper #@UnusedImport
    import jython.jyflatten as _flatten #@UnusedImport
else:
    import python.pyrpc as _rpc #@Reimport
    import python.pywrapper as _wrapper #@Reimport
    import python.pyflatten as _flatten #@Reimport

rpcserver=_rpc.rpcserver
rpcclient=_rpc.rpcclient
typednone=_wrapper.typednone
abstractdatasetdescriptor=_wrapper.abstractdatasetdescriptor
binarywrapper=_wrapper.binaryWrapper
settemplocation=_flatten.settemplocation
