#!/usr/bin/env python
# -*- coding: utf-8 -*-

import hashlib
import json
from os.path import isdir, islink

# config = {}
# with open("conf.txt") as f:
#     for line in f.readlines():
#        (key, val) = line.split("=")
#        config[key] = val.replace('\n', '')

#Leer archivo de config. Si no existe, no debería seguir con el programa
try:
    with open('conf.txt', 'r') as f:
        config = json.load(f)
except:
    print ("Archivo de configuración conf.txt no encontrado")
else:
    # Leer hashes almacenados
    hashes = {}
    try:
        with open('hashes.txt', 'r') as f:
            hashes = json.load(f)
    except:
        pass

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
            h = getattr(hashlib, config['algHashing'])(data).hexdigest()
            print "%s: %s" % (config['algHashing'], h)

            if not filename in hashes:
                hashes[filename] = h
                print "Nuevo hash agregado"
                with open('hashes.txt', 'w') as f:
                    json.dump(hashes, f)
            else:
                if hashes[filename] != h:
                    with open('log.txt', 'w') as f:
                        f.write("Error: Fichero %s comprometido." % filename)
                        f.close()
                    print("Chequeo de hash fallido")
                else:
                    print "Hash correcto"

            print ""