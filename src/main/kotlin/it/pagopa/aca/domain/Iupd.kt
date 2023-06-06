package it.pagopa.aca.domain
data class Iupd(val fiscalCode: String, val iuv: String) {

    fun value(): String {
        return StringBuilder()
            .append("ACA")
            .append("_")
            .append(fiscalCode)
            .append("_")
            .append(iuv)
            .toString()
    }
}
