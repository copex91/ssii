#!/usr/bin/env python
# -*- coding: utf-8 -*-

import socket
import json
import ssl


CERTFILE = 'cert_sinpass.pem'


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
        ssl_socket = ssl.wrap_socket(s, cert_reqs=ssl.CERT_REQUIRED, ca_certs=CERTFILE)

        ssl_socket.connect(('127.0.0.1', port))

        # # Recibir del servidor el nonce que se usará en esta conexión
        # nonce = get_msg(s)
        #
        # # Pedir al usuario por pantalla la transacción que desea realizar
        # origen = input("Introduza cuenta origen: ")
        # destino = input("Introduzca cuenta destino: ")
        # cantidad = input("Introduzca cantidad: ")
        #
        # # Generar el mensaje que se enviará al servidor
        # mensaje = str(origen) + "&" + str(destino) + "&" + str(cantidad)
        # mensaje_nonce = mensaje + "&" + nonce
        #
        # # Hasheo del mensaje para la verificación de integridad
        # hash = hmac.new(str(clave), mensaje_nonce, getattr(hashlib, algHashing))
        #
        # # Envío del mensaje
        # send_msg(s, mensaje + "&" + hash.hexdigest())

        # Imprimir respuesta del servidor
        print(ssl_socket.recv(1000))
        ssl_socket.close()


if __name__ == '__main__':
    client(8080)