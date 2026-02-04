import { Injectable } from '@angular/core';
import * as CryptoJS from 'crypto-js';

@Injectable({
  providedIn: 'root'
})
export class EncrDecrService {
  constructor() { }

  // Encrypt the message
  encrypt(secretKey: string, initVector: string, msg: string): string {
    console.log("Encrypting message:", msg);
    const key = CryptoJS.enc.Utf8.parse(secretKey);
    const iv = CryptoJS.enc.Utf8.parse(initVector);
    
    const encrypted = CryptoJS.AES.encrypt(
      CryptoJS.enc.Utf8.parse(msg.toString()),
      key,
      {
        keySize: 128 / 8,
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
      }
    );

    const result = encrypted.toString();
    console.log("Encrypted result:", result);
    return result;
  }

  // Decrypt the message
  decrypt(secretKey: string, initVector: string, msg: string): string {
    console.log("Decrypting message:", msg);
    const key = CryptoJS.enc.Utf8.parse(secretKey);
    const iv = CryptoJS.enc.Utf8.parse(initVector);

    const decrypted = CryptoJS.AES.decrypt(msg, key, {
      keySize: 128 / 8,
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    });

    const result = decrypted.toString(CryptoJS.enc.Utf8);
    console.log("Decrypted result:", result);
    return result;
  }
}
