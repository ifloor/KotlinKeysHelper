package com.mikhael.kotlinkeyshelper.menu

import com.mikhael.kotlinkeyshelper.defines.Strings
import com.mikhael.kotlinkeyshelper.rsa.RSAHelper
import java.security.Key
import java.security.KeyPair
import kotlin.system.exitProcess

class RootMenu {

    var keyPair: KeyPair? = null

    fun start() {
        this.loop()
    }

    private fun loop() {
        this.printMenu()

        println("\n ${Strings.GenericTypeRequest}")
        val option = (readlnOrNull() ?: "").trim()

        when (option) {
            "1" -> this.menu1()
            "2" -> this.menu2()
            "3" -> this.menu3()
            "4" -> this.menu4()
            "5" -> this.menu5()
            "6" -> this.menu6()
            "9" -> exitProcess(0)
            else -> {
                println("Option not understood. Try again.")
            }
        }

        this.loop()
    }

    private fun printMenu() {
        println("\n\n\n")
        println("1 - Generate keys")
        println("2 - Load keys from disk")
        println("3 - Sign a file using private key")
        println("4 - Verify a signature of a file using public key")
        println("5 - Encrypt a message using public or private key")
        println("6 - Decrypt a message using public or private key")
        println("9 - Exit")
    }

    private fun menu1() {
        println("Which file to save? It can be an absolute path")
        val file = (readlnOrNull() ?: "").trim()

        keyPair = RSAHelper.generateAndSaveNewKeyPair(file)
    }

    private fun menu2() {
        println("Which file to read? It can be an absolute path. Remember to type only the base (somekey) and not (somekey.key) or (somekey.pub)")
        val file = (readlnOrNull() ?: "").trim()

        this.keyPair = RSAHelper.readKeyPairFromBaseFileName(file)
    }

    private fun menu3() {
        println("Which file to sign?")
        val fileToSign = (readlnOrNull() ?: "").trim()


        println("Signature for file [$fileToSign] is [${RSAHelper.signFile(fileToSign, keyPair?.private)}]")
    }

    private fun menu4() {
        println("Which file to verify?")
        val fileToVerify = (readlnOrNull() ?: "").trim()

        println("Which is the signature you want to check (base64 encoded)?")
        val base64signature = (readlnOrNull() ?: "").trim()

        val isValid = RSAHelper.verifyIsSignatureValid(fileToVerify, base64signature, this.keyPair?.public)
        println("Signature is ${if (isValid) "valid" else "not valid"}")
    }

    private fun menu5() {
        println("Which is the text you want to encrypt?")
        val textToEncrypt = (readlnOrNull() ?: "").trim()

        println("Want to use the private or the public key? Type 'pub' for public key, 'priv' for private key")
        val keyOption = (readlnOrNull() ?: "").trim()

        val keyToUser: Key = when (keyOption) {
            "pub" -> this.keyPair?.public ?: throw Exception("Public key is not loaded")
            "priv" -> this.keyPair?.private ?: throw Exception("Private key is not loaded")
            else -> {
                println("Option not understood. Type 'pub' for public key, 'priv' for private key")
                return
            }
        }

        val encryptedText = RSAHelper.encrypt(keyToUser, textToEncrypt)
        println("Encrypted is the text inside [] -> [$encryptedText]")
    }

    private fun menu6() {
        println("Which is the text you want to decrypt?")
        val textToDecrypt = (readlnOrNull() ?: "").trim()

        println("Want to use the private or the public key? Type 'pub' for public key, 'priv' for private key")
        val keyOption = (readlnOrNull() ?: "").trim()

        val keyToUser: Key = when (keyOption) {
            "pub" -> this.keyPair?.public ?: throw Exception("Public key is not loaded")
            "priv" -> this.keyPair?.private ?: throw Exception("Private key is not loaded")
            else -> {
                println("Option not understood. Type 'pub' for public key, 'priv' for private key")
                return
            }
        }

        try {
            val decryptedText = RSAHelper.decrypt(keyToUser, textToDecrypt)
            println("Decrypted is the text inside [] -> [$decryptedText]")
        } catch (exception: Exception) {
            println("Error decrypting message")
            exception.printStackTrace()
        }

        Thread.sleep(1000)
    }
}