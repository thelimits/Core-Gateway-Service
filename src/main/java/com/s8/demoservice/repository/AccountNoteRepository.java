package com.s8.demoservice.repository;

import com.s8.demoservice.model.AccountNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountNoteRepository extends JpaRepository<AccountNote, String> {
    @Query("SELECT DISTINCT a FROM AccountNote a JOIN a.account c" +
            " WHERE c.accountNumber = :accountNumber")
    Page<AccountNote> findNotesByAccountId(@Param("accountNumber") String accountNumber, Pageable pageable);

    @Query("SELECT a FROM AccountNote a")
    Page<AccountNote> findAlls(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AccountNote a WHERE a.id = :id")
    void deleteAccountNote(@Param("id") String id);
}
