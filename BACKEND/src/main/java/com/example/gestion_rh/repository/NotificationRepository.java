package com.example.gestion_rh.repository;


import com.example.gestion_rh.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByEmploye_IdEmployeOrderByDateEnvoiDesc(Integer idEmploye);

    List<Notification> findByEmploye_IdEmployeAndLu(Integer idEmploye, Boolean lu);

    long countByEmploye_IdEmployeAndLu(Integer idEmploye, Boolean lu);

    List<Notification> findByTypeNotification(Notification.TypeNotification type);

    @Modifying
    @Query("UPDATE Notification n SET n.lu = true WHERE n.employe.idEmploye = :id AND n.lu = false")
    int marquerToutesCommeLues(@Param("id") Integer idEmploye);

    @Modifying
    @Query("UPDATE Notification n SET n.lu = true WHERE n.idNotification = :id")
    int marquerCommeLue(@Param("id") Integer idNotification);
}
