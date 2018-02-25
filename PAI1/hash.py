import hashlib
import json
from os.path import isdir, islink

# config = {}
# with open("conf.txt") as f:
#     for line in f.readlines():
#        (key, val) = line.split("=")
#        config[key] = val.replace('\n', '')

#Leer archivo de config
with open('conf.txt', 'r') as f:
    config = json.load(f)

documents = config['ficheros']
for d in documents:
    filename = d
    if not isdir(filename) and not islink(filename):
     try:
        f = open(filename)
     except IOError, e:
        print e
     else:
        data = f.read()
        f.close()
        print "** %s **" % filename
        h = getattr(hashlib, config['algHashing'])(data)
        print "%s: %s" % (config['algHashing'], h.hexdigest())
        print ""