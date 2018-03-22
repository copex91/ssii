#!/usr/bin/env python
# -*- coding: utf-8 -*-

import socket
import struct
import hashlib
import hmac
import json

global mensaje 
global mensaje_nonce 
global hash 

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


def client(port):
    try:
        with open('configClient.txt', 'r') as f:
            config = json.load(f)
            algHashing = config['algHashing']
            clave = config['clave']
    except:
        print ("Archivo de configuración no encontrado")
    else:
        # Establecer conexión con el socket del servidor
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('localhost', port))

        # Recibir del servidor el nonce que se usará en esta conexión
        nonce = get_msg(s)

        # Pedir al usuario por pantalla la transacción que desea realizar
        origen = input("Introduza cuenta origen: ")
        destino = input("Introduzca cuenta destino: ")
        cantidad = input("Introduzca cantidad: ")

        # Generar el mensaje que se enviará al servidor
        mensaje = str(origen) + "&" + str(destino) + "&" + str(cantidad)
        mensaje_nonce = mensaje + "&" + nonce

        # Hasheo del mensaje para la verificación de integridad
        hash = hmac.new(str(clave), mensaje_nonce, getattr(hashlib, algHashing))
        # send_msg(s, mensaje + "&" + hash.hexdigest())

        # Imprimir respuesta del servidor
        # print get_msg(s)
        s.shutdown(socket.SHUT_RDWR)
        s.close()

    try:
        with open('configClient.txt', 'r') as f:
            config = json.load(f)
            algHashing = config['algHashing']
            clave = config['clave']
    except:
        print ("Archivo de configuración no encontrado")
    else:
        # Establecer conexión con el socket del servidor
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('localhost', port))

        # Recibir del servidor el nonce que se usará en esta conexión
        nonce = get_msg(s)

        # Pedir al usuario por pantalla la transacción que desea realizar
        # origen = input("Introduza cuenta origen: ")
        # destino = input("Introduzca cuenta destino: ")
        # cantidad = input("Introduzca cantidad: ")

        # Generar el mensaje que se enviará al servidor
        # mensaje = str(origen) + "&" + str(destino) + "&" + str(cantidad)
        # mensaje_nonce = mensaje + "&" + nonce

        # Hasheo del mensaje para la verificación de integridad
        # hash = hmac.new(str(clave), mensaje_nonce, getattr(hashlib, algHashing))

        send_msg(s, mensaje + "&" + hash.hexdigest())

        # Imprimir respuesta del servidor
        print get_msg(s)
        s.shutdown(socket.SHUT_RDWR)
        s.close()

if __name__ == '__main__':
    client(8080)
