package com.lmt.global.base.data

import com.lmt.global.base.data.entity.CardEntity
import com.lmt.global.base.data.entity.AccountEntity
import com.lmt.global.base.data.entity.RecipientEntity
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.data.mapper.CardMapper
import com.lmt.global.base.data.mapper.AccountMapper
import com.lmt.global.base.data.mapper.RecipientMapper
import com.lmt.global.base.data.mapper.TransactionMapper
import com.lmt.global.base.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class WalletMapperTest {

    @Test
    fun accountMapper_exposesProfileButNotCredentialHash() {
        val entity = AccountEntity(
            id = 7L,
            name = "Account",
            email = "user@example.com",
            phone = null,
            passwordHash = "private-hash",
            createdAt = 123L
        )

        val model = AccountMapper.toModel(entity)

        assertEquals(7L, model.id)
        assertEquals("user@example.com", model.email)
    }

    @Test
    fun cardMapper_preservesPersistedFields() {
        val entity = CardEntity(7L, "CARD001", "Visa", "1234567812345678", 10_000L, 123L)

        assertEquals(entity, CardMapper.toEntity(CardMapper.toModel(entity), entity.accountId))
    }

    @Test
    fun recipientMapper_hidesNormalizedDatabaseField() {
        val entity = RecipientEntity(1L, 7L, "Anh An", "anh an", "avatar_steve", 123L)

        val model = RecipientMapper.toModel(entity)

        assertEquals("Anh An", model.name)
        assertEquals("anh an", RecipientMapper.toEntity(model, entity.accountId).normalizedName)
    }

    @Test
    fun transactionMapper_convertsStorageTypeToDomainEnum() {
        val entity = TransactionEntity(
            id = 1L,
            accountId = 7L,
            type = TransactionEntity.TYPE_TRANSFER,
            title = "Anh An",
            recipientId = 2L,
            iconKey = "avatar_steve",
            amountMinor = 10_000L,
            createdAt = 123L
        )

        val model = TransactionMapper.toModel(entity)

        assertEquals(TransactionType.TRANSFER, model.type)
        assertEquals(entity, TransactionMapper.toEntity(model, entity.accountId))
    }
}
