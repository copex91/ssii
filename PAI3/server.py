#!/usr/bin/env python
# -*- coding: utf-8 -*-

import socket
import hmac
import hashlib
import json
import ssl


KEYFILE = 'keystore/key_sinpass.pem'
CERTFILE = 'keystore/cert_sinpass.pem'

def server(port):
    try:
        # Leer fichero de configuración
        with open('configServer.txt', 'r') as f:
            config = json.load(f)
            alg_hashing = config['algHashing']
            IP = config['IP']
            clave = config['clave']
            usuarios = config['usuarios']

    except:
        print ("Archivo de configuración no encontrado")
    else:

        # Creación del socket del servidor
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind((IP, port))
        # Permanecer en escucha de nuevas conexiones
        s.listen(1)
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s_ssl = ssl.wrap_socket(s, keyfile=KEYFILE, certfile=CERTFILE, server_side=True)

        while True:
            # Aceptar nueva conexión entrante
            # c, addr = s.accept()

            c, addr = s_ssl.accept()
            # print('Got connection: ', c, addr)
            # try:

            # Recibir transacción que el cliente desea realizar
            mensaje = c.recv(10000)
            [usuario, password, secret, hash] = mensaje.split("&")

            print('Usuario: ' + usuario)
            print('Password: ' + password)
            print('Secret: ' + secret)
            print('Hash: ' + hash)

            # Chequear integridad del mensaje
            mensaje_nuevo = str(usuario) + "&" + str(password) + "&" + str(secret)

            hash_nuevo = hmac.new(str(clave), mensaje_nuevo, getattr(hashlib, alg_hashing)).hexdigest()

            # Enviar respuesta al cliente
            if hash == hash_nuevo:
                try:
                    if usuarios[usuario] == password:
                        c.send("El mensaje secreto ha sido almacenado correctamente")
                    else:
                        c.send("Usuario incorrecto")
                except:
                    c.send("Usuario incorrecto")
            else:
                c.send("Error, inténtelo de nuevo más tarde.")

        s_ssl.close()


if __name__ == '__main__':
    server(8080)
