package it.pagopa.aca

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class AcaApplication

fun main(args: Array<String>) {
    runApplication<AcaApplication>(*args)
}
