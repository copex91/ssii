#!/usr/bin/env python
# -*- coding: utf-8 -*-

import socket
import struct
import hmac
import random
import hashlib
import json
import datetime
from threading import Thread
import schedule
import os
import plotly.offline as py
import plotly.graph_objs as go
from collections import Counter


def _get_block(s, count):
    if count <= 0:
        return ''
    buf = ''
    while len(buf) < count:
        buf2 = s.recv(count - len(buf))
        if not buf2:
            # error or just end of connection?
            if buf:
                raise RuntimeError("underflow")
            else:
                return ''
        buf += buf2
    return buf


def _send_block(s, data):
    while data:
        data = data[s.send(data):]


def get_msg(s):
    count = struct.unpack('>i', _get_block(s, 4))[0]
    return _get_block(s, count)


def send_msg(s, data):
    header = struct.pack('>i', len(data))
    _send_block(s, header)
    _send_block(s, data)


def _get_count(s):
    buf = ''
    while True:
        c = s.recv(1)
        if not c:
            # error or just end of connection/
            if buf:
                raise RuntimeError("underflow")
            else:
                return -1
        if c == '|':
            return int(buf)
        else:
            buf += c


def get_msg(s):
    return _get_block(s, _get_count(s))


def send_msg(s, data):
    _send_block(s, str(len(data)) + '|')
    _send_block(s, data)


def server(port):
    try:
        # Leer fichero de configuración
        with open('configServer.txt', 'r') as f:
            config = json.load(f)
            algHashing = config['algHashing']
            clave = config['clave']

        # Leer fichero de resultados
        results = {}
        try:
            with open('results.txt', 'r') as f:
                results = json.load(f)
                f.close()
        except:
            pass
    except:
        print ("Archivo de configuración no encontrado")
    else:

        # Creación del socket del servidor
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind(('localhost', port))

        while True:
            # Permanecer en escucha de nuevas conexiones
            s.listen(1)

            # Aceptar nueva conexión entrante
            c, addr = s.accept()
            # try:
            # Generar y enviar nonce para la nueva conexión entrante
            nonce = ''.join([str(random.randint(0, 9)) for i in range(16)])
            send_msg(c, nonce)

            # Recibir transacción que el cliente desea realizar
            mensaje = get_msg(c)
            [origen, destino, cantidad, hash] = mensaje.split("&")

            # Chequear integridad del mensaje
            mensaje_nuevo = str(origen) + "&" + str(destino) + "&" + str(cantidad) + "&" + str(nonce)

            hash_nuevo = hmac.new(str(clave), mensaje_nuevo, getattr(hashlib, algHashing)).hexdigest()

            # Enviar respuesta al cliente
            result = 0
            if hash == hash_nuevo:
                send_msg(c, "Se han transferido " + str(cantidad) + " euros desde la cuenta " + str(
                    origen) + " a la cuenta " + str(destino))
                result = 1
            else:
                send_msg(c, "Error, inténtelo de nuevo más tarde.")

            # Escribir los resultados en archivo de results
            try:
                results[datetime.datetime.now().strftime("%Y%m")]
            except:
                # El mes todavía no existía, se crea
                results[datetime.datetime.now().strftime("%Y%m")] = {}
            else:
                # El mes ya existía
                pass

            try:
                results[datetime.datetime.now().strftime("%Y%m")][datetime.datetime.now().strftime("%d")]
            except:
                # El día todavía no existía, se crea
                results[datetime.datetime.now().strftime("%Y%m")][datetime.datetime.now().strftime("%d")] = [result]
            else:
                # El día ya existía, se añade un dato nuevo
                results[datetime.datetime.now().strftime("%Y%m")][datetime.datetime.now().strftime("%d")].append(result)

            # Almacenar resultado
            try:
                # Volcar archivo de resultados
                with open('results.txt', 'w') as f:
                    json.dump(results, f, indent=2)
                    f.flush()
                    f.close()
            except:
                pass
            # except:
            #     pass
            c.close()


def task():
    if datetime.datetime.now().strftime("%d") == "01":
        try:
            # Leer fichero de configuración
            with open('configServer.txt', 'r') as f:
                config = json.load(f)
                threshold = config['threshold']

            # Leer fichero de resultados
            results = {}
            try:
                with open('results.txt', 'r') as f:
                    results = json.load(f)
                    f.close()
            except:
                pass
        except:
            print ("Archivo de configuración no encontrado")
        else:
            # Crear carpeta graphs si no existe, para a
            if not os.path.exists("graphs"):
                os.makedirs("graphs")
            # Localizar mes anterior
            first = datetime.datetime.now().replace(day=1)
            lastMonth = first - datetime.timedelta(days=1)
            x = []
            y = []
            threshold_line = []

            try:
                result = results[lastMonth.strftime("%Y%m")]
                for i in sorted(result):
                    x.append(i)
                    y.append((float(Counter(result[i])[1]) * 100) / len(result[i]))
                    threshold_line.append(threshold)
            except:
                pass
            else:
                trace1 = go.Scatter(x=x, y=y, marker=dict(color='rgb(, 0, 128)', ), name='% de conexiones íntegras')
                trace2 = go.Scatter(x=x, y=threshold_line, marker=dict(color='rgb(128, 0, 0)', ), name='Umbral')

                py.plot({
                    "data": [
                        trace1, trace2
                    ],
                    "layout": go.Layout(title="Proporción diaria de conexiones íntegras para el mes " + lastMonth.strftime("%m/%Y"),
                                        font=dict(family='Courier New, monospace', size=18, color='rgb(0,0,0)'),
                                        yaxis=dict(title='% de conexiones íntegras', range=[0, 105]),
                                        xaxis=dict(title='Día',dtick=1))
                }, filename="graphs/" + lastMonth.strftime("%Y%m") + '.html', image='jpeg', auto_open=False)


def graph_print():
    schedule.every().day.at("10:30").do(task)


if __name__ == '__main__':
    # La impresión de la gráfica se hace en un hilo paralelo al servidor.
    # Para hacer pruebas a la gráfica, comentad las siguientes dos líneas y descomentad task(), además de cambiar el día
    # en el primer IF de la función.
    thread = Thread(target=graph_print)
    thread.start()
    # task()
    server(8080)
