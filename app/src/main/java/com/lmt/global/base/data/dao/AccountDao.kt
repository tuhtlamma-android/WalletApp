package com.lmt.global.base.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lmt.global.base.data.entity.AccountEntity

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(account: AccountEntity): Long

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE phone = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): AccountEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM accounts WHERE email = :email)")
    suspend fun emailExists(email: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM accounts WHERE phone = :phone)")
    suspend fun phoneExists(phone: String): Boolean
}
