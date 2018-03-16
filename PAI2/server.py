import socket
import struct
import hashlib
import hmac

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
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(('localhost', port))
    while True:
        s.listen(1)
        c, addr = s.accept()
        mensaje = get_msg(c)
        [origen, destino, cantidad, hash] = mensaje.split(" ")

        mensaje_nuevo = str(origen) + " " + str(destino) + " " + str(cantidad)
        clave = "c1314ed6"
        hash_nuevo = hmac.new(clave, mensaje_nuevo, hashlib.sha1).hexdigest()

        if hash == hash_nuevo:
            send_msg(c, "Se han transferido " + str(cantidad) + " euros desde la cuenta " + str(
                origen) + " a la cuenta " + str(destino))
        c.close()
    s.close()

if __name__ == '__main__':
    server(8080)