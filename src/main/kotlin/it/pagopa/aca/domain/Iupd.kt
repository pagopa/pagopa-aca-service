package it.pagopa.aca.domain
data class Iupd(val fiscalCode: String, val iuv: String) {

    fun value(): String {
        return "ACA_${fiscalCode}_${iuv}"
    }
}
