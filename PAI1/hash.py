#!/usr/bin/env python
# -*- coding: utf-8 -*-

import hashlib
import json
import os
import datetime
import sched, time
import plotly.offline as py
import plotly.graph_objs as go

try:
    with open('conf.txt', 'r') as f:
        config = json.load(f)
        hoursPeriod = config['periodo']*3600 #Horas tras las que se debe repetir el chequeo. Para pruebas, se deja en segundos
        threshold = config['threshold'] #Umbral
except:
    print ("Archivo de configuración conf.txt no encontrado")
else:
    s = sched.scheduler(time.time, time.sleep)
    def chequear_integridad(sc):
        today = datetime.datetime.now()
        #Leer archivo de config. Si no existe, no debería seguir con el programa
        try:
            with open('conf.txt', 'r') as f:
                config = json.load(f)
        except:
            print ("Archivo de configuración conf.txt no encontrado")
        else:
            #Se comprueba que el algotirmo de Hash es correcto. Si no lo es, no sigue con el programa
            if config['algHashing'] in hashlib.algorithms_available:
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
                    if not os.path.isdir(filename) and not os.path.islink(filename):
                     try:
                        f = open(filename)
                     except IOError, e:
                         print "** Error en el fichero o en el directorio: "+ filename + " **\n"
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
                                logErrores += today.strftime("%Y-%m-%d %H:%M:%S") + " -- Error: Fichero %s comprometido\n" % filename
                                errores+=1
                                analizados+=1
                                print("Chequeo de hash fallido")
                            else:
                                analizados += 1
                                print "Hash correcto"
                        print ""
                #Escribir los resultados en archivo de results
                try:
                    results[today.strftime("%Y%m")]
                except:
                    #El mes todavía no existía, se crea
                    results[today.strftime("%Y%m")] = [[errores, analizados]]
                else:
                    #El mes ya existía, se añade un nuevo par de datos
                    results[today.strftime("%Y%m")].append([errores, analizados])

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

                #Si estamos a día 1, generar el gráfico del mes anterior
                #Para pruebas, se aconseja cambiar el día 01 por el día actual
                if today.strftime("%d") == "01":
                    #Crear carpeta graphs si no existe, para a
                    if not os.path.exists("graphs"):
                        os.makedirs("graphs")
                    #Localizar mes anterior
                    first = today.replace(day=1)
                    lastMonth = first - datetime.timedelta(days=1)
                    cont = 0
                    x = []
                    y = []
                    threshold_line = []
                    try:
                        for i in results[lastMonth.strftime("%Y%m")]:
                            cont+=1
                            x.append(cont)
                            y.append((float(i[0]) * 100)/i[1])
                            threshold_line.append(threshold)
                    except:
                        pass
                    else:
                        trace1 = go.Scatter(x=x, y=y, marker=dict(color='rgb(, 0, 128)', ), name='ratio')
                        trace2 = go.Scatter(x=x, y=threshold_line, marker=dict(color='rgb(128, 0, 0)', ), name='threshold')

                        py.plot({
                            "data": [
                                trace1, trace2
                            ],
                            "layout": go.Layout(title="Dialy integrity ratio for month " + lastMonth.strftime("%m/%Y"), font=dict(family='Courier New, monospace', size=18, color='rgb(0,0,0)'))
                                                  },filename= "graphs/" + lastMonth.strftime("%Y%m") + '.html',image='jpeg',auto_open=False)
            else:
                print "El algoritmo seleccionado no está disponible o es erróneo"
        s.enter(float(hoursPeriod), 1, chequear_integridad, (s,))

    s.enter(0, 1, chequear_integridad, (s,))
    s.run()
