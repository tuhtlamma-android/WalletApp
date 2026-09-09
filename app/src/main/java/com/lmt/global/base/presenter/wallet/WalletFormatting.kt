package com.lmt.global.base.presenter.wallet

import androidx.annotation.DrawableRes
import com.lmt.global.base.R
import com.lmt.global.base.data.entity.RecipientEntity
import com.lmt.global.base.data.entity.TransactionEntity
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

object WalletMoney {
    fun parseToMinor(rawValue: String): Long? = runCatching {
        val value = BigDecimal(rawValue.trim()).setScale(2, RoundingMode.UNNECESSARY)
        value.movePointRight(2).longValueExact().takeIf { it > 0L }
    }.getOrNull()

    fun format(amountMinor: Long): String = NumberFormat.getCurrencyInstance(Locale.US)
        .format(BigDecimal.valueOf(amountMinor, 2))
}

object WalletVisuals {
    const val AVATAR_ALI = "avatar_ali"
    const val AVATAR_STEVE = "avatar_steve"
    const val AVATAR_AHMED = "avatar_ahmed"
    const val BILL_ELECTRICITY = "bill_electricity"
    const val BILL_WATER = "bill_water"
    const val BILL_PHONE = "bill_phone"

    private val avatarKeys = listOf(AVATAR_ALI, AVATAR_STEVE, AVATAR_AHMED)

    fun avatarKeyForName(name: String): String {
        val index = ((name.trim().lowercase(Locale.ROOT).hashCode().toLong() and 0x7fffffffL) %
            avatarKeys.size).toInt()
        return avatarKeys[index]
    }

    @DrawableRes
    fun iconRes(key: String): Int = when (key) {
        AVATAR_STEVE -> R.drawable.img_avatar_steve
        AVATAR_AHMED -> R.drawable.img_avatar_ahmed
        BILL_ELECTRICITY -> R.drawable.ic_biller_electricity
        BILL_WATER -> R.drawable.ic_biller_water
        BILL_PHONE -> R.drawable.ic_biller_phone
        else -> R.drawable.img_avatar_ali
    }
}

fun RecipientEntity.toWalletContact() = WalletContact(
    id = id,
    avatarKey = avatarKey,
    name = name
)

fun TransactionEntity.toWalletTransaction() = WalletTransaction(
    id = id,
    iconKey = iconKey,
    merchant = title,
    createdAt = createdAt,
    amountMinor = amountMinor,
    type = type
)

fun walletDateTime(timestamp: Long): String {
    val dateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
    val date = dateTime.toLocalDate()
    val prefix = when (date) {
        LocalDate.now() -> "Today"
        LocalDate.now().minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))
    }
    return "$prefix ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))}"
}

fun walletSection(timestamp: Long): String {
    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return when (date) {
        LocalDate.now() -> "Today"
        LocalDate.now().minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE\nMMMM d, yyyy", Locale.US))
    }
}

fun walletFullDateTime(timestamp: Long): String = Instant.ofEpochMilli(timestamp)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("MMMM d, yyyy – HH:mm", Locale.US))

fun walletTransactionNumber(id: Long): String = "TXN%012d".format(Locale.US, id)
