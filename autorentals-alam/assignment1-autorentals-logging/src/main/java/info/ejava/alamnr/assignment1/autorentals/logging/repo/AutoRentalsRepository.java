package info.ejava.alamnr.assignment1.autorentals.logging.repo;

public interface AutoRentalsRepository {
    AutoRentalDTO getLeaderByAutoId(String autoId);
    AutoRentalDTO getByRenterId(String renterId);
}
