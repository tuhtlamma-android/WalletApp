package com.lmt.global.base.data.mapper

import com.lmt.global.base.data.entity.CardEntity
import com.lmt.global.base.data.entity.AccountEntity
import com.lmt.global.base.data.entity.RecipientEntity
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.model.Card
import com.lmt.global.base.model.Account
import com.lmt.global.base.model.Recipient
import com.lmt.global.base.model.Transaction
import com.lmt.global.base.model.TransactionType
import java.util.Locale

interface IMapper<Entity, Model> {
    fun toModel(entity: Entity): Model
}

object AccountMapper : IMapper<AccountEntity, Account> {
    override fun toModel(entity: AccountEntity) = Account(
        id = entity.id,
        name = entity.name,
        email = entity.email,
        phone = entity.phone
    )
}

object CardMapper : IMapper<CardEntity, Card> {
    override fun toModel(entity: CardEntity): Card {
        return Card(
            id = entity.id,
            name = entity.name,
            cardNumber = entity.cardNumber,
            balanceMinor = entity.balanceMinor,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(model: Card, accountId: Long): CardEntity {
       return CardEntity(
           accountId = accountId,
           id = model.id,
           name = model.name,
           cardNumber = model.cardNumber,
           balanceMinor = model.balanceMinor,
           createdAt = model.createdAt
       )
    }
}

object RecipientMapper : IMapper<RecipientEntity, Recipient> {
    override fun toModel(entity: RecipientEntity) = Recipient(
        id = entity.id,
        name = entity.name,
        avatarKey = entity.avatarKey,
        lastTransferAt = entity.lastTransferAt
    )

    fun toEntity(model: Recipient, accountId: Long) = RecipientEntity(
        id = model.id,
        accountId = accountId,
        name = model.name,
        normalizedName = model.name.trim().lowercase(Locale.ROOT),
        avatarKey = model.avatarKey,
        lastTransferAt = model.lastTransferAt
    )
}

object TransactionMapper : IMapper<TransactionEntity, Transaction> {
    override fun toModel(entity: TransactionEntity) = Transaction(
        id = entity.id,
        type = TransactionType.fromStorage(entity.type),
        title = entity.title,
        recipientId = entity.recipientId,
        iconKey = entity.iconKey,
        billerType = entity.billerType,
        amountMinor = entity.amountMinor,
        createdAt = entity.createdAt
    )

    fun toEntity(model: Transaction, accountId: Long) = TransactionEntity(
        id = model.id,
        accountId = accountId,
        type = model.type.storageValue,
        title = model.title,
        recipientId = model.recipientId,
        iconKey = model.iconKey,
        billerType = model.billerType,
        amountMinor = model.amountMinor,
        createdAt = model.createdAt
    )
}
