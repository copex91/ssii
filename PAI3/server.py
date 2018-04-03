#!/usr/bin/env python
# -*- coding: utf-8 -*-

import socket
import random
import json
import ssl


KEYFILE = 'key_sinpass.pem'
CERTFILE = 'cert_sinpass.pem'


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
        s.bind(('127.0.0.1', port))
        # Permanecer en escucha de nuevas conexiones
        s.listen(1)
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s_ssl = ssl.wrap_socket(s, keyfile=KEYFILE, certfile=CERTFILE, server_side=True)

        while True:
            # Aceptar nueva conexión entrante
            # c, addr = s.accept()

            c, addr = s_ssl.accept()
            print('Got connection: ', c, addr)
            # try:
            # Generar y enviar nonce para la nueva conexión entrante
            # nonce = ''.join([str(random.randint(0, 9)) for i in range(16)])
            c.send(b'This is a response.')

            # # Recibir transacción que el cliente desea realizar
            # mensaje = get_msg(c)
            # [origen, destino, cantidad, hash] = mensaje.split("&")
            #
            # # Chequear integridad del mensaje
            # mensaje_nuevo = str(origen) + "&" + str(destino) + "&" + str(cantidad) + "&" + str(nonce)
            #
            # hash_nuevo = hmac.new(str(clave), mensaje_nuevo, getattr(hashlib, algHashing)).hexdigest()
            #
            # # Enviar respuesta al cliente
            # result = 0
            # if hash == hash_nuevo:
            #     send_msg(c, "Se han transferido " + str(cantidad) + " euros desde la cuenta " + str(
            #         origen) + " a la cuenta " + str(destino))
            #     result = 1
            # else:
            #     send_msg(c, "Error, inténtelo de nuevo más tarde.")
            #
            s_ssl.close()


if __name__ == '__main__':
    server(8080)
