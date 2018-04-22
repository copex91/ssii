#!/usr/bin/env python
# -*- coding: utf-8 -*-

from PIL import Image
from Crypto.Cipher import AES

option = input("Introduzca opción: \n 1.- Estrella \n 2.- Pingu \n")
if option == "1":
    filename = "estrella.jpg"
    filename_out_ecb = "estrella_ecb"
    filename_out_cbc = "estrella_cbc"
    format = "JPEG"
elif option == "2":
    filename = "pingu.png"
    filename_out_ecb = "pingu_ecb"
    filename_out_cbc = "pingu_cbc"
    format = "PNG"

key = "aaaabbbbccccdddd"


# AES requires that plaintexts be a multiple of 16, so we have to pad the data
def pad(data):
    return data + b"\x00" * (16 - len(data) % 16)


# Maps the RGB
def convert_to_RGB(data):
    r, g, b = tuple(map(lambda d: [data[i] for i in range(0, len(data)) if i % 3 == d], [0, 1, 2]))
    pixels = tuple(zip(r, g, b))
    return pixels


def process_image(filename):
    # Opens image and converts it to RGB format for PIL
    im = Image.open(filename)
    data = im.convert("RGB").tobytes()

    # Since we will pad the data to satisfy AES's multiple-of-16 requirement, we will store the original data length and "unpad" it later.
    original = len(data)

    # Encrypts using desired AES mode (we'll set it to ECB by default)
    new_ecb = convert_to_RGB(aes_ecb_encrypt(key, pad(data))[:original])
    new_cbc = convert_to_RGB(aes_cbc_encrypt(key, pad(data))[:original])

    # Create a new PIL Image object and save the old image data into the new image.
    im_ecb = Image.new(im.mode, im.size)
    im_ecb.putdata(new_ecb)

    im_cbc = Image.new(im.mode, im.size)
    im_cbc.putdata(new_cbc)

    # Save image
    im_ecb.save(filename_out_ecb + "." + format, format)
    im_cbc.save(filename_out_cbc + "." + format, format)


# CBC
def aes_cbc_encrypt(key, data, mode=AES.MODE_CBC):
    IV = "A" * 16  # We'll manually set the initialization vector to simplify things
    aes = AES.new(key, mode, IV)
    new_data = aes.encrypt(data)
    return new_data


# ECB
def aes_ecb_encrypt(key, data, mode=AES.MODE_ECB):
    aes = AES.new(key, mode)
    new_data = aes.encrypt(data)
    return new_data


process_image(filename)