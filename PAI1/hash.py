#!/usr/bin/env python
# -*- coding: utf-8 -*-

import hashlib
import json
from os.path import isdir, islink
from datetime import datetime
import sched, time

try:
    with open('conf.txt', 'r') as f:
        config = json.load(f)
        hoursPeriod = config['periodo'] #Horas tras las que se debe repetir el chequeo
except:
    print ("Archivo de configuración conf.txt no encontrado")
else:
    s = sched.scheduler(time.time, time.sleep)
    def chequear_integridad(sc):
        #Leer archivo de config. Si no existe, no debería seguir con el programa
        try:
            with open('conf.txt', 'r') as f:
                config = json.load(f)
        except:
            print ("Archivo de configuración conf.txt no encontrado")
        else:
            #Variables acumuladoras de errores y total de archivos analizados
            errores = 0
            analizados = 0
            logErrores = ""
            # Leer hashes almacenados
            results = {}
            results['hashes'] = {}
            try:
                with open('results.txt', 'r') as f:
                    results = json.load(f)
                    f.close()
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

                    if not filename in results['hashes']:
                        results['hashes'][filename] = h
                        analizados+=1
                        print "Nuevo hash agregado"
                    else:
                        if results['hashes'][filename] != h:
                            #Almacenar los errores que se vayan produciendo
                            logErrores += datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " -- Error: Fichero %s comprometido\n" % filename
                            errores+=1
                            analizados+=1
                            print("Chequeo de hash fallido")
                        else:
                            analizados += 1
                            print "Hash correcto"
                    print ""
            #Escribir los resultados en archivo de results
            try:
                results[datetime.now().strftime("%Y%m")]
            except:
                #El mes todavía no existía, se crea
                results[datetime.now().strftime("%Y%m")] = [[errores, analizados]]
            else:
                #El mes ya existía, se añade un nuevo par de datos
                results[datetime.now().strftime("%Y%m")].append([errores, analizados])

            #Almacenar archivo de resultados y log
            try:
                #Volcar archivo de resultados
                with open('results.txt', 'w') as f:
                    json.dump(results, f, indent=2)
                    f.flush()
                    f.close()
                #Volcar archivo de errores
                with open(config['incidencias'] + '.txt', 'a') as f:
                    f.write(logErrores)
                    f.close()
            except:
                pass
        s.enter(float(hoursPeriod), 1, chequear_integridad, (s,))
    s.enter(float(hoursPeriod), 1, chequear_integridad, (s,))
    s.run()