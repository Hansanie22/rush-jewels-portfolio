package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.SupportTicket;
import lk.dio.rush_jewels.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {
    List<SupportTicket> findAllByOrderByUpdatedAtDesc();

    List<SupportTicket> findByUserOrderByUpdatedAtDesc(User user);
}