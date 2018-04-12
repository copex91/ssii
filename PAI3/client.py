#!/usr/bin/env python
# -*- coding: utf-8 -*-

import socket
import json
import ssl
import getpass
import hmac
import hashlib


CERTFILE = 'keystore/cert_sinpass.pem'

def client(port):
    try:
        with open('configClient.txt', 'r') as f:
            config = json.load(f)
            alg_hashing = config['algHashing']
            IP = config['IP']
            clave = config['clave']
    except:
        print ("Archivo de configuración no encontrado")
    else:
        # Establecer conexión con el socket del servidor
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        ssl_socket = ssl.wrap_socket(s, cert_reqs=ssl.CERT_REQUIRED, ca_certs=CERTFILE)

        ssl_socket.connect((IP, port))

        # Pedir al usuario por pantalla la transacción que desea realizar
        usuario = raw_input("Introduzca usuario: ")
        password = raw_input("Introduzca contraseña: ")
        secret = raw_input("Introduzca mensaje secreto: ")

        # Generar el mensaje que se enviará al servidor
        mensaje = str(usuario) + "&" + str(password) + "&" + str(secret)

        # Hasheo del mensaje para la verificación de integridad
        hash = hmac.new(str(clave), mensaje, getattr(hashlib, alg_hashing))

        # Envío del mensaje
        ssl_socket.send(mensaje + "&" + hash.hexdigest())

        # Imprimir respuesta del servidor
        print('Respuesta: ' + ssl_socket.recv(1000))
        ssl_socket.close()


if __name__ == '__main__':
    client(8080)
