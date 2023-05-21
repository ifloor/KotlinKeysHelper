package com.mikhael.kotlinkeyshelper

import com.mikhael.kotlinkeyshelper.helper.HelperEntrance
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class KotlinKeysHelperApplication

fun main(args: Array<String>) {
//	runApplication<KotlinKeysHelperApplication>(*args)
	HelperEntrance(args)
}
