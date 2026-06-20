package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.api.economy.Invoice
import java.math.BigDecimal

fun Double.invoice(on: String): Invoice = Invoice(BigDecimal(this), description = on)