import hashlib
from os import listdir
from os.path import isdir, islink

config = {}
with open("Downloads/Telegram Desktop/conf.txt") as f:
    for line in f.readlines():
       (key, val) = line.split("=")
       config[key] = val.replace('\n', '')

documents = config['ficheros'].split(',')
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