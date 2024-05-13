package io.unreal.chet.chetapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChetapiApplication

fun main(args: Array<String>) {
	runApplication<ChetapiApplication>(*args)
}
