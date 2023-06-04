package com.mikhael.kotlinkeyshelper.rsa

import java.io.FileWriter
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

class RSAHelper {
    companion object {
        fun generateAndSaveNewKeyPair(baseFileName: String): KeyPair {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
            val keyPair = generator.genKeyPair()

            val pub = keyPair.public
            val private = keyPair.private

            val encoder = Base64.getEncoder()

            val privateStream = FileWriter("$baseFileName.key")
            privateStream.write("-----BEGIN RSA PRIVATE KEY-----\n")
            privateStream.write(encoder.encodeToString(private.encoded))
            privateStream.write("\n-----END RSA PRIVATE KEY-----")
            privateStream.write("\n-----PRIVATE KEY FORMAT: ${private.format}-----")
            privateStream.close()
            println("Wrote $baseFileName.key")

            val publicStream = FileWriter("$baseFileName.pub")
            publicStream.write("-----BEGIN RSA PUBLIC KEY-----\n")
            publicStream.write(encoder.encodeToString(pub.encoded))
            publicStream.write("\n-----END RSA PUBLIC KEY-----")
            publicStream.write("\n-----PUBLIC KEY FORMAT: ${pub.format}-----")
            publicStream.close()
            println("Wrote $baseFileName.pub")

            return keyPair
        }

        fun readKeyPairFromBaseFileName(baseFileName: String): KeyPair {
            val decoder = Base64.getDecoder()

            val keyPath = Paths.get("$baseFileName.key")
            val base64DirtPrivateKey = Files.readString(keyPath)
            val base64PrivateKey = this.removeKeyCommentLines(base64DirtPrivateKey)
            val bytesPrivateKey = decoder.decode(base64PrivateKey)
            val privateKeySpec = PKCS8EncodedKeySpec(bytesPrivateKey)
            val keyFactory = KeyFactory.getInstance("RSA")
            val privateKey = keyFactory.generatePrivate(privateKeySpec)


            //
            val pubPath = Paths.get("$baseFileName.pub")
            val base64DirtPubKey = Files.readString(pubPath)
            val base64PubKey = this.removeKeyCommentLines(base64DirtPubKey)
            val bytesPubKey = decoder.decode(base64PubKey)
            val publicKeySpec = X509EncodedKeySpec(bytesPubKey)
            val publicKey = keyFactory.generatePublic(publicKeySpec)

            println("Private and public keys loaded correctly")

            return KeyPair(publicKey, privateKey)
        }

        private fun signBytes(bytesToSign: ByteArray, privKey: PrivateKey?): String {
            val signature = Signature.getInstance("SHA256withRSA")
            if (privKey == null) throw Exception("Keypair not loaded")
            signature.initSign(privKey)


            signature.update(bytesToSign)
            val sigBytes = signature.sign()
            val encoder = Base64.getEncoder()

            return encoder.encodeToString(sigBytes)
        }

        private fun verifyIsSignatureValidBytes(bytesToVerify: ByteArray, base64SignatureBytes: String, pubKey: PublicKey?): Boolean {
            val signature = Signature.getInstance("SHA256withRSA")


            if (pubKey == null) throw Exception("Public key must be loaded")
            signature.initVerify(pubKey)

            signature.update(bytesToVerify)

            val decoder = Base64.getDecoder()
            val signatureBytes = decoder.decode(base64SignatureBytes)

            return signature.verify(signatureBytes)
        }

        fun signText(textToSign: String, privKey: PrivateKey?): String {
            return signBytes(
                textToSign.toByteArray(StandardCharsets.UTF_8),
                privKey
            )
        }

        fun verifyIsSignatureValidText(textToVerify: String, base64SignatureBytes: String, pubKey: PublicKey?): Boolean {
            return verifyIsSignatureValidBytes(
                textToVerify.toByteArray(StandardCharsets.UTF_8),
                base64SignatureBytes,
                pubKey
            )
        }

        fun signFile(file: String, privKey: PrivateKey?): String {
            return signBytes(
                Files.readAllBytes(Paths.get(file)),
                privKey
            )
        }

        fun verifyIsSignatureValid(fileToVerify: String, base64SignatureBytes: String, pubKey: PublicKey?): Boolean {
            return verifyIsSignatureValidBytes(
                Files.readAllBytes(Paths.get(fileToVerify)),
                base64SignatureBytes,
                pubKey
            )
        }

        fun encrypt(key: Key, message: String): String {
            val encryptCipher = Cipher.getInstance("RSA")
            encryptCipher.init(Cipher.ENCRYPT_MODE, key)

            val messageBytes = message.toByteArray(StandardCharsets.UTF_8)
            val encryptedMessageBytes = encryptCipher.doFinal(messageBytes)

            return Base64.getEncoder().encodeToString(encryptedMessageBytes)
        }

        fun decrypt(key: Key, base64Message: String): String {
            val decryptCipher = Cipher.getInstance("RSA")
            decryptCipher.init(Cipher.DECRYPT_MODE, key)

            val messageBytes = Base64.getDecoder().decode(base64Message)
            val decryptedMessageBytes = decryptCipher.doFinal(messageBytes)

            return String(decryptedMessageBytes, StandardCharsets.UTF_8)
        }

        private fun removeKeyCommentLines(originalString: String): String {
            var outString = ""
            val lines = originalString.split("\n")
            for (line in lines) {
                if (line.isEmpty() || line.startsWith("-----")) continue
                outString += ((if (outString.isNotEmpty()) "\n" else "") + line)
            }

            return outString
        }
    }
}